package com.example.vokabeltrainer.srs

import com.example.vokabeltrainer.data.LearningState
import java.util.Calendar
import java.util.TimeZone

/**
 * Spaced-Repetition-Engine mit festen Intervallen.
 *
 * Level -> Tage bis zur nächsten Wiederholung (bei RICHTIG):
 *   0 -> 1, 1 -> 3, 2 -> 10, 3 -> 30, 4 -> 90, 5 -> 180
 *
 * Bei RICHTIG wird Level um 1 erhöht (max 5) und das Intervall des
 * NEUEN Levels genutzt.
 * Bei FALSCH: Level zurück auf 0, nächste Abfrage morgen (heute + 1 Tag).
 */
object SrsEngine {

    val intervalsDays: IntArray = intArrayOf(1, 3, 10, 30, 90, 180)

    fun startOfTodayMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.timeInMillis = nowMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun daysToMillis(days: Int): Long = days * 24L * 60L * 60L * 1000L

    /**
     * Wendet das Ergebnis einer Abfrage auf den Zustand an und liefert den neuen Zustand zurück.
     */
    fun apply(state: LearningState, correct: Boolean, nowMillis: Long = System.currentTimeMillis()): LearningState {
        val todayStart = startOfTodayMillis(nowMillis)
        return if (correct) {
            val newLevel = (state.level + 1).coerceAtMost(5)
            val next = todayStart + daysToMillis(intervalsDays[newLevel])
            state.copy(
                level = newLevel,
                nextReviewDate = next,
                lastResult = "correct",
                lastReviewedAt = nowMillis,
                timesSeen = state.timesSeen + 1,
                timesCorrect = state.timesCorrect + 1
            )
        } else {
            val next = todayStart + daysToMillis(1)
            state.copy(
                level = 0,
                nextReviewDate = next,
                lastResult = "wrong",
                lastReviewedAt = nowMillis,
                timesSeen = state.timesSeen + 1
            )
        }
    }
}
