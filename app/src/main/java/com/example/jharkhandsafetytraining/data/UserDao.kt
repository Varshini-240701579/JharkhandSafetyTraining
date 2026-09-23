package com.example.jharkhandsafetytraining.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): User?

    @Query("UPDATE users SET language = :lang WHERE id = :userId")
    suspend fun updateLanguage(userId: Long, lang: String)
}