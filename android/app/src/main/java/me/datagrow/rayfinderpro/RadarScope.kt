package me.datagrow.rayfinderpro

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Radar animé : faisceau rotatif et onde pendant le scan, un point par appareil.
 * La distance au centre suit la puissance du signal ; l'angle est stable pour un même appareil.
 */
@Composable
fun RadarScope(devices: List<SeenDevice>, scanning: Boolean, alert: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "radar")
    val sweep by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(3200, easing = LinearEasing)), label = "sweep")
    val ping by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearOutSlowInEasing)), label = "ping")
    val accent by animateColorAsState(
        when {
            alert -> AlertRed
            scanning -> Coral
            else -> Muted
        },
        tween(500),
        label = "accent",
    )
    val activity by animateFloatAsState(if (scanning) 1f else 0f, tween(600), label = "activity")

    Canvas(modifier.aspectRatio(1f)) {
        val r = size.minDimension / 2f
        val c = center
        val stroke = 1.dp.toPx()

        // Halo de fond
        drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = 0.16f), Color.Transparent), c, r), r, c)

        // Anneaux et axes
        listOf(1f, 0.74f, 0.48f, 0.22f).forEach { f ->
            drawCircle(Color.White.copy(alpha = 0.08f), r * f, c, style = Stroke(stroke))
        }
        drawLine(Color.White.copy(alpha = 0.05f), Offset(c.x - r, c.y), Offset(c.x + r, c.y), stroke)
        drawLine(Color.White.copy(alpha = 0.05f), Offset(c.x, c.y - r), Offset(c.x, c.y + r), stroke)

        if (activity > 0f) {
            // Faisceau : dégradé conique qui se termine sur l'arête lumineuse
            rotate(sweep, c) {
                drawCircle(
                    Brush.sweepGradient(
                        0f to Color.Transparent,
                        0.78f to Color.Transparent,
                        1f to accent.copy(alpha = 0.42f * activity),
                        center = c,
                    ),
                    r,
                    c,
                )
                drawLine(accent.copy(alpha = 0.9f * activity), c, Offset(c.x + r, c.y), 2.dp.toPx())
            }
            // Onde qui s'élargit
            drawCircle(accent.copy(alpha = (1f - ping) * 0.45f * activity), r * ping, c, style = Stroke(2.dp.toPx()))
        }

        // Appareils
        devices.forEach { d ->
            val dist = ((-d.rssi - 40) / 60f).coerceIn(0.14f, 0.94f)
            val angle = Math.toRadians(((d.address.hashCode() and 0x7fffffff) % 360).toDouble())
            val p = Offset(c.x + (cos(angle) * r * dist).toFloat(), c.y + (sin(angle) * r * dist).toFloat())
            val color = if (d.isMeta) AlertRed else Muted
            if (d.isMeta) {
                drawCircle(color.copy(alpha = (1f - ping) * 0.5f), 6.dp.toPx() + 14.dp.toPx() * ping, p)
            }
            drawCircle(color.copy(alpha = 0.25f), 9.dp.toPx(), p)
            drawCircle(color, if (d.isMeta) 5.dp.toPx() else 3.5.dp.toPx(), p)
        }

        // Centre (vous)
        drawCircle(accent.copy(alpha = 0.25f), 11.dp.toPx(), c)
        drawCircle(accent, 5.dp.toPx(), c)
    }
}
