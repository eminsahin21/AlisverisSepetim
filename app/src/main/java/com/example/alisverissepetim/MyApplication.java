package com.example.alisverissepetim;

import android.app.Application;
import com.example.alisverissepetim.manager.ProductCacheManager;
import com.example.alisverissepetim.manager.CartManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // CartManager'ı uygulama başlatıldığında initialize et
        CartManager.getInstance().onAppStart(this);

        // Cache manager'ı başlat
        ProductCacheManager.getInstance();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        // Uygulama kapatılırken sepetleri kaydet
        CartManager.getInstance().onAppClose();
    }

}