package com.sonianicoletti.progettolam.ui.game.notes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Player
import com.sonianicoletti.progettolam.ui.game.cards.CardItem
import com.sonianicoletti.usecases.repositories.GameRepository
import com.sonianicoletti.usecases.servives.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val authService: AuthService
) : ViewModel() {

    var cardsStateEmitter = MutableLiveData<ViewState>()
    var cardsState : LiveData<ViewState> = cardsStateEmitter

    class ViewState(val defaultCards: MutableList<CardItem>, val otherPlayers: MutableList<Player>)

    fun handleGameState() {
        viewModelScope.launch {
            val currentGame = gameRepository.getOngoingGame()
            val currentUser = authService.getUser()
            val currentPlayer = currentGame.players.find { it.id == currentUser?.id }
            val yourCards = currentPlayer?.cards?.map { CardItem.fromCard(it) }.orEmpty().toMutableList()
            val leftoverCards = currentGame.leftoverCards.map { CardItem.fromCard(it) }.toMutableList()
            val defaultCards = yourCards + leftoverCards
            val otherPlayers = currentGame.players.filter { it.id != currentUser?.id }
            val viewState = NotesViewModel.ViewState(defaultCards.toMutableList(), otherPlayers.toMutableList())
            cardsStateEmitter.postValue(viewState)
        }
    }


}