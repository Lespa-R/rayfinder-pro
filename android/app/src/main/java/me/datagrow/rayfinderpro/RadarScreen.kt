package me.datagrow.rayfinderpro

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val FORGET_AFTER_MS = 30_000L

private val requiredPermissions =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) arrayOf(Manifest.permission.BLUETOOTH_SCAN)
    else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

private fun Context.hasPermissions() = requiredPermissions.all {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun RadarScreen(scanner: BleScanner, backend: Backend, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val devices by scanner.devices.collectAsState()
    val scanning by scanner.scanning.collectAsState()
    val error by scanner.error.collectAsState()
    var showAll by rememberSaveable { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val session by backend.session.collectAsState()
    val scope = rememberCoroutineScope()
    var shared by remember { mutableStateOf(setOf<String>()) }

    fun share(device: SeenDevice) {
        scope.launch {
            try {
                backend.shareSighting(device)
                shared = shared + device.address
                context.toast("Détection partagée avec la communauté")
            } catch (e: BackendException) {
                context.toast(e.message ?: "Partage impossible")
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        permissionDenied = !granted.values.all { it }
        if (!permissionDenied) scanner.start()
    }

    fun startScan() {
        if (context.hasPermissions()) scanner.start() else permissionLauncher.launch(requiredPermissions)
    }

    DisposableEffect(scanner) {
        scanner.onNewMetaDevice = { context.vibrateAlert() }
        onDispose { scanner.onNewMetaDevice = null }
    }

    // Garde l'écran allumé pendant le scan
    val view = LocalView.current
    DisposableEffect(scanning) {
        view.keepScreenOn = scanning
        onDispose { view.keepScreenOn = false }
    }

    LaunchedEffect(scanning) {
        while (scanning) {
            scanner.prune(FORGET_AFTER_MS)
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    // Tri stable : Meta d'abord, puis par palier de proximité, puis par ordre d'apparition.
    // Trier sur le RSSI brut ferait sauter les cartes à chaque variation du signal.
    val stableOrder = compareByDescending<SeenDevice> { it.isMeta }
        .thenByDescending { proximityLevel(it.rssi) }
        .thenBy { it.firstSeen }
        .thenBy { it.address }
    val allDevices = devices.values.sortedWith(stableOrder)
    val metaDevices = allDevices.filter { it.isMeta }
    val listed = if (showAll) allDevices else metaDevices
    val alert = metaDevices.isNotEmpty()

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "header") { RadarHeader(scanning, alert) }

        item(key = "scope") {
            RadarScope(
                devices = allDevices,
                scanning = scanning,
                alert = alert,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 56.dp, vertical = 4.dp),
            )
        }

        item(key = "status") {
            AnimatedContent(
                targetState = Triple(scanning, metaDevices.size, alert),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "status",
            ) { (isScanning, count, isAlert) ->
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        when {
                            !isScanning -> "Radar en veille"
                            count == 0 -> "Aucune lunette détectée"
                            count == 1 -> "Lunettes Meta à proximité"
                            else -> "$count appareils Meta à proximité"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isAlert) AlertRed else Color.White,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        when {
                            !isScanning -> "Lancez le radar pour analyser les alentours"
                            isAlert -> "Quelqu'un peut filmer près de vous"
                            else -> "Analyse des signaux Bluetooth en cours…"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        item(key = "action") {
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when {
                    !scanner.hasBluetooth -> Notice("Cet appareil ne prend pas en charge le Bluetooth Low Energy.")
                    !scanner.isBluetoothOn && !scanning -> {
                        Notice("Le Bluetooth est désactivé.")
                        BigButton(
                            "Activer le Bluetooth",
                            onClick = { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) },
                            icon = R.drawable.ic_bluetooth,
                        )
                    }
                    else -> BigButton(
                        text = if (scanning) "Arrêter le radar" else "Lancer le radar",
                        onClick = { if (scanning) scanner.stop() else startScan() },
                        icon = R.drawable.ic_radar,
                        container = if (scanning) NavyRaised else Coral,
                        content = if (scanning) Color.White else NavySurface,
                    )
                }
                if (permissionDenied) {
                    Notice("Autorisation refusée : sans l'accès aux appareils à proximité, le radar ne peut pas scanner.")
                }
                error?.let { Notice(it) }
            }
        }

        if (scanning || devices.isNotEmpty()) {
            item(key = "filter") {
                SegmentedToggle(
                    options = listOf("Lunettes Meta · ${metaDevices.size}", "Tout · ${allDevices.size}"),
                    selected = if (showAll) 1 else 0,
                    onSelect = { showAll = it == 1 },
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }

        items(listed, key = { it.address }) { device ->
            DeviceCard(
                device = device,
                now = now,
                // Partage proposé seulement pour les lunettes identifiées, une fois par appareil
                onShare = if (device.isMeta && session != null && device.address !in shared) {
                    { share(device) }
                } else null,
                isShared = device.address in shared,
                modifier = Modifier.animateItem(),
            )
        }

        if (scanning && listed.isEmpty()) {
            item(key = "empty") {
                Text(
                    if (showAll) "Aucun appareil Bluetooth pour l'instant…" else "Rien à signaler. Le radar continue de surveiller.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                )
            }
        }

        if (session == null && alert) {
            item(key = "account-hint") {
                Text(
                    "Créez un compte (onglet Compte) pour partager une détection et alerter vos amis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item(key = "footer") {
            Column(Modifier.padding(top = 16.dp)) {
                Text(
                    "Détection basée sur les signaux Bluetooth publics des lunettes. Elle n'est pas infaillible : " +
                        "des lunettes éteintes ou en veille peuvent ne rien émettre, et d'autres appareils Meta peuvent apparaître.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                LegalLinks()
            }
        }
    }
}

@Composable
private fun RadarHeader(scanning: Boolean, alert: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("RayFinder", style = MaterialTheme.typography.titleLarge)
        Text(" Pro", style = MaterialTheme.typography.titleLarge, color = Coral)
        Spacer(Modifier.weight(1f))
        val color by animateColorAsState(
            when {
                alert -> AlertRed
                scanning -> SafeGreen
                else -> Muted
            },
            label = "chip",
        )
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.14f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                when {
                    alert -> "Alerte"
                    scanning -> "Scan actif"
                    else -> "En veille"
                },
                style = MaterialTheme.typography.labelMedium,
                color = color,
            )
        }
    }
}

@Composable
private fun DeviceCard(
    device: SeenDevice,
    now: Long,
    onShare: (() -> Unit)?,
    isShared: Boolean,
    modifier: Modifier = Modifier,
) {
    val (proximity, color) = proximityOf(device.rssi)
    val seconds = ((now - device.lastSeen) / 1000).coerceAtLeast(0)
    AppCard(modifier.fillMaxWidth(), color = if (device.isMeta) AlertRed.copy(alpha = 0.10f) else NavyCard) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(if (device.isMeta) R.drawable.ic_glasses else R.drawable.ic_bluetooth, if (device.isMeta) color else Muted)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    device.name ?: if (device.isMeta) "Lunettes Meta" else "Appareil inconnu",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "$proximity · ${if (seconds < 2) "à l'instant" else "il y a $seconds s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (device.isMeta) color else Muted,
                )
                device.metaReason?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.width(10.dp))
            SignalBars(device.rssi, if (device.isMeta) color else Muted)
            if (isShared || onShare != null) {
                Spacer(Modifier.width(10.dp))
                FilledTonalIconButton(
                    onClick = { onShare?.invoke() },
                    enabled = !isShared,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Coral.copy(alpha = 0.16f),
                        contentColor = Coral,
                        disabledContainerColor = SafeGreen.copy(alpha = 0.16f),
                        disabledContentColor = SafeGreen,
                    ),
                ) {
                    Icon(
                        painterResource(if (isShared) R.drawable.ic_check else R.drawable.ic_share),
                        contentDescription = if (isShared) "Partagé" else "Partager",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Notice(text: String) {
    Text(
        text,
        color = Coral,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun Context.vibrateAlert() {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION") getSystemService(Vibrator::class.java)
    }
    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1))
}
