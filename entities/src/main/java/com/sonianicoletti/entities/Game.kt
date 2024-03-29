package com.sonianicoletti.entities

data class Game(
    val id: String,
    val host: String,
    var status: GameStatus,
    val players: MutableList<Player>,
    var leftoverCards: MutableList<Card>,
    var solutionCards: MutableList<Card>,
    var turnPlayerId: String,
    var accusation: Accusation?,
    var winner: String,
    var losers: MutableList<String>
)
