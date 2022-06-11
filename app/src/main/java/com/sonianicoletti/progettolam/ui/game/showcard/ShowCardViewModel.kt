package com.sonianicoletti.progettolam.ui.game.showcard

import androidx.lifecycle.ViewModel
import com.sonianicoletti.usecases.repositories.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShowCardViewModel @Inject constructor(
    private val gameRepository: GameRepository,
) : ViewModel() {

}