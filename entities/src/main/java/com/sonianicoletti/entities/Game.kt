package com.sonianicoletti.entities

data class Game(
    val id: String,
    val host: String,
    var status: GameStatus,
    val players: MutableList<Player>
)