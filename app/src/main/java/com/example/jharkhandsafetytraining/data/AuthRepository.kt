package com.example.jharkhandsafetytraining.data

import com.example.jharkhandsafetytraining.common.PinHasher

class AuthRepository(private val userDao: UserDao) {

    suspend fun login(phone: String, pin: String): AuthResult {
        val user = userDao.findByPhone(phone)
            ?: return AuthResult.Failure("No account found for this number")

        return if (PinHasher.verify(pin, user.pinHash)) {
            AuthResult.Success(user)
        } else {
            AuthResult.Failure("Incorrect PIN")
        }
    }

    suspend fun register(name: String, phone: String, pin: String, language: String): AuthResult {
        val existing = userDao.findByPhone(phone)
        if (existing != null) {
            return AuthResult.Failure("This phone number is already registered")
        }
        val newUser = User(
            name = name,
            phone = phone,
            pinHash = PinHasher.hash(pin),
            language = language
        )
        val newId = userDao.insert(newUser)
        return AuthResult.Success(newUser.copy(id = newId))
    }
}