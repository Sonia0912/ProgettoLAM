package com.sonianicoletti.progettolam

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sonianicoletti.entities.User
import com.sonianicoletti.usecases.servives.AuthService
import com.sonianicoletti.usecases.servives.UserService
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// @Inject dice a Hilt come fornire un'istanza della classe
class FirebaseAuthService @Inject constructor(private val userService: UserService) : AuthService {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore

    // suspend permette di sospendere l'esecuzione della funzione durante operazioni lunghe
    // e' come avere una callback, permette al resto del codice di continuare
    override suspend fun signIn(email: String, password: String): User {
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw NullPointerException("user is null during login")
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email.orEmpty(),
            // nel caso non abbia un nome viene mostrata l'email
            displayName = firebaseUser.displayName?:firebaseUser.email.orEmpty()
        )
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun register(email: String, password: String, displayName: String): User {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw NullPointerException("user is null during register")
        createUser(email, displayName)
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email.orEmpty(),
            // nel caso non abbia un nome viene mostrata l'email
            displayName = firebaseUser.displayName?:firebaseUser.email.orEmpty()
        )
    }

    override suspend fun getUser() = firebaseAuth.currentUser?.let{
        it.email?.let { email ->
            val user = userService.getUserByEmail(email)
            // orEmpty usa una stringa vuota se e' null
            User(it.uid, email, user.displayName)
        }
    }

    private suspend fun createUser(email: String, username: String) {
        // Create a new user
        val user = hashMapOf(
            "displayName" to username,
            "email" to email
        )
        // Add a new document with a generated ID
        firestore.collection("users")
            .add(user)
            .await()
    }


}