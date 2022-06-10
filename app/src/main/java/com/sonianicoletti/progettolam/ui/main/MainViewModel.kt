package com.sonianicoletti.progettolam.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.progettolam.ui.main.MainViewModel.ViewEvent.NavigateToAuth
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.servives.AuthService
import com.sonianicoletti.usecases.utils.MessagingTokenRefresher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authService: AuthService,
    private val messagingTokenRefresher: MessagingTokenRefresher,
) : ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    init {
        refreshMessagingToken()
    }

    private fun refreshMessagingToken() {
        messagingTokenRefresher.refresh()
    }

    fun checkUserLoggedIn() = viewModelScope.launch {
        val user = authService.getUser()
        if (user == null) {
            viewEventEmitter.postValue(NavigateToAuth)
        }
    }

    sealed class ViewEvent {
        object NavigateToAuth : ViewEvent()
    }
}
