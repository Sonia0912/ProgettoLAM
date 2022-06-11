package com.sonianicoletti.entities

enum class GameStatus {
    LOBBY,
    CHARACTER_SELECT,
    ACTIVE,
    SHOW_CARD,
    FINISHED,
    CANCELLED;

    companion object {
        fun fromValue(value: String?) = when (value) {
            "LOBBY" -> LOBBY
            "CHARACTER_SELECT" -> CHARACTER_SELECT
            "ACTIVE" -> ACTIVE
            "SHOW_CARD" -> SHOW_CARD
            "FINISHED" -> FINISHED
            else -> CANCELLED
        }
    }
}