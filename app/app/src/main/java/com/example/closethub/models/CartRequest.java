package com.example.closethub.models;

public class CartRequest {
    private String id_user;
    private String id_product;
    private String id_variant;
    private int quantity;

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getId_product() {
        return id_product;
    }

    public void setId_product(String id_product) {
        this.id_product = id_product;
    }

    public String getId_variant() {
        return id_variant;
    }

    public void setId_variant(String id_variant) {
        this.id_variant = id_variant;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
