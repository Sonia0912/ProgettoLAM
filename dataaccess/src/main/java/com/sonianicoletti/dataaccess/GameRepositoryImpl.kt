package com.sonianicoletti.dataaccess

import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.exceptions.GameNotRunningException
import com.sonianicoletti.usecases.repositories.GameRepository
import javax.inject.Inject

class GameRepositoryImpl @Inject constructor() : GameRepository {

    private var ongoingGame: Game? = null

    override fun getOngoingGame() = ongoingGame ?: throw GameNotRunningException()
}
