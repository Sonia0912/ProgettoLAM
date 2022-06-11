package com.sonianicoletti.progettolam.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.Player
import com.sonianicoletti.entities.exceptions.GameNotRunningException
import com.sonianicoletti.entities.exceptions.UserNotLoggedInException
import com.sonianicoletti.progettolam.ui.game.cards.CardItem
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.repositories.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    private val viewStateEmitter = MutableLiveData(ViewState())
    val viewState: LiveData<ViewState> = viewStateEmitter

    private val gameStateEmitter = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = gameStateEmitter

    private val observeGameJob = Job()
    private var isCurrentTurn: Boolean = false

    init {
        observeGameUpdates()
    }

    private fun observeGameUpdates() = viewModelScope.launch(observeGameJob) {
        try {
            gameRepository.getOngoingGameUpdates().collect { game -> handleGameUpdate(game) }
        } catch (e: GameNotRunningException) {
            handleGameNotRunning()
        } catch (e: UserNotLoggedInException) {
            handleUserNotLoggedIn()
        }
    }

    private suspend fun handleGameUpdate(game: Game) {
        val isHost = gameRepository.isHost()
        gameStateEmitter.postValue(GameState(game, isHost))

        if (isCurrentTurn && !gameRepository.isCurrentTurn()) {
            isCurrentTurn = false
            viewEventEmitter.postValue(ViewEvent.NavigateToCards)
        }
        isCurrentTurn = gameRepository.isCurrentTurn()

        if (game.accusation != null && !gameRepository.isCurrentTurn()) {
            viewEventEmitter.postValue(ViewEvent.NavigateToCards)
        }

        game.accusation?.displayCard?.let { displayCard ->
            if (gameRepository.isCurrentTurn()) {
                viewEventEmitter.postValue(ViewEvent.ShowDisplayCard(CardItem.fromCard(displayCard), true, gameRepository.getTurnPlayer().displayName))
            } else if (gameRepository.isAccusationResponder()) {
                viewEventEmitter.postValue(ViewEvent.ShowDisplayCard(CardItem.fromCard(displayCard), false, gameRepository.getTurnPlayer().displayName))
            }
        } ?: viewEventEmitter.postValue(ViewEvent.HideDisplayCard)
    }

    private fun handleGameNotRunning() = viewModelScope.launch {
        viewEventEmitter.value = ViewEvent.ShowGameNotRunningToast
        gameRepository.leaveGame()
        viewEventEmitter.value = ViewEvent.NavigateToMain
    }

    private fun handleUserNotLoggedIn() {
        viewEventEmitter.value = ViewEvent.ShowUserNotLoggedInToast
        viewEventEmitter.value = ViewEvent.NavigateToAuth
    }

    fun toggleNavigationFab() {
        val viewState = viewState.value
        val newViewState = viewState?.copy(navigationFabOpened = viewState.navigationFabOpened.not())
        viewStateEmitter.postValue(newViewState)
    }

    fun leaveGame() {
        observeGameJob.cancel()
        viewModelScope.launch {
            gameRepository.leaveGame()
            viewEventEmitter.value = ViewEvent.NavigateToMain
        }
    }

    fun updateCheckBoxCharacters(column: Int, row: Int, toAdd: Boolean) {
        if(toAdd) {
            viewState.value?.checkedBoxesCharacters?.add(column to row)
        } else {
            viewState.value?.checkedBoxesCharacters?.remove(column to row)
        }
        viewStateEmitter.postValue(viewState.value)
    }

    fun updateCheckBoxWeapons(column: Int, row: Int, toAdd: Boolean) {
        if(toAdd) {
            viewState.value?.checkedBoxesWeapons?.add(column to row)
        } else {
            viewState.value?.checkedBoxesWeapons?.remove(column to row)
        }
        viewStateEmitter.postValue(viewState.value)
    }

    fun updateCheckBoxRooms(column: Int, row: Int, toAdd: Boolean) {
        if(toAdd) {
            viewState.value?.checkedBoxesRooms?.add(column to row)
        } else {
            viewState.value?.checkedBoxesRooms?.remove(column to row)
        }
        viewStateEmitter.postValue(viewState.value)
    }

    fun onDisplayCardButtonClicked() = viewModelScope.launch {
        gameRepository.nextTurn()
    }

    data class ViewState(
        val navigationFabOpened: Boolean = false,
        val checkedBoxesCharacters: MutableList<Pair<Int, Int>> = mutableListOf(),
        val checkedBoxesWeapons: MutableList<Pair<Int, Int>> = mutableListOf(),
        val checkedBoxesRooms: MutableList<Pair<Int, Int>> = mutableListOf(),
        val displayCard: CardItem? = null,
        val turnPlayer: Player? = null,
        val isTurnPlayer: Boolean? = false,
    )

    sealed class ViewEvent {
        object ShowUserNotLoggedInToast : ViewEvent()
        object ShowGameNotRunningToast : ViewEvent()
        object NavigateToAuth : ViewEvent()
        object NavigateToMain : ViewEvent()
        object NavigateToCards : ViewEvent()
        class ShowDisplayCard(val cardItem: CardItem, val isTurnPlayer: Boolean, val turnPlayerName: String) : ViewEvent()
        object HideDisplayCard : ViewEvent()
    }
}
