package com.sonianicoletti.progettolam

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sonianicoletti.entities.Invitation
import com.sonianicoletti.usecases.servives.AuthService
import com.sonianicoletti.usecases.servives.InvitesService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessagingServiceImpl: FirebaseMessagingService() {

    @Inject
    lateinit var appScope: CoroutineScope

    @Inject
    lateinit var authService: AuthService

    @Inject
    lateinit var invitesService: InvitesService

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
        val invite = Invitation(message.data["gameID"].toString(), message.data["inviter"].toString())
        invitesService.onInviteReceived(invite)
    }
}
