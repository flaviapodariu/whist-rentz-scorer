package com.example.whistrentzscorer.utils

enum class GameMode {
    WHIST,
    RENTZ;

    companion object {
        fun fromString(input: String): GameMode? {
            return when (input.uppercase().trim()) {
                "WHIST" -> WHIST
                "RENTZ" -> RENTZ
                else -> null
            }
        }
    }
}