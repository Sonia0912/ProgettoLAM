package com.sonianicoletti.progettolam.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.progettolam.ui.main.MainViewModel.ViewEvent.NavigateToAuth
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.servives.AuthService
import com.sonianicoletti.usecases.servives.InvitesService
import com.sonianicoletti.usecases.utils.MessagingTokenRefresher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authService: AuthService,
    private val messagingTokenRefresher: MessagingTokenRefresher,
    private val inviteService: InvitesService
) : ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    init {
        refreshMessagingToken()
        listenForInvites()
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

    private fun listenForInvites() = viewModelScope.launch {
        inviteService.listenForInvites().collect {
            viewEventEmitter.postValue(ViewEvent.InvitationReceived(it.inviter, it.gameID))
        }
    }

    sealed class ViewEvent {
        object NavigateToAuth : ViewEvent()
        data class InvitationReceived(val inviter: String, val gameID: String) : ViewEvent()
    }
}
