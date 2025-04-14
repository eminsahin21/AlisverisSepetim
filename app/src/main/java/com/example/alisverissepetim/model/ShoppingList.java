package com.example.alisverissepetim.model;

public class ShoppingList {

    private String basketName;
    private String basketTur;

    public String getBasketName() {
        return basketName;
    }

    public void setBasketName(String basketName) {
        this.basketName = basketName;
    }

    public String getBasketTur() {
        return basketTur;
    }

    public void setBasketTur(String basketTur) {
        this.basketTur = basketTur;
    }

    public ShoppingList(String basketName, String basketTur) {
        this.basketName = basketName;
        this.basketTur = basketTur;
    }
}
