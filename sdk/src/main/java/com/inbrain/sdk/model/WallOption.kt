package com.inbrain.sdk.model

enum class WallOption(val raw: Int) {
    ALL(0),
    SURVEYS(1),
    OFFERS(2);

    companion object {
        fun fromRaw(raw: Int): WallOption? {
            return values().find { it.raw == raw }
        }
    }
}