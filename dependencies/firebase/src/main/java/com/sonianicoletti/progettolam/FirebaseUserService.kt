package com.sonianicoletti.progettolam

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
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
        return userDocument.toUser()
    }

    private fun DocumentSnapshot.toUser() = User(
        id = id,
        email = getString("email").orEmpty(),
        displayName = getString("displayName").orEmpty(),
        messagingToken = getString("messaging_token"),
        score = getLong("score")?.toInt() ?: 0
    )

    override suspend fun getUsers(userIds: List<String>): List<User> {
        val usersRef = firestore.collection("users")
        val query = usersRef.whereIn(FieldPath.documentId(), userIds)
        val userDocuments = query.get().await().documents
        return userDocuments.map { it.toUser() }
    }

    override suspend fun addScore(userID: String, score: Int) {
        val userDocument = firestore.collection("users").document(userID)
        userDocument.update("score", FieldValue.increment(score.toLong()))
    }
}
