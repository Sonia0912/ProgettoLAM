package com.sonianicoletti.progettolam.ui.game.solution

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.progettolam.ui.game.cards.CardItem
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.repositories.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SolutionViewModel @Inject constructor(private val gameRepository: GameRepository) : ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    init {
        viewModelScope.launch {
            val game = gameRepository.getOngoingGame()
            viewEventEmitter.value = ViewEvent.ShowSolutionCards(CardItem.fromCard(game.solutionCards[0]), CardItem.fromCard(game.solutionCards[1]), CardItem.fromCard(game.solutionCards[2]))
            viewEventEmitter.value = ViewEvent.ShowResult(gameRepository.isWinner())
        }
    }

    sealed class ViewEvent {
        class ShowResult(val won: Boolean) : SolutionViewModel.ViewEvent()
        class ShowSolutionCards(val card1: CardItem, val card2: CardItem, val card3: CardItem) : SolutionViewModel.ViewEvent()
    }

}