package me.datagrow.rayfinderpro

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.OffsetDateTime
import kotlin.math.pow

data class Session(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long, // secondes epoch
    val userId: String,
    val email: String,
)

data class CityStat(val city: String, val detections: Long)

data class SafeZoneAlert(val id: String, val message: String, val city: String?, val createdAt: OffsetDateTime?, val mine: Boolean)

/** [httpCode] vaut 0 pour une erreur réseau. */
class BackendException(message: String, val httpCode: Int = 0) : Exception(message)

/**
 * Client minimal du backend Lovable Cloud (Supabase) partagé avec le site :
 * mêmes comptes, mêmes tables (profiles, sightings, friends, safe_zone_alerts), mêmes règles RLS.
 */
class Backend(context: Context) {

    private val baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
    private val apiKey = BuildConfig.SUPABASE_KEY
    private val prefs = context.getSharedPreferences("backend", Context.MODE_PRIVATE)
    private val refreshLock = Mutex()

    private val _session = MutableStateFlow(loadSession())
    val session: StateFlow<Session?> = _session.asStateFlow()

    private val _city = MutableStateFlow(prefs.getString("city", "").orEmpty())
    /** Ville saisie par l'utilisateur, utilisée pour les partages et les statistiques. */
    val city: StateFlow<String> = _city.asStateFlow()

    val isConfigured get() = baseUrl.isNotBlank() && apiKey.isNotBlank()

    // --- Authentification -------------------------------------------------------------------

    suspend fun signIn(email: String, password: String) {
        val res = call("POST", "/auth/v1/token?grant_type=password", JSONObject().put("email", email.trim()).put("password", password))
        storeSession(JSONObject(res))
        syncCityFromProfile()
    }

    /** Renvoie true si le compte est actif tout de suite, false s'il faut d'abord confirmer l'e-mail. */
    suspend fun signUp(email: String, password: String, displayName: String): Boolean {
        val body = JSONObject()
            .put("email", email.trim())
            .put("password", password)
            .put("data", JSONObject().put("display_name", displayName.ifBlank { email.substringBefore('@') }))
        val json = JSONObject(call("POST", "/auth/v1/signup", body))
        if (!json.has("access_token")) return false
        storeSession(json)
        return true
    }

    fun signOut() {
        prefs.edit().remove("session").apply()
        _session.value = null
    }

    /** Supprime le compte et, en cascade, toutes les données associées (fonction SQL delete_my_account). */
    suspend fun deleteAccount() {
        call("POST", "/rest/v1/rpc/delete_my_account", JSONObject(), token = validToken())
        signOut()
    }

    // --- Données communautaires -------------------------------------------------------------

    suspend fun shareSighting(device: SeenDevice) {
        val s = requireSession()
        val body = JSONObject()
            .put("user_id", s.userId)
            .put("device_name", device.name ?: "Lunettes Meta")
            .put("device_key", device.address)
            .put("model", device.metaReason)
            .put("rssi", device.rssi)
            .put("distance_m", estimateDistance(device.rssi))
            .put("city", _city.value.ifBlank { JSONObject.NULL })
            .put("shared", true)
        call("POST", "/rest/v1/sightings", body, token = validToken(), prefer = "return=minimal")
    }

    suspend fun cityStatsToday(): List<CityStat> {
        val arr = JSONArray(call("POST", "/rest/v1/rpc/city_stats_today", JSONObject(), token = validToken()))
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            CityStat(o.optString("city", "Inconnue"), o.optLong("detections"))
        }
    }

    suspend fun sendSafeZoneAlert() {
        val s = requireSession()
        val body = JSONObject()
            .put("user_id", s.userId)
            .put("message", "Smart glasses détectées à proximité")
            .put("city", _city.value.ifBlank { JSONObject.NULL })
        call("POST", "/rest/v1/safe_zone_alerts", body, token = validToken(), prefer = "return=minimal")
    }

    /** Alertes envoyées par moi ou par les personnes qui m'ont ajouté en ami (filtrage par RLS). */
    suspend fun recentAlerts(): List<SafeZoneAlert> {
        val me = requireSession().userId
        val arr = JSONArray(
            call("GET", "/rest/v1/safe_zone_alerts?select=id,message,city,created_at,user_id&order=created_at.desc&limit=20", token = validToken())
        )
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            SafeZoneAlert(
                id = o.getString("id"),
                message = o.optString("message"),
                city = o.optString("city").takeIf { c -> c.isNotBlank() && c != "null" },
                createdAt = runCatching { OffsetDateTime.parse(o.getString("created_at")) }.getOrNull(),
                mine = o.optString("user_id") == me,
            )
        }
    }

    /** Enregistre la ville localement et, si connecté, dans le profil (visible des amis). */
    suspend fun updateCity(value: String) {
        val v = value.trim()
        prefs.edit().putString("city", v).apply()
        _city.value = v
        val s = _session.value ?: return
        // Upsert (HttpURLConnection ne gère pas PATCH) : seule la colonne city est mise à jour.
        call(
            "POST", "/rest/v1/profiles",
            JSONObject().put("id", s.userId).put("city", v.ifBlank { JSONObject.NULL }),
            token = validToken(), prefer = "resolution=merge-duplicates,return=minimal",
        )
    }

    private suspend fun syncCityFromProfile() {
        val s = _session.value ?: return
        runCatching {
            val arr = JSONArray(call("GET", "/rest/v1/profiles?select=city&id=eq.${s.userId}", token = validToken()))
            val remote = arr.optJSONObject(0)?.optString("city").orEmpty().takeIf { it != "null" }.orEmpty()
            if (remote.isNotBlank()) {
                prefs.edit().putString("city", remote).apply()
                _city.value = remote
            } else if (_city.value.isNotBlank()) {
                updateCity(_city.value)
            }
        }
    }

    // --- Session ----------------------------------------------------------------------------

    private fun requireSession() = _session.value ?: throw BackendException("Connectez-vous d'abord (onglet Compte).")

    /** Jeton d'accès valide, rafraîchi automatiquement s'il expire dans moins d'une minute. */
    private suspend fun validToken(): String = refreshLock.withLock {
        val s = requireSession()
        if (System.currentTimeMillis() / 1000 < s.expiresAt - 60) return s.accessToken
        try {
            val res = call("POST", "/auth/v1/token?grant_type=refresh_token", JSONObject().put("refresh_token", s.refreshToken))
            storeSession(JSONObject(res)).accessToken
        } catch (e: BackendException) {
            if (e.httpCode == 0) throw e // hors ligne : on garde la session
            signOut()
            throw BackendException("Session expirée, reconnectez-vous.", e.httpCode)
        }
    }

    private fun storeSession(json: JSONObject): Session {
        val user = json.getJSONObject("user")
        val expiresAt = json.optLong("expires_at").takeIf { it > 0 }
            ?: (System.currentTimeMillis() / 1000 + json.optLong("expires_in", 3600))
        val s = Session(
            accessToken = json.getString("access_token"),
            refreshToken = json.getString("refresh_token"),
            expiresAt = expiresAt,
            userId = user.getString("id"),
            email = user.optString("email"),
        )
        prefs.edit().putString(
            "session",
            JSONObject()
                .put("access_token", s.accessToken).put("refresh_token", s.refreshToken)
                .put("expires_at", s.expiresAt).put("user_id", s.userId).put("email", s.email)
                .toString(),
        ).apply()
        _session.value = s
        return s
    }

    private fun loadSession(): Session? = prefs.getString("session", null)?.let {
        runCatching {
            val o = JSONObject(it)
            Session(o.getString("access_token"), o.getString("refresh_token"), o.getLong("expires_at"), o.getString("user_id"), o.optString("email"))
        }.getOrNull()
    }

    // --- HTTP -------------------------------------------------------------------------------

    private suspend fun call(
        method: String,
        path: String,
        body: JSONObject? = null,
        token: String? = null,
        prefer: String? = null,
    ): String = withContext(Dispatchers.IO) {
        if (!isConfigured) throw BackendException("Service indisponible dans cette version de l'app.")
        val conn = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("apikey", apiKey)
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            token?.let { setRequestProperty("Authorization", "Bearer $it") }
            prefer?.let { setRequestProperty("Prefer", it) }
        }
        try {
            if (body != null) {
                conn.doOutput = true
                conn.outputStream.use { it.write(body.toString().toByteArray()) }
            }
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw BackendException(friendlyError(code, text), code)
            text
        } catch (e: java.io.IOException) {
            throw BackendException("Pas de connexion Internet.")
        } finally {
            conn.disconnect()
        }
    }

    private fun friendlyError(code: Int, body: String): String {
        val json = runCatching { JSONObject(body) }.getOrNull()
        return when (json?.optString("error_code")?.ifBlank { null } ?: json?.optString("error")) {
            "invalid_credentials", "invalid_grant" -> "E-mail ou mot de passe incorrect."
            "email_not_confirmed" -> "Confirmez d'abord votre adresse e-mail (lien reçu par mail)."
            "user_already_exists", "email_exists" -> "Un compte existe déjà avec cette adresse."
            "weak_password" -> "Mot de passe trop faible (6 caractères minimum)."
            "over_email_send_rate_limit", "over_request_rate_limit" -> "Trop de tentatives, réessayez dans quelques minutes."
            else -> json?.optString("msg")?.ifBlank { null }
                ?: json?.optString("message")?.ifBlank { null }
                ?: json?.optString("error_description")?.ifBlank { null }
                ?: "Erreur du serveur ($code)."
        }
    }

    companion object {
        /** Modèle de propagation simple, identique au site (puissance de référence -59 dBm à 1 m). */
        fun estimateDistance(rssi: Int): Double =
            (10.0.pow((-59 - rssi) / 20.0)).coerceIn(0.5, 60.0).let { Math.round(it * 10) / 10.0 }
    }
}
