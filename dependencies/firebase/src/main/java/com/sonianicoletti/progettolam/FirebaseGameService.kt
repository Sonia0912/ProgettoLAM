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
            players = mutableListOf(currentUser)
        )
    }

    private suspend fun getCurrentUser() = authService.getUser() ?: throw NullPointerException("User must not be null")

    private fun createNewGameData(currentUser: User) = hashMapOf(
        HOST to currentUser.id,
        PLAYERS to listOf(currentUser.id)
    )

    override suspend fun updateGame(game: Game) {
        val gameRef = firestore.collection("games").document(game.id)
        gameRef.update(game.toMap()).await()
    }

    private fun Game.toMap() = mapOf(
        HOST to host,
        PLAYERS to players
    )

    private suspend fun addGameToFirestore(gameData: HashMap<String, Any>) = firestore.collection("games")
        .add(gameData)
        .await() // restituisce DocumentReference
        .get()
        .await() // restituisce DocumentSpanshot

    companion object {
        private const val HOST = "host"
        private const val PLAYERS = "players"
    }
}
