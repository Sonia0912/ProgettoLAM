package com.sonianicoletti.dataaccess

import com.sonianicoletti.entities.*
import com.sonianicoletti.entities.exceptions.GameNotRunningException
import com.sonianicoletti.entities.exceptions.UserNotFoundException
import com.sonianicoletti.entities.exceptions.UserNotLoggedInException
import com.sonianicoletti.usecases.repositories.GameRepository
import com.sonianicoletti.usecases.servives.AuthService
import com.sonianicoletti.usecases.servives.GameService
import com.sonianicoletti.usecases.servives.UserService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val gameService: GameService,
    private val authService: AuthService,
    private val userService: UserService,
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
                !(game.status == GameStatus.LOBBY || game.status == GameStatus.FINISHED) -> gameService.deleteGame(game.id)
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

    override suspend fun isHost() : Boolean {
        val game = getOngoingGame()
        val user = authService.getUser() ?: throw UserNotLoggedInException()
        return user.id == game.host
    }

    override suspend fun isCurrentTurn(): Boolean {
        val game = getOngoingGame()
        val user = authService.getUser() ?: throw UserNotLoggedInException()
        return game.turnPlayerId == user.id
    }

    override fun getTurnPlayer(): Player {
        val game = getOngoingGame()
        return game.players.first { it.id == game.turnPlayerId }
    }

    override suspend fun distributeCards() {
        val game = getOngoingGame()
        val characterCards = Cards.characters.toMutableList()
        val weaponCards = Cards.weapons.toMutableList()
        val roomCards = Cards.rooms.toMutableList()

        // carte soluzione
        val characterSolution = characterCards.random()
        characterCards.remove(characterSolution)
        val weaponSolution = weaponCards.random()
        weaponCards.remove(weaponSolution)
        val roomSolution = roomCards.random()
        roomCards.remove(roomSolution)
        val solutionCards = mutableListOf(characterSolution, weaponSolution, roomSolution)
        game.solutionCards = solutionCards

        // carte distribuite tra i giocatori
        val allCards = (characterCards + weaponCards + roomCards).toMutableList()
        val numberOfCardsEach = (18 / game.players.size)

        game.players.forEach { player ->
            val yourCards = allCards.asSequence().shuffled().take(numberOfCardsEach).toList()
            player.cards = yourCards
            allCards.removeAll(yourCards)
        }

        // carte rimanenti
        if (18 % game.players.size != 0) {
            game.leftoverCards = allCards
        }
        gameService.updateGame(game)
    }

    override suspend fun nextTurn() {
        val game = getOngoingGame()
        val turnPlayer = game.players.firstOrNull { it.id == game.turnPlayerId }
        game.accusation = null
        val remainingPlayers = game.players.filterNot { player -> player.id in game.losers }
        game.turnPlayerId = when (turnPlayer) {
            null -> remainingPlayers.first()
            remainingPlayers.last() -> remainingPlayers.first()
            else -> remainingPlayers[remainingPlayers.indexOf(turnPlayer) + 1]
        }.id

        gameService.updateGame(game)
    }

    override suspend fun makeAccusation(characterCard: Card, weaponCard: Card, roomCard: Card) {
        val game = getOngoingGame()
        val currentPlayerIndex = game.players.indexOf(game.players.first { it.id == authService.getUser()?.id })
        val nextPlayerId = (if (currentPlayerIndex == game.players.lastIndex) game.players[0] else game.players[currentPlayerIndex + 1]).id
        game.accusation = Accusation(cards = mutableListOf(characterCard, weaponCard, roomCard), responder = nextPlayerId)
        gameService.updateGame(game)
    }

    override suspend fun makeFinalAccusation(characterCard: Card, weaponCard: Card, roomCard: Card) {
        val game = getOngoingGame()
        val victory = gameService.checkVictory(game.id, characterCard, weaponCard, roomCard)
        if(victory) {
            game.winner = authService.getUser()?.id.toString()
            userService.addScore(game.winner, 100)
            game.status = GameStatus.FINISHED
            gameService.updateGame(game)
        } else {
            authService.getUser()?.id?.let { game.losers.add(it) }
            // controllo se e' rimasto un solo giocatore
            if(game.losers.size == (game.players.size - 1)) {
                game.winner = findRemainingPlayer(game.players.map { it.id }, game.losers)
                userService.addScore(game.winner, 100)
                game.status = GameStatus.FINISHED
            }
            gameService.updateGame(game)
        }
    }

    private fun findRemainingPlayer(players: List<String>, losers: List<String>) : String {
        players.forEach { playerID ->
            if(playerID !in losers) return playerID
        }
        return ""
    }

    override suspend fun nextAccusationResponder() {
        val game = getOngoingGame()
        val currentResponderIndex = game.players.indexOf(game.players.first { it.id == game.accusation?.responder })
        val nextPlayerId = (if (currentResponderIndex == game.players.lastIndex) game.players[0] else game.players[currentResponderIndex + 1]).id

        if (nextPlayerId == game.turnPlayerId) {
            game.accusation = null
            nextTurn()
        } else {
            game.accusation?.responder = nextPlayerId
        }

        gameService.updateGame(game)
    }

    override suspend fun isWinner() : Boolean {
        val game = getOngoingGame()
        val user = authService.getUser() ?: throw UserNotLoggedInException()
        return game.winner == user.id
    }

    override suspend fun setAccusationDisplayCard(card: Card) {
        val game = getOngoingGame()
        game.accusation?.displayCard = card
        gameService.updateGame(game)
    }

    override suspend fun isAccusationResponder(): Boolean {
        val game = getOngoingGame()
        val user = authService.getUser() ?: throw UserNotLoggedInException()
        return game.accusation?.responder == user.id
    }
}
