package com.sonianicoletti.entities

data class Accusation(
    val cards: MutableList<Card>,
    val responder: String // user id of player who should currently respond to the accusation
)
