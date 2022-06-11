package com.sonianicoletti.progettolam.ui.game.cards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Card
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.Player
import com.sonianicoletti.progettolam.ui.game.GameState
import com.sonianicoletti.usecases.repositories.GameRepository
import com.sonianicoletti.usecases.servives.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardsViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val authService: AuthService,
) : ViewModel() {

    private val viewStateEmitter = MutableLiveData<ViewState>()
    val viewState : LiveData<ViewState> = viewStateEmitter

    init {
        initialiseGame()
    }

    private fun initialiseGame() {
        viewModelScope.launch {
            if (gameRepository.isHost()) {
                delay(3000)
                gameRepository.nextTurn()
            }
        }
    }

    fun handleGameState(gameState: GameState) = viewModelScope.launch {
        prepareViewState(gameState.game)
    }

    private suspend fun prepareViewState(game: Game) {
        val currentUser = authService.getUser()
        val currentPlayer = game.players.find { it.id == currentUser?.id }
        val yourCards = currentPlayer?.cards.mapToCardItems()
        val leftoverCards = game.leftoverCards.mapToCardItems()

        val turnPlayer = game.players.firstOrNull { it.id == game.turnPlayerId }

        val viewState = ViewState(yourCards, leftoverCards, turnPlayer)
        viewStateEmitter.postValue(viewState)
    }

    private fun List<Card>?.mapToCardItems(): MutableList<CardItem> {
        return orEmpty().map { CardItem.fromCard(it) }.toMutableList()
    }

    data class ViewState(
        val yourCards: MutableList<CardItem>,
        val leftoverCards: MutableList<CardItem>,
        val turnPlayer: Player?,
    )
}
