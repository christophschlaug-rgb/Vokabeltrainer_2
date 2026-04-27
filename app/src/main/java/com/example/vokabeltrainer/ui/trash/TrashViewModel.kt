package com.example.vokabeltrainer.ui.trash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vokabeltrainer.VokabelApp
import com.example.vokabeltrainer.data.Word
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrashViewModel(app: Application) : AndroidViewModel(app) {

    private val db = (app as VokabelApp).db
    private val repo = (app as VokabelApp).repository

    val deletedWords: StateFlow<List<Word>> =
        db.wordDao().deletedFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun restore(id: String) {
        viewModelScope.launch { repo.restoreWord(id) }
    }
}
