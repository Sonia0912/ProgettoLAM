package com.sonianicoletti.progettolam.ui.game.characters

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Character
import com.sonianicoletti.entities.exceptions.GameNotRunningException
import com.sonianicoletti.entities.exceptions.UserNotFoundException
import com.sonianicoletti.progettolam.R
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

    private val selectedCharactersEmitter = MutableLiveData<List<SelectCharacterItem>>(createCharacterItems())
    val selectedCharacters: LiveData<List<SelectCharacterItem>> = selectedCharactersEmitter

    init {
        observeGameUpdates()
    }

    fun selectCharacter(character: Character) = viewModelScope.launch {
        try {
            val user = authService.getUser() ?: throw UserNotFoundException()
            when {
                isCharacterSelected(user.id, character) -> gameRepository.updateCharacter(user.id, Character.UNSELECTED)
                isCharacterTaken(character) -> viewEventEmitter.postValue(ViewEvent.ShowCharacterTaken(character))
                else -> gameRepository.updateCharacter(user.id, character)
            }
        } catch (e: UserNotFoundException) {
            viewEventEmitter.postValue(ViewEvent.ShowUserNotFoundAlert)
        } catch (e: GameNotRunningException) {
            viewEventEmitter.postValue(ViewEvent.ShowGameNotRunningAlert)
        }
    }

    private fun isCharacterSelected(userID: String, character: Character) = selectedCharacters.value?.find { it.assignedPlayer?.id == userID }?.character == character

    private fun isCharacterTaken(character: Character) = selectedCharacters.value?.find { it.character == character }?.assignedPlayer != null

    private fun observeGameUpdates() = viewModelScope.launch {
        gameRepository.getOngoingGameUpdates().collect { game ->
            selectedCharacters.value?.forEach { characterItem ->
                val player = game.players.find { it.character == characterItem.character }
                characterItem.assignedPlayer = player
            }
            selectedCharactersEmitter.emit()
        }
    }

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
        object ShowUserNotFoundAlert : ViewEvent()
        object ShowGameNotRunningAlert : ViewEvent()
        data class ShowCharacterTaken(val character: Character) : ViewEvent()
    }
}
