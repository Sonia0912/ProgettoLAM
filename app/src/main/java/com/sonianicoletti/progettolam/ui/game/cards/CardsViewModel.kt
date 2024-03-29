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
            if (gameRepository.isHost() && gameRepository.getOngoingGame().turnPlayerId.isEmpty()) {
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
        val accusingPlayer = game.players.firstOrNull { it.id == game.accusation?.responder }
        val accusationCards = game.accusation?.cards.mapToCardItems()
        val canSkip = !yourCards.any { it in accusationCards } && accusingPlayer?.id == currentUser?.id

        val viewState = ViewState(
            yourCards,
            leftoverCards,
            turnPlayer,
            accusingPlayer,
            accusationCards,
            canSkip,
            currentPlayer?.id.orEmpty()
        )
        viewStateEmitter.postValue(viewState)
    }

    private fun List<Card>?.mapToCardItems(): MutableList<CardItem> {
        return orEmpty().map { CardItem.fromCard(it) }.toMutableList()
    }

    fun skipAccusation() = viewModelScope.launch {
        gameRepository.nextAccusationResponder()
    }

    fun onAccusationCardClicked(cardItem: CardItem) = viewModelScope.launch {
        gameRepository.setAccusationDisplayCard(cardItem.card)
    }

    data class ViewState(
        val yourCards: MutableList<CardItem>,
        val leftoverCards: MutableList<CardItem>,
        val turnPlayer: Player?,
        val respondingPlayer: Player?,
        val accusationCards: MutableList<CardItem>,
        val canSkip: Boolean,
        val currentUserId: String,
    )
}
