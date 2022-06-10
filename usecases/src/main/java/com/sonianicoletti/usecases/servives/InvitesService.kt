package com.sonianicoletti.usecases.servives

import com.sonianicoletti.entities.Invitation
import com.sonianicoletti.entities.User
import kotlinx.coroutines.flow.Flow

interface InvitesService {

    suspend fun sendInvite(gameID: String, inviter: String, targetUser: User)

    suspend fun listenForInvites() : Flow<Invitation>

    suspend fun onInviteReceived(invite: Invitation)
}