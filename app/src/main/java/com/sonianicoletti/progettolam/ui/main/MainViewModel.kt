package com.sonianicoletti.progettolam.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonianicoletti.progettolam.ui.main.MainViewModel.ViewEvent.NavigateToAuth
import com.sonianicoletti.progettolam.util.MutableSingleLiveEvent
import com.sonianicoletti.usecases.servives.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    private val viewEventEmitter = MutableSingleLiveEvent<ViewEvent>()
    val viewEvent: LiveData<ViewEvent> = viewEventEmitter

    fun checkUserLoggedIn() = viewModelScope.launch {
        val user = authService.getUser()
        if (user == null) {
            viewEventEmitter.postValue(NavigateToAuth)
        }
    }

    private suspend fun handleUserNotLoggedIn() = withContext(Dispatchers.Main) {
        viewEventEmitter.value = NavigateToAuth
    }

    sealed class ViewEvent {
        object NavigateToAuth : ViewEvent()
    }
}
