package com.sonianicoletti.progettolam

import com.google.firebase.messaging.FirebaseMessagingService
import com.sonianicoletti.usecases.servives.AuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class FirebaseMessagingServiceImpl: FirebaseMessagingService() {

    @Inject
    lateinit var appScope: CoroutineScope

    @Inject
    lateinit var authService: AuthService

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        appScope.launch { authService.setNotificationToken(token) }
    }
}
