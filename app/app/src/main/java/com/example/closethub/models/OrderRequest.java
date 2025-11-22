package com.example.closethub.models;

public class OrderRequest {
    private String id_user;
    private String address;

    public OrderRequest() {
    }

    public OrderRequest(String id_user, String address) {
        this.id_user = id_user;
        this.address = address;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

