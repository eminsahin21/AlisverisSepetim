package com.example.alisverissepetim.model;

public class Product {
    private String name;
    private String imageUrl;
    private String category;

    public Product(String name, String imageUrl, String category) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategory() {
        return category;
    }
}
