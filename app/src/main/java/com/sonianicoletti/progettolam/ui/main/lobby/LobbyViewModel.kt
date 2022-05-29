package com.sonianicoletti.progettolam.ui.main.lobby

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.User
import com.sonianicoletti.entities.exceptions.UserNotFoundException
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.servives.GameService
import com.sonianicoletti.usecases.servives.UserService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(val gameService: GameService, val userService: UserService) : ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter
    lateinit var game: Game

    init {
        viewModelScope.launch {
            game = gameService.createGame()
            viewEventEmitter.postValue(ViewEvent.UpdatePlayersList(game.players))
        }
    }



    fun addPlayer(playerEmail : String) = viewModelScope.launch {
        if(game.players.size > 5) {
            viewEventEmitter.value = ViewEvent.OpenMaxPlayersDialog
            return@launch
        }
        try {
            val invitedPlayer = userService.getUserByEmail(playerEmail)
            // controllo che non ci sia gia' lo stesso giocatore
            if(game.players.any { it.id == invitedPlayer.id }) {
                viewEventEmitter.value = ViewEvent.DuplicatePlayerAlert
                return@launch
            }
            addPlayerToGame(invitedPlayer)
        } catch(e: UserNotFoundException) {
            viewEventEmitter.value = ViewEvent.NotFoundUserAlert
        }
    }

    private suspend fun addPlayerToGame(player: User) {
        val newGamePlayers = mutableListOf<User>().apply {
            addAll(game.players)
            add(player)
        }
        game = game.copy(players = newGamePlayers)
        gameService.updateGamePlayers(game.id, newGamePlayers)
        // switcha al main thread (per settare i valori di viewEvent dal main thread)
        // non uso postValue() perche' e' asincrono e ne farebbe solo uno dei due
        withContext(Dispatchers.Main) {
            viewEventEmitter.value = ViewEvent.UpdatePlayersList(newGamePlayers)
            viewEventEmitter.value = ViewEvent.ClearText
        }
    }

    // rappresenta una ristretta gerarchia di classe per fornire piu' controllo sull'ereditarieta'
    // vogliamo che tutto quello che c'e' dentro la classe erediti dal tipo originale
    sealed class ViewEvent {
        object OpenMaxPlayersDialog : ViewEvent()
        object NotFoundUserAlert : ViewEvent()
        object DuplicatePlayerAlert: ViewEvent()
        class UpdatePlayersList(val players : List<User>) : ViewEvent()
        object ClearText : ViewEvent()
    }

}
