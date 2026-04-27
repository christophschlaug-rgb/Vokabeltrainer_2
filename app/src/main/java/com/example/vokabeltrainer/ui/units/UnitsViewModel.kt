package com.example.vokabeltrainer.ui.units

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vokabeltrainer.VokabelApp
import com.example.vokabeltrainer.data.LearningUnit
import com.example.vokabeltrainer.data.UnitWithCount
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class UnitsViewModel(app: Application) : AndroidViewModel(app) {

    private val db = (app as VokabelApp).db

    val units: StateFlow<List<UnitWithCount>> =
        db.unitDao().listWithCountFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun createUnit(name: String) {
        val cleaned = name.trim().take(80)
        if (cleaned.isBlank()) return
        viewModelScope.launch {
            db.unitDao().upsert(
                LearningUnit(
                    id = "u_" + UUID.randomUUID().toString().take(10),
                    name = cleaned,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteUnit(unitId: String) {
        viewModelScope.launch {
            // Erst die Unit-Wörter und ihre Lernzustände entfernen
            val words = db.wordDao().forUnit(unitId)
            for (w in words) {
                if (w.customTranslations) {
                    // Standard-Wort, das vom Nutzer überschrieben wurde:
                    // Unit-Bindung lösen, Standard wieder herstellen wäre komplex
                    // (alte Übersetzung verloren). Pragmatisch: Eintrag bleibt mit
                    // den Custom-Translations, nur Unit-Bindung wird entfernt.
                    db.wordDao().update(
                        w.copy(unitId = null, customTranslations = false)
                    )
                } else {
                    // Reine Unit-Vokabel: hard löschen
                    db.wordDao().hardDelete(w.id)
                    db.learningDao().deleteByWord(w.id)
                }
            }
            db.unitDao().deleteById(unitId)
        }
    }
}
