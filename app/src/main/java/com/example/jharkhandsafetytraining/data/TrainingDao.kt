package com.example.jharkhandsafetytraining.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TrainingDao {
    @Insert suspend fun insertProgress(p: ModuleProgress)
    @Insert suspend fun insertAttempt(a: QuizAttempt)
    @Insert suspend fun insertCertificate(c: Certificate)

    @Query("SELECT * FROM module_progress WHERE userId = :userId")
    suspend fun getProgress(userId: Long): List<ModuleProgress>

    @Query("SELECT * FROM certificate WHERE userId = :userId")
    suspend fun getCertificates(userId: Long): List<Certificate>
}