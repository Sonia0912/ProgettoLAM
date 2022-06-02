package com.sonianicoletti.entities

data class Player (
    val id: String,
    val displayName: String,
    var character: Character,
    var cards: List<Card>
) {
    // creo un'istanza di Player da un oggetto User
    companion object {
        fun fromUser(user: User) = Player(user.id, user.displayName, Character.UNSELECTED, emptyList())
    }
}