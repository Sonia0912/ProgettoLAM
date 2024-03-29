package com.sonianicoletti.usecases.repositories

import com.sonianicoletti.entities.*
import kotlinx.coroutines.flow.Flow

interface GameRepository {

    suspend fun createGame()

    suspend fun loadGame(gameID: String)

    fun getOngoingGame(): Game

    suspend fun isUserInGame(userID: String, gameID: String): Boolean

    suspend fun isGameJoinable(gameID: String): Boolean

    suspend fun getOngoingGameUpdates(): Flow<Game>

    suspend fun addPlayer(player: Player)

    suspend fun addPlayer(gameID: String, player: Player)

    suspend fun leaveGame()

    suspend fun updateGameStatus(gameStatus: GameStatus)

    suspend fun updateCharacter(userID: String, character: Character)

    suspend fun isHost(): Boolean

    suspend fun isCurrentTurn(): Boolean

    fun getTurnPlayer(): Player

    suspend fun distributeCards()

    suspend fun nextTurn()

    suspend fun makeAccusation(characterCard: Card, weaponCard: Card, roomCard: Card)

    suspend fun makeFinalAccusation(characterCard: Card, weaponCard: Card, roomCard: Card)

    suspend fun nextAccusationResponder()

    suspend fun setAccusationDisplayCard(card: Card)

    suspend fun isAccusationResponder(): Boolean

    suspend fun isWinner(): Boolean
}