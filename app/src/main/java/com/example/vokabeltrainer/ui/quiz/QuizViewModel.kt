package com.example.vokabeltrainer.ui.quiz

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vokabeltrainer.VokabelApp
import com.example.vokabeltrainer.data.AppDatabase
import com.example.vokabeltrainer.data.LearningState
import com.example.vokabeltrainer.data.Word
import com.example.vokabeltrainer.grading.Grader
import com.example.vokabeltrainer.srs.SrsEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class QuizCard(
    val word: Word,
    val direction: Grader.Direction
) {
    /** Aufgabenseite (das, was angezeigt wird). */
    val prompt: String
        get() = if (direction == Grader.Direction.EN_TO_DE) word.en else word.deList.joinToString(" / ")

    /** Lösung(en) (gegen die geprüft wird). */
    val solutions: List<String>
        get() = if (direction == Grader.Direction.EN_TO_DE) word.deList else listOf(word.en)
}

data class QuizUiState(
    val loading: Boolean = true,
    val remaining: Int = 0,
    val done: Int = 0,
    val card: QuizCard? = null,
    val lastAnswerCorrect: Boolean? = null,
    val lastSolutionShown: List<String>? = null,
    val finished: Boolean = false,
    val sessionCorrect: Int = 0,
    val sessionWrong: Int = 0
)

class QuizViewModel(app: Application) : AndroidViewModel(app) {

    private val db: AppDatabase = (app as VokabelApp).db
    private val queue = ArrayDeque<Word>()

    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    init { loadQueue() }

    private fun loadQueue() {
        viewModelScope.launch {
            val today = SrsEngine.startOfTodayMillis()
            val due = db.learningDao().dueList(today, 100)
            val words = due.mapNotNull { db.wordDao().byId(it.wordId) }
            queue.clear()
            queue.addAll(words.shuffled())
            _state.value = QuizUiState(
                loading = false,
                remaining = queue.size,
                done = 0,
                card = nextCard()
            )
            if (_state.value.card == null) {
                _state.value = _state.value.copy(finished = true)
            }
        }
    }

    private fun nextCard(): QuizCard? {
        val w = queue.removeFirstOrNull() ?: return null
        val dir = if (Random.nextBoolean()) Grader.Direction.EN_TO_DE else Grader.Direction.DE_TO_EN
        return QuizCard(w, dir)
    }

    fun submit(input: String) {
        val current = _state.value.card ?: return
        val correct = Grader.check(current.direction, current.solutions, input)
        viewModelScope.launch {
            val existing = db.learningDao().byWord(current.word.id)
                ?: LearningState(wordId = current.word.id, nextReviewDate = SrsEngine.startOfTodayMillis())
            val updated = SrsEngine.apply(existing, correct)
            db.learningDao().upsert(updated)

            _state.value = _state.value.copy(
                lastAnswerCorrect = correct,
                lastSolutionShown = current.solutions,
                sessionCorrect = _state.value.sessionCorrect + if (correct) 1 else 0,
                sessionWrong = _state.value.sessionWrong + if (!correct) 1 else 0
            )
        }
    }

    fun next() {
        val nxt = nextCard()
        if (nxt == null) {
            _state.value = _state.value.copy(
                card = null,
                lastAnswerCorrect = null,
                lastSolutionShown = null,
                finished = true,
                done = _state.value.done + 1,
                remaining = 0
            )
        } else {
            _state.value = _state.value.copy(
                card = nxt,
                lastAnswerCorrect = null,
                lastSolutionShown = null,
                done = _state.value.done + 1,
                remaining = queue.size
            )
        }
    }
}
