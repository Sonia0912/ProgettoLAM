package com.sonianicoletti.progettolam

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.User
import com.sonianicoletti.usecases.servives.GameService
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class FirebaseGameService @Inject constructor(private val authService: FirebaseAuthService) : GameService {

    private val firestore = Firebase.firestore

    override suspend fun createGame() : Game {
        val currentUser = getCurrentUser()
        val gameMap = createNewGameData(currentUser)
        val gameDocument = addGameToFirestore(gameMap)

        return Game(
            id = gameDocument.id,
            host = currentUser.id,
            players = listOf(currentUser)
        )
    }

    override suspend fun updateGamePlayers(gameID: String, players: List<User>) {
        val gameRef = firestore.collection("games").document(gameID)
        gameRef
            .update("players", players.map { it.id })
            .await()
    }

    private suspend fun getCurrentUser() = authService.getUser() ?: throw NullPointerException("User must not be null")

    private fun createNewGameData(currentUser: User) = hashMapOf(
        "host" to currentUser.id,
        "players" to listOf(currentUser.id)
    )

    private suspend fun addGameToFirestore(gameData: HashMap<String, Any>) = firestore.collection("games")
        .add(gameData)
        .await() // restituisce DocumentReference
        .get()
        .await() // restituisce DocumentSpanshot
}
