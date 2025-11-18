package com.example.closethub.models;

public class Transaction {
    private String _id;
    private String type; // "deposit" hoặc "withdraw"
    private double amount;
    private String description;
    private String created_date;
    private double balance_after; // Số dư sau giao dịch

    public Transaction() {
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

    public double getBalance_after() {
        return balance_after;
    }

    public void setBalance_after(double balance_after) {
        this.balance_after = balance_after;
    }
}

