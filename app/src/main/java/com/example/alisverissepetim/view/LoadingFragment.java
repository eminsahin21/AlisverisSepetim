package com.example.alisverissepetim.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.alisverissepetim.R;
import com.example.alisverissepetim.model.Product;
import com.example.alisverissepetim.service.ApiServiceProduct;
import com.example.alisverissepetim.service.RetrofitClient;

import java.util.List;

import kotlinx.coroutines.Job;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadingFragment extends Fragment {

    private ImageView logoImageView;
    private ProgressBar progressBar;
    private TextView loadingText;
    private TextView subText;
    private LinearLayout dotLayout;
    private View dot1, dot2, dot3;
    private Handler mainHandler;
    private ObjectAnimator rotationAnimator;

    String sepetAdi;
    String sepetTuru;

    private static final String BASE_URL = "https://my-market-api-w9i3.onrender.com/";

    // Yüklenen veriler
    private List<Product> loadedProducts;
    private boolean isDataLoaded = false;
    private boolean isAnimationComplete = false;

    // Animation handlers
    private Handler textAnimationHandler;
    private Handler dotAnimationHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Argümanları al
        if (getArguments() != null) {
            LoadingFragmentArgs args = LoadingFragmentArgs.fromBundle(getArguments());
            sepetAdi = args.getSepetAdi();
            sepetTuru = args.getSepetTuru();
        }

        mainHandler = new Handler(Looper.getMainLooper());
        textAnimationHandler = new Handler(Looper.getMainLooper());
        dotAnimationHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loading, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        startLoadingAnimation();

        // Minimum 2.5 saniye animasyon göster
        mainHandler.postDelayed(() -> {
            isAnimationComplete = true;
            checkIfReadyToNavigate();
        }, 2500);

        // API'den verileri çek
        fetchProductsFromApi();

    }

    private void initializeViews(View view) {
        logoImageView = view.findViewById(R.id.logoImageView);
        progressBar = view.findViewById(R.id.progressBar);
        loadingText = view.findViewById(R.id.loadingText);
        subText = view.findViewById(R.id.subText);
        dotLayout = view.findViewById(R.id.dotLayout);
        dot1 = view.findViewById(R.id.dot1);
        dot2 = view.findViewById(R.id.dot2);
        dot3 = view.findViewById(R.id.dot3);
    }

    private void startLoadingAnimation() {
        startLogoAnimation();
        animateLoadingText();
        animateDots();
        animateProgressBar();
    }

    private void startLogoAnimation() {
        if (logoImageView != null) {
            // Logo'ya smooth rotate + scale animasyonu
            PropertyValuesHolder rotateHolder = PropertyValuesHolder.ofFloat("rotation", 0f, 360f);
            PropertyValuesHolder scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f, 1f);
            PropertyValuesHolder scaleYHolder = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f, 1f);

            rotationAnimator = ObjectAnimator.ofPropertyValuesHolder(logoImageView,
                    rotateHolder, scaleXHolder, scaleYHolder);
            rotationAnimator.setDuration(3000);
            rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            rotationAnimator.setRepeatMode(ObjectAnimator.RESTART);
            rotationAnimator.start();
        }
    }

    private void animateProgressBar() {
        if (progressBar != null) {
            // ProgressBar'a pulse animasyonu
            ObjectAnimator scaleAnimator = ObjectAnimator.ofFloat(progressBar, "scaleX", 0.8f, 1.2f);
            scaleAnimator.setDuration(1000);
            scaleAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            scaleAnimator.setRepeatMode(ObjectAnimator.REVERSE);

            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(progressBar, "scaleY", 0.8f, 1.2f);
            scaleYAnimator.setDuration(1000);
            scaleYAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            scaleYAnimator.setRepeatMode(ObjectAnimator.REVERSE);

            scaleAnimator.start();
            scaleYAnimator.start();
        }
    }

    private void animateLoadingText() {
        final String[] loadingMessages = {
                "Ürünler yükleniyor...",
                "En iyi fiyatlar aranıyor...",
                "Market verisi analiz ediliyor...",
                "Sepetiniz hazırlanıyor...",
                "Son kontroller yapılıyor...",
                "Neredeyse hazır..."
        };

        final int[] currentIndex = {0};

        Runnable textUpdater = new Runnable() {
            @Override
            public void run() {
                if (loadingText != null && !isDataLoaded && isAdded()) {
                    // Fade out
                    loadingText.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    if (loadingText != null) {
                                        loadingText.setText(loadingMessages[currentIndex[0] % loadingMessages.length]);
                                        // Fade in
                                        loadingText.animate()
                                                .alpha(1f)
                                                .setDuration(300)
                                                .setListener(null)
                                                .start();
                                    }
                                }
                            })
                            .start();

                    currentIndex[0]++;

                    if (!isDataLoaded && isAdded()) {
                        textAnimationHandler.postDelayed(this, 1800);
                    }
                }
            }
        };
        textAnimationHandler.postDelayed(textUpdater, 100);
    }

    private void animateDots() {
        if (dot1 == null || dot2 == null || dot3 == null) return;

        final View[] dots = {dot1, dot2, dot3};
        final int[] currentDot = {0};

        Runnable dotAnimator = new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) return;

                // Tüm noktaları normal boyuta getir
                for (View dot : dots) {
                    dot.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(0.5f)
                            .setDuration(200)
                            .start();
                }

                // Aktif noktayı büyüt
                dots[currentDot[0]].animate()
                        .scaleX(1.5f)
                        .scaleY(1.5f)
                        .alpha(1f)
                        .setDuration(200)
                        .start();

                currentDot[0] = (currentDot[0] + 1) % dots.length;

                if (!isDataLoaded && isAdded()) {
                    dotAnimationHandler.postDelayed(this, 500);
                }
            }
        };
        dotAnimationHandler.postDelayed(dotAnimator, 500);
    }

    private void fetchProductsFromApi() {
        ApiServiceProduct apiServiceProduct = RetrofitClient.getClient(BASE_URL).create(ApiServiceProduct.class);
        Call<List<Product>> call = apiServiceProduct.getProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    loadedProducts = response.body();
                    isDataLoaded = true;

                    Log.d("LoadingFragment", "Veriler yüklendi: " + loadedProducts.size() + " ürün");

                    // Yükleme tamamlandı mesajı
                    if (loadingText != null) {
                        loadingText.setText("✓ Hazır! Yönlendiriliyor...");
                    }

                    // Sub text'i güncelle
                    if (subText != null) {
                        subText.setText(loadedProducts.size() + " ürün yüklendi");
                    }

                    checkIfReadyToNavigate();

                } else {
                    Log.e("LoadingFragment", "API Hatası: " + response.code());
                    handleLoadingError("Ürünler yüklenemedi. Tekrar deneyin.");
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                if (!isAdded()) return;

                Log.e("LoadingFragment", "Bağlantı Hatası: " + t.getMessage());
                handleLoadingError("Bağlantı hatası. İnternet bağlantınızı kontrol edin.");
            }
        });
    }

    private void checkIfReadyToNavigate() {
        // Hem animasyon tamamlandı hem de veriler yüklendi
        if (isAnimationComplete && isDataLoaded && loadedProducts != null) {
            // 500ms gecikme ile geçiş yap (kullanıcının "hazır" mesajını görmesi için)
            mainHandler.postDelayed(this::navigateToDetailFragment, 500);
        }
    }

    private void navigateToDetailFragment() {
        if (!isAdded()) return;

        // Animasyonları durdur
        stopAllAnimations();

        // DetailFragment'a geçiş
        if (getView() != null) {
            // Ürün listesini ProductDataHolder'a kaydet
            ProductDataHolder.getInstance().setProducts(loadedProducts);

            try {
                LoadingFragmentDirections.ActionLoadingFragmentToDetailFragment action =
                        LoadingFragmentDirections.actionLoadingFragmentToDetailFragment(sepetAdi,sepetTuru);

                Navigation.findNavController(getView()).navigate(action);

            } catch (Exception e) {
                Log.e("LoadingFragment", "Navigation hatası: " + e.getMessage());
                handleLoadingError("Sayfa yüklenemedi. Tekrar deneyin.");
            }
        }
    }

    private void handleLoadingError(String errorMessage) {
        if (!isAdded()) return;

        stopAllAnimations();

        if (loadingText != null) {
            loadingText.setText(errorMessage);
        }

        if (subText != null) {
            subText.setText("3 saniye sonra geri dönülecek...");
        }

        // 3 saniye sonra geri dön
        mainHandler.postDelayed(() -> {
            if (isAdded() && getView() != null) {
                try {
                    Navigation.findNavController(getView()).popBackStack();
                } catch (Exception e) {
                    Log.e("LoadingFragment", "Geri dönüş hatası: " + e.getMessage());
                }
            }
        }, 3000);
    }

    private void stopAllAnimations() {
        if (rotationAnimator != null) {
            rotationAnimator.cancel();
        }

        // Handler'ları temizle
        if (textAnimationHandler != null) {
            textAnimationHandler.removeCallbacksAndMessages(null);
        }

        if (dotAnimationHandler != null) {
            dotAnimationHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        stopAllAnimations();

        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Handler'ları null yap
        mainHandler = null;
        textAnimationHandler = null;
        dotAnimationHandler = null;
    }

    // Singleton pattern ile ürün verilerini geçici olarak tutma
    public static class ProductDataHolder {
        private static ProductDataHolder instance;
        private List<Product> products;

        public static synchronized ProductDataHolder getInstance() {
            if (instance == null) {
                instance = new ProductDataHolder();
            }
            return instance;
        }

        public void setProducts(List<Product> products) {
            this.products = products;
        }

        public List<Product> getProducts() {
            return products;
        }

        public void clearProducts() {
            products = null;
        }
    }
}