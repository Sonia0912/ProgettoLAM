package com.sonianicoletti.usecases.servives

import com.sonianicoletti.entities.Card
import com.sonianicoletti.entities.Game
import kotlinx.coroutines.flow.Flow

interface GameService {

    suspend fun createGame() : Game

    suspend fun updateGame(game: Game)

    suspend fun getGameByID(gameID: String) : Game

    suspend fun observeGameByID(gameID: String): Flow<Game>

    suspend fun deleteGame(gameID: String)

    suspend fun checkVictory(gameID: String, characterCard: Card, weaponCard: Card, roomCard: Card) : Boolean
}
