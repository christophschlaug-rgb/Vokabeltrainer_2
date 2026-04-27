package com.example.vokabeltrainer.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.vokabeltrainer.VokabelApp
import com.example.vokabeltrainer.data.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val dailyLimit: Int = AppPreferences.DEFAULT_DAILY_LIMIT,
    val limitOptions: List<Int> = AppPreferences.LIMIT_OPTIONS
)

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs: AppPreferences = (app as VokabelApp).prefs

    private val _state = MutableStateFlow(SettingsUiState(dailyLimit = prefs.dailyLimit))
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun setDailyLimit(value: Int) {
        prefs.dailyLimit = value
        _state.value = _state.value.copy(dailyLimit = prefs.dailyLimit)
    }
}
