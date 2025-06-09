package com.glowagarden.stocknotifier.model

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


data class SelectableItem(
    val name: String,
    val tier: String,
    val category: String,
    val initialIsSelected: Boolean = true // Default to selected, or load from prefs
) {
    var isSelected by mutableStateOf(initialIsSelected)
}
