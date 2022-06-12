package com.sonianicoletti.progettolam.ui.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.servives.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val authService: AuthService) : ViewModel() {

    private val viewStateEmitter = MutableLiveData<ViewState>(ViewState.Idle)
    val viewState: LiveData<ViewState> = viewStateEmitter

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    fun handleRegistration(email: String, displayName: String, password: String) {
        viewModelScope.launch {
            try {
                viewStateEmitter.postValue(ViewState.Idle)
                authService.register(email, password, displayName)
                viewEventEmitter.postValue(ViewEvent.NavigateToMain)
            } catch (e: FirebaseAuthWeakPasswordException) {
                viewEventEmitter.value = ViewEvent.HandleTakenUsername(e.reason ?: "Unknown error")
            } catch (e: Exception) {
                viewEventEmitter.value = ViewEvent.HandleTakenUsername(e.message ?: "Unknown error")
            } finally {
                viewStateEmitter.postValue(ViewState.Idle)
            }
        }
    }

    sealed class ViewState {
        object Loading : ViewState()
        object Idle : ViewState()
    }

    sealed class ViewEvent {
        data class HandleTakenUsername(val errorMessage: String) : ViewEvent()
        object NavigateToMain: ViewEvent()
    }
}