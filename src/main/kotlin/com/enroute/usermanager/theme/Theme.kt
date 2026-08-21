package com.enroute.usermanager.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// Pulled directly from the website's frontpage.html palette, so the admin
// tool reads as the same product rather than a generic dev utility.
object EnrouteColors {
    val Background = Color(0xFF0A0D12)
    val Surface = Color(0xFF0E1B2C)
    val SurfaceVariant = Color(0xFF132840)
    val Border = Color(0xFF2C5DA6).copy(alpha = 0.35f)

    val AccentPrimary = Color(0xFF2C5DA6)
    val AccentLight = Color(0xFF6FA8DC)

    val TextPrimary = Color(0xFFF7FAFD)
    val TextSecondary = Color(0xFF8FA0B5)
    val TextMuted = Color(0xFF5C7292)

    val Danger = Color(0xFFE0605A)
    val Warning = Color(0xFFE0A64C)
    val Success = Color(0xFF4CAF7D)
}

// The website loads Space Grotesk / Inter / IBM Plex Mono from Google
// Fonts. Desktop Compose can't fetch web fonts at runtime the same way, so
// this falls back to close system-available equivalents. Swap in the real
// font files here (see README) if you want an exact match.
object EnrouteFonts {
    val Heading = FontFamily.SansSerif
    val Body = FontFamily.SansSerif
    val Mono = FontFamily.Monospace
}

private val EnrouteDarkColorScheme = darkColorScheme(
    background = EnrouteColors.Background,
    surface = EnrouteColors.Surface,
    surfaceVariant = EnrouteColors.SurfaceVariant,
    primary = EnrouteColors.AccentPrimary,
    secondary = EnrouteColors.AccentLight,
    onBackground = EnrouteColors.TextPrimary,
    onSurface = EnrouteColors.TextPrimary,
    onPrimary = EnrouteColors.TextPrimary,
    error = EnrouteColors.Danger,
    outline = EnrouteColors.Border
)

@Composable
fun EnrouteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EnrouteDarkColorScheme,
        typography = MaterialTheme.typography.copy(
            headlineSmall = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = EnrouteFonts.Heading,
                fontWeight = FontWeight.SemiBold
            ),
            titleMedium = MaterialTheme.typography.titleMedium.copy(
                fontFamily = EnrouteFonts.Heading,
                fontWeight = FontWeight.SemiBold
            ),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = EnrouteFonts.Body
            ),
            bodySmall = MaterialTheme.typography.bodySmall.copy(
                fontFamily = EnrouteFonts.Body
            ),
            labelSmall = MaterialTheme.typography.labelSmall.copy(
                fontFamily = EnrouteFonts.Mono
            )
        ),
        content = content
    )
}

val MonoLabel = TextStyle(fontFamily = EnrouteFonts.Mono)
