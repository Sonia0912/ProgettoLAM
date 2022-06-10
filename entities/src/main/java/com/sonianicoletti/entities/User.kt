package com.sonianicoletti.entities

data class User(
    val id: String,
    val email: String,
    var displayName: String,
    val messagingToken: String?,
)