package com.sonianicoletti.progettolam.ui.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.servives.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val authService: AuthService) : ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<ProfileViewModel.ViewEvent>()
    val viewEvent: LiveData<ProfileViewModel.ViewEvent> = viewEventEmitter

    fun handleLogOut() {
        viewModelScope.launch {
            authService.signOut()
            viewEventEmitter.postValue(ViewEvent.NavigateToLogin)
        }
    }

    sealed class ViewEvent {
        object NavigateToLogin : ProfileViewModel.ViewEvent()
    }

}
