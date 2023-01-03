package com.inbrain.sdk.model

import android.text.TextUtils
import org.json.JSONObject

enum class SurveyOutcomeType(val type: Int) {
    Completed(0),
    Terminated(1);

    companion object {
        fun fromType(type: Int): SurveyOutcomeType {
            if (type == 0) { return Completed }
            return Terminated
        }
    }
}

class InBrainSurveyReward(var json: String) {
    var surveyId: String
    var placementId: String?
    var userReward: Double

    var outcomeType: SurveyOutcomeType
    var categories: List<SurveyCategory>? = null

    init {
        val jsonObject = JSONObject(json)
        surveyId = jsonObject.getString("surveyId")
        placementId = jsonObject.optString("placementId")
        userReward = jsonObject.getDouble("userReward")

        val outcome = jsonObject.getInt("outcomeType")
        outcomeType = SurveyOutcomeType.Companion.fromType(outcome)

        val categoryIds = jsonObject.optJSONArray("categoryIds")
        if (categoryIds != null && categoryIds.length() > 0) {
            val categories: MutableList<SurveyCategory> = ArrayList()
            for (i in 0 until categoryIds.length()) {
                val categoryId = categoryIds.getInt(i)
                val category = SurveyCategory.fromId(categoryId)
                categories.add(category)
            }
            this.categories = categories
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is InBrainSurveyReward) return false
        return TextUtils.equals(other.surveyId, surveyId) }

    override fun hashCode(): Int {
        return surveyId.hashCode()
    }
}