package com.sonianicoletti.progettolam.ui.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.servives.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val authService: AuthService) : ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<LoginViewModel.ViewEvent>()
    val viewEvent: LiveData<LoginViewModel.ViewEvent> = viewEventEmitter

    init {
        viewModelScope.launch {
            // se l'utente ha gia' fatto il login va direttamente alla MainActivity
            if(authService.getUser() != null) {
                viewEventEmitter.postValue(ViewEvent.navigateToMain)
            }
        }
    }

    fun handleLoginButton(email: String, password: String) {
        // inizio della coroutine
        viewModelScope.launch {
            try {
                authService.signIn(email, password)
                viewEventEmitter.postValue(ViewEvent.navigateToMain)
            } catch(e: Exception) {
                viewEventEmitter.value = LoginViewModel.ViewEvent.handleWrongCredentials
            }
        }
    }

    sealed class ViewEvent {
        object handleWrongCredentials : LoginViewModel.ViewEvent()
        object navigateToMain : LoginViewModel.ViewEvent()
    }


}