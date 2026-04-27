package com.example.vokabeltrainer

import com.example.vokabeltrainer.data.LearningState
import com.example.vokabeltrainer.srs.SrsEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class SrsEngineTest {

    private fun state(level: Int) = LearningState(wordId = "x", level = level, nextReviewDate = 0)

    @Test fun `correct at L0 advances to L1 with 3 days`() {
        val before = state(0)
        val after = SrsEngine.apply(before, correct = true)
        assertEquals(1, after.level)
        val diff = after.nextReviewDate - SrsEngine.startOfTodayMillis()
        assertEquals(SrsEngine.daysToMillis(3), diff)
    }

    @Test fun `correct at L4 advances to L5 with 180 days`() {
        val after = SrsEngine.apply(state(4), true)
        assertEquals(5, after.level)
        assertEquals(SrsEngine.daysToMillis(180),
            after.nextReviewDate - SrsEngine.startOfTodayMillis())
    }

    @Test fun `correct at L5 stays at L5`() {
        val after = SrsEngine.apply(state(5), true)
        assertEquals(5, after.level)
        assertEquals(SrsEngine.daysToMillis(180),
            after.nextReviewDate - SrsEngine.startOfTodayMillis())
    }

    @Test fun `wrong resets to L0 and schedules tomorrow`() {
        val after = SrsEngine.apply(state(3), false)
        assertEquals(0, after.level)
        assertEquals(SrsEngine.daysToMillis(1),
            after.nextReviewDate - SrsEngine.startOfTodayMillis())
    }
}
