package com.example.closethub.models;

import com.google.gson.annotations.SerializedName;

public class Cart {
    @SerializedName("_id")
    private String id;

    @SerializedName("id_user")
    private String idUser;

    @SerializedName("id_product")
    private String idProduct;

    @SerializedName("id_variant")
    private String idVariant;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("added_date")
    private String addedDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(String idProduct) {
        this.idProduct = idProduct;
    }

    public String getIdVariant() {
        return idVariant;
    }

    public void setIdVariant(String idVariant) {
        this.idVariant = idVariant;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(String addedDate) {
        this.addedDate = addedDate;
    }
}
