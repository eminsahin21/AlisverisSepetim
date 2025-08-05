package com.example.alisverissepetim.manager;

import com.example.alisverissepetim.model.Product;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartManager {
    private static CartManager instance;
    // Anahtar: alışveriş listesi adı, Değer: o listeye ait sepet
    private Map<String, Map<String, CartItem>> allCarts;

    private CartManager() {
        allCarts = new HashMap<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    // Sepete ürün ekle
    // Yeni parametre: sepet adı
    public void addProduct(String shoppingListName, Product product) {
        // Sepet yoksa oluştur
        allCarts.computeIfAbsent(shoppingListName, k -> new HashMap<>());
        Map<String, CartItem> cartItems = allCarts.get(shoppingListName);

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
    public void increaseQuantity(String shoppingListName, String productKey) {
        Map<String, CartItem> cartItems = allCarts.get(shoppingListName);
        if (cartItems != null && cartItems.containsKey(productKey)) {
            CartItem item = cartItems.get(productKey);
            item.setQuantity(item.getQuantity() + 1);
        }
    }

    // Ürün adedini azalt
    public void decreaseQuantity(String shoppingListName, String productKey) {
        Map<String, CartItem> cartItems = allCarts.get(shoppingListName);
        if (cartItems != null && cartItems.containsKey(productKey)) {
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
    public int getTotalItemCount(String shoppingListName) {
        Map<String, CartItem> cartItems = allCarts.get(shoppingListName);
        if (cartItems == null) {
            return 0;
        }
        int total = 0;
        for (CartItem item : cartItems.values()) {
            total += item.getQuantity();
        }
        return total;
    }

    // Sepetteki tüm ürünleri getir
    public List<CartItem> getCartItems(String shoppingListName) {
        Map<String, CartItem> cartItems = allCarts.get(shoppingListName);
        if (cartItems == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(cartItems.values());
    }

    // Sepeti temizle
    public void clearCart(String shoppingListName) {
        if (allCarts.containsKey(shoppingListName)) {
            allCarts.get(shoppingListName).clear();
        }
    }

    // Sepet boş mu kontrol et
    public boolean isCartEmpty(String shoppingListName) {
        Map<String, CartItem> cartItems = allCarts.get(shoppingListName);
        return cartItems == null || cartItems.isEmpty();
    }

    // CartItem sınıfı (değişiklik yok)
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

    public static class ShoppingListSummary {
        public String listName;
        public int itemCount;

        public ShoppingListSummary(String listName, int itemCount) {
            this.listName = listName;
            this.itemCount = itemCount;
        }
    }

    public List<ShoppingListSummary> getAllShoppingListSummaries() {
        List<ShoppingListSummary> summaries = new ArrayList<>();
        for (String listName : allCarts.keySet()) {
            summaries.add(new ShoppingListSummary(listName, getTotalItemCount(listName)));
        }
        return summaries;
    }
}

