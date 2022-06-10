package com.sonianicoletti.usecases.servives

import com.sonianicoletti.entities.User

interface InvitesService {

    suspend fun sendInvite(gameID: String, inviter: String, targetUser: User)
}