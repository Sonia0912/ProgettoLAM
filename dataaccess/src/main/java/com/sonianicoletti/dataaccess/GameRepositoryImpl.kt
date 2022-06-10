package com.sonianicoletti.dataaccess

import com.sonianicoletti.entities.*
import com.sonianicoletti.entities.exceptions.GameNotRunningException
import com.sonianicoletti.entities.exceptions.UserNotFoundException
import com.sonianicoletti.entities.exceptions.UserNotLoggedInException
import com.sonianicoletti.usecases.repositories.GameRepository
import com.sonianicoletti.usecases.servives.AuthService
import com.sonianicoletti.usecases.servives.GameService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val gameService: GameService,
    private val authService: AuthService,
) : GameRepository {

    private var game: Game? = null
    private var gameFlow: Flow<Game>? = null

    override suspend fun createGame() {
        gameService.createGame().also {
            game = it
            gameFlow = gameService.observeGameByID(it.id)
        }
    }

    override suspend fun loadGame(gameID: String) {
        gameFlow = gameService.observeGameByID(gameID)
    }

    override fun getOngoingGame() = game ?: throw GameNotRunningException()

    override suspend fun isUserInGame(userID: String, gameID: String): Boolean {
        val game = gameService.getGameByID(gameID)
        return game.players.any { it.id == userID }
    }

    override suspend fun isGameJoinable(gameID: String): Boolean {
        val game = gameService.getGameByID(gameID)
        // controlla che la partita non sia terminata o in gioco e che ci siano meno di 6 giocatori
        return game.players.size <= 5 || game.status == GameStatus.LOBBY
    }

    override suspend fun getOngoingGameUpdates() = gameFlow?.onEach { game = it }
        ?.catch { cancelGameAndRethrow(it) }
        ?: throw GameNotRunningException()

    private fun cancelGameAndRethrow(throwable: Throwable) {
        game = null;
        gameFlow = null;
        throw throwable
    }

    override suspend fun addPlayer(player: Player) {
        val game = getOngoingGame()
        game.players.add(player)
        gameService.updateGame(game)
    }

    override suspend fun addPlayer(gameID: String, player: Player) {
        val game = gameService.getGameByID(gameID)
        game.players.add(player)
        gameService.updateGame(game)
    }

    override suspend fun leaveGame() {
        try {
            val game = getOngoingGame()
            val user = authService.getUser() ?: throw UserNotFoundException()

            when {
                game.host == user.id -> gameService.deleteGame(game.id)
                game.status != GameStatus.LOBBY -> gameService.deleteGame(game.id)
                else -> game.removePlayer(user.id)
            }
        } catch (e: GameNotRunningException) {
            // do nothing
        } finally {
            gameFlow = null
            this.game = null
        }
    }

    private suspend fun Game.removePlayer(playerID: String) {
        players.removeAll { it.id == playerID }
        gameService.updateGame(this)
    }

    override suspend fun updateGameStatus(gameStatus: GameStatus) {
        val game = getOngoingGame()
        game.status = gameStatus
        gameService.updateGame(game)
    }

    override suspend fun updateCharacter(userID: String, character: Character) {
        val game = getOngoingGame()
        game.players.find { it.id == userID }?.character = character
        gameService.updateGame(game)
    }

    override suspend fun isUserHost(game: Game) : Boolean {
        val user = authService.getUser() ?: throw UserNotLoggedInException()
        return user.id == game.host
    }

    override suspend fun distributeCards() {
        val characterCards = mutableListOf(
            Card("Mrs Peacock", "character"),
            Card("Colonel Mustard", "character"),
            Card("Reverend Green", "character"),
            Card("Professor Plum", "character"),
            Card("Mrs White", "character"),
            Card("Miss Scarlett", "character")
        )
        val weaponCards = mutableListOf(
            Card("Candlestick", "weapon"),
            Card("Revolver", "weapon"),
            Card("Rope", "weapon"),
            Card("Dagger", "weapon"),
            Card("Lead Pipe", "weapon"),
            Card("Wrench", "weapon")
        )
        val roomCards = mutableListOf(
            Card("Kitchen", "room"),
            Card("Library", "room"),
            Card("Lounge", "room"),
            Card("Study", "room"),
            Card("Ballroom", "room"),
            Card("Billiard Room", "room"),
            Card("Conservatory", "room"),
            Card("Dining Room", "room"),
            Card("Hall", "room")
        )

        // carte soluzione
        val characterSolution = characterCards.random()
        characterCards.remove(characterSolution)
        val weaponSolution = weaponCards.random()
        weaponCards.remove(weaponSolution)
        val roomSolution = roomCards.random()
        roomCards.remove(roomSolution)
        val solutionCards = listOf(characterSolution, weaponSolution, roomSolution)
        println("Solution Cards: $solutionCards")
        saveSolutionCards(solutionCards)

        // carte distribuite tra i giocatori
//        val allCards = (characterCards + weaponCards + roomCards).toMutableList()
//        val game = getOngoingGame()
//        val numberOfCardsEach = (18 / game.players.size)
//        for(i in 0 until game.players.size) {
//            var yourCards = allCards.asSequence().shuffled().take(numberOfCardsEach).toList()
//            yourCards.forEach { allCards.remove(it) }
//            assignCardsToPlayer(i, yourCards)
//        }
//
//        // carte rimanenti
//        if (18 % game.players.size != 0) {
//            saveLeftoverCards(allCards)
//        }
    }

    private suspend fun saveSolutionCards(solutionCards : List<Card>) {
        val game = getOngoingGame()
        game.solutionCards = solutionCards.toMutableList()
        gameService.updateGame(game)
    }

    private suspend fun assignCardsToPlayer(playerIndex : Int, yourCards : List<Card>) {
        val game = getOngoingGame()
        game.players[playerIndex].cards = yourCards
        gameService.updateGame(game)
    }

    private suspend fun saveLeftoverCards(leftoverCards : List<Card>) {
        val game = getOngoingGame()
        game.leftoverCards = leftoverCards.toMutableList()
        gameService.updateGame(game)
    }
}
