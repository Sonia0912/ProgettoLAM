package com.sonianicoletti.usecases.repositories

import com.sonianicoletti.entities.Game

interface GameRepository {

    fun getOngoingGame(): Game

}