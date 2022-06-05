package com.sonianicoletti.dataaccess

import com.sonianicoletti.entities.Character
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.GameStatus
import com.sonianicoletti.entities.Player
import com.sonianicoletti.entities.exceptions.GameNotRunningException
import com.sonianicoletti.usecases.repositories.GameRepository
import com.sonianicoletti.usecases.servives.GameService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(private val gameService: GameService) : GameRepository {

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

    override suspend fun isGameJoinable(gameID: String): Boolean {
        val game = gameService.getGameByID(gameID)
        // controlla che la partita non sia terminata o in gioco e che ci siano meno di 6 giocatori
        return game.players.size <= 5 || game.status == GameStatus.LOBBY
    }

    override suspend fun getOngoingGameUpdates() = gameFlow?.onEach { game = it } ?: throw GameNotRunningException()

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
