package com.sonianicoletti.progettolam.ui.game.cards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Card
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

    private val cardsStateEmitter = MutableLiveData<ViewState>()
    val cardsState : LiveData<ViewState> = cardsStateEmitter

    fun handleGameState(game: Game) {
        viewModelScope.launch {
            prepareCards(game)
        }
    }

    private suspend fun prepareCards(game: Game) {
        val currentUser = authService.getUser()
        val currentPlayer = game.players.find { it.id == currentUser?.id }

        val yourCards = currentPlayer?.cards.mapToCardItems()
        val leftoverCards = game.leftoverCards.mapToCardItems()

        val viewState = ViewState(yourCards, leftoverCards)
        cardsStateEmitter.postValue(viewState)
    }

    private fun List<Card>?.mapToCardItems(): MutableList<CardItem> {
        return orEmpty().map { CardItem.fromCard(it) }.toMutableList()
    }

    class ViewState(val yourCards: MutableList<CardItem>, val leftoverCards: MutableList<CardItem>)
}
