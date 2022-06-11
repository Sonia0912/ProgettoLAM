package com.sonianicoletti.entities

data class Accusation(
    val cards: MutableList<Card>,
    var responder: String // user id of player who should currently respond to the accusation
)
