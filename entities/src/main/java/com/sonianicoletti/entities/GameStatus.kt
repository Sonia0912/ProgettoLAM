package com.sonianicoletti.entities

enum class GameStatus {
    LOBBY,
    ACTIVE,
    FINISHED,
    UNKNOWN;

    companion object {
        fun fromValue(value: String?) = when(value) {
            "LOBBY" -> LOBBY
            "ACTIVE" -> ACTIVE
            "FINISHED" -> FINISHED
            else -> UNKNOWN
        }
    }
}