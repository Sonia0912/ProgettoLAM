package com.sonianicoletti.progettolam.ui.game.characters

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.entities.Character
import com.sonianicoletti.entities.exceptions.UserNotFoundException
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.repositories.GameRepository
import com.sonianicoletti.usecases.servives.AuthService
import com.sonianicoletti.usecases.servives.GameService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharactersViewModel @Inject constructor(
    private val gameService: GameService,
    private val authService: AuthService,
    private val gameRepository: GameRepository,
) : ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    fun selectCharacter(character: Character) = viewModelScope.launch {
        try {
            val user = authService.getUser() ?: throw UserNotFoundException()
            gameService.chooseCharacter(user.id, character)
        } catch (e: UserNotFoundException) {
            viewEventEmitter.postValue(ViewEvent.ShowUserNotFoundAlert)
        }
    }

    sealed class ViewEvent {
        object ShowUserNotFoundAlert : ViewEvent()
    }
}
