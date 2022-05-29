package com.sonianicoletti.progettolam.ui.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.servives.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val authService: AuthService) : ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<ProfileViewModel.ViewEvent>()
    val viewEvent: LiveData<ProfileViewModel.ViewEvent> = viewEventEmitter

    fun handleLogOut() {
        authService.signOut()
        viewEventEmitter.postValue(ViewEvent.navigateToLogin)
    }

    sealed class ViewEvent {
        object navigateToLogin : ProfileViewModel.ViewEvent()
    }

}