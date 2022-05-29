package com.sonianicoletti.usecases.servives

import com.sonianicoletti.entities.User

interface UserService {

    suspend fun getUserByEmail(email: String) : User

}