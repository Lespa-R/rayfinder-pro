package me.datagrow.rayfinderpro

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private enum class Tab(val label: String, @DrawableRes val icon: Int) {
    Radar("Radar", R.drawable.ic_radar),
    SafeZone("Safe Zone", R.drawable.ic_shield),
    Stats("Stats", R.drawable.ic_stats),
    Account("Compte", R.drawable.ic_user),
}

@Composable
fun AppRoot(scanner: BleScanner, backend: Backend) {
    var tab by rememberSaveable { mutableStateOf(Tab.Radar) }
    Scaffold(
        containerColor = Navy,
        bottomBar = {
            Column {
                HorizontalDivider(color = Hairline)
                NavigationBar(containerColor = NavySurface, tonalElevation = 0.dp) {
                    Tab.entries.forEach {
                        NavigationBarItem(
                            selected = tab == it,
                            onClick = { tab = it },
                            icon = { Icon(painterResource(it.icon), contentDescription = null, modifier = Modifier.size(22.dp)) },
                            label = { Text(it.label, style = MaterialTheme.typography.labelMedium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Coral,
                                selectedTextColor = Coral,
                                indicatorColor = Coral.copy(alpha = 0.14f),
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        // Fondu entre onglets
        AnimatedContent(
            targetState = tab,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
            modifier = Modifier.padding(padding),
            label = "tabs",
        ) { current ->
            when (current) {
                Tab.Radar -> RadarScreen(scanner, backend)
                Tab.SafeZone -> SafeZoneScreen(backend) { tab = Tab.Account }
                Tab.Stats -> StatsScreen(backend) { tab = Tab.Account }
                Tab.Account -> AccountScreen(backend)
            }
        }
    }
}

/** Page défilante, avec tirer-pour-actualiser optionnel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Page(
    title: String,
    subtitle: String?,
    refreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val body = @Composable {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PageHeader(title, subtitle)
            content()
            Spacer(Modifier.height(12.dp))
        }
    }
    if (onRefresh != null) {
        PullToRefreshBox(isRefreshing = refreshing, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) { body() }
    } else {
        body()
    }
}

// --- Safe Zone -------------------------------------------------------------------------------

@Composable
private fun SafeZoneScreen(backend: Backend, goToAccount: () -> Unit) {
    val session by backend.session.collectAsState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    var alerts by remember { mutableStateOf<List<SafeZoneAlert>?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var reload by remember { mutableIntStateOf(0) }

    LaunchedEffect(session, reload) {
        if (session == null) return@LaunchedEffect
        alerts = try {
            backend.recentAlerts()
        } catch (e: BackendException) {
            context.toast(e.message ?: "Chargement impossible")
            alerts ?: emptyList()
        }
        refreshing = false
    }

    Page(
        "Safe Zone",
        "Prévenez vos proches d'un geste quand des lunettes à caméra sont repérées.",
        refreshing = refreshing,
        onRefresh = if (session != null) ({ refreshing = true; reload++ }) else null,
    ) {
        if (session == null) {
            NeedAccount(R.drawable.ic_shield, goToAccount)
            return@Page
        }
        AppCard(Modifier.fillMaxWidth(), color = AlertRed.copy(alpha = 0.10f)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(R.drawable.ic_shield, AlertRed, size = 48.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Alerte instantanée", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Reçue par ceux qui vous ont ajouté à leur Safe Zone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }
                BigButton(
                    "Alerter mes amis",
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
                    icon = R.drawable.ic_shield,
                    container = AlertRed,
                    content = Color.White,
                    enabled = !busy,
                )
            }
        }

        AppCard(Modifier.fillMaxWidth()) {
            SettingsRow(R.drawable.ic_user, "Gérer mes amis sur le site", onClick = { uriHandler.openUri("${BuildConfig.SITE_URL}/safe-zone") })
        }

        SectionLabel("Alertes récentes", Modifier.padding(top = 8.dp))
        val list = alerts
        when {
            list == null -> Loading()
            list.isEmpty() -> EmptyState("Aucune alerte récente. Tirez vers le bas pour actualiser.")
            else -> list.forEach { a -> AlertRow(a) }
        }
    }
}

@Composable
private fun AlertRow(a: SafeZoneAlert) {
    AppCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(if (a.mine) R.drawable.ic_check else R.drawable.ic_shield, if (a.mine) SafeGreen else AlertRed, size = 40.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(if (a.mine) "Alerte envoyée" else a.message, style = MaterialTheme.typography.titleSmall)
                Text(
                    listOf(a.city ?: "Lieu inconnu", relativeTime(a.createdAt)).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }
    }
}

// --- Statistiques ----------------------------------------------------------------------------

@Composable
private fun StatsScreen(backend: Backend, goToAccount: () -> Unit) {
    val session by backend.session.collectAsState()
    val context = LocalContext.current
    var stats by remember { mutableStateOf<List<CityStat>?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    var reload by remember { mutableIntStateOf(0) }

    LaunchedEffect(session, reload) {
        if (session == null) return@LaunchedEffect
        stats = try {
            backend.cityStatsToday()
        } catch (e: BackendException) {
            context.toast(e.message ?: "Chargement impossible")
            stats ?: emptyList()
        }
        refreshing = false
    }

    Page(
        "Stats",
        "Détections partagées par la communauté depuis minuit.",
        refreshing = refreshing,
        onRefresh = if (session != null) ({ refreshing = true; reload++ }) else null,
    ) {
        if (session == null) {
            NeedAccount(R.drawable.ic_stats, goToAccount)
            return@Page
        }
        val list = stats
        if (list == null) {
            Loading()
            return@Page
        }
        val total = list.sumOf { it.detections }
        val max = (list.maxOfOrNull { it.detections } ?: 1L).coerceAtLeast(1L)

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Coral, AlertRed)))
                .padding(24.dp),
        ) {
            Column {
                Text("AUJOURD'HUI", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                Text("$total", style = MaterialTheme.typography.displaySmall, color = Color.White)
                Text(
                    if (total <= 1) "détection partagée" else "détections partagées",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
        }

        SectionLabel("Par ville", Modifier.padding(top = 8.dp))
        if (list.isEmpty()) {
            EmptyState("Aucune détection partagée aujourd'hui. Soyez le premier !")
        } else {
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(vertical = 6.dp)) {
                    list.forEachIndexed { i, row -> CityRow(i + 1, row, row.detections.toFloat() / max) }
                }
            }
        }
    }
}

@Composable
private fun CityRow(rank: Int, row: CityStat, ratio: Float) {
    val progress by animateFloatAsState(ratio, tween(700), label = "bar")
    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$rank", style = MaterialTheme.typography.labelLarge, color = Muted, modifier = Modifier.width(24.dp))
            Text(row.city, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text("${row.detections}", style = MaterialTheme.typography.titleMedium, color = Coral)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .padding(start = 24.dp)
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Coral, AlertRed))),
            )
        }
    }
}

// --- Compte ----------------------------------------------------------------------------------

@Composable
private fun AccountScreen(backend: Backend) {
    val session by backend.session.collectAsState()
    val savedCity by backend.city.collectAsState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
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

    val s = session
    Page("Compte", if (s == null) "Facultatif : le radar fonctionne sans. Même compte que sur le site." else null) {
        if (!backend.isConfigured) {
            EmptyState("Les fonctions de compte ne sont pas disponibles dans cette version.")
            return@Page
        }
        if (s == null) {
            SignInForm(busy) { mode, email, password, name ->
                run {
                    if (mode == 0) {
                        backend.signIn(email, password)
                        context.toast("Connecté")
                    } else if (backend.signUp(email, password, name)) {
                        context.toast("Compte créé")
                    } else {
                        context.toast("Compte créé : confirmez votre adresse via le lien reçu par e-mail, puis connectez-vous.")
                    }
                }
            }
            Benefits()
        } else {
            // En-tête de profil
            AppCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Coral, AlertRed))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(s.email.take(1).uppercase(), style = MaterialTheme.typography.titleLarge, color = Color.White)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Connecté", style = MaterialTheme.typography.bodySmall, color = Muted)
                        Text(s.email, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    }
                }
            }

            SectionLabel("Ma ville", Modifier.padding(top = 8.dp))
            var city by rememberSaveable(savedCity) { mutableStateOf(savedCity) }
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Field(city, { city = it }, "Ville (pour les stats et vos alertes)", R.drawable.ic_pin)
                    AnimatedVisibility(city.trim() != savedCity) {
                        BigButton("Enregistrer", onClick = { run { backend.updateCity(city); context.toast("Ville enregistrée") } }, enabled = !busy)
                    }
                }
            }

            SectionLabel("Informations", Modifier.padding(top = 8.dp))
            AppCard(Modifier.fillMaxWidth()) {
                Column {
                    SettingsRow(R.drawable.ic_file, "Mentions légales", { uriHandler.openUri("${BuildConfig.SITE_URL}/mentions-legales") })
                    HorizontalDivider(color = Hairline)
                    SettingsRow(R.drawable.ic_lock, "Confidentialité", { uriHandler.openUri("${BuildConfig.SITE_URL}/confidentialite") })
                }
            }

            var confirmDelete by remember { mutableStateOf(false) }
            AppCard(Modifier.fillMaxWidth()) {
                Column {
                    SettingsRow(R.drawable.ic_logout, "Se déconnecter", { backend.signOut() }, trailing = {})
                    HorizontalDivider(color = Hairline)
                    SettingsRow(R.drawable.ic_trash, "Supprimer mon compte", { confirmDelete = true }, tint = AlertRed, trailing = {})
                }
            }
            if (confirmDelete) {
                AlertDialog(
                    onDismissRequest = { confirmDelete = false },
                    containerColor = NavyCard,
                    title = { Text("Supprimer votre compte ?") },
                    text = { Text("Votre compte, vos détections partagées, vos amis et vos alertes Safe Zone seront effacés définitivement.", color = Muted) },
                    confirmButton = {
                        TextButton(onClick = {
                            confirmDelete = false
                            run { backend.deleteAccount(); context.toast("Compte supprimé") }
                        }) { Text("Supprimer", color = AlertRed) }
                    },
                    dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Annuler", color = Color.White) } },
                )
            }
        }
        if (s == null) LegalLinks()
    }
}

@Composable
private fun SignInForm(busy: Boolean, onSubmit: (Int, String, String, String) -> Unit) {
    var mode by rememberSaveable { mutableIntStateOf(0) } // 0 = connexion, 1 = inscription
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }

    SegmentedToggle(listOf("Connexion", "Inscription"), mode, { mode = it })
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AnimatedVisibility(mode == 1) {
                Field(name, { name = it }, "Pseudo", R.drawable.ic_user)
            }
            Field(email, { email = it }, "E-mail", R.drawable.ic_mail, KeyboardType.Email)
            Field(password, { password = it }, "Mot de passe (6 caractères min.)", R.drawable.ic_lock, KeyboardType.Password, secret = true)
            BigButton(
                if (mode == 0) "Se connecter" else "Créer mon compte",
                onClick = { onSubmit(mode, email, password, name) },
                enabled = !busy && email.contains('@') && password.length >= 6,
            )
        }
    }
}

@Composable
private fun Benefits() {
    Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(
            R.drawable.ic_share to "Partagez vos détections",
            R.drawable.ic_shield to "Alertez vos amis avec la Safe Zone",
            R.drawable.ic_stats to "Suivez les stats de votre ville",
        ).forEach { (icon, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(icon), contentDescription = null, tint = Coral, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
    }
}

@Composable
private fun Field(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    @DrawableRes icon: Int,
    keyboard: KeyboardType = KeyboardType.Text,
    secret: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(18.dp)) },
        singleLine = true,
        visualTransformation = if (secret) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Coral,
            unfocusedBorderColor = Hairline,
            focusedLabelColor = Coral,
            unfocusedLabelColor = Muted,
            focusedLeadingIconColor = Coral,
            unfocusedLeadingIconColor = Muted,
            cursorColor = Coral,
            focusedContainerColor = NavySurface,
            unfocusedContainerColor = NavySurface,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

// --- États communs ---------------------------------------------------------------------------

@Composable
private fun NeedAccount(@DrawableRes icon: Int, goToAccount: () -> Unit) {
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            IconBadge(icon, Coral, size = 56.dp)
            Text("Réservé aux membres", style = MaterialTheme.typography.titleMedium)
            Text(
                "Un compte gratuit suffit. Le radar, lui, fonctionne sans.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                textAlign = TextAlign.Center,
            )
            BigButton("Se connecter ou s'inscrire", onClick = goToAccount)
        }
    }
}

@Composable
private fun Loading() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) { CircularProgressIndicator(color = Coral, strokeWidth = 3.dp, modifier = Modifier.size(28.dp)) }
}

@Composable
private fun EmptyState(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = Muted,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
    )
}
