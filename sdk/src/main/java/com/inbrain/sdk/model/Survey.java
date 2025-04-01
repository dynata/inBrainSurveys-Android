package com.inbrain.sdk.model;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.List;

public class Survey implements Serializable {
    public String id;
    public long rank;
    public long time;
    public float value;
    public boolean currencySale;
    public float multiplier;
    public SurveyConversionLevel conversionLevel;
    public String searchId;
    public List<SurveyCategory> categories;
    public boolean isProfilerSurvey;

    public Survey(String id, long rank, long time, float value, boolean currencySale, float multiplier, int convThreshold,
                  String searchId, List<SurveyCategory> categories, boolean isProfilerSurvey) {
        this.id = id;
        this.rank = rank;
        this.time = time;
        this.value = value;
        this.currencySale = currencySale;
        this.multiplier = multiplier;
        this.conversionLevel = SurveyConversionLevel.Companion.fromLevel(convThreshold);
        this.categories = categories;
        this.searchId = searchId;
        this.isProfilerSurvey = isProfilerSurvey;
    }

    public Survey(String id, long rank, long time, float value, boolean currencySale, float multiplier, SurveyConversionLevel convLevel,
                  String searchId, List<SurveyCategory> categories, boolean isProfilerSurvey) {
        this.id = id;
        this.rank = rank;
        this.time = time;
        this.value = value;
        this.currencySale = currencySale;
        this.multiplier = multiplier;
        this.conversionLevel = convLevel;
        this.categories = categories;
        this.searchId = searchId;
        this.isProfilerSurvey = isProfilerSurvey;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Survey))
            return false;

        Survey reward = (Survey) obj;
        return TextUtils.equals(reward.id, id) &&
                reward.rank == rank &&
                reward.time == time &&
                reward.value == value &&
                reward.currencySale == currencySale &&
                reward.multiplier == multiplier &&
                reward.conversionLevel.getLevel() == conversionLevel.getLevel() &&
                reward.isProfilerSurvey == isProfilerSurvey;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}