package com.example.closethub.models;

public class WalletTransactionRequest {
    private double amount;
    private String pin;

    public WalletTransactionRequest() {
    }

    public WalletTransactionRequest(double amount, String pin) {
        this.amount = amount;
        this.pin = pin;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}

