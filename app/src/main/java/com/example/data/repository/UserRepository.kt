package com.example.data.repository

import com.example.data.local.UserDao
import com.example.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {

    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email)
    }

    suspend fun registerUser(user: User): Result<User> = withContext(Dispatchers.IO) {
        try {
            val existing = userDao.getUserByEmail(user.email)
            if (existing != null) {
                Result.failure(Exception("Email already registered"))
            } else {
                userDao.insertUser(user)
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun authenticateUser(email: String, passwordHash: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getUserByEmail(email)
            if (user == null) {
                Result.failure(Exception("Invalid email or password"))
            } else if (user.passwordHash != passwordHash) {
                Result.failure(Exception("Invalid email or password"))
            } else {
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
