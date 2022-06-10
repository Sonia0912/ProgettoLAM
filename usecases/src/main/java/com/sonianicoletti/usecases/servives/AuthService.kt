package com.sonianicoletti.usecases.servives

import com.sonianicoletti.entities.User

interface AuthService {

    suspend fun signIn(email: String, password: String): User

    suspend fun register(email: String, password: String, displayName: String): User

    suspend fun getUser(): User?

    suspend fun setNotificationToken(token: String)

    suspend fun signOut()

    suspend fun updateUser(updatedUser: User)
}