package com.sonianicoletti.usecases.servives

import com.sonianicoletti.entities.Game

interface GameService {

    suspend fun createGame() : Game

    suspend fun updateGame(game: Game)

    suspend fun getGameByID(gameID: String) : Game
}
