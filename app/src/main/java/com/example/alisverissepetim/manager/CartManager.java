package com.example.alisverissepetim.manager;

import android.content.Context;
import android.util.Log;

import com.example.alisverissepetim.model.Product;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.alisverissepetim.utils.CartPreferences;

public class CartManager {
    private static CartManager instance;
    // Anahtar: alışveriş listesi adı, Değer: o listeye ait sepet
    private Map<String, Map<String, CartItem>> allCarts;

    private Map<String, List<Product>> carts;
    private CartPreferences cartPreferences;
    private Context context;
    private static final String TAG = "CartManager";

    private CartManager() {
        carts = new HashMap<>();
        allCarts = new HashMap<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    /**
     * Context'i set et ve SharedPreferences'ı başlat
     */
    public void init(Context context) {
        this.context = context;

        // carts Map'ini null check ile güvenli hale getir
        if (carts == null) {
            carts = new HashMap<>();
            allCarts = new HashMap<>();
        }

        this.cartPreferences = new CartPreferences(context);
        loadAllCartsFromPreferences();
    }

    /**
     * Tüm sepetleri SharedPreferences'tan yükle
     */
    private void loadAllCartsFromPreferences() {
        try {
            if (cartPreferences != null) {
                Map<String, List<Product>> savedCarts = cartPreferences.loadAllCarts();

                // null güvenliği
                if (savedCarts == null) {
                    savedCarts = new HashMap<>();
                }

                if (carts == null) {
                    carts = new HashMap<>();
                    allCarts = new HashMap<>();
                } else {
                    carts.clear();
                }

                carts.putAll(savedCarts);

                Log.d(TAG, "Kaydedilmiş sepetler yüklendi. Sepet sayısı: " + carts.size());
            }
        } catch (Exception e) {
            Log.e(TAG, "Sepetler yüklenirken hata: " + e.getMessage());
            // Hata durumunda boş map oluştur
            if (carts == null) {
                carts = new HashMap<>();
            }
        }
    }

    /**
     * Tüm sepetleri SharedPreferences'a kaydet
     */
    private void saveAllCartsToPreferences() {
        if (cartPreferences != null) {
            cartPreferences.saveAllCarts(carts);
        }
    }

    /**
     * Belirli bir sepeti SharedPreferences'a kaydet
     */
    private void saveCartToPreferences(String shoppingListName) {
        if (cartPreferences != null && carts.containsKey(shoppingListName)) {
            cartPreferences.saveCartProducts(shoppingListName, carts.get(shoppingListName));
        }
    }

    /**
     * Ürün sepete ekle
     */
    /**
     * Ürün sepete ekle
     */
    public void addProduct(String shoppingListName, Product product) {
        if (shoppingListName == null || product == null) return;

        // carts Map'ini güvenli hale getir
        if (carts == null) {
            carts = new HashMap<>();
        }

        // Sepet yoksa oluştur
        if (!carts.containsKey(shoppingListName)) {
            carts.put(shoppingListName, new ArrayList<>());
        }

        List<Product> cartProducts = carts.get(shoppingListName);

        // Aynı ürün var mı kontrol et
        boolean productExists = false;
        for (Product existingProduct : cartProducts) {
            if (existingProduct.getUrun_adi().equals(product.getUrun_adi()) &&
                    existingProduct.getMarket_adi().equals(product.getMarket_adi())) {
                // Mevcut ürünün miktarını artır
                int currentQuantity = existingProduct.getQuantity();
                if (currentQuantity <= 0) {
                    currentQuantity = 1; // minimum 1 kabul et
                }
                existingProduct.setQuantity(currentQuantity + 1);
                productExists = true;
                Log.d(TAG, "Mevcut ürün bulundu, miktar artırıldı: " + existingProduct.getUrun_adi() + " -> " + existingProduct.getQuantity());
                break;
            }
        }

        if (!productExists) {
            // Yeni ürün ekleniyor, varsayılan miktarı 1 yap
            if (product.getQuantity() <= 0) {
                product.setQuantity(1);
            }
            cartProducts.add(product);
            Log.d(TAG, "Yeni ürün sepete eklendi: " + product.getUrun_adi() + " - Sepet: " + shoppingListName);
        }

        // SharedPreferences'a kaydet
        saveCartToPreferences(shoppingListName);
    }

    /**
     * Ürünü sepetten çıkar
     */
    public void removeProduct(String shoppingListName, Product product) {
        if (shoppingListName == null || product == null) return;

        if (carts.containsKey(shoppingListName)) {
            List<Product> cartProducts = carts.get(shoppingListName);
            boolean removed = cartProducts.removeIf(p ->
                    p.getUrun_adi().equals(product.getUrun_adi()) &&
                            p.getMarket_adi().equals(product.getMarket_adi()));

            if (removed) {
                Log.d(TAG, "Ürün sepetten çıkarıldı: " + product.getUrun_adi() + " - Sepet: " + shoppingListName);
                saveCartToPreferences(shoppingListName);
            }
        }
    }

    /**
     * Sepetteki ürünleri al
     */
    public List<Product> getCartProducts(String shoppingListName) {
        if (shoppingListName == null) return new ArrayList<>();

        // carts Map'ini güvenli hale getir
        if (carts == null) {
            carts = new HashMap<>();
        }

        if (!carts.containsKey(shoppingListName)) {
            // SharedPreferences'tan yüklemeyi dene
            if (cartPreferences != null) {
                List<Product> savedProducts = cartPreferences.loadCartProducts(shoppingListName);
                if (!savedProducts.isEmpty()) {
                    carts.put(shoppingListName, savedProducts);
                    Log.d(TAG, "Sepet SharedPreferences'tan yüklendi: " + shoppingListName);
                }
            }
        }

        return carts.getOrDefault(shoppingListName, new ArrayList<>());
    }

    // Ürün adedini artır
    public void increaseQuantity(String shoppingListName, String productKey) {
        if (shoppingListName == null || productKey == null) return;
        if (carts == null) {
            carts = new HashMap<>();
        }
        List<Product> products = carts.get(shoppingListName);
        if (products == null) return;

        for (Product p : products) {
            String key = p.getUrun_adi() + "_" + p.getMarket_adi();
            if (productKey.equals(key)) {
                int currentQuantity = p.getQuantity();
                if (currentQuantity <= 0) currentQuantity = 1;
                p.setQuantity(currentQuantity + 1);
                saveCartToPreferences(shoppingListName);
                return;
            }
        }
    }

    // Ürün adedini azalt
    public void decreaseQuantity(String shoppingListName, String productKey) {
        if (shoppingListName == null || productKey == null) return;
        if (carts == null) {
            carts = new HashMap<>();
        }
        List<Product> products = carts.get(shoppingListName);
        if (products == null) return;

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            String key = p.getUrun_adi() + "_" + p.getMarket_adi();
            if (productKey.equals(key)) {
                int currentQuantity = p.getQuantity();
                if (currentQuantity > 1) {
                    p.setQuantity(currentQuantity - 1);
                } else {
                    // 1'e eşitse listeden çıkar
                    products.remove(i);
                }
                saveCartToPreferences(shoppingListName);
                return;
            }
        }
    }

    /**
     * Sepetteki toplam ürün sayısı
     */
    public int getTotalItemCount(String shoppingListName) {
        if (shoppingListName == null) return 0;

        // carts Map'ini güvenli hale getir
        if (carts == null) {
            carts = new HashMap<>();
        }

        List<Product> products = getCartProducts(shoppingListName);
        return products != null ? products.size() : 0;
    }

    // Sepetteki tüm ürünleri getir
    public List<CartItem> getCartItems(String shoppingListName) {
        List<CartItem> result = new ArrayList<>();
        if (shoppingListName == null) return result;

        if (carts == null) {
            carts = new HashMap<>();
        }
        List<Product> products = carts.get(shoppingListName);
        if (products == null) return result;

        for (Product p : products) {
            int quantity = p.getQuantity();
            if (quantity <= 0) quantity = 1;
            result.add(new CartItem(p, quantity));
        }
        return result;
    }

    /**
     * Sepeti temizle
     */
    public void clearCart(String shoppingListName) {
        if (shoppingListName == null) return;

        if (carts != null) {
            carts.remove(shoppingListName);
        }

        // SharedPreferences'tan da sil
        if (cartPreferences != null) {
            cartPreferences.clearCart(shoppingListName);
        }

        Log.d(TAG, "Sepet temizlendi: " + shoppingListName);
    }

    /**
     * Tüm sepetleri temizle
     */
    public void clearAllCarts() {
        if (carts == null) {
            carts = new HashMap<>();
        } else {
            carts.clear();
        }

        // SharedPreferences'tan da sil
        if (cartPreferences != null) {
            cartPreferences.clearAllCarts();
        }

        Log.d(TAG, "Tüm sepetler temizlendi");
    }

    /**
     * Mevcut tüm sepet isimlerini al
     */
    public List<String> getAllCartNames() {
        if (carts == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(carts.keySet());
    }


    // Sepet boş mu kontrol et
    public boolean isCartEmpty(String shoppingListName) {
        if (shoppingListName == null) return true;
        if (carts == null) return true;
        List<Product> products = carts.get(shoppingListName);
        return products == null || products.isEmpty();
    }

    /**
     * Belirli bir sepet var mı kontrol et
     */
    public boolean hasCart(String shoppingListName) {
        if (shoppingListName == null) return false;

        // Memory'de var mı kontrol et
        if (carts != null && carts.containsKey(shoppingListName)) {
            return true;
        }

        // SharedPreferences'ta var mı kontrol et
        if (cartPreferences != null) {
            int itemCount = cartPreferences.getCartItemCount(shoppingListName);
            return itemCount > 0;
        }

        return false;
    }

    /**
     * Uygulama kapatılırken çağrılmalı
     */
    public void onAppClose() {
        saveAllCartsToPreferences();
        Log.d(TAG, "Uygulama kapatılıyor, tüm sepetler kaydedildi");
    }

    /**
     * Uygulama açılırken çağrılmalı
     */
    public void onAppStart(Context context) {
        init(context);
        Log.d(TAG, "Uygulama başlatılıyor, sepetler yüklendi");
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
        if (carts == null) return summaries;
        for (String listName : carts.keySet()) {
            summaries.add(new ShoppingListSummary(listName, getTotalItemCount(listName)));
        }
        return summaries;
    }
}

