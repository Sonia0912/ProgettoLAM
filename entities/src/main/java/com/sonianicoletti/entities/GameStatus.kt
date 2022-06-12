package com.sonianicoletti.entities

enum class GameStatus {
    LOBBY,
    CHARACTER_SELECT,
    ACTIVE,
    FINISHED,
    CANCELLED;

    companion object {
        fun fromValue(value: String?) = when (value) {
            "LOBBY" -> LOBBY
            "CHARACTER_SELECT" -> CHARACTER_SELECT
            "ACTIVE" -> ACTIVE
            "FINISHED" -> FINISHED
            else -> CANCELLED
        }
    }
}