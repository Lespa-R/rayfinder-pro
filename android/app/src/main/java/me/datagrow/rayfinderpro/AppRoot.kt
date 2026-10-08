package me.datagrow.rayfinderpro

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.ZoneId

private enum class Tab(val label: String, val icon: ImageVector) {
    Radar("Radar", Icons.Filled.Search),
    SafeZone("Safe Zone", Icons.Filled.Warning),
    Stats("Stats", Icons.AutoMirrored.Filled.List),
    Account("Compte", Icons.Filled.Person),
}

@Composable
fun AppRoot(scanner: BleScanner, backend: Backend) {
    var tab by rememberSaveable { mutableStateOf(Tab.Radar) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach {
                    NavigationBarItem(
                        selected = tab == it,
                        onClick = { tab = it },
                        icon = { Icon(it.icon, contentDescription = null) },
                        label = { Text(it.label) },
                    )
                }
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (tab) {
            Tab.Radar -> RadarScreen(scanner, backend, modifier)
            Tab.SafeZone -> SafeZoneScreen(backend, modifier) { tab = Tab.Account }
            Tab.Stats -> StatsScreen(backend, modifier) { tab = Tab.Account }
            Tab.Account -> AccountScreen(backend, modifier)
        }
    }
}

// --- Safe Zone -------------------------------------------------------------------------------

@Composable
private fun SafeZoneScreen(backend: Backend, modifier: Modifier, goToAccount: () -> Unit) {
    val session by backend.session.collectAsState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    var alerts by remember { mutableStateOf<List<SafeZoneAlert>?>(null) }
    var busy by remember { mutableStateOf(false) }
    var reload by remember { mutableStateOf(0) }

    LaunchedEffect(session, reload) {
        if (session == null) return@LaunchedEffect
        alerts = try {
            backend.recentAlerts()
        } catch (e: BackendException) {
            context.toast(e.message ?: "Chargement impossible")
            emptyList()
        }
    }

    Page(modifier, "Safe Zone", "Prévenez vos amis d'un seul geste quand des lunettes à caméra sont détectées.") {
        if (session == null) {
            NeedAccount(goToAccount)
            return@Page
        }
        Button(
            onClick = {
                busy = true
                scope.launch {
                    try {
                        backend.sendSafeZoneAlert()
                        context.toast("Vos amis ont été prévenus")
                        reload++
                    } catch (e: BackendException) {
                        context.toast(e.message ?: "Alerte non envoyée")
                    } finally {
                        busy = false
                    }
                }
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AlertRed, contentColor = MaterialTheme.colorScheme.onBackground),
        ) { Text("Alerter mes amis") }

        Muted("Vos amis reçoivent l'alerte s'ils vous ont ajouté à leur Safe Zone.")
        TextButton(onClick = { uriHandler.openUri("${BuildConfig.SITE_URL}/safe-zone") }) {
            Text("Gérer mes amis sur le site")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("Alertes récentes", Modifier.weight(1f))
            TextButton(onClick = { reload++ }) { Text("Actualiser") }
        }
        when {
            alerts == null -> CircularProgressIndicator()
            alerts!!.isEmpty() -> Muted("Aucune alerte récente.")
            else -> alerts!!.forEach { a ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(if (a.mine) "${a.message} (envoyée par vous)" else a.message, fontWeight = FontWeight.SemiBold)
                        Muted(listOfNotNull(a.city ?: "Lieu inconnu", a.createdAt?.let { formatDate(it) }).joinToString(" · "))
                    }
                }
            }
        }
    }
}

// --- Statistiques ----------------------------------------------------------------------------

@Composable
private fun StatsScreen(backend: Backend, modifier: Modifier, goToAccount: () -> Unit) {
    val session by backend.session.collectAsState()
    val context = LocalContext.current
    var stats by remember { mutableStateOf<List<CityStat>?>(null) }
    var reload by remember { mutableStateOf(0) }

    LaunchedEffect(session, reload) {
        if (session == null) return@LaunchedEffect
        stats = try {
            backend.cityStatsToday()
        } catch (e: BackendException) {
            context.toast(e.message ?: "Chargement impossible")
            emptyList()
        }
    }

    Page(modifier, "Stats communautaires", "Détections partagées depuis minuit, ville par ville.") {
        if (session == null) {
            NeedAccount(goToAccount)
            return@Page
        }
        val list = stats
        if (list == null) {
            CircularProgressIndicator()
            return@Page
        }
        val total = list.sumOf { it.detections }
        val max = (list.maxOfOrNull { it.detections } ?: 1L).coerceAtLeast(1L)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Muted("Total aujourd'hui")
                Text("$total", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
        if (list.isEmpty()) Muted("Aucune détection partagée aujourd'hui. Soyez le premier.")
        list.forEach { row ->
            Column(Modifier.fillMaxWidth()) {
                Row {
                    Text(row.city, Modifier.weight(1f))
                    Text("${row.detections}", color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { row.detections.toFloat() / max },
                    modifier = Modifier.fillMaxWidth(),
                    color = AlertRed,
                    trackColor = NavyCard,
                )
            }
        }
        TextButton(onClick = { reload++ }) { Text("Actualiser") }
    }
}

// --- Compte ----------------------------------------------------------------------------------

@Composable
private fun AccountScreen(backend: Backend, modifier: Modifier) {
    val session by backend.session.collectAsState()
    val savedCity by backend.city.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }

    fun run(block: suspend () -> Unit) {
        busy = true
        scope.launch {
            try {
                block()
            } catch (e: BackendException) {
                context.toast(e.message ?: "Erreur")
            } finally {
                busy = false
            }
        }
    }

    Page(modifier, "Compte", null) {
        if (!backend.isConfigured) {
            Muted("Les fonctions de compte ne sont pas disponibles dans cette version.")
            return@Page
        }
        val s = session
        if (s == null) {
            SignInForm(busy) { mode, email, password, name ->
                run {
                    if (mode == AuthMode.SignIn) {
                        backend.signIn(email, password)
                        context.toast("Connecté")
                    } else if (backend.signUp(email, password, name)) {
                        context.toast("Compte créé")
                    } else {
                        context.toast("Compte créé : confirmez votre adresse via le lien reçu par e-mail, puis connectez-vous.")
                    }
                }
            }
            Muted("Le compte est facultatif : le radar fonctionne sans. Il sert à partager vos détections, alerter vos amis et voir les statistiques. Même compte que sur le site.")
        } else {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Muted("Connecté")
                    Text(s.email, fontWeight = FontWeight.SemiBold)
                }
            }
            var city by rememberSaveable(savedCity) { mutableStateOf(savedCity) }
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Ma ville (pour les stats)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { run { backend.updateCity(city); context.toast("Ville enregistrée") } },
                enabled = !busy && city.trim() != savedCity,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Enregistrer la ville") }
            OutlinedButton(onClick = { backend.signOut() }, modifier = Modifier.fillMaxWidth()) { Text("Se déconnecter") }

            var confirmDelete by remember { mutableStateOf(false) }
            TextButton(onClick = { confirmDelete = true }) { Text("Supprimer mon compte", color = AlertRed) }
            if (confirmDelete) {
                AlertDialog(
                    onDismissRequest = { confirmDelete = false },
                    title = { Text("Supprimer votre compte ?") },
                    text = { Text("Votre compte, vos détections partagées, vos amis et vos alertes Safe Zone seront effacés définitivement.") },
                    confirmButton = {
                        TextButton(onClick = {
                            confirmDelete = false
                            run { backend.deleteAccount(); context.toast("Compte supprimé") }
                        }) { Text("Supprimer", color = AlertRed) }
                    },
                    dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Annuler") } },
                )
            }
        }
        LegalLinks()
    }
}

private enum class AuthMode { SignIn, SignUp }

@Composable
private fun SignInForm(busy: Boolean, onSubmit: (AuthMode, String, String, String) -> Unit) {
    var mode by rememberSaveable { mutableStateOf(AuthMode.SignIn) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }

    SectionTitle(if (mode == AuthMode.SignIn) "Connexion" else "Créer un compte")
    if (mode == AuthMode.SignUp) {
        OutlinedTextField(name, { name = it }, label = { Text("Pseudo") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    }
    OutlinedTextField(
        email, { email = it }, label = { Text("E-mail") }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        password, { password = it }, label = { Text("Mot de passe (6 caractères min.)") }, singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
    )
    Button(
        onClick = { onSubmit(mode, email, password, name) },
        enabled = !busy && email.contains('@') && password.length >= 6,
        modifier = Modifier.fillMaxWidth(),
    ) { Text(if (mode == AuthMode.SignIn) "Se connecter" else "S'inscrire") }
    TextButton(onClick = { mode = if (mode == AuthMode.SignIn) AuthMode.SignUp else AuthMode.SignIn }) {
        Text(if (mode == AuthMode.SignIn) "Pas encore de compte ? Inscrivez-vous" else "Déjà un compte ? Connectez-vous")
    }
}

// --- Éléments communs ------------------------------------------------------------------------

@Composable
private fun Page(modifier: Modifier, title: String, subtitle: String?, content: @Composable () -> Unit) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        subtitle?.let { Muted(it) }
        content()
    }
}

@Composable
private fun NeedAccount(goToAccount: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Cette fonction nécessite un compte (gratuit).")
            Button(onClick = goToAccount) { Text("Se connecter ou s'inscrire") }
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = modifier)
}

@Composable
private fun Muted(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun LegalLinks() {
    val uriHandler = LocalUriHandler.current
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row {
            TextButton(onClick = { uriHandler.openUri("${BuildConfig.SITE_URL}/mentions-legales") }) {
                Text("Mentions légales", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = { uriHandler.openUri("${BuildConfig.SITE_URL}/confidentialite") }) {
                Text("Confidentialité", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private val dateFormat = DateTimeFormatter.ofPattern("dd/MM à HH:mm")

private fun formatDate(date: java.time.OffsetDateTime): String =
    date.atZoneSameInstant(ZoneId.systemDefault()).format(dateFormat)

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
