package com.sonianicoletti.progettolam.ui.game.characters

import androidx.lifecycle.ViewModel
import com.sonianicoletti.entities.Character
import com.sonianicoletti.usecases.servives.AuthService
import com.sonianicoletti.usecases.servives.GameService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CharactersViewModel @Inject constructor(private val gameService: GameService, private val authService: AuthService) : ViewModel() {

    fun selectCharacter(selectedCharacter: Character) {

    }

}