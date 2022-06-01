package com.sonianicoletti.progettolam.ui.main.joingame

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.GameStatus
import com.sonianicoletti.entities.exceptions.GameNotFoundException
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.servives.AuthService
import com.sonianicoletti.usecases.servives.GameService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoingameViewModel @Inject constructor(
    private val gameService: GameService,
    private val authService: AuthService,
    ) :  ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    fun joinGame(gameID : String) {
        viewModelScope.launch {
            if (checkGameAvailability(gameID)) {
                // Add the current user to the game's players in firestore
                // navigate to LobbyFragment
                authService.getUser()?.let { user ->
                    val game = gameService.getGameByID(gameID)
                    game.players.add(user)
                    gameService.updateGame(game)
                    viewEventEmitter.postValue(ViewEvent.NavigateToLobby(game.id))
                }
            } else {
                viewEventEmitter.postValue(ViewEvent.GameNotFoundAlert)
            }
        }
    }

    private suspend fun checkGameAvailability(gameID: String) : Boolean {
        // controlla che l'ID esista
        var gameToJoin : Game? = null
        try {
            gameToJoin = gameService.getGameByID(gameID)
        } catch(e: GameNotFoundException) {
            return false
        }
        // controlla che la partita non sia terminata o in gioco e  che ci siano meno di 6 giocatori
        return (gameToJoin.players.size <= 5) && (gameToJoin.status == GameStatus.LOBBY)
    }

    sealed class ViewEvent {
        object GameNotFoundAlert : ViewEvent()
        data class NavigateToLobby(val gameID: String) : ViewEvent()
    }

}