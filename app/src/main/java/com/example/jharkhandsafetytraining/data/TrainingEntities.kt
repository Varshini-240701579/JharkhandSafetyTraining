package com.example.jharkhandsafetytraining.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "module_progress")
data class ModuleProgress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val moduleId: String,
    val arCompleted: Boolean = false,
    val quizPassed: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(tableName = "quiz_attempt")
data class QuizAttempt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val moduleId: String,
    val score: Int,
    val total: Int,
    val passed: Boolean,
    val attemptedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(tableName = "certificate")
data class Certificate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val moduleId: String,
    val issuedAt: Long = System.currentTimeMillis(),
    val signedPayload: String = "",
    val synced: Boolean = false
)