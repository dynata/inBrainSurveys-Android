package com.inbrain.sdk.model

enum class SurveyConversionLevel(val level: Int) {
    NEW_SURVEY(0),
    VERY_POOR(1),
    POOR(2),
    FAIR(3),
    GOOD(4),
    VERY_GOOD(5),
    EXCELLENT(6);

    companion object {
        fun fromLevel(level: Int): SurveyConversionLevel? {
            for (conversionLevel in values()) {
                if (conversionLevel.level == level) {
                    return conversionLevel
                }
            }
            return null
        }
    }
}