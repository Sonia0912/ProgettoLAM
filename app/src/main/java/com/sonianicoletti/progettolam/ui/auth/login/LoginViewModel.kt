package com.sonianicoletti.progettolam.ui.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.servives.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val authService: AuthService) : ViewModel() {

    private val viewStateEmitter = MutableLiveData<ViewState>(ViewState.Idle)
    val viewState: LiveData<ViewState> = viewStateEmitter

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    init {
        viewModelScope.launch {
            // se l'utente ha gia' fatto il login va direttamente alla MainActivity
            if(authService.getUser() != null) {
                viewEventEmitter.postValue(ViewEvent.NavigateToMain)
            }
        }
    }

    fun handleLoginButton(email: String, password: String) {
        // inizio della coroutine
        viewModelScope.launch {
            try {
                viewStateEmitter.postValue(ViewState.Loading)
                authService.signIn(email, password)
                viewEventEmitter.postValue(ViewEvent.NavigateToMain)
            } catch(e: Exception) {
                viewEventEmitter.value = ViewEvent.HandleWrongCredentials
            } finally {
                viewStateEmitter.value = ViewState.Idle
            }
        }
    }

    sealed class ViewState {
        object Loading : ViewState()
        object Idle : ViewState()
    }

    sealed class ViewEvent {
        object HandleWrongCredentials : ViewEvent()
        object NavigateToMain : ViewEvent()
    }
}