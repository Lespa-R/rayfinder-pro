package me.datagrow.rayfinderpro

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale

/** Icône selon ce qu'on sait du type d'appareil. */
@DrawableRes
fun SeenDevice.icon(): Int {
    if (isMeta) return R.drawable.ic_glasses
    val k = listOfNotNull(info.kind, name, info.manufacturer).joinToString(" ").lowercase(Locale.FRENCH)
    return when {
        "lunettes" in k -> R.drawable.ic_glasses
        listOf("écouteurs", "airpods", "buds", "casque", "enceinte", "audio", "beats", "bose", "jbl").any { it in k } -> R.drawable.ic_headphones
        listOf("iphone", "téléphone", "smartphone", "galaxy").any { it in k } -> R.drawable.ic_phone
        listOf("montre", "bracelet", "band", "watch", "garmin", "amazfit").any { it in k } -> R.drawable.ic_watch
        listOf("airtag", "tile", "tag", "traceur", "porte-clés", "balise", "localiser").any { it in k } -> R.drawable.ic_tag
        listOf("pc ", "pc windows", "ordinateur", "mac").any { it in k } -> R.drawable.ic_laptop
        else -> R.drawable.ic_bluetooth
    }
}

/** Ligne d'info sous le nom, sans répéter ce que le titre dit déjà. */
fun SeenDevice.secondaryLine(): String? {
    if (isMeta) return metaReason
    val title = displayName
    val parts = listOfNotNull(
        info.kind?.takeIf { it != title },
        info.manufacturer?.takeIf { !title.contains(it) },
    )
    return when {
        parts.isNotEmpty() -> parts.joinToString(" · ")
        !isIdentified -> "Rien d'annoncé · touchez pour en savoir plus"
        else -> null
    }
}

private fun formatDistance(rssi: Int): String =
    "≈ " + String.format(Locale.FRENCH, "%.1f", Backend.estimateDistance(rssi)) + " m"

private fun formatAge(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return when {
        s < 2 -> "à l'instant"
        s < 60 -> "$s s"
        else -> "${s / 60} min ${s % 60} s"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailsSheet(
    device: SeenDevice,
    now: Long,
    stillVisible: Boolean,
    onShare: (() -> Unit)?,
    isShared: Boolean,
    onDismiss: () -> Unit,
) {
    val (proximity, color) = proximityOf(device.rssi)
    val accent = if (device.isMeta) color else Coral
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = NavySurface,
        contentColor = androidx.compose.ui.graphics.Color.White,
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // En-tête
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(device.icon(), accent, size = 56.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(device.displayName, style = MaterialTheme.typography.titleLarge)
                    Text(
                        if (stillVisible) proximity else "Signal perdu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (stillVisible) color else Muted,
                    )
                }
            }

            if (device.isMeta) {
                AppCard(Modifier.fillMaxWidth(), color = AlertRed.copy(alpha = 0.12f)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Lunettes connectées Meta", style = MaterialTheme.typography.titleSmall, color = AlertRed)
                        Text(
                            "Identifiées par : ${device.metaReason}. Ces lunettes peuvent prendre des photos et filmer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }
            }

            // Mesures principales
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Distance", formatDistance(device.rssi), Modifier.weight(1f))
                StatTile("Signal", "${device.rssi} dBm", Modifier.weight(1f)) {
                    SignalBars(device.rssi, accent)
                }
                StatTile("Vu", formatAge(now - device.lastSeen), Modifier.weight(1f))
            }

            // Ce que l'appareil annonce
            SectionLabel("Ce que l'appareil annonce")
            AppCard(Modifier.fillMaxWidth()) {
                Column {
                    InfoRow("Nom", device.name ?: "Non diffusé")
                    InfoRow("Type", device.info.kind ?: "Inconnu")
                    InfoRow("Fabricant", device.info.manufacturer ?: "Non diffusé")
                    if (device.info.services.isNotEmpty()) InfoRow("Services", device.info.services.joinToString(", "))
                    device.info.txPower?.let { InfoRow("Puissance d'émission", "$it dBm") }
                    device.info.connectable?.let { InfoRow("Connectable", if (it) "Oui" else "Non (simple balise)") }
                    InfoRow("Détecté depuis", formatAge(now - device.firstSeen))
                    InfoRow("Adresse Bluetooth", device.address, mono = true, last = device.info.rawManufacturer == null)
                    device.info.rawManufacturer?.let { InfoRow("Données fabricant", it, mono = true, last = true) }
                }
            }

            // Explications
            AppCard(Modifier.fillMaxWidth(), color = NavyCard) {
                Row(Modifier.padding(16.dp)) {
                    Icon(painterResource(R.drawable.ic_info), contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        buildString {
                            if (!device.isIdentified) {
                                append("Cet appareil n'annonce ni nom ni fabricant. C'est fréquent : beaucoup d'objets restent discrets pour protéger la vie privée de leur propriétaire. ")
                            }
                            append("La plupart des appareils changent d'adresse Bluetooth toutes les 15 minutes environ : un même appareil peut donc apparaître plusieurs fois dans la liste.")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
            }

            if (isShared || onShare != null) {
                BigButton(
                    if (isShared) "Détection partagée" else "Partager cette détection",
                    onClick = { onShare?.invoke() },
                    icon = if (isShared) R.drawable.ic_check else R.drawable.ic_share,
                    enabled = !isShared,
                )
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier, extra: (@Composable () -> Unit)? = null) {
    AppCard(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Muted)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            extra?.let {
                Spacer(Modifier.height(6.dp))
                it()
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, mono: Boolean = false, last: Boolean = false) {
    Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.Top) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Muted, modifier = Modifier.width(130.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.let { if (mono) it.copy(fontFamily = FontFamily.Monospace) else it },
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
    if (!last) HorizontalDivider(color = Hairline, modifier = Modifier.padding(horizontal = 16.dp))
}
