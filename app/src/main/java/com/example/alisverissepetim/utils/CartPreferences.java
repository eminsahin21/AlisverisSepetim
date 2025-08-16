package com.example.alisverissepetim.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.alisverissepetim.model.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartPreferences {

    private static final String PREF_NAME = "cart_prefs";
    private static final String TAG = "CartPreferences";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public CartPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Belirli bir sepet için ürün listesini kaydet
     */
    public void saveCartProducts(String shoppingListName, List<Product> products) {
        try {
            String key = "cart_" + shoppingListName;
            String json = gson.toJson(products);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, json);
            boolean success = editor.commit();

            Log.d(TAG, "Sepet ürünleri kaydedildi - Sepet: " + shoppingListName +
                    ", Ürün sayısı: " + products.size() + ", Başarılı: " + success);
        } catch (Exception e) {
            Log.e(TAG, "Sepet ürünleri kaydedilirken hata - Sepet: " + shoppingListName + ", Hata: " + e.getMessage());
        }
    }

    /**
     * Belirli bir sepet için ürün listesini yükle
     */
    public List<Product> loadCartProducts(String shoppingListName) {
        try {
            String key = "cart_" + shoppingListName;
            String json = sharedPreferences.getString(key, null);

            if (json != null && !json.isEmpty()) {
                Type type = new TypeToken<List<Product>>(){}.getType();
                List<Product> loadedProducts = gson.fromJson(json, type);

                Log.d(TAG, "Sepet ürünleri yüklendi - Sepet: " + shoppingListName +
                        ", Ürün sayısı: " + (loadedProducts != null ? loadedProducts.size() : 0));

                return loadedProducts != null ? loadedProducts : new ArrayList<>();
            } else {
                Log.d(TAG, "Bu sepet için kaydedilmiş ürün bulunamadı: " + shoppingListName);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            Log.e(TAG, "Sepet ürünleri yüklenirken hata - Sepet: " + shoppingListName + ", Hata: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Tüm sepetlerin ürün verilerini kaydet
     */
    public void saveAllCarts(Map<String, List<Product>> allCarts) {
        try {
            String json = gson.toJson(allCarts);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("all_carts", json);
            boolean success = editor.commit();

            Log.d(TAG, "Tüm sepetler kaydedildi. Sepet sayısı: " + allCarts.size() + ", Başarılı: " + success);
        } catch (Exception e) {
            Log.e(TAG, "Tüm sepetler kaydedilirken hata: " + e.getMessage());
        }
    }

    /**
     * Tüm sepetlerin ürün verilerini yükle
     */
    public Map<String, List<Product>> loadAllCarts() {
        try {
            String json = sharedPreferences.getString("all_carts", null);

            if (json != null && !json.isEmpty()) {
                Type type = new TypeToken<Map<String, List<Product>>>(){}.getType();
                Map<String, List<Product>> loadedCarts = gson.fromJson(json, type);

                Log.d(TAG, "Tüm sepetler yüklendi. Sepet sayısı: " +
                        (loadedCarts != null ? loadedCarts.size() : 0));

                return loadedCarts != null ? loadedCarts : new HashMap<>();
            } else {
                Log.d(TAG, "Hiç kaydedilmiş sepet bulunamadı.");
                return new HashMap<>();
            }
        } catch (Exception e) {
            Log.e(TAG, "Tüm sepetler yüklenirken hata: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Belirli bir sepeti temizle
     */
    public void clearCart(String shoppingListName) {
        String key = "cart_" + shoppingListName;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        boolean success = editor.commit();

        Log.d(TAG, "Sepet temizlendi - Sepet: " + shoppingListName + ", Başarılı: " + success);
    }

    /**
     * Tüm sepet verilerini temizle
     */
    public void clearAllCarts() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        boolean success = editor.commit();

        Log.d(TAG, "Tüm sepet verileri temizlendi. Başarılı: " + success);
    }

    /**
     * Belirli bir sepetteki ürün sayısını döndür
     */
    public int getCartItemCount(String shoppingListName) {
        List<Product> products = loadCartProducts(shoppingListName);
        return products.size();
    }
}