package com.sonianicoletti.progettolam

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.GameStatus
import com.sonianicoletti.entities.User
import com.sonianicoletti.entities.exceptions.GameNotFoundException
import com.sonianicoletti.usecases.servives.GameService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class FirebaseGameService @Inject constructor(private val authService: FirebaseAuthService) : GameService {

    private val firestore = Firebase.firestore

    override suspend fun createGame(): Game {
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

    private suspend fun getCurrentUser() = authService.getUser() ?: throw NullPointerException("User must not be null")

    private suspend fun generateID(): String {
        val gameID = (100000..999999).random().toString()
        val existingGame = firestore.collection("games").document(gameID).get().await()
        if (existingGame.exists()) {
            return generateID()
        }
        return gameID
    }

    private fun createNewGameData(currentUser: User) = hashMapOf(
        HOST to currentUser.id,
        STATUS to GameStatus.LOBBY,
        PLAYERS to listOf(currentUser)
    )


    private suspend fun addGameToFirestore(gameData: HashMap<String, Any>, gameID: String) = firestore.collection("games")
        .document(gameID)
        .set(gameData)
        .await() // restituisce DocumentReference

    override suspend fun updateGame(game: Game) {
        val gameRef = firestore.collection("games").document(game.id)
        gameRef.update(game.toMap()).await()
    }

    private fun Game.toMap() = mapOf(
        HOST to host,
        STATUS to status,
        PLAYERS to players
    )

    override suspend fun getGameByID(gameID: String): Game {
        val gameSnapshot = getGameRef(gameID)
        val players = getPlayersFromGameSnapshot(gameSnapshot)

        return Game(
            id = gameSnapshot.id,
            host = gameSnapshot.getString("host").orEmpty(),
            status = GameStatus.fromValue(gameSnapshot.getString("status")),
            players = players
        )
    }

    private suspend fun getGameRef(gameID: String): DocumentSnapshot {
        val gameRef = firestore.collection("games").document(gameID).get().await()
        if (!gameRef.exists()) throw GameNotFoundException()
        return gameRef
    }

    private fun getPlayersFromGameSnapshot(gameSnapshot: DocumentSnapshot): MutableList<User> {
        val playersMapList = gameSnapshot["players"] as? List<HashMap<String, Any>> ?: emptyList()
        return playersMapList.map { it.toUser() }.toMutableList()
    }

    private fun HashMap<String, Any>.toUser() = User(
        get("id") as String,
        get("email") as String,
        get("displayName") as String
    )

    override suspend fun observeGameByID(gameID: String) = firestore.collection("games").document(gameID).observe()

    private fun DocumentReference.observe() = callbackFlow {
        val callback = addSnapshotListener { value, error ->
            if (value != null) trySend(value.toGame()) else close(GameNotFoundException(error))
        }
        awaitClose { callback.remove() }
    }

    private fun DocumentSnapshot.toGame(): Game {
        val players = getPlayersFromGameSnapshot(this)

        return Game(
            id = id,
            host = getString("host").orEmpty(),
            status = GameStatus.fromValue(getString("status")),
            players = players
        )
    }

    companion object {
        private const val HOST = "host"
        private const val STATUS = "status"
        private const val PLAYERS = "players"
    }
}
