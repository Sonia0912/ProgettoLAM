package com.sonianicoletti.progettolam.ui.main.lobby

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.User
import com.sonianicoletti.entities.exceptions.UserNotFoundException
import com.sonianicoletti.zxing.QRCodeGenerator
import com.sonianicoletti.progettolam.ui.main.lobby.exception.DuplicatePlayerException
import com.sonianicoletti.progettolam.ui.main.lobby.exception.MaxPlayersException
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.servives.GameService
import com.sonianicoletti.usecases.servives.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val gameService: GameService,
    private val userService: UserService,
    private val qrCodeGenerator: QRCodeGenerator,
) : ViewModel() {

    private val viewStateEmitter = MutableLiveData<ViewState>()
    val viewState: LiveData<ViewState> = viewStateEmitter

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    private lateinit var game: Game

    init {
        createGame()
    }

    private fun createGame() = viewModelScope.launch {
        try {
            viewStateEmitter.postValue(ViewState.Loading)
            game = gameService.createGame()
            viewStateEmitter.postValue(ViewState.Loaded(game))
            generateGameIdQRCode(game.id)
        } catch (e: Throwable) {
            e.printStackTrace()
            viewStateEmitter.postValue(ViewState.Error(e))
        }
    }

    fun addPlayer(playerEmail: String) = viewModelScope.launch {
        val invitedPlayer = userService.getUserByEmail(playerEmail)

        try {
            checkPlayerCapacity()
            checkDuplicatePlayer(invitedPlayer)
            addPlayerToGame(invitedPlayer)
        } catch (e: UserNotFoundException) {
            viewEventEmitter.postValue(ViewEvent.NotFoundUserAlert)
        } catch (e: MaxPlayersException) {
            viewEventEmitter.postValue(ViewEvent.OpenMaxPlayersDialog)
        } catch (e: DuplicatePlayerException) {
            viewEventEmitter.postValue(ViewEvent.DuplicatePlayerAlert)
        }
    }

    private fun checkPlayerCapacity() {
        if (game.players.size > 5) {
            throw MaxPlayersException()
        }
    }

    private fun checkDuplicatePlayer(invitedPlayer: User) {
        // controllo che non ci sia gia' lo stesso giocatore
        if (game.players.any { it.id == invitedPlayer.id }) {
            throw DuplicatePlayerException()
        }
    }

    private suspend fun addPlayerToGame(player: User) {
        game.players.add(player)
        gameService.updateGame(game)
        viewStateEmitter.postValue(ViewState.Loaded(game))
        viewEventEmitter.postValue(ViewEvent.ClearText)
    }

    private fun generateGameIdQRCode(gameId: String) {
        val qrCodeBitmap = qrCodeGenerator.generateQRCode(gameId, 100, 100)
        viewEventEmitter.postValue(ViewEvent.SetQRCode(qrCodeBitmap))
    }

    sealed class ViewState {
        object Loading : ViewState()
        data class Loaded(val game: Game) : ViewState()
        data class Error(val error: Throwable) : ViewState()
    }

    // rappresenta una ristretta gerarchia di classe per fornire piu' controllo sull'ereditarieta'
    // vogliamo che tutto quello che c'e' dentro la classe erediti dal tipo originale
    sealed class ViewEvent {
        object OpenMaxPlayersDialog : ViewEvent()
        object NotFoundUserAlert : ViewEvent()
        object DuplicatePlayerAlert : ViewEvent()
        object ClearText : ViewEvent()
        class SetQRCode(val qrCodeBitmap: Bitmap) : ViewEvent()
    }
}
