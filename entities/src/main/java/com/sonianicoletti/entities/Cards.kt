package com.sonianicoletti.entities

import com.sonianicoletti.entities.Card

object Cards {

    val characters = listOf(
        Card("Mrs Peacock", "character"),
        Card("Colonel Mustard", "character"),
        Card("Reverend Green", "character"),
        Card("Professor Plum", "character"),
        Card("Mrs White", "character"),
        Card("Miss Scarlett", "character")
    )

    val weapons = listOf(
        Card("Candlestick", "weapon"),
        Card("Revolver", "weapon"),
        Card("Rope", "weapon"),
        Card("Dagger", "weapon"),
        Card("Lead Pipe", "weapon"),
        Card("Wrench", "weapon")
    )

    val rooms = listOf(
        Card("Kitchen", "room"),
        Card("Library", "room"),
        Card("Lounge", "room"),
        Card("Study", "room"),
        Card("Ballroom", "room"),
        Card("Billiard Room", "room"),
        Card("Conservatory", "room"),
        Card("Dining Room", "room"),
        Card("Hall", "room")
    )
}
