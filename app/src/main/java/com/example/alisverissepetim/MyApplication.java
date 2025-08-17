package com.example.alisverissepetim;

import android.app.Application;
import com.example.alisverissepetim.manager.ProductCacheManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Cache manager'ı başlat
        ProductCacheManager.getInstance();
    }
}