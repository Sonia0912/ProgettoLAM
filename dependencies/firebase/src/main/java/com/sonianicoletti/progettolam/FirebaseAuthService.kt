package com.sonianicoletti.progettolam

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sonianicoletti.entities.User
import com.sonianicoletti.usecases.servives.AuthService
import com.sonianicoletti.usecases.servives.UserService
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// @Inject dice a Hilt come fornire un'istanza della classe
class FirebaseAuthService @Inject constructor(
    private val userService: UserService
) : AuthService {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore

    // suspend permette di sospendere l'esecuzione della funzione durante operazioni lunghe
    // e' come avere una callback, permette al resto del codice di continuare
    override suspend fun signIn(email: String, password: String): User {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return userService.getUserByEmail(email)
    }

    override suspend fun register(email: String, password: String, displayName: String): User {
        val createUserResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = createUserResult.user ?: throw NullPointerException("user is null during register")
        addUserToFirestore(firebaseUser, displayName)
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email.orEmpty(),
            // nel caso non abbia un nome viene mostrata l'email
            displayName = displayName,
            messagingToken = null,
        )
    }

    private suspend fun addUserToFirestore(firebaseUser: FirebaseUser, username: String) {
        // Create a new user
        val user = hashMapOf(
            "displayName" to username,
            "email" to firebaseUser.email
        )
        // Add a new document with a generated ID
        firestore.collection("users")
            .document(firebaseUser.uid)
            .set(user)
            .await()
    }

    override suspend fun getUser() = firebaseAuth.currentUser?.email?.let {
        userService.getUserByEmail(it)
    }

    override suspend fun setNotificationToken(token: String) {
        val user = firebaseAuth.currentUser

        if (user != null) {
            firestore.collection("users")
                .document(user.uid)
                .update("messaging_token", token)
                .await()
        }
    }

    override suspend fun signOut() {
        val user = firebaseAuth.currentUser

        if (user != null) {
            firestore.collection("users")
                .document(user.uid)
                .update("messaging_token", FieldValue.delete())
                .await()
            firebaseAuth.signOut()
        }
    }

    override suspend fun updateUser(updatedUser: User) {
        val userRef = firestore.collection(FirebaseAuthService.USERS_COLLECTION).document(updatedUser.id)
        userRef.update(updatedUser.toMap()).await()
    }

    private fun User.toMap() = mapOf(
        FirebaseAuthService.DISPLAY_NAME to displayName,
        FirebaseAuthService.EMAIL to email,
        FirebaseAuthService.MESSAGING_TOKEN to messagingToken,
    )

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val DISPLAY_NAME = "displayName"
        private const val EMAIL = "email"
        private const val MESSAGING_TOKEN = "messaging_token"
    }

}
