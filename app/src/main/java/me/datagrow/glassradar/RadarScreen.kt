package me.datagrow.glassradar

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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

private const val FORGET_AFTER_MS = 30_000L

private val requiredPermissions =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) arrayOf(Manifest.permission.BLUETOOTH_SCAN)
    else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

private fun Context.hasPermissions() = requiredPermissions.all {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun RadarScreen(scanner: BleScanner) {
    val context = LocalContext.current
    val devices by scanner.devices.collectAsState()
    val scanning by scanner.scanning.collectAsState()
    val error by scanner.error.collectAsState()
    var showAll by rememberSaveable { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

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

    val metaDevices = devices.values.filter { it.isMeta }.sortedByDescending { it.rssi }
    val listed = if (showAll) devices.values.sortedByDescending { it.rssi } else metaDevices

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            StatusBadge(scanning = scanning, alert = metaDevices.isNotEmpty())
            Spacer(Modifier.height(16.dp))
            Text(
                text = when {
                    !scanning -> "Radar à l'arrêt"
                    metaDevices.isEmpty() -> "Aucune lunette Meta détectée"
                    metaDevices.size == 1 -> "Lunettes Meta à proximité !"
                    else -> "${metaDevices.size} appareils Meta à proximité !"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = if (scanning) "Recherche Bluetooth en cours…" else "Lancez le radar pour analyser les alentours",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            when {
                !scanner.hasBluetooth -> Warning("Cet appareil ne prend pas en charge le Bluetooth Low Energy.")
                !scanner.isBluetoothOn && !scanning -> {
                    Warning("Le Bluetooth est désactivé.")
                    OutlinedButton(onClick = {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    }) { Text("Ouvrir les réglages Bluetooth") }
                }
                else -> Button(
                    onClick = { if (scanning) scanner.stop() else startScan() },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (scanning) "Arrêter le radar" else "Lancer le radar") }
            }
            if (permissionDenied) {
                Warning("Autorisation refusée : l'application ne peut pas scanner sans l'accès aux appareils à proximité.")
            }
            error?.let { Warning(it) }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Afficher tous les appareils Bluetooth", Modifier.weight(1f))
                Switch(checked = showAll, onCheckedChange = { showAll = it })
            }

            LazyColumn(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(listed, key = { it.address }) { DeviceCard(it, now) }
                item {
                    Text(
                        "Détection basée sur les signaux Bluetooth publics émis par les lunettes. " +
                            "Elle n'est pas infaillible : des lunettes éteintes ou en veille profonde peuvent ne rien émettre, " +
                            "et d'autres appareils Meta (casques Quest…) peuvent apparaître.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(scanning: Boolean, alert: Boolean) {
    val color = when {
        alert -> AlertRed
        scanning -> SafeGreen
        else -> MaterialTheme.colorScheme.outline
    }
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = if (scanning) 1.15f else 1f,
        animationSpec = infiniteRepeatable(tween(if (alert) 400 else 1200), RepeatMode.Reverse),
        label = "scale",
    )
    Box(
        Modifier
            .size(140.dp)
            .scale(pulse)
            .background(color.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(88.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(if (alert) "!" else "👓", color = Color.White, style = MaterialTheme.typography.headlineLarge)
        }
    }
}

@Composable
private fun DeviceCard(device: SeenDevice, now: Long) {
    val (proximity, color) = when {
        device.rssi >= -60 -> "Très proche (< 2 m)" to AlertRed
        device.rssi >= -75 -> "Proche (quelques mètres)" to Color(0xFFDC6803)
        else -> "À portée (> 5 m)" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(
        colors = if (device.isMeta) CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.10f))
        else CardDefaults.cardColors(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                device.name ?: if (device.isMeta) "Lunettes Meta (nom masqué)" else "Appareil inconnu",
                fontWeight = FontWeight.SemiBold,
            )
            device.metaReason?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = AlertRed)
            }
            Spacer(Modifier.height(4.dp))
            Row {
                Text(proximity, color = color, modifier = Modifier.weight(1f))
                Text("${device.rssi} dBm", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "Vu il y a ${((now - device.lastSeen) / 1000).coerceAtLeast(0)} s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Warning(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(vertical = 8.dp),
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
