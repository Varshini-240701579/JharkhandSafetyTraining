package com.example.jharkhandsafetytraining.quiz

object QuizEngine {
    // answers = questionId -> index of the option the worker picked
    fun score(bank: QuizBank, answers: Map<String, Int>): QuizResult {
        val wrong = bank.questions
            .filter { answers[it.id] != it.correctIndex }
            .map { it.id }
        val total = bank.questions.size
        val score = total - wrong.size
        val percent = if (total == 0) 0 else score * 100 / total
        return QuizResult(score, total, percent >= bank.passPercent, wrong)
    }
}