package me.datagrow.rayfinderpro

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Navy = Color(0xFF0B1220)
val NavySurface = Color(0xFF101828)
val NavyCard = Color(0xFF172133)
val NavyRaised = Color(0xFF1D2939)
val Coral = Color(0xFFF97066)
val AlertRed = Color(0xFFF04438)
val Orange = Color(0xFFF79009)
val SafeGreen = Color(0xFF17B26A)
val Muted = Color(0xFF98A2B3)
val Hairline = Color(0x14FFFFFF)

// Inter, la police du site (licence OFL)
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_bold, FontWeight.Bold),
)

private val scheme = darkColorScheme(
    primary = Coral,
    onPrimary = NavySurface,
    background = Navy,
    onBackground = Color.White,
    surface = Navy,
    onSurface = Color.White,
    surfaceVariant = NavyCard,
    onSurfaceVariant = Muted,
    surfaceContainer = NavySurface,
    surfaceContainerHigh = NavyCard,
    surfaceContainerHighest = NavyRaised,
    secondaryContainer = Coral.copy(alpha = 0.16f),
    onSecondaryContainer = Coral,
    outline = Color(0xFF344054),
    outlineVariant = Hairline,
    error = Coral,
)

private fun TextStyle.inter(weight: FontWeight = FontWeight.Normal, tracking: Float = 0f) =
    copy(fontFamily = Inter, fontWeight = weight, letterSpacing = tracking.sp)

private val base = Typography()

private val typography = Typography(
    displaySmall = base.displaySmall.inter(FontWeight.Bold, -1f),
    headlineMedium = base.headlineMedium.inter(FontWeight.Bold, -0.6f),
    headlineSmall = base.headlineSmall.inter(FontWeight.Bold, -0.4f),
    titleLarge = base.titleLarge.inter(FontWeight.Bold, -0.3f),
    titleMedium = base.titleMedium.inter(FontWeight.Bold, -0.1f),
    titleSmall = base.titleSmall.inter(FontWeight.Bold),
    bodyLarge = base.bodyLarge.inter(),
    bodyMedium = base.bodyMedium.inter(),
    bodySmall = base.bodySmall.inter(),
    labelLarge = base.labelLarge.inter(FontWeight.Bold, 0.1f),
    labelMedium = base.labelMedium.inter(FontWeight.Bold, 0.2f),
    labelSmall = base.labelSmall.inter(FontWeight.Bold, 0.8f),
)

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun RayFinderProTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, typography = typography, shapes = shapes, content = content)
}
