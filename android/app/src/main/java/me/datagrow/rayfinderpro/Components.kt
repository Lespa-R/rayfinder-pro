package me.datagrow.rayfinderpro

import android.content.Context
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.Duration
import java.time.OffsetDateTime

/** Carte standard : coins arrondis, fond légèrement relevé, liseré discret. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    color: Color = NavyCard,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    val border = BorderStroke(1.dp, Hairline)
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = shape, color = color, contentColor = Color.White, border = border, content = content)
    } else {
        Surface(modifier = modifier, shape = shape, color = color, contentColor = Color.White, border = border, content = content)
    }
}

@Composable
fun PageHeader(title: String, subtitle: String? = null) {
    Column(Modifier.padding(top = 12.dp, bottom = 8.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        subtitle?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = Muted, modifier = modifier)
}

/** Pastille ronde avec une icône, utilisée en tête de carte. */
@Composable
fun IconBadge(@DrawableRes icon: Int, tint: Color, size: Dp = 44.dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.48f))
    }
}

/** Bouton principal pleine largeur, 56 dp, avec retour haptique. */
@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    container: Color = Coral,
    content: Color = NavySurface,
    enabled: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    val bg by animateColorAsState(container, label = "button")
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = content,
            disabledContainerColor = NavyRaised,
            disabledContentColor = Muted,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        icon?.let {
            Icon(painterResource(it), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Sélecteur à deux (ou plus) segments, avec pastille animée. */
@Composable
fun SegmentedToggle(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NavySurface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { i, label ->
            val active = i == selected
            val bg by animateColorAsState(if (active) NavyRaised else Color.Transparent, label = "segment")
            val fg by animateColorAsState(if (active) Color.White else Muted, label = "segmentText")
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .clickable { onSelect(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = fg, textAlign = TextAlign.Center)
            }
        }
    }
}

/** Force du signal en 4 barres, plus lisible que les dBm. */
@Composable
fun SignalBars(rssi: Int, color: Color, modifier: Modifier = Modifier) {
    val level = when {
        rssi >= -60 -> 4
        rssi >= -70 -> 3
        rssi >= -80 -> 2
        else -> 1
    }
    Row(modifier.height(16.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom) {
        (1..4).forEach { i ->
            val h by animateFloatAsState(i * 4f, spring(), label = "bar")
            Box(
                Modifier
                    .width(4.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i <= level) color else Color.White.copy(alpha = 0.12f)),
            )
        }
    }
}

/** Ligne de réglage (icône, libellé, chevron), regroupée dans une AppCard. */
@Composable
fun SettingsRow(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color.White,
    trailing: @Composable RowScope.() -> Unit = {
        Icon(painterResource(R.drawable.ic_chevron), contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
    },
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = tint, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
fun LegalLinks() {
    val uriHandler = LocalUriHandler.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        TextButton(onClick = { uriHandler.openUri("${BuildConfig.SITE_URL}/mentions-legales") }) {
            Text("Mentions légales", style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        TextButton(onClick = { uriHandler.openUri("${BuildConfig.SITE_URL}/confidentialite") }) {
            Text("Confidentialité", style = MaterialTheme.typography.bodySmall, color = Muted)
        }
    }
}

/** 2 = très proche, 1 = proche, 0 = à portée. */
fun proximityLevel(rssi: Int): Int = when {
    rssi >= -60 -> 2
    rssi >= -75 -> 1
    else -> 0
}

fun proximityOf(rssi: Int): Pair<String, Color> = when {
    rssi >= -60 -> "Très proche" to AlertRed
    rssi >= -75 -> "Proche" to Orange
    else -> "À portée" to Muted
}

fun relativeTime(date: OffsetDateTime?): String {
    if (date == null) return ""
    val d = Duration.between(date, OffsetDateTime.now())
    return when {
        d.toMinutes() < 1 -> "à l'instant"
        d.toMinutes() < 60 -> "il y a ${d.toMinutes()} min"
        d.toHours() < 24 -> "il y a ${d.toHours()} h"
        else -> "il y a ${d.toDays()} j"
    }
}

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
