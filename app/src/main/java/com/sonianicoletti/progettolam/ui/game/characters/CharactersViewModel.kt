package com.sonianicoletti.progettolam.ui.game.characters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Character
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.GameStatus
import com.sonianicoletti.entities.exceptions.UserNotFoundException
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.extension.emit
import com.sonianicoletti.progettolam.ui.game.characters.CharactersViewModel.ViewEvent.ShowCharacterTaken
import com.sonianicoletti.progettolam.ui.game.characters.CharactersViewModel.ViewState.*
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.repositories.GameRepository
import com.sonianicoletti.usecases.servives.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharactersViewModel @Inject constructor(
    private val authService: AuthService,
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    private val viewStateEmitter = MutableLiveData<ViewState>(Data(createCharacterItems()))
    val viewState: LiveData<ViewState> = viewStateEmitter

    // richiamata ogni volta che un giocatore seleziona un personaggio
    fun handleGameUpdate(game: Game) {
        assignPlayersSelectedCharacters(game)
        viewStateEmitter.emit()
        viewModelScope.launch(Dispatchers.IO) {
            // controllo che sia l'host e se tutti i giocatori hanno un personaggio assegnato
            if (areAllCharactersSelected(game)) {
                viewStateEmitter.postValue(Loading)

                if (gameRepository.isHost()) {
                    gameRepository.updateGameStatus(GameStatus.ACTIVE) // cambio lo stato del gioco
                    // distribuisce le carte
                    gameRepository.distributeCards()
                }
            }

        }
        // controllo se lo stato del gioco e' Active, in tal caso navigo al prossimo fragment
        if(game.status == GameStatus.ACTIVE) {
            viewEventEmitter.postValue(ViewEvent.NavigateToCards)
        }
    }

    private fun areAllCharactersSelected(game: Game): Boolean {
        return (viewState.value as? Data)?.characters?.filter { it.assignedPlayer != null }?.size == game.players.size
                && game.status == GameStatus.CHARACTER_SELECT
    }

    private fun assignPlayersSelectedCharacters(game: Game) {
        (viewState.value as? Data)?.characters?.forEach { characterItem ->
            val player = game.players.find { it.character == characterItem.character }
            characterItem.assignedPlayer = player
        }
    }

    fun selectCharacter(character: Character) = viewModelScope.launch(Dispatchers.IO) {
        val game = gameRepository.getOngoingGame()
        val user = authService.getUser() ?: throw UserNotFoundException()
        if (game.players.find { it.id == user.id }?.character != Character.UNSELECTED) {
            return@launch
        }

        when {
            isCharacterSelected(user.id, character) -> {
                gameRepository.updateCharacter(user.id, Character.UNSELECTED)
            }
            isCharacterTaken(character) -> {
                viewEventEmitter.postValue(ShowCharacterTaken(character))
            }
            else -> {
                game.players.first { it.id == user.id }.character = character
                viewStateEmitter.emit()
                gameRepository.updateCharacter(user.id, character)
            }
        }
    }

    private fun isCharacterSelected(userID: String, character: Character) =
        (viewState.value as? Data)?.characters?.find { it.assignedPlayer?.id == userID }?.character == character

    private fun isCharacterTaken(character: Character) =
        (viewState.value as? Data)?.characters?.find { it.character == character }?.assignedPlayer != null

    private fun MutableLiveData<List<SelectCharacterItem>>.emit() = postValue(value)

    private fun createCharacterItems() = mutableListOf(
        SelectCharacterItem(Character.PEACOCK, R.drawable.mrs_peacock, null),
        SelectCharacterItem(Character.MUSTARD, R.drawable.colonel_mustard, null),
        SelectCharacterItem(Character.SCARLETT, R.drawable.miss_scarlett, null),
        SelectCharacterItem(Character.PLUM, R.drawable.professor_plum, null),
        SelectCharacterItem(Character.WHITE, R.drawable.mrs_white, null),
        SelectCharacterItem(Character.GREEN, R.drawable.rev_green, null),
    )

    sealed class ViewState {
        object Loading : ViewState()
        data class Data(val characters: List<SelectCharacterItem>) : ViewState()
    }

    sealed class ViewEvent {
        data class ShowCharacterTaken(val character: Character) : ViewEvent()
        object ShowUserNotLoggedInToast : ViewEvent()
        object ShowGameNotRunningToast : ViewEvent()
        object NavigateToAuth : ViewEvent()
        object NavigateToMain : ViewEvent()
        object NavigateToCards : ViewEvent()
    }
}
