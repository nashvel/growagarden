package com.glowagarden.stocknotifier.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme // Ensure this is Material3

@Composable
fun getColorForTier(tier: String): Color {
    val isDark = isSystemInDarkTheme()
    return when (tier.lowercase()) {
        "common" -> if (isDark) Color(0xFFB0BEC5) else Color(0xFF78909C) // Lighter Gray / Darker Gray
        "uncommon" -> if (isDark) Color(0xFF81C784) else Color(0xFF388E3C) // Lighter Green / Darker Green
        "rare" -> if (isDark) Color(0xFF64B5F6) else Color(0xFF1976D2) // Lighter Blue / Darker Blue
        "epic" -> if (isDark) Color(0xFFBA68C8) else Color(0xFF7B1FA2) // Lighter Purple / Darker Purple
        "legendary" -> if (isDark) Color(0xFFFFD54F) else Color(0xFFFFA000) // Lighter Gold / Darker Orange-Gold
        "mythical" -> if (isDark) Color(0xFFFF8A80) else Color(0xFFD32F2F) // Lighter Red / Darker Red
        "admin" -> if (isDark) Color(0xFFF06292) else Color(0xFFC2185B) // Lighter Pink / Darker Pink
        "event" -> if (isDark) Color(0xFF4DD0E1) else Color(0xFF0097A7) // Lighter Cyan / Darker Cyan
        "divine" -> if (isDark) Color(0xFF81D4FA) else Color(0xFF03A9F4) // Light Sky Blue / Sky Blue
        "prismatic" -> if (isDark) Color(0xFFF48FB1) else Color(0xFFEC407A) // Light Magenta / Magenta
        else -> MaterialTheme.colorScheme.onSurface // Default color
    }
}
