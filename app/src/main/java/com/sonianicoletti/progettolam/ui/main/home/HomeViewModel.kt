package com.sonianicoletti.progettolam.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.repositories.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val gameRepository: GameRepository) : ViewModel() {

    private val viewStateEmitter = MutableLiveData<ViewState>(ViewState.Idle)
    val viewState: LiveData<ViewState> = viewStateEmitter

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    fun handleCreateGameButton() = viewModelScope.launch {
        try {
            viewStateEmitter.postValue(ViewState.Loading)
            gameRepository.createGame()
            viewEventEmitter.postValue(ViewEvent.NavigateToLobby)
        } catch (e: Exception) {
            e.printStackTrace()
            viewEventEmitter.postValue(ViewEvent.ShowGeneralErrorDialog)
        } finally {
            viewStateEmitter.postValue(ViewState.Idle)
        }
    }

    fun handleJoinGameButton() {
        viewEventEmitter.value = ViewEvent.NavigateToJoinGame
    }

    fun handleProfileButton() {
        viewEventEmitter.value = ViewEvent.NavigateToProfile
    }

    fun handleRulesButton() {
        viewEventEmitter.value = ViewEvent.NavigateToRules
    }

    sealed class ViewState {
        object Loading : ViewState()
        object Idle : ViewState()
    }

    sealed class ViewEvent {
        object NavigateToLobby : ViewEvent()
        object NavigateToJoinGame : ViewEvent()
        object NavigateToProfile : ViewEvent()
        object NavigateToRules : ViewEvent()
        object ShowGeneralErrorDialog : ViewEvent()
    }
}
