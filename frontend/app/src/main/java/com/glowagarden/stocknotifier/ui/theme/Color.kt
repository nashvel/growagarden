package com.glowagarden.stocknotifier.ui.theme

import androidx.compose.ui.graphics.Color

// Glow a Garden Inspired Theme Colors
val PrimaryGreen = Color(0xFF4CAF50) // A vibrant green
val DarkGreen = Color(0xFF388E3C)    // A darker shade for primary variants or dark theme
val LightGreen = Color(0xFFC8E6C9)   // A light green for backgrounds or secondary elements

val AccentYellow = Color(0xFFFFEB3B) // A bright yellow for accents
val DarkYellow = Color(0xFFFBC02D)   // A darker yellow

val BackgroundLight = Color(0xFFF5F5F5) // Light gray for background
val SurfaceLight = Color(0xFFFFFFFF)    // White for card surfaces

val BackgroundDark = Color(0xFF121212)  // Standard dark theme background
val SurfaceDark = Color(0xFF1E1E1E)     // Slightly lighter dark for surfaces

val TextPrimaryLight = Color(0xFF212121) // Dark gray for text on light backgrounds
val TextSecondaryLight = Color(0xFF757575) // Lighter gray for secondary text

val TextPrimaryDark = Color(0xFFFFFFFF)   // White for text on dark backgrounds
val TextSecondaryDark = Color(0xFFB0B0B0)  // Light gray for secondary text on dark

// Additional Green Shades for the "All Green" Theme
val SoftGreen = Color(0xFF81C784)
val LighterGreen = Color(0xFFA5D6A7)
val VeryLightGreen = Color(0xFFE8F5E9) // For overall background light
val CardSurfaceLight = Color(0xFFF1F8E9) // For card surfaces light

val DarkSurfaceGreen = Color(0xFF2A3A2A) // For card surfaces dark
val DarkBackgroundGreen = Color(0xFF1B261B) // For overall background dark
val MutedDarkGreen = Color(0xFF2E7D32)
val DeepDarkGreen = Color(0xFF1B5E20)

// Tier Colors
val TierCommon = Color.White // Or Color(0xFFE0E0E0) for a light gray if pure white is too stark on some backgrounds
val TierUncommon = Color(0xFF8D6E63) // Brown
val TierRare = Color(0xFF7E57C2)     // Bluish-Purple (Deep Purple 400)
val TierLegendary = AccentYellow    // #FFEB3B (Using existing AccentYellow)
val TierMythical = Color(0xFFAB47BC)  // Purple (Purple 400)
val TierDivine = Color(0xFFFF7043)   // Orange (Deep Orange 400)
val TierPrismaticColors = listOf(
    Color(0xFFFF0000), // Red
    Color(0xFFFF7F00), // Orange
    Color(0xFFFFFF00), // Yellow
    Color(0xFF00FF00), // Green
    Color(0xFF0000FF), // Blue
    Color(0xFF4B0082), // Indigo
    Color(0xFF9400D3)  // Violet
)

// Material 3 specific roles
val md_theme_light_primary = PrimaryGreen // #4CAF50
val md_theme_light_onPrimary = Color.White
val md_theme_light_primaryContainer = LightGreen     // #C8E6C9
val md_theme_light_onPrimaryContainer = DarkGreen    // #388E3C
val md_theme_light_secondary = SoftGreen           // #81C784
val md_theme_light_onSecondary = Color.Black        // Or DarkGreen
val md_theme_light_secondaryContainer = PrimaryGreen // Changed from AccentYellow for selected Nav item BG (matches screenshot)
val md_theme_light_onSecondaryContainer = DarkYellow // Text/Icon on Accent Yellow
val md_theme_light_tertiary = LighterGreen         // #A5D6A7
val md_theme_light_onTertiary = DarkGreen
val md_theme_light_tertiaryContainer = VeryLightGreen // #E8F5E9
val md_theme_light_onTertiaryContainer = DarkGreen
val md_theme_light_error = Color(0xFFB00020)
val md_theme_light_errorContainer = Color(0xFFFCD8DF)
val md_theme_light_onError = Color.White
val md_theme_light_onErrorContainer = Color(0xFFB00020)
val md_theme_light_background = VeryLightGreen     // #E8F5E9
val md_theme_light_onBackground = TextPrimaryLight   // #212121
val md_theme_light_surface = CardSurfaceLight      // #F1F8E9
val md_theme_light_onSurface = TextPrimaryLight
val md_theme_light_surfaceVariant = LightGreen       // #C8E6C9
val md_theme_light_onSurfaceVariant = TextPrimaryLight
val md_theme_light_outline = SoftGreen             // #81C784

val md_theme_dark_primary = LightGreen // #C8E6C9 (Lighter green for primary in dark mode for contrast)
val md_theme_dark_onPrimary = DarkGreen    // #388E3C
val md_theme_dark_primaryContainer = DarkGreen // #388E3C
val md_theme_dark_onPrimaryContainer = LightGreen // #C8E6C9
val md_theme_dark_secondary = SoftGreen    // #81C784
val md_theme_dark_onSecondary = Color.Black // Or DeepDarkGreen
val md_theme_dark_secondaryContainer = MutedDarkGreen // #2E7D32 (Darker green for container)
val md_theme_dark_onSecondaryContainer = LightGreen
val md_theme_dark_tertiary = PrimaryGreen  // #4CAF50
val md_theme_dark_onTertiary = Color.White
val md_theme_dark_tertiaryContainer = DeepDarkGreen // #1B5E20
val md_theme_dark_onTertiaryContainer = LightGreen
val md_theme_dark_error = Color(0xFFCF6679)
val md_theme_dark_errorContainer = Color(0xFFB00020)
val md_theme_dark_onError = Color.Black
val md_theme_dark_onErrorContainer = Color(0xFFFCD8DF)
val md_theme_dark_background = DarkBackgroundGreen // #1B261B
val md_theme_dark_onBackground = TextPrimaryDark    // White
val md_theme_dark_surface = DarkSurfaceGreen      // #2A3A2A
val md_theme_dark_onSurface = TextPrimaryDark
val md_theme_dark_surfaceVariant = MutedDarkGreen   // #2E7D32
val md_theme_dark_onSurfaceVariant = TextSecondaryDark // Light Gray
val md_theme_dark_outline = SoftGreen             // #81C784