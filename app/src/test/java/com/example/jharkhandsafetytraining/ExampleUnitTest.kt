package com.example.jharkhandsafetytraining

import com.example.jharkhandsafetytraining.common.CertificateService
import com.example.jharkhandsafetytraining.quiz.Question
import com.example.jharkhandsafetytraining.quiz.QuizBank
import com.example.jharkhandsafetytraining.quiz.QuizEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun quizEngine_scoresPassAndFailCorrectly() {
        val bank = QuizBank(
            moduleId = "FIRE",
            version = 1,
            passPercent = 50,
            questions = listOf(
                Question(
                    id = "fire_1",
                    text = mapOf("en" to "Q1"),
                    options = listOf(mapOf("en" to "A"), mapOf("en" to "B")),
                    correctIndex = 1
                ),
                Question(
                    id = "fire_2",
                    text = mapOf("en" to "Q2"),
                    options = listOf(mapOf("en" to "A"), mapOf("en" to "B")),
                    correctIndex = 0
                )
            )
        )

        val passResult = QuizEngine.score(bank, mapOf("fire_1" to 1, "fire_2" to 1))
        assertTrue(passResult.passed)
        assertEquals(1, passResult.score)
        assertEquals(listOf("fire_2"), passResult.wrongQuestionIds)

        val failResult = QuizEngine.score(bank, mapOf("fire_1" to 0, "fire_2" to 1))
        assertFalse(failResult.passed)
        assertEquals(0, failResult.score)
        assertEquals(listOf("fire_1", "fire_2"), failResult.wrongQuestionIds)
    }

    @Test
    fun certificateService_hmacAndModuleCompletionCheck() {
        val sig1 = CertificateService.computeHmacSha256("uid=1|mod=ALL")
        val sig2 = CertificateService.computeHmacSha256("uid=1|mod=ALL")
        val tamperedSig = CertificateService.computeHmacSha256("uid=2|mod=ALL")

        assertEquals(sig1, sig2)
        assertNotEquals(sig1, tamperedSig)

        assertFalse(CertificateService.hasPassedAllRequiredModules(setOf("FIRE", "GAS")))
        assertTrue(CertificateService.hasPassedAllRequiredModules(CertificateService.REQUIRED_MODULES.toSet()))
    }
}