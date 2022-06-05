package com.sonianicoletti.dataaccess

import com.sonianicoletti.entities.Character
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.GameStatus
import com.sonianicoletti.entities.Player
import com.sonianicoletti.entities.exceptions.GameNotRunningException
import com.sonianicoletti.usecases.repositories.GameRepository
import com.sonianicoletti.usecases.servives.GameService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(private val gameService: GameService) : GameRepository {

    private var ongoingGame: Game? = null
    private var gameFlow: Flow<Game>? = null

    override suspend fun createGame() {
        gameService.createGame().also {
            ongoingGame = it
            gameFlow = gameService.observeGameByID(it.id)
        }
    }

    override suspend fun loadGame(gameID: String) {
        startObservingGame(gameID)
    }

    override fun getOngoingGame() = ongoingGame ?: throw GameNotRunningException()

    override suspend fun isGameJoinable(gameID: String): Boolean {
        val game = gameService.getGameByID(gameID)
        // controlla che la partita non sia terminata o in gioco e che ci siano meno di 6 giocatori
        return game.players.size > 5 || game.status != GameStatus.LOBBY
    }

    override suspend fun startObservingGame(gameID: String) {
        gameFlow = gameService.observeGameByID(gameID)
        gameFlow?.collect { ongoingGame = it }
    }

    override suspend fun getOngoingGameUpdates() = gameFlow ?: throw GameNotRunningException()

    override suspend fun addPlayer(player: Player) {
        val game = getGame()
        game.players.add(player)
        gameService.updateGame(game)
    }

    override suspend fun addPlayer(gameID: String, player: Player) {
        val game = gameService.getGameByID(gameID)
        game.players.add(player)
        gameService.updateGame(game)
    }

    override suspend fun updateGameStatus(gameStatus: GameStatus) {
        val game = getGame()
        game.status = gameStatus
        gameService.updateGame(game)
    }

    private fun getGame() = ongoingGame ?: throw GameNotRunningException()

    override suspend fun updateCharacter(userID: String, character: Character) {
        val game = getGame()
        game.players.find { it.id == userID }?.character = character
        gameService.updateGame(game)
    }
}
