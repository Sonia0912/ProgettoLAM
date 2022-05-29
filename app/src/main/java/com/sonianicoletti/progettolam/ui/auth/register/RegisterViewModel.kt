package com.sonianicoletti.progettolam.ui.auth.register

import androidx.lifecycle.LiveData
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

    private val viewEventEmitter = MutableSingleLiveEvent<RegisterViewModel.ViewEvent>()
    val viewEvent: LiveData<RegisterViewModel.ViewEvent> = viewEventEmitter

    fun handleRegistration(email: String, displayName: String, password: String) {
        viewModelScope.launch {
            try {
                authService.register(email, password, displayName)
                //authService.signIn(email, password)
                viewEventEmitter.postValue(ViewEvent.navigateToMain)
            } catch (e: FirebaseAuthWeakPasswordException) {
                viewEventEmitter.value = RegisterViewModel.ViewEvent.handleTakenUsername(e.reason ?: "Unknow error")
            } catch (e: Exception) {
                viewEventEmitter.value = RegisterViewModel.ViewEvent.handleTakenUsername(e.message ?: "Unknow error")
            }
        }
    }

    sealed class ViewEvent {
        data class handleTakenUsername(val errorMessage: String) : RegisterViewModel.ViewEvent()
        object navigateToMain: RegisterViewModel.ViewEvent()
    }


}