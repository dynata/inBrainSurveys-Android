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
}