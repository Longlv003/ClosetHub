package com.example.closethub.models;

public class CartLookUpProduct {
    private String _id;
    private String id_user;
    private Product id_product;
    private Variant id_variant;
    private int quantity;

    public Variant getId_variant() {
        return id_variant;
    }

    public void setId_variant(Variant id_variant) {
        this.id_variant = id_variant;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public Product getId_product() {
        return id_product;
    }

    public void setId_product(Product id_product) {
        this.id_product = id_product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
