package com.example.jharkhandsafetytraining.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TrainingDao {
    @Insert
    suspend fun insertProgress(p: ModuleProgress)

    @Update
    suspend fun updateProgress(p: ModuleProgress)

    @Insert
    suspend fun insertAttempt(a: QuizAttempt)

    @Insert
    suspend fun insertCertificate(c: Certificate)

    @Query("SELECT * FROM module_progress WHERE userId = :userId")
    suspend fun getProgress(userId: Long): List<ModuleProgress>

    @Query("SELECT * FROM module_progress WHERE userId = :userId AND moduleId = :moduleId LIMIT 1")
    suspend fun getModuleProgress(userId: Long, moduleId: String): ModuleProgress?

    @Query("SELECT * FROM module_progress WHERE userId = :userId AND moduleId = :moduleId LIMIT 1")
    suspend fun getProgressFor(userId: Long, moduleId: String): ModuleProgress?

    @Query("SELECT * FROM quiz_attempt WHERE userId = :userId AND moduleId = :moduleId ORDER BY attemptedAt DESC")
    suspend fun getAttemptsForModule(userId: Long, moduleId: String): List<QuizAttempt>

    @Query("SELECT * FROM certificate WHERE userId = :userId ORDER BY issuedAt DESC")
    suspend fun getCertificates(userId: Long): List<Certificate>

    @Query("SELECT * FROM certificate WHERE userId = :userId AND moduleId = :moduleId ORDER BY issuedAt DESC LIMIT 1")
    suspend fun getCertificateForModule(userId: Long, moduleId: String): Certificate?

    suspend fun upsertProgress(p: ModuleProgress) {
        val existing = getProgressFor(p.userId, p.moduleId)
        if (existing == null) {
            insertProgress(p)
        } else {
            updateProgress(p.copy(id = existing.id))
        }
    }

    suspend fun markQuizPassed(userId: Long, moduleId: String) {
        val existing = getModuleProgress(userId, moduleId)
        val now = System.currentTimeMillis()
        if (existing != null) {
            updateProgress(
                existing.copy(
                    quizPassed = true,
                    updatedAt = now,
                    synced = false
                )
            )
        } else {
            insertProgress(
                ModuleProgress(
                    userId = userId,
                    moduleId = moduleId,
                    arCompleted = false,
                    quizPassed = true,
                    updatedAt = now,
                    synced = false
                )
            )
        }
    }

    suspend fun markArCompleted(userId: Long, moduleId: String) {
        val existing = getModuleProgress(userId, moduleId)
        val now = System.currentTimeMillis()
        if (existing != null) {
            updateProgress(
                existing.copy(
                    arCompleted = true,
                    updatedAt = now,
                    synced = false
                )
            )
        } else {
            insertProgress(
                ModuleProgress(
                    userId = userId,
                    moduleId = moduleId,
                    arCompleted = true,
                    quizPassed = false,
                    updatedAt = now,
                    synced = false
                )
            )
        }
    }
}