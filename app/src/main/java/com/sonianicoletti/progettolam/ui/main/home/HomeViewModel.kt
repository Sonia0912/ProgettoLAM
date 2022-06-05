package com.sonianicoletti.progettolam.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.repositories.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val gameRepository: GameRepository) : ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    fun handleCreateGameButton() = viewModelScope.launch {
        gameRepository.createGame()
        viewEventEmitter.postValue(ViewEvent.NavigateToLobby)
    }

    fun handleJoinGameButton() {
        viewEventEmitter.value = ViewEvent.NavigateToJoinGame
    }

    fun handleProfileButton() {
        viewEventEmitter.value = ViewEvent.NavigateToProfile
    }

    sealed class ViewEvent {
        object NavigateToLobby : ViewEvent()
        object NavigateToJoinGame : ViewEvent()
        object NavigateToProfile : ViewEvent()
    }
}
