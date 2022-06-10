package com.sonianicoletti.progettolam.ui.game.cards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Game
import com.sonianicoletti.usecases.repositories.GameRepository
import com.sonianicoletti.usecases.servives.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardsViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val authService: AuthService
) : ViewModel() {

    var cardsStateEmitter = MutableLiveData<ViewState>()
    var cardsState : LiveData<ViewState> = cardsStateEmitter

    class ViewState(val yourCards: MutableList<CardItem>, val leftoverCards: MutableList<CardItem>)

    fun handleGameState(game: Game) {
        viewModelScope.launch {
            val currentUser = authService.getUser()
            val currentPlayer = game.players.find { it.id == currentUser?.id }
            val yourCards = currentPlayer?.cards?.map { CardItem.fromCard(it) }.orEmpty().toMutableList()
            val leftoverCards = game.leftoverCards.map { CardItem.fromCard(it) }.toMutableList()
            val viewState = ViewState(yourCards, leftoverCards)
            cardsStateEmitter.postValue(viewState)
        }
    }

}