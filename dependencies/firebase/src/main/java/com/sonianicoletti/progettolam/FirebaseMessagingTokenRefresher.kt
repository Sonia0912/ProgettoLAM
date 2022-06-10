package com.sonianicoletti.progettolam

import com.google.firebase.messaging.FirebaseMessaging
import com.sonianicoletti.usecases.servives.AuthService
import com.sonianicoletti.usecases.utils.MessagingTokenRefresher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseMessagingTokenRefresher @Inject constructor(
    private val authService: AuthService,
    private val appScope: CoroutineScope,
) : MessagingTokenRefresher {

    override fun refresh() {
        appScope.launch {
            val token = FirebaseMessaging.getInstance().token.await()
            authService.setNotificationToken(token)
        }
    }
}