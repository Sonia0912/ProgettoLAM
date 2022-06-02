package com.sonianicoletti.progettolam.ui.main.joingame

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.GameStatus
import com.sonianicoletti.entities.exceptions.*
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.servives.AuthService
import com.sonianicoletti.usecases.servives.GameService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinGameViewModel @Inject constructor(
    private val gameService: GameService,
    private val authService: AuthService,
) : ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    fun joinGame(gameID: String) {
        viewModelScope.launch {
            try {
                validateGameId(gameID)
                checkGameAvailability(gameID)
                addCurrentPlayerToGame(gameID)
                viewEventEmitter.postValue(ViewEvent.NavigateToLobby(gameID))
            } catch (e: BlankFieldException) {
                viewEventEmitter.postValue(ViewEvent.ShowBlankFieldError)
            } catch (e: MinCharsNotAddedException) {
                viewEventEmitter.postValue(ViewEvent.ShowMinCharsNotAddedError)
            } catch (e: UnableToJoinGameException) {
                viewEventEmitter.postValue(ViewEvent.ShowUnableToJoinGameAlert)
            } catch (e: GameNotFoundException) {
                viewEventEmitter.postValue(ViewEvent.ShowGameNotFoundAlert)
            } catch (e: UserNotFoundException) {
                viewEventEmitter.postValue(ViewEvent.ShowUserNotFoundAlert)
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

    private suspend fun checkGameAvailability(gameID: String) {
        // controlla che l'ID esista
        val gameToJoin = gameService.getGameByID(gameID)

        // controlla che la partita non sia terminata o in gioco e che ci siano meno di 6 giocatori
        if (gameToJoin.players.size > 5 || gameToJoin.status != GameStatus.LOBBY) {
            throw UnableToJoinGameException()
        }
    }

    private suspend fun addCurrentPlayerToGame(gameID: String) {
        authService.getUser()?.let { player ->
            val game = gameService.getGameByID(gameID)
            game.players.add(player)
            gameService.updateGame(game)
        } ?: throw UserNotFoundException()
    }

    sealed class ViewEvent {
        object ShowGameNotFoundAlert : ViewEvent()
        object ShowUserNotFoundAlert : ViewEvent()
        object ShowUnableToJoinGameAlert : ViewEvent()
        object ShowBlankFieldError : ViewEvent()
        object ShowMinCharsNotAddedError : ViewEvent()
        data class NavigateToLobby(val gameID: String) : ViewEvent()
    }
}
