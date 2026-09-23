package com.example.jharkhandsafetytraining.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val pinHash: String,
    val role: String = "WORKER",
    val language: String = "en",
    val createdAt: Long = System.currentTimeMillis()
)