package com.example.jharkhandsafetytraining.data

import com.example.jharkhandsafetytraining.common.PinHasher

class AuthRepository(private val userDao: UserDao) {

    suspend fun login(phoneOrId: String, pin: String): AuthResult {
        return try {
            val user = userDao.findByPhone(phoneOrId)
            if (user == null) {
                AuthResult.Error("Worker not found. Please register.")
            } else if (PinHasher.verify(pin, user.pinHash)) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Invalid PIN.")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Login failed")
        }
    }

    suspend fun register(
        name: String,
        phone: String,
        pin: String,
        language: String = "hi",
        role: String = "WORKER"
    ): AuthResult {
        return try {
            val existing = userDao.findByPhone(phone)
            if (existing != null) {
                return AuthResult.Error("A worker with this phone/ID already exists")
            }

            val newUser = User(
                id = 0L,
                name = name,
                phone = phone,
                pinHash = PinHasher.hash(pin),
                role = role,
                language = language,
                createdAt = System.currentTimeMillis()
            )

            val generatedId = userDao.insert(newUser)
            val createdUser = newUser.copy(id = generatedId)

            AuthResult.Success(createdUser)
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Registration failed")
        }
    }
}