package com.example.closethub.models;

public class WalletLoginRequest {
    private String pin;

    public WalletLoginRequest() {
    }

    public WalletLoginRequest(String pin) {
        this.pin = pin;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}

