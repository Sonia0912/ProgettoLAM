package com.sonianicoletti.progettolam

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw NullPointerException("user is null during login")
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email.orEmpty(),
            // nel caso non abbia un nome viene mostrata l'email
            displayName = firebaseUser.displayName ?: firebaseUser.email.orEmpty()
        )
    }

    override suspend fun register(email: String, password: String, displayName: String): User {
        val createUserResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = createUserResult.user ?: throw NullPointerException("user is null during register")
        addUserToFirestore(firebaseUser, displayName)
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email.orEmpty(),
            // nel caso non abbia un nome viene mostrata l'email
            displayName = displayName
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

    override suspend fun getUser() = firebaseAuth.currentUser?.let {
        it.email?.let { email ->
            val user = userService.getUserByEmail(email)
            // orEmpty usa una stringa vuota se e' null
            User(it.uid, email, user.displayName)
        }
    }

    override suspend fun setNotificationToken(token: String) {
        val user = getUser()

        if (user != null) {
            firestore.collection("users")
                .document(user.id)
                .update("messaging_token", token)
                .await()
        }
    }

    override fun signOut() = firebaseAuth.signOut()
}
