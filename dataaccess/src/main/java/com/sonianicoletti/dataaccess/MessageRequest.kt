package com.sonianicoletti.dataaccess

data class MessageRequest(
    val to: String,
    val notification: NotificationBody,
    val data: InviteData,
)

data class NotificationBody(
    val title: String,
    val body: String,
)

data class InviteData(
    val gameID: String,
    val inviter: String
)
