package com.sonianicoletti.progettolam.ui.game.characters

import androidx.annotation.DrawableRes
import com.sonianicoletti.entities.Character
import com.sonianicoletti.entities.Player

data class SelectCharacterItem(
    val character: Character,
    @DrawableRes val avatarRes: Int,
    val assignedPlayer: Player?,
)
