package com.sonianicoletti.usecases.repositories

import com.sonianicoletti.entities.Character
import com.sonianicoletti.entities.Game
import kotlinx.coroutines.flow.Flow

interface GameRepository {

    suspend fun startObservingGame(gameID: String)

    suspend fun getOngoingGameUpdates(): Flow<Game>

    suspend fun updateCharacter(userID: String, character: Character)
}