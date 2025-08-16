package com.example.alisverissepetim.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.alisverissepetim.model.ShoppingList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesHelper {

    private static final String PREF_NAME = "shopping_lists_prefs";
    private static final String KEY_SHOPPING_LISTS = "shopping_lists";
    private static final String TAG = "ShoppingListPrefs";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Alışveriş listelerini SharedPreferences'a kaydet
     */
    public void saveShoppingLists(List<ShoppingList> shoppingLists) {
        try {
            String json = gson.toJson(shoppingLists);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_SHOPPING_LISTS, json);
            boolean success = editor.commit();

            Log.d(TAG, "Alışveriş listeleri kaydedildi. Başarılı: " + success);
            Log.d(TAG, "Kaydedilen liste sayısı: " + shoppingLists.size());
        } catch (Exception e) {
            Log.e(TAG, "Alışveriş listeleri kaydedilirken hata: " + e.getMessage());
        }
    }

    /**
     * SharedPreferences'tan alışveriş listelerini yükle
     */
    public List<ShoppingList> loadShoppingLists() {
        try {
            String json = sharedPreferences.getString(KEY_SHOPPING_LISTS, null);

            if (json != null && !json.isEmpty()) {
                Type type = new TypeToken<List<ShoppingList>>(){}.getType();
                List<ShoppingList> loadedLists = gson.fromJson(json, type);

                Log.d(TAG, "Alışveriş listeleri yüklendi. Liste sayısı: " +
                        (loadedLists != null ? loadedLists.size() : 0));

                return loadedLists != null ? loadedLists : new ArrayList<>();
            } else {
                Log.d(TAG, "Hiç kaydedilmiş alışveriş listesi bulunamadı.");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            Log.e(TAG, "Alışveriş listeleri yüklenirken hata: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Tek bir alışveriş listesi ekle
     */
    public void addShoppingList(ShoppingList shoppingList) {
        List<ShoppingList> currentLists = loadShoppingLists();

        // Aynı isimde liste var mı kontrol et
        boolean exists = false;
        for (ShoppingList existingList : currentLists) {
            if (existingList.getBasketName().equals(shoppingList.getBasketName())) {
                exists = true;
                Log.w(TAG, "Aynı isimde liste zaten mevcut: " + shoppingList.getBasketName());
                break;
            }
        }

        if (!exists) {
            currentLists.add(shoppingList);
            saveShoppingLists(currentLists);
            Log.d(TAG, "Yeni alışveriş listesi eklendi: " + shoppingList.getBasketName());
        }
    }

    /**
     * Belirli bir alışveriş listesini sil
     */
    public boolean removeShoppingList(String basketName) {
        List<ShoppingList> currentLists = loadShoppingLists();
        boolean removed = false;

        for (int i = 0; i < currentLists.size(); i++) {
            if (currentLists.get(i).getBasketName().equals(basketName)) {
                currentLists.remove(i);
                removed = true;
                break;
            }
        }

        if (removed) {
            saveShoppingLists(currentLists);
            Log.d(TAG, "Alışveriş listesi silindi: " + basketName);
        } else {
            Log.w(TAG, "Silinecek liste bulunamadı: " + basketName);
        }

        return removed;
    }

    /**
     * Belirli bir alışveriş listesini güncelle
     */
    public boolean updateShoppingList(String oldBasketName, ShoppingList newShoppingList) {
        List<ShoppingList> currentLists = loadShoppingLists();
        boolean updated = false;

        for (int i = 0; i < currentLists.size(); i++) {
            if (currentLists.get(i).getBasketName().equals(oldBasketName)) {
                currentLists.set(i, newShoppingList);
                updated = true;
                break;
            }
        }

        if (updated) {
            saveShoppingLists(currentLists);
            Log.d(TAG, "Alışveriş listesi güncellendi: " + oldBasketName + " -> " +
                    newShoppingList.getBasketName());
        } else {
            Log.w(TAG, "Güncellenecek liste bulunamadı: " + oldBasketName);
        }

        return updated;
    }

    /**
     * Tüm alışveriş listelerini temizle
     */
    public void clearAllShoppingLists() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_SHOPPING_LISTS);
        boolean success = editor.commit();

        Log.d(TAG, "Tüm alışveriş listeleri temizlendi. Başarılı: " + success);
    }

    /**
     * Belirli bir liste var mı kontrol et
     */
    public boolean isShoppingListExists(String basketName) {
        List<ShoppingList> currentLists = loadShoppingLists();

        for (ShoppingList list : currentLists) {
            if (list.getBasketName().equals(basketName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Toplam liste sayısını döndür
     */
    public int getShoppingListCount() {
        return loadShoppingLists().size();
    }
}