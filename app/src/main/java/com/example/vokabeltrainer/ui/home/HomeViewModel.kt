package com.example.vokabeltrainer.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vokabeltrainer.VokabelApp
import com.example.vokabeltrainer.srs.SrsEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = false,
    val total: Int = 0,
    val dueToday: Int = 0,
    val dailyLimit: Int = 100,
    val mastered: Int = 0,
    val avgLevel: Double = 0.0,
    val levelDistribution: Map<Int, Int> = emptyMap(),
    val message: String? = null,
    val error: String? = null
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as VokabelApp).repository
    private val db = (app as VokabelApp).db
    private val prefs = (app as VokabelApp).prefs

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            repo.seedIfEmpty()
            loadStats()
        }
    }

    private suspend fun loadStats() {
        val today = SrsEngine.startOfTodayMillis()
        val total = db.learningDao().total()
        val due = db.learningDao().dueCount(today)
        val mastered = db.learningDao().masteredCount()
        val avg = db.learningDao().averageLevel() ?: 0.0
        val limit = prefs.dailyLimit
        val rawCounts = db.learningDao().countByLevel().associate { it.level to it.count }
        val distribution = (0..5).associateWith { (rawCounts[it] ?: 0) }
        _state.value = _state.value.copy(
            loading = false,
            total = total,
            dueToday = due.coerceAtMost(limit),
            dailyLimit = limit,
            mastered = mastered,
            avgLevel = avg,
            levelDistribution = distribution
        )
    }

    /**
     * Inkrementelles Nachladen aus dem mitgelieferten Asset.
     * Bestehende Wörter werden nicht überschrieben, gelöschte nicht wiederbelebt.
     */
    fun reloadFromAsset() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, message = null)
            try {
                val n = repo.refreshFromAsset()
                _state.value = _state.value.copy(
                    message = if (n == 0) "Keine neuen Vokabeln gefunden."
                              else "$n neue Vokabeln hinzugefügt."
                )
            } catch (t: Throwable) {
                _state.value = _state.value.copy(
                    error = "Aktualisieren fehlgeschlagen: ${t.message ?: "Unbekannter Fehler"}"
                )
            } finally {
                loadStats()
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null, error = null)
    }
}
