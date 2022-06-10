package com.sonianicoletti.progettolam

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
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
        try {
            appScope.launch { authService.setNotificationToken(token) }
        } catch (e: Exception) {
            // do nothing
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)


    }
}
