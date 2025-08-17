package com.example.alisverissepetim.model;

import java.util.Objects;

public class Product {
    private String id;
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

    public Product(String id, String urun_adi, String fiyat, String market_adi, String market_logo, String urun_gorsel, String kategori,int quantity) {
        this.id = id;
        this.urun_adi = urun_adi;
        this.fiyat = fiyat;
        this.market_adi = market_adi;
        this.market_logo = market_logo;
        this.urun_gorsel = urun_gorsel;
        this.kategori = kategori;
        this.quantity = 0; // Varsayılan olarak 0

    }

    public String getId() {
        // Eğer id field'ı yoksa, benzersiz bir identifier oluşturun
        if (id == null || id.isEmpty()) {
            // Ürün adı + market adı + fiyat kombinasyonu ile benzersiz ID
            return (urun_adi != null ? urun_adi : "") + "_" +
                    (market_adi != null ? market_adi : "") + "_" +
                    (fiyat != null ? fiyat : "");
        }
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

    // equals() metodunu override edin - DiffUtil için gerekli
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;

        return Objects.equals(getId(), product.getId()) &&
                Objects.equals(urun_adi, product.urun_adi) &&
                Objects.equals(fiyat, product.fiyat) &&
                Objects.equals(market_adi, product.market_adi) &&
                Objects.equals(kategori, product.kategori);
    }

    // hashCode() metodunu da override edin
    @Override
    public int hashCode() {
        return Objects.hash(getId(), urun_adi, fiyat, market_adi, kategori);
    }

    // toString() metodu - debug için yararlı
    @Override
    public String toString() {
        return "Product{" +
                "id='" + getId() + '\'' +
                ", urun_adi='" + urun_adi + '\'' +
                ", fiyat='" + fiyat + '\'' +
                ", market_adi='" + market_adi + '\'' +
                ", kategori='" + kategori + '\'' +
                '}';
    }
}
