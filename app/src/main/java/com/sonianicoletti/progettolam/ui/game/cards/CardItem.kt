package com.sonianicoletti.progettolam.ui.game.cards

import androidx.annotation.DrawableRes
import com.sonianicoletti.entities.Card
import com.sonianicoletti.progettolam.R
import java.io.Serializable

// questa classe serve per prendere gli ID delle immagini dal modulo App che altrimenti non sarebbero accessibili
data class CardItem(
    val card: Card,
    @DrawableRes val imageRes: Int
) : Serializable {
    companion object {
        fun fromCard(card: Card) : CardItem {
            val imageRes =
                when(card.name) {
                    "Mrs Peacock" -> R.drawable.mrs_peacock
                    "Colonel Mustard" -> R.drawable.colonel_mustard
                    "Reverend Green" -> R.drawable.rev_green
                    "Professor Plum" -> R.drawable.professor_plum
                    "Mrs White" -> R.drawable.mrs_white
                    "Miss Scarlett" -> R.drawable.miss_scarlett
                    "Candlestick" -> R.drawable.candlestick
                    "Revolver" -> R.drawable.revolver
                    "Rope" -> R.drawable.rope
                    "Dagger" -> R.drawable.dagger
                    "Lead Pipe" -> R.drawable.lead_pipe
                    "Wrench" -> R.drawable.wrench
                    "Kitchen" -> R.drawable.kitchen
                    "Library" -> R.drawable.library
                    "Lounge" -> R.drawable.lounge
                    "Study" -> R.drawable.study
                    "Ballroom" -> R.drawable.ballroom
                    "Billiard Room" -> R.drawable.billiard_room
                    "Conservatory" -> R.drawable.conservatory
                    "Dining Room" -> R.drawable.dining_room
                    "Hall" -> R.drawable.hall
                    else -> throw IllegalArgumentException()
                }
            return CardItem(card, imageRes)
        }
    }
}