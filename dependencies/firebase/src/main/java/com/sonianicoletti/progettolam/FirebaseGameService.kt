package com.sonianicoletti.progettolam

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.GameStatus
import com.sonianicoletti.entities.User
import com.sonianicoletti.entities.exceptions.GameNotFoundException
import com.sonianicoletti.usecases.servives.GameService
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class FirebaseGameService @Inject constructor(private val authService: FirebaseAuthService) : GameService {

    private val firestore = Firebase.firestore

    override suspend fun createGame() : Game {
        val currentUser = getCurrentUser()
        val gameID = generateID()
        val gameMap = createNewGameData(currentUser)
        addGameToFirestore(gameMap, gameID)

        return Game(
            id = gameID,
            host = currentUser.id,
            status = GameStatus.LOBBY,
            players = mutableListOf(currentUser)
        )
    }

    private suspend fun generateID() : String {
        val gameID =  (100000..999999).random().toString()
        val existingGame = firestore.collection("games").document(gameID).get().await()
        if(existingGame.exists()) {
            return generateID()
        }
        return gameID
    }

    private suspend fun getCurrentUser() = authService.getUser() ?: throw NullPointerException("User must not be null")

    private fun createNewGameData(currentUser: User) = hashMapOf(
        HOST to currentUser.id,
        STATUS to GameStatus.LOBBY,
        PLAYERS to listOf(currentUser.id)
    )

    override suspend fun updateGame(game: Game) {
        val gameRef = firestore.collection("games").document(game.id)
        gameRef.update(game.toMap()).await()
    }

    private fun Game.toMap() = mapOf(
        HOST to host,
        STATUS to status,
        PLAYERS to players
    )

    private suspend fun addGameToFirestore(gameData: HashMap<String, Any>, gameID: String) = firestore.collection("games")
        .document(gameID)
        .set(gameData)
        .await() // restituisce DocumentReference

    override suspend fun getGameByID(gameID: String) : Game {
        val gameRef = firestore.collection("games").document(gameID).get().await()
        if(!gameRef.exists()) throw GameNotFoundException()
        return Game(
            id = gameRef.id,
            host = gameRef.getString("host").orEmpty(),
            status = gameRef["status"] as? GameStatus ?: throw IllegalStateException(),
            players = gameRef["players"] as? MutableList<User> ?: mutableListOf<User>()
        )
    }

    companion object {
        private const val HOST = "host"
        private const val STATUS = "status"
        private const val PLAYERS = "players"
    }
}
