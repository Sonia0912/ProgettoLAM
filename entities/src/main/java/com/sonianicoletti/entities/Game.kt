package com.sonianicoletti.entities

data class Game(
    val id: String,
    val host: String,
    val status: GameStatus,
    val players: MutableList<User>
)