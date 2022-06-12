package com.sonianicoletti.progettolam.ui.game.lobby

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.GameStatus
import com.sonianicoletti.entities.User
import com.sonianicoletti.entities.exceptions.*
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.repositories.GameRepository
import com.sonianicoletti.usecases.servives.AuthService
import com.sonianicoletti.usecases.servives.InvitesService
import com.sonianicoletti.usecases.servives.NetworkService
import com.sonianicoletti.usecases.servives.UserService
import com.sonianicoletti.zxing.QRCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val userService: UserService,
    private val authService: AuthService,
    private val qrCodeGenerator: QRCodeGenerator,
    private val gameRepository: GameRepository,
    private val networkService: NetworkService,
    private val invitesService: InvitesService,
) : ViewModel() {

    private val viewStateEmitter = MutableLiveData<ViewState>(ViewState.Idle)
    val viewState: LiveData<ViewState> = viewStateEmitter

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    fun invitePlayer(playerEmail: String) = viewModelScope.launch {
        try {
            validateEmail(playerEmail)
            checkNetworkConnectivity()
            checkMaxPlayersReached()
            viewStateEmitter.postValue(ViewState.AddingPlayer)
            val invitedUser = userService.getUserByEmail(playerEmail.lowercase().trim())
            checkDuplicatePlayer(invitedUser)
            inviteUserToGame(invitedUser)
            clearText()
        } catch (e: IllegalArgumentException) {
            viewEventEmitter.postValue(ViewEvent.ShowInvalidEmailAlert)
        } catch (e: NetworkNotConnectedException) {
            viewEventEmitter.postValue(ViewEvent.ShowNoNetworkConnectionAlert)
        } catch (e: MaxPlayersException) {
            viewEventEmitter.postValue(ViewEvent.ShowMaxPlayersAlert)
        } catch (e: DuplicatePlayerException) {
            viewEventEmitter.postValue(ViewEvent.ShowDuplicatePlayerAlert)
        } catch (e: UserNotFoundException) {
            viewEventEmitter.postValue(ViewEvent.ShowUserNotFoundAlert)
        } catch (e: Exception) {
            e.printStackTrace()
            viewEventEmitter.postValue(ViewEvent.ShowGeneralErrorAlert)
        } finally {
            viewStateEmitter.postValue(ViewState.Idle)
        }
    }

    private suspend fun inviteUserToGame(invitedUser: User) {
        val game = gameRepository.getOngoingGame()
        val user = authService.getUser() ?: throw UserNotFoundException()
        invitesService.sendInvite(game.id, user.displayName, invitedUser)
    }

    private fun validateEmail(email: String) {
        val emailParts = email.split("@").filter { it.isNotBlank() }
        if (emailParts.size < 2) {
            throw IllegalArgumentException()
        }
        val splitFromDot = emailParts[1].split(".").filter { it.isNotBlank() }
        if (splitFromDot.size < 2) {
            throw IllegalArgumentException()
        }
    }

    private fun checkNetworkConnectivity() {
        if (!networkService.isConnected()) {
            throw NetworkNotConnectedException()
        }
    }

    private suspend fun checkMaxPlayersReached() {
        val game = gameRepository.getOngoingGame()
        if (game.players.size == 6) {
            throw MaxPlayersException()
        }
    }

    private suspend fun checkDuplicatePlayer(invitedPlayer: User) {
        // controllo che non ci sia gia' lo stesso giocatore
        val game = gameRepository.getOngoingGame()
        if (gameRepository.isUserInGame(invitedPlayer.id, game.id)) {
            throw DuplicatePlayerException()
        }
    }

    fun startGame() {
        viewModelScope.launch {
            try {
                checkNetworkConnectivity()
                viewStateEmitter.postValue(ViewState.Loading)
//                gameRepository.getOngoingGame().checkHasMinimumPlayers()
                gameRepository.updateGameStatus(GameStatus.CHARACTER_SELECT)
            } catch (e: NetworkNotConnectedException) {
                viewEventEmitter.postValue(ViewEvent.ShowNoNetworkConnectionAlert)
            } catch (e: NotEnoughPlayersException) {
                viewEventEmitter.postValue(ViewEvent.ShowNotEnoughPlayersAlert)
            } finally {
                viewStateEmitter.postValue(ViewState.Idle)
            }
        }
    }

    private fun Game.checkHasMinimumPlayers() {
        if (players.size < 3) {
            throw NotEnoughPlayersException()
        }
    }

    private fun clearText() {
        viewEventEmitter.value = ViewEvent.ClearText
    }

    fun generateQrCode(gameId: String) {
        val qrCodeBitmap = qrCodeGenerator.generateQRCode(gameId, 100, 100)
        viewEventEmitter.value = ViewEvent.SetQRCode(qrCodeBitmap)
    }

    sealed class ViewState {
        object Loading : ViewState()
        object AddingPlayer : ViewState()
        object Idle : ViewState()
    }

    // tutto quello che c'e' dentro la classe eredita dal tipo originale
    sealed class ViewEvent {
        object ShowInvalidEmailAlert : ViewEvent()
        object ShowNoNetworkConnectionAlert : ViewEvent()
        object ShowMaxPlayersAlert : ViewEvent()
        object ShowUserNotFoundAlert : ViewEvent()
        object ShowDuplicatePlayerAlert : ViewEvent()
        object ShowNotEnoughPlayersAlert : ViewEvent()
        object ShowGeneralErrorAlert : ViewEvent()
        object ClearText : ViewEvent()
        class SetQRCode(val qrCodeBitmap: Bitmap) : ViewEvent()
    }
}
