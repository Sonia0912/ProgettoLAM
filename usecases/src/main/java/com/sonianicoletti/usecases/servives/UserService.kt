package com.sonianicoletti.usecases.servives

import com.sonianicoletti.entities.Score
import com.sonianicoletti.entities.User

interface UserService {

    suspend fun getUserByEmail(email: String) : User

    suspend fun getUsers(userIds: List<String>): List<User>

    suspend fun addScore(userID: String, score: Int)
}