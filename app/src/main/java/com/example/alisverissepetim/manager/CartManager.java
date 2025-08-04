package com.example.alisverissepetim.manager;

import com.example.alisverissepetim.model.Product;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartManager {
    private static CartManager instance;
    private Map<String, CartItem> cartItems;

    private CartManager() {
        cartItems = new HashMap<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    // Sepete ürün ekle
    public void addProduct(Product product) {
        String productKey = product.getUrun_adi() + "_" + product.getMarket_adi();

        if (cartItems.containsKey(productKey)) {
            // Ürün zaten sepette varsa adetini artır
            CartItem item = cartItems.get(productKey);
            item.setQuantity(item.getQuantity() + 1);
        } else {
            // Yeni ürün ekle
            CartItem newItem = new CartItem(product, 1);
            cartItems.put(productKey, newItem);
        }
    }

    // Ürün adedini artır
    public void increaseQuantity(String productKey) {
        if (cartItems.containsKey(productKey)) {
            CartItem item = cartItems.get(productKey);
            item.setQuantity(item.getQuantity() + 1);
        }
    }

    // Ürün adedini azalt
    public void decreaseQuantity(String productKey) {
        if (cartItems.containsKey(productKey)) {
            CartItem item = cartItems.get(productKey);
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
            } else {
                // Adet 1'e düştüyse ürünü sepetten çıkar
                cartItems.remove(productKey);
            }
        }
    }

    // Sepetteki toplam ürün sayısını getir
    public int getTotalItemCount() {
        int total = 0;
        for (CartItem item : cartItems.values()) {
            total += item.getQuantity();
        }
        return total;
    }

    // Sepetteki tüm ürünleri getir
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems.values());
    }

    // Sepeti temizle
    public void clearCart() {
        cartItems.clear();
    }

    // Sepet boş mu kontrol et
    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    // CartItem sınıfı
    public static class CartItem {
        private Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getProductKey() {
            return product.getUrun_adi() + "_" + product.getMarket_adi();
        }
    }
}