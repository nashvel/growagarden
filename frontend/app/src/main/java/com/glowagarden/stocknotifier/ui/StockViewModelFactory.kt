package com.glowagarden.stocknotifier.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.glowagarden.stocknotifier.UserPreferencesRepository

class StockViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StockViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StockViewModel(userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
