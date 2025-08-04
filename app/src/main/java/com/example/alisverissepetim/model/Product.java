package com.example.alisverissepetim.model;

public class Product {
    private int id;
    private String urun_adi;
    private String fiyat;
    private String market_adi;
    private String market_logo;
    private String urun_gorsel;
    private String kategori;
    private int quantity = 0;

    // Boş constructor Retrofit ve Gson için gerekli olabilir
    public Product() {

    }

    public Product(int id, String urun_adi, String fiyat, String market_adi, String market_logo, String urun_gorsel, String kategori,int quantity) {
        this.id = id;
        this.urun_adi = urun_adi;
        this.fiyat = fiyat;
        this.market_adi = market_adi;
        this.market_logo = market_logo;
        this.urun_gorsel = urun_gorsel;
        this.kategori = kategori;
        this.quantity = 0; // Varsayılan olarak 0

    }

    public int getId() {
        return id;
    }

    public String getUrun_adi() {
        return urun_adi;
    }

    public String getFiyat() {
        return fiyat;
    }

    public String getMarket_adi() {
        return market_adi;
    }

    public String getMarket_logo() {
        return market_logo;
    }

    public String getUrun_gorsel() {
        return urun_gorsel;
    }

    public String getKategori() {
        return kategori;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
