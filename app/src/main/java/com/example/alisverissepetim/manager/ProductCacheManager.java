package com.example.alisverissepetim.manager;

import com.example.alisverissepetim.model.Product;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton sınıf - Ürünleri bellekte tutar
 * Uygulama yaşam döngüsü boyunca veriler saklanır
 */
public class ProductCacheManager {
    private static ProductCacheManager instance;
    private List<Product> cachedProducts;
    private boolean isDataLoaded = false;
    private long lastUpdateTime = 0;

    // Cache geçerlilik süresi (30 dakika)
    private static final long CACHE_VALIDITY_DURATION = 30 * 60 * 1000;

    private ProductCacheManager() {
        cachedProducts = new ArrayList<>();
    }

    public static synchronized ProductCacheManager getInstance() {
        if (instance == null) {
            instance = new ProductCacheManager();
        }
        return instance;
    }

    /**
     * Ürünleri cache'e kaydet
     */
    public void cacheProducts(List<Product> products) {
        cachedProducts.clear();
        cachedProducts.addAll(products);
        isDataLoaded = true;
        lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Cache'deki ürünleri al
     */
    public List<Product> getCachedProducts() {
        return new ArrayList<>(cachedProducts);
    }

    /**
     * Cache'de veri var mı kontrol et
     */
    public boolean hasValidCache() {
        return isDataLoaded && !isCacheExpired() && !cachedProducts.isEmpty();
    }

    /**
     * Cache süresi dolmuş mu kontrol et
     */
    private boolean isCacheExpired() {
        return (System.currentTimeMillis() - lastUpdateTime) > CACHE_VALIDITY_DURATION;
    }

    /**
     * Cache'i temizle
     */
    public void clearCache() {
        cachedProducts.clear();
        isDataLoaded = false;
        lastUpdateTime = 0;
    }

    /**
     * Cache durumunu kontrol et
     */
    public boolean isDataLoaded() {
        return isDataLoaded;
    }

    /**
     * Cache boyutunu al
     */
    public int getCacheSize() {
        return cachedProducts.size();
    }
}