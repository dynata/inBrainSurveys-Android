package com.inbrain.sdk.model;

public class Reward {
    public long transactionId;
    public float amount;
    public String currency;
    public int transactionType;

    public Reward(long transactionId, double amount, String currency, int transactionType) {
        this.transactionId = transactionId;
        this.amount = (float) amount;
        this.currency = currency;
        this.transactionType = transactionType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Reward) {
            Reward reward = (Reward) obj;
            return reward.transactionId == transactionId &&
                    reward.amount == amount &&
                    reward.currency.equals(currency) &&
                    reward.transactionType == transactionType;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Long.valueOf(transactionId).hashCode();
    }
}