package com.sonianicoletti.entities

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val messagingToken: String?,
)