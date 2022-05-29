package com.sonianicoletti.usecases.servives

import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.User

interface GameService {

    suspend fun createGame() : Game
    suspend fun updateGamePlayers(gameID: String, players: List<User>)

}