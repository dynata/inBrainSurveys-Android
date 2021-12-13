package com.inbrain.sdk.model;

import android.text.TextUtils;

import java.io.Serializable;

public class Survey implements Serializable {
    public String id;
    public long rank;
    public long time;
    public float value;
    public boolean currencySale;
    public float multiplier;

    public Survey(String id, long rank, long time, float value, boolean currencySale, float multiplier) {
        this.id = id;
        this.rank = rank;
        this.time = time;
        this.value = value;
        this.currencySale = currencySale;
        this.multiplier = multiplier;
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
                reward.multiplier == multiplier;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}