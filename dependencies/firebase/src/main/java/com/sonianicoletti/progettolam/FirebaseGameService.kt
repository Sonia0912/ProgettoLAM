package com.sonianicoletti.progettolam

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sonianicoletti.entities.*
import com.sonianicoletti.entities.exceptions.GameNotFoundException
import com.sonianicoletti.entities.exceptions.GameNotRunningException
import com.sonianicoletti.usecases.servives.GameService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class FirebaseGameService @Inject constructor(private val authService: FirebaseAuthService) : GameService {

    private val firestore = Firebase.firestore

    override suspend fun createGame(): Game {
        val currentUser = getCurrentUser()
        val gameID = generateID()
        val currentPlayer = Player.fromUser(currentUser)
        val gameMap = createNewGameData(currentPlayer)
        addGameToFirestore(gameMap, gameID)

        return Game(
            id = gameID,
            host = currentUser.id,
            status = GameStatus.LOBBY,
            players = mutableListOf(currentPlayer),
            leftoverCards = mutableListOf(),
            solutionCards = mutableListOf(),
            turnPlayerId = "",
            accusation = null,
            winner = "",
            losers = mutableListOf()
        )
    }

    private suspend fun getCurrentUser() = authService.getUser() ?: throw NullPointerException("User must not be null")

    private suspend fun generateID(): String {
        val gameID = (100000..999999).random().toString()
        val existingGame = firestore.collection(GAMES_COLLECTION).document(gameID).get().await()
        if (existingGame.exists()) {
            return generateID()
        }
        return gameID
    }

    private fun createNewGameData(currentPlayer: Player) = hashMapOf(
        HOST to currentPlayer.id,
        STATUS to GameStatus.LOBBY,
        PLAYERS to listOf(currentPlayer)
    )

    private suspend fun addGameToFirestore(gameData: HashMap<String, Any>, gameID: String) = firestore.collection(GAMES_COLLECTION)
        .document(gameID)
        .set(gameData)
        .await() // restituisce DocumentReference

    override suspend fun updateGame(game: Game) {
        val gameRef = firestore.collection(GAMES_COLLECTION).document(game.id)
        gameRef.update(game.toMap()).await()
    }

    private fun Game.toMap() = mapOf(
        HOST to host,
        STATUS to status,
        PLAYERS to players,
        SOLUTION_CARDS to solutionCards,
        LEFTOVER_CARDS to leftoverCards,
        TURN_PLAYER to turnPlayerId,
        ACCUSATION to accusation,
        WINNER to winner,
        LOSERS to losers
    )

    override suspend fun getGameByID(gameID: String): Game {
        val gameSnapshot = getGameRef(gameID)
        return gameSnapshot.toGame()
    }

    private suspend fun getGameRef(gameID: String): DocumentSnapshot {
        val gameRef = firestore.collection(GAMES_COLLECTION).document(gameID).get().await()
        if (!gameRef.exists()) throw GameNotFoundException()
        return gameRef
    }

    private fun getPlayersFromGameSnapshot(gameSnapshot: DocumentSnapshot): MutableList<Player> {
        val playersMapList = gameSnapshot["players"] as? List<HashMap<String, Any>> ?: emptyList()
        return playersMapList.map { it.toPlayer() }.toMutableList()
    }

    private fun getLosersFromGameSnapshot(gameSnapshot: DocumentSnapshot): MutableList<String> {
        val losersMapList = gameSnapshot["losers"] as? List<String> ?: emptyList()
        return losersMapList.toMutableList()
    }

    private fun HashMap<String, Any>.toPlayer() = Player(
        get("id") as String,
        get("displayName") as String,
        Character.valueOf(get("character") as String),
        (get("cards") as List<HashMap<String, Any>>).map { it.toCard() }
    )

    private fun HashMap<String, Any>.toCard() : Card {
        return Card(
            get("name") as String,
            get("type") as String
        )
    }

    override suspend fun observeGameByID(gameID: String) = firestore.collection(GAMES_COLLECTION)
        .document(gameID)
        .observe()
        .map { it.takeUnless { it.status == GameStatus.CANCELLED } ?: throw GameNotRunningException() }

    private fun DocumentReference.observe() = callbackFlow {
        val callback = addSnapshotListener { value, error ->
            if (value != null) trySend(value.toGame()) else close(GameNotFoundException(error))
        }
        awaitClose { callback.remove() }
    }

    // converte lo Snapshot in un Game
    private fun DocumentSnapshot.toGame(): Game {
        val players = getPlayersFromGameSnapshot(this)
        val leftoverCards = getLeftoverCardsFromGameSnapshot(this)
        val solutionCards = getSolutionCardsFromGameSnapshot(this)
        val accusation = getAccusationFromGameSnapshot(this)
        val losers = getLosersFromGameSnapshot(this)

        return Game(
            id = id,
            host = getString(HOST).orEmpty(),
            status = GameStatus.fromValue(getString(STATUS)),
            players = players,
            leftoverCards = leftoverCards,
            solutionCards = solutionCards,
            turnPlayerId = getString(TURN_PLAYER).orEmpty(),
            accusation = accusation,
            winner = getString(WINNER).orEmpty(),
            losers = losers
        )
    }

    private fun getLeftoverCardsFromGameSnapshot(gameSnapshot: DocumentSnapshot): MutableList<Card> {
        val cardsMapList = gameSnapshot[LEFTOVER_CARDS] as? List<HashMap<String, Any>> ?: emptyList()
        return cardsMapList.map { it.toCard() }.toMutableList()
    }

    private fun getSolutionCardsFromGameSnapshot(gameSnapshot: DocumentSnapshot): MutableList<Card> {
        val cardsMapList = gameSnapshot[SOLUTION_CARDS] as? List<HashMap<String, Any>> ?: emptyList()
        return cardsMapList.map { it.toCard() }.toMutableList()
    }

    private fun getAccusationFromGameSnapshot(gameSnapshot: DocumentSnapshot): Accusation? {
        val accusationMap = gameSnapshot[ACCUSATION] as? HashMap<String, Any> ?: return null
        val cardsMapList = accusationMap[ACCUSATION_CARDS] as? List<HashMap<String, Any>> ?: emptyList()
        val accusationCards = cardsMapList.map { it.toCard() }.toMutableList()
        val responder = accusationMap[ACCUSATION_RESPONDER].toString()
        val displayCard = (accusationMap[ACCUSATION_DISPLAY_CARD] as? HashMap<String, Any>)?.toCard()
        return Accusation(accusationCards, responder, displayCard)
    }

    override suspend fun deleteGame(gameID: String) {
        firestore.collection(GAMES_COLLECTION).document(gameID).delete().await()
    }

    override suspend fun checkVictory(gameID: String, characterCard: Card, weaponCard: Card, roomCard: Card): Boolean {
        val game = getGameByID(gameID)
        return (characterCard in game.solutionCards && weaponCard in game.solutionCards && roomCard in game.solutionCards)
    }

    companion object {
        private const val GAMES_COLLECTION = "games"
        private const val HOST = "host"
        private const val STATUS = "status"
        private const val PLAYERS = "players"
        private const val SOLUTION_CARDS = "solution_cards"
        private const val LEFTOVER_CARDS = "leftover_cards"
        private const val TURN_PLAYER = "turn_player"
        private const val ACCUSATION = "accusation"
        private const val ACCUSATION_CARDS = "cards"
        private const val ACCUSATION_RESPONDER = "responder"
        private const val ACCUSATION_DISPLAY_CARD = "displayCard"
        private const val WINNER = "winner"
        private const val LOSERS = "losers"
    }
}
