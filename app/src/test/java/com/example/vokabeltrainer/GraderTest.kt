package com.example.vokabeltrainer

import com.example.vokabeltrainer.grading.Grader
import com.example.vokabeltrainer.grading.Grader.Direction.DE_TO_EN
import com.example.vokabeltrainer.grading.Grader.Direction.EN_TO_DE
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GraderTest {

    @Test fun `DE to EN - to run accepts run`() =
        assertTrue(Grader.check(DE_TO_EN, listOf("to run"), "run"))

    @Test fun `DE to EN - to run accepts 'to run'`() =
        assertTrue(Grader.check(DE_TO_EN, listOf("to run"), "to run"))

    @Test fun `DE to EN - trims whitespace`() =
        assertTrue(Grader.check(DE_TO_EN, listOf("to run"), "  RUN  "))

    @Test fun `DE to EN - case insensitive`() =
        assertTrue(Grader.check(DE_TO_EN, listOf("to run"), "To Run"))

    @Test fun `DE to EN - solution without 'to' accepts 'to run'`() =
        assertTrue(Grader.check(DE_TO_EN, listOf("run"), "to run"))

    @Test fun `DE to EN - to abate accepts abate`() =
        assertTrue(Grader.check(DE_TO_EN, listOf("to abate"), "abate"))

    @Test fun `DE to EN - abates is wrong`() =
        assertFalse(Grader.check(DE_TO_EN, listOf("to abate"), "abates"))

    @Test fun `EN to DE - any of multiple solutions accepted`() {
        assertTrue(Grader.check(EN_TO_DE, listOf("nachlassen", "abnehmen"), "Nachlassen"))
        assertTrue(Grader.check(EN_TO_DE, listOf("nachlassen", "abnehmen"), "abnehmen"))
    }

    @Test fun `EN to DE - wrong translation rejected`() =
        assertFalse(Grader.check(EN_TO_DE, listOf("nachlassen", "abnehmen"), "aufhören"))

    @Test fun `EN to DE - case insensitive for nouns`() =
        assertTrue(Grader.check(EN_TO_DE, listOf("Scharfsinn"), "scharfsinn"))
}
