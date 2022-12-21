package com.inbrain.sdk.model

import android.text.TextUtils
import java.io.Serializable


class InBrainNativeSurvey(
    var id: String, var rank: Long, var time: Long, var value: Float, var currencySale: Boolean, var multiplier: Float, var conversionThreshold: Int,
    var searchId: String, var categories: List<SurveyCategory>
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (other !is InBrainNativeSurvey) return false
        return TextUtils.equals(
            other.id,
            id
        ) && other.rank == rank && other.time == time && other.value == value && other.currencySale == currencySale && other.multiplier == multiplier && other.conversionThreshold == conversionThreshold
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

@Deprecated(
    "This class has been renamed to InBrainNativeSurvey."
)
typealias Survey = InBrainNativeSurvey
