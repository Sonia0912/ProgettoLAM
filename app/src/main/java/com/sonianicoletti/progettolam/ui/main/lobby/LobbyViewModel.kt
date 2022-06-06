package com.sonianicoletti.progettolam.ui.main.lobby

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.GameStatus
import com.sonianicoletti.entities.Player
import com.sonianicoletti.entities.User
import com.sonianicoletti.entities.exceptions.*
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.repositories.GameRepository
import com.sonianicoletti.usecases.servives.AuthService
import com.sonianicoletti.usecases.servives.UserService
import com.sonianicoletti.zxing.QRCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val userService: UserService,
    private val authService: AuthService,
    private val qrCodeGenerator: QRCodeGenerator,
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val viewStateEmitter = MutableLiveData<ViewState>()
    val viewState: LiveData<ViewState> = viewStateEmitter

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    private val observeGameJob = Job()

    init {
        startObservingGame()
    }

    /**
     * TODO: Check for user updates on each game collect
     *       Move game updates to GameViewModel
     */

    private fun startObservingGame() = viewModelScope.launch(observeGameJob) {
        try {
            gameRepository.getOngoingGameUpdates().collect { game -> handleGameUpdate(game) }
        } catch (e: GameNotRunningException) {
            handleGameNotRunning()
        }
    }

    private suspend fun handleGameUpdate(game: Game) {
        try {
            emitGameState(game)
        } catch (e: UserNotLoggedInException) {
            handleUserNotLoggedIn()
        }
    }

    private suspend fun emitGameState(game: Game) {
        val user = authService.getUser() ?: throw UserNotLoggedInException()
        val isCurrentUserHost = user.id == game.host
        viewStateEmitter.postValue(ViewState(game, isCurrentUserHost))
    }

    private fun handleUserNotLoggedIn() {
        viewEventEmitter.value = ViewEvent.ShowUserNotLoggedInToast
        viewEventEmitter.value = ViewEvent.NavigateToAuth
    }

    private fun handleGameNotRunning() = viewModelScope.launch {
        viewEventEmitter.value = ViewEvent.ShowGameNotRunningToast
        leaveGame()
    }

    private suspend fun leaveGame() {
        gameRepository.leaveGame()
        viewEventEmitter.value = ViewEvent.NavigateUp
    }

    fun addPlayer(playerEmail: String) = viewModelScope.launch {
        try {
            checkPlayerCapacity()
            val invitedPlayer = userService.getUserByEmail(playerEmail.lowercase())
            checkDuplicatePlayer(invitedPlayer)
            addPlayerToGame(Player.fromUser(invitedPlayer))
        } catch (e: UserNotFoundException) {
            viewEventEmitter.postValue(ViewEvent.ShowUserNotFoundAlert)
        } catch (e: MaxPlayersException) {
            viewEventEmitter.postValue(ViewEvent.ShowMaxPlayersAlert)
        } catch (e: DuplicatePlayerException) {
            viewEventEmitter.postValue(ViewEvent.DuplicatePlayerAlert)
        }
    }

    private fun Game.hasMinimumPlayers() = players.size >= 3

    private fun checkPlayerCapacity() {
        if (gameRepository.getOngoingGame().players.size > 5) {
            throw MaxPlayersException()
        }
    }

    private fun checkDuplicatePlayer(invitedPlayer: User) {
        // controllo che non ci sia gia' lo stesso giocatore
        if (gameRepository.getOngoingGame().players.any { it.id == invitedPlayer.id }) {
            throw DuplicatePlayerException()
        }
    }

    fun startGame() {
        viewModelScope.launch {
            if(gameRepository.getOngoingGame().hasMinimumPlayers()) {
                gameRepository.updateGameStatus(GameStatus.CHARACTER_SELECT)
            } else {
                viewEventEmitter.postValue(ViewEvent.NotEnoughPlayersAlert)
            }
        }
    }

    private suspend fun addPlayerToGame(player: Player) {
        gameRepository.addPlayer(player)
        viewEventEmitter.value = ViewEvent.ClearText
    }

    fun generateQrCode(gameId: String) {
        val qrCodeBitmap = qrCodeGenerator.generateQRCode(gameId, 100, 100)
        viewEventEmitter.value = ViewEvent.SetQRCode(qrCodeBitmap)
    }

    fun handleLeaveGameButton() = viewModelScope.launch {
        observeGameJob.cancel()
        leaveGame()
    }

    data class ViewState(val game: Game, val isHost: Boolean)

    // rappresenta una ristretta gerarchia di classe per fornire piu' controllo sull'ereditarieta'
    // vogliamo che tutto quello che c'e' dentro la classe erediti dal tipo originale
    sealed class ViewEvent {
        object ShowMaxPlayersAlert : ViewEvent()
        object ShowUserNotFoundAlert : ViewEvent()
        object ShowUserNotLoggedInToast : ViewEvent()
        object DuplicatePlayerAlert : ViewEvent()
        object NotEnoughPlayersAlert : ViewEvent()
        object ShowGameNotRunningToast : ViewEvent()
        object ClearText : ViewEvent()
        object NavigateToAuth : ViewEvent()
        object NavigateToGame : ViewEvent()
        object NavigateUp : ViewEvent()
        class SetQRCode(val qrCodeBitmap: Bitmap) : ViewEvent()
    }
}
