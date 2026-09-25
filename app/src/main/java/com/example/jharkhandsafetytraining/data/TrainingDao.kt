package com.example.jharkhandsafetytraining.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TrainingDao {
    @Insert suspend fun insertProgress(p: ModuleProgress)
    @Insert suspend fun insertAttempt(a: QuizAttempt)
    @Insert suspend fun insertCertificate(c: Certificate)

    @Query("SELECT * FROM module_progress WHERE userId = :userId")
    suspend fun getProgress(userId: Long): List<ModuleProgress>

    @Query("SELECT * FROM certificate WHERE userId = :userId")
    suspend fun getCertificates(userId: Long): List<Certificate>

    @Query("SELECT * FROM module_progress WHERE userId = :userId AND moduleId = :moduleId LIMIT 1")
    suspend fun getProgressFor(userId: Long, moduleId: String): ModuleProgress?

    @Update
    suspend fun updateProgress(p: ModuleProgress)

    suspend fun upsertProgress(p: ModuleProgress) {
        val existing = getProgressFor(p.userId, p.moduleId)
        if (existing == null) insertProgress(p) else updateProgress(p.copy(id = existing.id))
    }
}