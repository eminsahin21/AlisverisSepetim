package com.example.alisverissepetim.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.alisverissepetim.model.SimpleProductItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SimpleProductPref {

    private static final String PREF_NAME = "simple_products_prefs";
    private static final String TAG = "SimpleProductPrefs";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public SimpleProductPref(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Belirli bir sepet için ürün listesini kaydet
     */
    public void saveProductsForBasket(String basketName, List<SimpleProductItem> products) {
        try {
            String key = "products_" + basketName;
            String json = gson.toJson(products);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, json);
            boolean success = editor.commit();

            Log.d(TAG, "Ürünler kaydedildi - Sepet: " + basketName +
                    ", Ürün sayısı: " + products.size() + ", Başarılı: " + success);
        } catch (Exception e) {
            Log.e(TAG, "Ürünler kaydedilirken hata - Sepet: " + basketName + ", Hata: " + e.getMessage());
        }
    }

    /**
     * Belirli bir sepet için ürün listesini yükle
     */
    public List<SimpleProductItem> loadProductsForBasket(String basketName) {
        try {
            String key = "products_" + basketName;
            String json = sharedPreferences.getString(key, null);

            if (json != null && !json.isEmpty()) {
                Type type = new TypeToken<List<SimpleProductItem>>(){}.getType();
                List<SimpleProductItem> loadedProducts = gson.fromJson(json, type);

                Log.d(TAG, "Ürünler yüklendi - Sepet: " + basketName +
                        ", Ürün sayısı: " + (loadedProducts != null ? loadedProducts.size() : 0));

                return loadedProducts != null ? loadedProducts : new ArrayList<>();
            } else {
                Log.d(TAG, "Bu sepet için kaydedilmiş ürün bulunamadı: " + basketName);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            Log.e(TAG, "Ürünler yüklenirken hata - Sepet: " + basketName + ", Hata: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Belirli bir sepetteki tek bir ürünü güncelle
     */
    public void updateProductInBasket(String basketName, SimpleProductItem product) {
        List<SimpleProductItem> products = loadProductsForBasket(basketName);

        // Ürünü bul ve güncelle
        boolean updated = false;
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getName().equalsIgnoreCase(product.getName())) {
                products.set(i, product);
                updated = true;
                break;
            }
        }

        // Ürün bulunamazsa ekle
        if (!updated) {
            products.add(product);
            Log.d(TAG, "Yeni ürün eklendi - Sepet: " + basketName + ", Ürün: " + product.getName());
        } else {
            Log.d(TAG, "Ürün güncellendi - Sepet: " + basketName + ", Ürün: " + product.getName());
        }

        saveProductsForBasket(basketName, products);
    }

    /**
     * Belirli bir sepetten ürün sil
     */
    public boolean removeProductFromBasket(String basketName, String productName) {
        List<SimpleProductItem> products = loadProductsForBasket(basketName);
        boolean removed = false;

        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getName().equalsIgnoreCase(productName)) {
                products.remove(i);
                removed = true;
                break;
            }
        }

        if (removed) {
            saveProductsForBasket(basketName, products);
            Log.d(TAG, "Ürün silindi - Sepet: " + basketName + ", Ürün: " + productName);
        } else {
            Log.w(TAG, "Silinecek ürün bulunamadı - Sepet: " + basketName + ", Ürün: " + productName);
        }

        return removed;
    }

    /**
     * Belirli bir sepetteki tüm ürünleri temizle
     */
    public void clearProductsForBasket(String basketName) {
        String key = "products_" + basketName;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        boolean success = editor.commit();

        Log.d(TAG, "Sepet temizlendi - Sepet: " + basketName + ", Başarılı: " + success);
    }

    /**
     * Belirli bir sepetteki toplam ürün sayısını döndür
     */
    public int getProductCountForBasket(String basketName) {
        return loadProductsForBasket(basketName).size();
    }

    /**
     * Belirli bir sepetteki toplam adet sayısını döndür (quantity toplamı)
     */
    public int getTotalQuantityForBasket(String basketName) {
        List<SimpleProductItem> products = loadProductsForBasket(basketName);
        int totalQuantity = 0;

        for (SimpleProductItem product : products) {
            totalQuantity += product.getQuantity();
        }

        return totalQuantity;
    }

    /**
     * Tüm sepetlerin ürün verilerini temizle
     */
    public void clearAllProductData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        boolean success = editor.commit();

        Log.d(TAG, "Tüm ürün verileri temizlendi. Başarılı: " + success);
    }
}