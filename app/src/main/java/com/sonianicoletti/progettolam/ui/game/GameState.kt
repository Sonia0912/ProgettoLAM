package com.sonianicoletti.progettolam.ui.game

import com.sonianicoletti.entities.Game

data class GameState(
    val game: Game,
    val isHost: Boolean,
)