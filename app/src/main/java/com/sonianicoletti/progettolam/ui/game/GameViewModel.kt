package com.sonianicoletti.progettolam.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.usecases.repositories.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
) : ViewModel() {

    fun setOngoingGame(gameID: String) = viewModelScope.launch {
        gameRepository.startObservingGame(gameID)
    }
}
