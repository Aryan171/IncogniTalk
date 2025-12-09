package com.incognitalk.app.data.repository

import com.incognitalk.app.data.dao.UserDao
import com.incognitalk.app.data.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    fun getUser(): Flow<User?> = userDao.getUser()

    suspend fun saveUser(user: User) {
        userDao.insertUser(user)
    }
}
