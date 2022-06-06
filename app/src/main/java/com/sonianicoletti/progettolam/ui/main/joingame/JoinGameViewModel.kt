package com.sonianicoletti.progettolam.ui.main.joingame

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Player
import com.sonianicoletti.entities.exceptions.*
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.repositories.GameRepository
import com.sonianicoletti.usecases.servives.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinGameViewModel @Inject constructor(
    private val authService: AuthService,
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    fun joinGame(gameID: String) {
        viewModelScope.launch {
            try {
                validateGameId(gameID)
                checkIsAlreadyInGame(gameID)
                checkGameAvailability(gameID)
                addCurrentPlayerToGame(gameID)
                gameRepository.loadGame(gameID)
                viewEventEmitter.postValue(ViewEvent.NavigateToLobby(gameID))
            } catch (e: BlankFieldException) {
                viewEventEmitter.postValue(ViewEvent.ShowBlankFieldError)
            } catch (e: MinCharsNotAddedException) {
                viewEventEmitter.postValue(ViewEvent.ShowMinCharsNotAddedError)
            } catch (e: UserAlreadyInGameException) {
                viewEventEmitter.postValue(ViewEvent.ShowUserAlreadyInGameAlert)
            } catch (e: UnableToJoinGameException) {
                viewEventEmitter.postValue(ViewEvent.ShowUnableToJoinGameAlert)
            } catch (e: GameNotFoundException) {
                viewEventEmitter.postValue(ViewEvent.ShowGameNotFoundAlert)
            } catch (e: UserNotLoggedInException) {
                viewEventEmitter.postValue(ViewEvent.ShowUserNotLoggedInAlert)
            }
        }
    }

    private fun validateGameId(gameID: String) {
        if (gameID.isBlank()) {
            throw BlankFieldException()
        } else if (gameID.length != 6) {
            throw MinCharsNotAddedException()
        }
    }

    private suspend fun checkIsAlreadyInGame(gameID: String) {
        val user = authService.getUser() ?: throw UserNotLoggedInException()
        if (gameRepository.isUserInGame(user.id, gameID)) {
            throw UserAlreadyInGameException()
        }
    }

    private suspend fun checkGameAvailability(gameID: String) {
        if (!gameRepository.isGameJoinable(gameID)) {
            throw UnableToJoinGameException()
        }
    }

    private suspend fun addCurrentPlayerToGame(gameID: String) {
        val user = authService.getUser() ?: throw UserNotLoggedInException()
        gameRepository.addPlayer(gameID, Player.fromUser(user))
    }

    sealed class ViewEvent {
        object ShowGameNotFoundAlert : ViewEvent()
        object ShowUserNotLoggedInAlert : ViewEvent()
        object ShowUserAlreadyInGameAlert : ViewEvent()
        object ShowUnableToJoinGameAlert : ViewEvent()
        object ShowBlankFieldError : ViewEvent()
        object ShowMinCharsNotAddedError : ViewEvent()
        data class NavigateToLobby(val gameID: String) : ViewEvent()
    }
}
