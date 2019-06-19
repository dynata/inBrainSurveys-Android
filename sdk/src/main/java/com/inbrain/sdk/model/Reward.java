package com.inbrain.sdk.model;

public class Reward {
    long transactionId;
    float amount;
    String currency;
    int transactionType;

    public Reward(long transactionId, double amount, String currency, int transactionType) {
        this.transactionId = transactionId;
        this.amount = (float) amount;
        this.currency = currency;
        this.transactionType = transactionType;
    }
}