package com.sonianicoletti.progettolam

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sonianicoletti.entities.User
import com.sonianicoletti.entities.exceptions.UserNotFoundException
import com.sonianicoletti.usecases.servives.UserService
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserService @Inject constructor() : UserService {

    private val firestore = Firebase.firestore

    override suspend fun getUserByEmail(email: String): User {
        val usersRef = firestore.collection("users")
        val query = usersRef.whereEqualTo("email", email)
        val userDocument = query.get().await().documents.firstOrNull() ?: throw UserNotFoundException()
        return User(
            id = userDocument.id,
            email = email,
            displayName = userDocument.getString("displayName").orEmpty(),
            messagingToken = userDocument.getString("messaging_token"),
            score = userDocument.getLong("score")?.toInt() ?: 0
        )
    }
}
