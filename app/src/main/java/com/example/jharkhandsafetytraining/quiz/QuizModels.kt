package com.example.jharkhandsafetytraining.quiz

data class Question(
    val id: String,
    val text: Map<String, String>,
    val options: List<Map<String, String>>,
    val correctIndex: Int
)

data class QuizBank(
    val moduleId: String,
    val version: Int,
    val passPercent: Int,
    val questions: List<Question>
)

data class QuizResult(
    val score: Int,
    val total: Int,
    val passed: Boolean,
    val wrongQuestionIds: List<String>
)