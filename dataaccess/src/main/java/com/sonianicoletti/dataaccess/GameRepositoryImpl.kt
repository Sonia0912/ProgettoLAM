package com.sonianicoletti.dataaccess

import com.sonianicoletti.entities.Character
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.GameStatus
import com.sonianicoletti.entities.Player
import com.sonianicoletti.entities.exceptions.GameNotRunningException
import com.sonianicoletti.entities.exceptions.UserNotFoundException
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
}
