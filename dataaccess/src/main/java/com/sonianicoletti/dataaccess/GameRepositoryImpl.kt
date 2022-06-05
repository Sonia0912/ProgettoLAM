package com.sonianicoletti.dataaccess

import com.sonianicoletti.entities.Character
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.exceptions.GameNotRunningException
import com.sonianicoletti.usecases.repositories.GameRepository
import com.sonianicoletti.usecases.servives.GameService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(private val gameService: GameService) : GameRepository {

    private var ongoingGame: Game? = null
    private var gameFlow: Flow<Game>? = null

    override suspend fun startObservingGame(gameID: String) {
        gameFlow = gameService.observeGameByID(gameID)
        gameFlow?.collect { ongoingGame = it }
    }

    override suspend fun getOngoingGameUpdates() = gameFlow ?: throw GameNotRunningException()

    override suspend fun updateCharacter(userID: String, character: Character) = ongoingGame?.let { game ->
        game.players.find { it.id == userID }?.character = character
        gameService.updateGame(game)
    } ?: throw GameNotRunningException()
}
