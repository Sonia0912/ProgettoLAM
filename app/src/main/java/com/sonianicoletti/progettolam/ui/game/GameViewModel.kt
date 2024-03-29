package com.sonianicoletti.progettolam.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Card
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.GameStatus
import com.sonianicoletti.entities.Player
import com.sonianicoletti.entities.exceptions.GameNotRunningException
import com.sonianicoletti.entities.exceptions.UserNotLoggedInException
import com.sonianicoletti.progettolam.ui.game.cards.CardItem
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.repositories.GameRepository
import com.sonianicoletti.usecases.servives.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val authService: AuthService,
) : ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    private val viewStateEmitter = MutableLiveData(ViewState())
    val viewState: LiveData<ViewState> = viewStateEmitter

    private val gameStateEmitter = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = gameStateEmitter

    private val observeGameJob = Job()
    private var isCurrentTurn: Boolean = false

    private val losers = mutableListOf<String>()

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
        val user = authService.getUser() ?: throw UserNotLoggedInException()
        gameStateEmitter.value = GameState(game, isHost)

        when {
            game.status == GameStatus.FINISHED -> endGame(game, user.id)
            game.turnPlayerId in game.losers && isCurrentTurn -> showDefeat(game)
            game.accusation?.displayCard != null && isCurrentTurn -> showDisplayCard(game.accusation!!.displayCard!!, true)
            game.accusation?.displayCard != null && gameRepository.isAccusationResponder() -> showDisplayCard(game.accusation!!.displayCard!!, false)
            game.accusation != null && gameRepository.isAccusationResponder() -> startAccusation()
            game.accusation != null -> viewStateEmitter.value = viewState.value?.copy(isAccusationResponder = false)
            isCurrentTurn != gameRepository.isCurrentTurn() -> resetTurn()
            else -> {
                viewStateEmitter.value = viewState.value?.copy(isAccusationResponder = false)
                viewEventEmitter.value = ViewEvent.HideDisplayCard
            }
        }

        if (game.losers.size != losers.size) {
            losers.clear()
            losers.addAll(game.losers)
            game.losers.lastOrNull()?.takeIf { it != user.id && game.status == GameStatus.ACTIVE }?.let { loserID ->
                val player = game.players.find { it.id == loserID }
                if (player != null) {
                    viewEventEmitter.value = ViewEvent.ShowPlayerHasLost(player.displayName)
                }
            }
        }
    }

    private suspend fun resetTurn() {
        isCurrentTurn = gameRepository.isCurrentTurn()
        viewEventEmitter.value = ViewEvent.HideDisplayCard
        viewStateEmitter.value = viewState.value?.copy(isAccusationResponder = false)

        if (!isCurrentTurn) {
            viewEventEmitter.value = ViewEvent.NavigateToCards
        }
    }

    private fun startAccusation() {
        viewEventEmitter.value = ViewEvent.NavigateToCards
        viewStateEmitter.apply { value = value?.copy(isAccusationResponder = true) }
    }

    private fun showDisplayCard(card: Card, isTurnPlayer: Boolean) {
        viewEventEmitter.value = ViewEvent.ShowDisplayCard(CardItem.fromCard(card), isTurnPlayer, gameRepository.getTurnPlayer().displayName)
    }

    private fun endGame(game: Game, currentUserId: String) {
        // Se il giocatore che ha fatto l'ultima accusa vince o se rimane un solo giocatore
        val winningPlayer = game.players.first { it.id == game.winner }
        stopObservingGame()
        viewStateEmitter.value = viewState.value?.copy(hasGameEnded = true)
        viewEventEmitter.value = when {
            game.winner == currentUserId && game.turnPlayerId == currentUserId -> ViewEvent.NavigateToSolutionVictory(false)
            game.winner == currentUserId -> ViewEvent.NavigateToSolutionVictory(true)
            game.losers.lastOrNull() == currentUserId && game.turnPlayerId == currentUserId -> ViewEvent.NavigateToSolutionDefeat(game.solutionCards.map {
                CardItem.fromCard(
                    it
                )
            }, winningPlayer.displayName, false, true)
            game.players.size - game.losers.size == 1 -> ViewEvent.NavigateToSolutionDefeat(
                game.solutionCards.map { CardItem.fromCard(it) },
                winningPlayer.displayName,
                true,
                false
            )
            else -> ViewEvent.NavigateToSolutionDefeat(game.solutionCards.map { CardItem.fromCard(it) }, winningPlayer.displayName, false, false)
        }
    }

    private suspend fun showDefeat(game: Game) {
        // Se il giocatore che ha fatto l'ultima accusa ha perso
        viewEventEmitter.value = ViewEvent.NavigateToSolutionDefeat(game.solutionCards.map { CardItem.fromCard(it) }, null, false, false)
        delay(1)
        gameRepository.nextTurn()
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
            try {
                gameRepository.leaveGame()
            } catch (e: Exception) {
                // do nothing. game might already be deleted
            } finally {
                viewEventEmitter.value = ViewEvent.NavigateToMain
            }
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

    fun stopObservingGame() {
        this.observeGameJob.cancel()
    }

    data class ViewState(
        val navigationFabOpened: Boolean = false,
        val checkedBoxesCharacters: MutableList<Pair<Int, Int>> = mutableListOf(),
        val checkedBoxesWeapons: MutableList<Pair<Int, Int>> = mutableListOf(),
        val checkedBoxesRooms: MutableList<Pair<Int, Int>> = mutableListOf(),
        val displayCard: CardItem? = null,
        val turnPlayer: Player? = null,
        val isTurnPlayer: Boolean = false,
        val isAccusationResponder: Boolean = false,
        var hasGameEnded: Boolean = false,
    )

    sealed class ViewEvent {
        object ShowUserNotLoggedInToast : ViewEvent()
        object ShowGameNotRunningToast : ViewEvent()
        object NavigateToAuth : ViewEvent()
        object NavigateToMain : ViewEvent()
        object NavigateToCards : ViewEvent()
        class ShowDisplayCard(val cardItem: CardItem, val isTurnPlayer: Boolean, val turnPlayerName: String) : ViewEvent()
        object HideDisplayCard : ViewEvent()
        class ShowPlayerHasLost(val playerName: String) : ViewEvent()
        class NavigateToSolutionVictory(val wonByNoPlayersRemaining: Boolean) : ViewEvent()
        class NavigateToSolutionDefeat(val solutionCards: List<CardItem>, val winnerName: String?, val wonByDefault: Boolean, val lostByAccusation: Boolean) : ViewEvent()
    }
}
