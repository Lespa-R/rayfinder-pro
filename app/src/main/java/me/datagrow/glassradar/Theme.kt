package me.datagrow.glassradar

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF0B1220)
val NavySurface = Color(0xFF101828)
val NavyCard = Color(0xFF1D2939)
val Coral = Color(0xFFF97066)
val AlertRed = Color(0xFFF04438)
val SafeGreen = Color(0xFF17B26A)

// Charte fixe (identique à l'icône et aux visuels Play Store), toujours sombre
private val scheme = darkColorScheme(
    primary = Coral,
    onPrimary = NavySurface,
    background = Navy,
    onBackground = Color.White,
    surface = Navy,
    onSurface = Color.White,
    surfaceVariant = NavyCard,
    onSurfaceVariant = Color(0xFF98A2B3),
    surfaceContainerHighest = NavyCard,
    outline = Color(0xFF475467),
    error = Coral,
)

@Composable
fun GlassRadarTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
