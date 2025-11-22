package com.example.closethub.models;

public class WalletRequest {
    private String pin;

    public WalletRequest() {
    }

    public WalletRequest(String pin) {
        this.pin = pin;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}

