package com.example.vokabeltrainer.ui.units

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.vokabeltrainer.VokabelApp
import com.example.vokabeltrainer.data.LearningUnit
import com.example.vokabeltrainer.data.Word
import com.example.vokabeltrainer.network.VocabRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UnitDetailUiState(
    val unit: LearningUnit? = null,
    val message: String? = null,
    val error: String? = null
)

class UnitDetailViewModel(
    app: Application,
    val unitId: String
) : AndroidViewModel(app) {

    private val db = (app as VokabelApp).db
    private val repo: VocabRepository = (app as VokabelApp).repository

    private val _state = MutableStateFlow(UnitDetailUiState())
    val state: StateFlow<UnitDetailUiState> = _state.asStateFlow()

    val words: StateFlow<List<Word>> =
        db.wordDao().forUnitFlow(unitId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            val unit = db.unitDao().byId(unitId)
            _state.value = _state.value.copy(unit = unit)
        }
    }

    /**
     * Fügt eine Vokabel zur Unit hinzu. Dedupliziert ggf. mit Standard-Wörterbuch.
     */
    fun addWord(en: String, deRaw: String, pos: String) {
        viewModelScope.launch {
            try {
                val deList = deRaw.split(",", "/")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                if (deList.isEmpty()) {
                    _state.value = _state.value.copy(error = "Mindestens eine deutsche Übersetzung angeben.")
                    return@launch
                }
                val w = repo.addUnitWord(
                    unitId = unitId,
                    en = en.trim(),
                    de = deList,
                    pos = pos.trim()
                )
                val msg = if (w.customTranslations) {
                    "Standardvokabel \"${w.en}\" mit deinen Übersetzungen ersetzt."
                } else {
                    "\"${w.en}\" hinzugefügt."
                }
                _state.value = _state.value.copy(message = msg)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(error = t.message ?: "Fehler beim Hinzufügen.")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null, error = null)
    }

    companion object {
        fun factory(unitId: String) = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as Application
                UnitDetailViewModel(app, unitId)
            }
        }
    }
}
