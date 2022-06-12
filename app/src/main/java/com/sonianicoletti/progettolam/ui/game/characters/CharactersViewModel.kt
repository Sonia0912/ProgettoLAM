package com.sonianicoletti.progettolam.ui.game.characters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Character
import com.sonianicoletti.entities.Game
import com.sonianicoletti.entities.GameStatus
import com.sonianicoletti.entities.exceptions.UserNotFoundException
import com.sonianicoletti.entities.exceptions.UserNotLoggedInException
import com.sonianicoletti.progettolam.R
import com.sonianicoletti.progettolam.ui.game.characters.CharactersViewModel.ViewEvent.ShowCharacterTaken
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.repositories.GameRepository
import com.sonianicoletti.usecases.servives.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharactersViewModel @Inject constructor(
    private val authService: AuthService,
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    private val charactersEmitter = MutableLiveData<List<SelectCharacterItem>>(createCharacterItems())
    val characters: LiveData<List<SelectCharacterItem>> = charactersEmitter

    // richiamata ogni volta che un giocatore seleziona un personaggio
    fun handleGameUpdate(game: Game) {
        assignPlayersSelectedCharacters(game)
        charactersEmitter.emit()
        viewModelScope.launch {
            // controllo che sia l'host e se tutti i giocatori hanno un personaggio assegnato
            if(gameRepository.isHost() && characters.value?.filter { it.assignedPlayer != null }?.size == game.players.size && game.status == GameStatus.CHARACTER_SELECT) {
                gameRepository.updateGameStatus(GameStatus.ACTIVE) // cambio lo stato del gioco
                // distribuisce le carte
                gameRepository.distributeCards()
            }
        }
        // controllo se lo stato del gioco e' Active, in tal caso navigo al prossimo fragment
        if(game.status == GameStatus.ACTIVE) {
            viewEventEmitter.postValue(ViewEvent.NavigateToCards)
        }
    }

    private fun assignPlayersSelectedCharacters(game: Game) {
        characters.value?.forEach { characterItem ->
            val player = game.players.find { it.character == characterItem.character }
            characterItem.assignedPlayer = player
        }
    }

    fun selectCharacter(character: Character) = viewModelScope.launch {
        handleSelectedCharacter(character)
    }

    private suspend fun handleSelectedCharacter(character: Character) {
        val user = authService.getUser() ?: throw UserNotFoundException()
        when {
            isCharacterSelected(user.id, character) -> gameRepository.updateCharacter(user.id, Character.UNSELECTED)
            isCharacterTaken(character) -> viewEventEmitter.postValue(ShowCharacterTaken(character))
            else -> gameRepository.updateCharacter(user.id, character)
        }
    }

    private fun isCharacterSelected(userID: String, character: Character) =
        characters.value?.find { it.assignedPlayer?.id == userID }?.character == character

    private fun isCharacterTaken(character: Character) =
        characters.value?.find { it.character == character }?.assignedPlayer != null

    private fun MutableLiveData<List<SelectCharacterItem>>.emit() = postValue(value)

    private fun createCharacterItems() = mutableListOf(
        SelectCharacterItem(Character.PEACOCK, R.drawable.mrs_peacock, null),
        SelectCharacterItem(Character.MUSTARD, R.drawable.colonel_mustard, null),
        SelectCharacterItem(Character.SCARLETT, R.drawable.miss_scarlett, null),
        SelectCharacterItem(Character.PLUM, R.drawable.professor_plum, null),
        SelectCharacterItem(Character.WHITE, R.drawable.mrs_white, null),
        SelectCharacterItem(Character.GREEN, R.drawable.rev_green, null),
    )

    sealed class ViewEvent {
        data class ShowCharacterTaken(val character: Character) : ViewEvent()
        object ShowUserNotLoggedInToast : ViewEvent()
        object ShowGameNotRunningToast : ViewEvent()
        object NavigateToAuth : ViewEvent()
        object NavigateToMain : ViewEvent()
        object NavigateToCards : ViewEvent()
    }
}
