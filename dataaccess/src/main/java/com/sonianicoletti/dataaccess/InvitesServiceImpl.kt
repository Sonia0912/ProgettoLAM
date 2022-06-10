package com.sonianicoletti.dataaccess

import com.sonianicoletti.entities.Invitation
import com.sonianicoletti.entities.User
import com.sonianicoletti.usecases.servives.InvitesService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import javax.inject.Inject

class InvitesServiceImpl @Inject constructor() : InvitesService {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val messagingApi = Retrofit.Builder()
        .baseUrl("https://fcm.googleapis.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create<MessagingApi>()

    private val invitesFlow = MutableSharedFlow<Invitation>()

    override suspend fun sendInvite(gameID: String, inviter: String, targetUser: User) {
        targetUser.messagingToken?.let { token ->
            val messageBody = MessageRequest(
                to = token,
                notification = NotificationBody(
                    "$inviter has invited you to a game",
                    "Click here to join"
                ),
                data = InviteData(gameID = gameID, inviter = inviter)
            )

            messagingApi.send(
                authHeader = "key=$FCM_SERVER_KEY",
                body = messageBody
            )
        }
    }

    override suspend fun listenForInvites(): Flow<Invitation> {
        return invitesFlow
    }

    override suspend fun onInviteReceived(invite: Invitation) {
        invitesFlow.emit(invite)
    }

    companion object {
        private const val FCM_SERVER_KEY = "AAAAk6-GUls:APA91bF41PxVBCkK4wWoJl2YHNUB3fTbjs5UCna-xz080SKt7NPKTWNHjfYer5tyoKZOcOiG_pBtKIuiuRYcODczjkdTbLHd7t_-WsciZcJwFwv6eG2BwLtJR6zpRkNnZPPEBiR0U8WR"
    }
}
