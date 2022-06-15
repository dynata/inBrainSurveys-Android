package com.inbrain.sdk.model;

import android.text.TextUtils;

import java.io.Serializable;

public class CurrencySale implements Serializable {
    public String startOn;
    public String endOn;
    public String description;
    public float multiplier;

    public CurrencySale(String startOn, String endOn, String description, float multiplier) {
        this.startOn = startOn;
        this.endOn = endOn;
        this.description = description;
        this.multiplier = multiplier;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CurrencySale))
            return false;

        CurrencySale other = (CurrencySale) obj;
        return TextUtils.equals(other.startOn, startOn) &&
                TextUtils.equals(other.endOn, endOn) &&
                TextUtils.equals(other.description, description) &&
                other.multiplier == multiplier;
    }

    @Override
    public int hashCode() {
        return startOn.concat(endOn).concat(description).hashCode();
    }
}