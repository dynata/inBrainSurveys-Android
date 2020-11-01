package com.inbrain.sdk.model;

import android.text.TextUtils;

import java.io.Serializable;

public class Reward implements Serializable {
    public long transactionId;
    public float amount;
    public String currency;
    public int transactionType;

    public Reward(long transactionId, float amount, String currency, int transactionType) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.transactionType = transactionType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Reward))
            return false;

        Reward reward = (Reward) obj;
        return reward.transactionId == transactionId &&
                reward.amount == amount &&
                TextUtils.equals(reward.currency, currency) &&
                reward.transactionType == transactionType;
    }

    @Override
    public int hashCode() {
        return (int) transactionId;
    }
}