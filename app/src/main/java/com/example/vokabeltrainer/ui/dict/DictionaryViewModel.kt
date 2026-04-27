package com.example.vokabeltrainer.ui.dict

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vokabeltrainer.VokabelApp
import com.example.vokabeltrainer.data.LearningState
import com.example.vokabeltrainer.data.Word
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DictionaryViewModel(app: Application) : AndroidViewModel(app) {

    private val db = (app as VokabelApp).db
    private val repo = (app as VokabelApp).repository

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    val words: StateFlow<List<Word>> = _query
        .debounce(150)
        .distinctUntilChanged()
        .flatMapLatest { q -> db.wordDao().searchFlow(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) { _query.value = q }

    suspend fun getWord(id: String): Word? = db.wordDao().byId(id)
    suspend fun getState(id: String): LearningState? = db.learningDao().byWord(id)

    fun deleteWord(id: String) {
        viewModelScope.launch { repo.deleteWord(id) }
    }
}
