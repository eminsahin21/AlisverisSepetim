package com.example.alisverissepetim.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alisverissepetim.R;
import com.example.alisverissepetim.adapter.CategoryAdapter;
import com.example.alisverissepetim.adapter.ProductAdapter;
import com.example.alisverissepetim.manager.CartManager;
import com.example.alisverissepetim.manager.ProductCacheManager;
import com.example.alisverissepetim.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailFragment extends Fragment {

    private RecyclerView recyclerViewCategory, recyclerViewProducts;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private EditText searchEditText;
    private ImageView cartIcon;
    private TextView cartBadge;

    // Veri listeleri
    private ArrayList<Product> allProducts = new ArrayList<>();
    private ArrayList<Product> displayedProducts = new ArrayList<>();
    private ArrayList<Product> filteredProducts = new ArrayList<>(); // Filtrelenmiş ürünleri ayrı tut
    private ArrayList<String> categoryList = new ArrayList<>();
    private String selectedCategory = "";
    private String currentSearchQuery = "";

    // Kategori ve market isim mapping'leri
    private Map<String, String> categoryDisplayNames = new HashMap<>();
    private Map<String, String> marketDisplayNames = new HashMap<>();

    // Performans ayarları - Optimize edildi
    private static final int ITEMS_PER_LOAD = 20; // İlk yüklemede daha az
    private static final int ITEMS_PER_BATCH = 10; // Batch boyutu küçültüldü
    private int currentDisplayIndex = 0;
    private boolean isLoading = false;
    private boolean isFilteringComplete = false;

    // Threading için
    private Handler mainHandler;
    private Handler searchHandler;
    private ExecutorService backgroundExecutor;

    private String shoppingListName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Handler'ları ve Executor'ı başlat
        mainHandler = new Handler(Looper.getMainLooper());
        searchHandler = new Handler(Looper.getMainLooper());
        backgroundExecutor = Executors.newSingleThreadExecutor();

        // Safe Args ile gelen sepet adını alın
        if (getArguments() != null) {
            DetailFragmentArgs args = DetailFragmentArgs.fromBundle(getArguments());
            this.shoppingListName = args.getSepetAdi();
            Log.d("DetailFragment", "Gelen sepet adı: " + shoppingListName);
        }

        // ProductAdapter'ı sepet callback'i ile başlat
        productAdapter = new ProductAdapter(displayedProducts, product -> {
            // Sepete ürün ekleme callback'i
            CartManager.getInstance().addProduct(shoppingListName, product);
            updateCartBadge();
            showAddToCartToast(product.getUrun_adi());
        });

        initializeDisplayNames();
        initializeViews(view);
        setupRecyclerViews();
        setupSearchFunctionality();
        setupScrollListener();
        updateCartBadge();

        cartIcon.setOnClickListener(view1 -> navigateToCart());

        // Cache'den veya LoadingFragment'tan ürünleri yükle
        loadProducts();
    }

    private void loadProducts() {
        // Önce cache'de veri var mı kontrol et
        if (ProductCacheManager.getInstance().hasValidCache()) {
            Log.d("DetailFragment", "Cache'den veri yükleniyor...");
            allProducts.clear();
            allProducts.addAll(ProductCacheManager.getInstance().getCachedProducts());

            setupCategoriesFromProducts(allProducts);
            selectedCategory = "";
            filterAndDisplayProductsAsync();

            Toast.makeText(getContext(), "Ürünler hazır! (" + allProducts.size() + ")", Toast.LENGTH_SHORT).show();
        } else {
            // Cache'de veri yoksa LoadingFragment'tan al
            Log.d("DetailFragment", "LoadingFragment'tan veri yükleniyor...");
            loadProductsFromHolder();
        }
    }

    private void loadProductsFromHolder() {
        List<Product> products = LoadingFragment.ProductDataHolder.getInstance().getProducts();

        if (products != null && !products.isEmpty()) {
            allProducts.clear();
            allProducts.addAll(products);

            // Verileri cache'e kaydet
            ProductCacheManager.getInstance().cacheProducts(allProducts);

            Log.d("DetailFragment", "Veriler yüklendi ve cache'lendi: " + allProducts.size() + " ürün");

            setupCategoriesFromProducts(allProducts);
            selectedCategory = "";
            filterAndDisplayProductsAsync();

            // LoadingFragment verilerini temizle
            LoadingFragment.ProductDataHolder.getInstance().clearProducts();

            Toast.makeText(getContext(), allProducts.size() + " ürün hazır!", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("DetailFragment", "Ürün verileri bulunamadı!");
            Toast.makeText(getContext(), "Ürünler yüklenemedi. Lütfen tekrar deneyin.", Toast.LENGTH_LONG).show();
        }
    }

    // Arka planda filtreleme işlemi
    private void filterAndDisplayProductsAsync() {
        if (isLoading) return;

        isLoading = true;
        isFilteringComplete = false;

        // UI'da loading durumunu göster (opsiyonel)
        // showLoadingIndicator();

        backgroundExecutor.execute(() -> {
            try {
                // Arka planda filtreleme yap
                ArrayList<Product> tempFilteredProducts = new ArrayList<>();

                for (Product product : allProducts) {
                    boolean categoryMatch = true;
                    boolean searchMatch = true;

                    // Kategori filtresi
                    if (!selectedCategory.isEmpty() && !selectedCategory.equals("Tümü")) {
                        String originalCategoryName = getOriginalCategoryName(selectedCategory);
                        if (product.getKategori() != null) {
                            categoryMatch = product.getKategori().equals(originalCategoryName);
                        } else {
                            categoryMatch = false;
                        }
                    }

                    // Arama filtresi
                    if (!currentSearchQuery.isEmpty()) {
                        String productName = product.getUrun_adi() != null ? product.getUrun_adi().toLowerCase() : "";
                        String marketName = product.getMarket_adi() != null ? product.getMarket_adi().toLowerCase() : "";
                        String searchQuery = currentSearchQuery.toLowerCase();

                        searchMatch = productName.contains(searchQuery) || marketName.contains(searchQuery);
                    }

                    if (categoryMatch && searchMatch) {
                        tempFilteredProducts.add(product);
                    }
                }

                // UI thread'de güncelleme yap
                mainHandler.post(() -> {
                    filteredProducts.clear();
                    filteredProducts.addAll(tempFilteredProducts);

                    displayedProducts.clear();
                    currentDisplayIndex = 0;

                    int itemsToShow = Math.min(ITEMS_PER_LOAD, filteredProducts.size());
                    for (int i = 0; i < itemsToShow; i++) {
                        displayedProducts.add(filteredProducts.get(i));
                    }

                    currentDisplayIndex = itemsToShow;
                    isFilteringComplete = true;

                    // Adapter'ı güncelle
                    productAdapter.updateList(new ArrayList<>(displayedProducts));

                    Log.d("FILTER", "Filtrelenen: " + filteredProducts.size() +
                            ", Gösterilen: " + displayedProducts.size());

                    isLoading = false;
                    // hideLoadingIndicator();
                });

            } catch (Exception e) {
                Log.e("DetailFragment", "Filtreleme hatası: " + e.getMessage());
                mainHandler.post(() -> {
                    isLoading = false;
                    // hideLoadingIndicator();
                });
            }
        });
    }

    private void loadMoreProducts() {
        if (isLoading || !isFilteringComplete) return;

        if (currentDisplayIndex >= filteredProducts.size()) {
            // Daha fazla yüklenecek ürün yok
            return;
        }

        isLoading = true;

        // Yeni batch'i arka planda hazırla
        backgroundExecutor.execute(() -> {
            try {
                int itemsToAdd = Math.min(ITEMS_PER_BATCH, filteredProducts.size() - currentDisplayIndex);

                if (itemsToAdd > 0) {
                    ArrayList<Product> newProducts = new ArrayList<>();

                    for (int i = currentDisplayIndex; i < currentDisplayIndex + itemsToAdd; i++) {
                        newProducts.add(filteredProducts.get(i));
                    }

                    // UI thread'de güncelleme
                    mainHandler.post(() -> {
                        int startPosition = displayedProducts.size();
                        displayedProducts.addAll(newProducts);
                        currentDisplayIndex += itemsToAdd;

                        productAdapter.notifyItemRangeInserted(startPosition, itemsToAdd);

                        Log.d("LOAD_MORE", "Yeni ürün eklendi: " + itemsToAdd +
                                " - Toplam: " + displayedProducts.size());

                        isLoading = false;
                    });
                } else {
                    mainHandler.post(() -> isLoading = false);
                }

            } catch (Exception e) {
                Log.e("DetailFragment", "Daha fazla yükleme hatası: " + e.getMessage());
                mainHandler.post(() -> isLoading = false);
            }
        });
    }

    private void initializeDisplayNames() {
        // Kategori isimlerini düzenle
        categoryDisplayNames.put("atistirmalik", "Atıştırmalık");
        categoryDisplayNames.put("icecek", "İçecek");
        categoryDisplayNames.put("temizlikUrunleri", "Temizlik Ürünleri");
        categoryDisplayNames.put("temelgida", "Temel Gıda");
        categoryDisplayNames.put("kahvaltilik", "Kahvaltılık & Süt Ürünleri");
        categoryDisplayNames.put("meyvesebze", "Meyve & Sebze");
        categoryDisplayNames.put("urunlerEt", "Et & Tavuk");

        // Market isimlerini düzenle
        marketDisplayNames.put("carrefour", "CarrefourSA");
        marketDisplayNames.put("bim", "BİM");
        marketDisplayNames.put("a101", "A101");
        marketDisplayNames.put("migros", "Migros");
        marketDisplayNames.put("sok", "ŞOK");
        marketDisplayNames.put("real", "Real");
        marketDisplayNames.put("macro", "Macro Center");
        marketDisplayNames.put("onur", "Onur Market");
        marketDisplayNames.put("tarim_kredi", "Tarım Kredi Market");
    }

    private void initializeViews(View view) {
        recyclerViewCategory = view.findViewById(R.id.recyclerViewCategory);
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);
        searchEditText = view.findViewById(R.id.searchEditText);
        cartIcon = view.findViewById(R.id.cartIcon);
        cartBadge = view.findViewById(R.id.cartBadge);
    }

    private void setupRecyclerViews() {
        recyclerViewCategory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Adaptörleri başlat
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            selectedCategory = category;
            filterAndDisplayProductsAsync(); // Async olarak değiştir
            categoryAdapter.setSelectedCategory(category);
        });

        recyclerViewCategory.setAdapter(categoryAdapter);
        recyclerViewProducts.setAdapter(productAdapter);

        // RecyclerView optimizasyonları
        recyclerViewProducts.setHasFixedSize(true);
        recyclerViewProducts.setItemViewCacheSize(20);
    }

    private void setupSearchFunctionality() {
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Arama işlemini 300ms gecikme ile yap
                    searchHandler.removeCallbacksAndMessages(null);
                    searchHandler.postDelayed(() -> {
                        currentSearchQuery = s.toString().trim();
                        filterAndDisplayProductsAsync(); // Async olarak değiştir
                    }, 300);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupScrollListener() {
        recyclerViewProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0 && !isLoading) {
                    GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                        // Daha erken tetikle (3 item kala)
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount - 3) {
                            loadMoreProducts();
                        }
                    }
                }
            }
        });
    }

    private void setupCategoriesFromProducts(List<Product> products) {
        Set<String> uniqueCategories = new HashSet<>();

        for (Product product : products) {
            if (product.getKategori() != null && !product.getKategori().isEmpty()) {
                uniqueCategories.add(product.getKategori());
            }
        }

        categoryList.clear();
        categoryList.add("Tümü");
        for (String originalCategory : uniqueCategories) {
            categoryList.add(originalCategory);
        }

        beautifyCategoryNames();

        if (categoryAdapter != null) {
            categoryAdapter.notifyDataSetChanged();
        }

        Log.d("CATEGORIES", "Kategori sayısı: " + categoryList.size());
    }

    private void beautifyCategoryNames() {
        for (int i = 0; i < categoryList.size(); i++) {
            String originalName = categoryList.get(i);
            String displayName = categoryDisplayNames.get(originalName);
            if (displayName != null) {
                categoryList.set(i, displayName);
            }
        }
    }

    private String getOriginalCategoryName(String displayName) {
        for (Map.Entry<String, String> entry : categoryDisplayNames.entrySet()) {
            if (entry.getValue().equals(displayName)) {
                return entry.getKey();
            }
        }
        return displayName;
    }

    // Sepet badge'ini güncelle
    private void updateCartBadge() {
        int itemCount = CartManager.getInstance().getTotalItemCount(shoppingListName);
        if (itemCount > 0) {
            cartBadge.setVisibility(View.VISIBLE);
            cartBadge.setText(String.valueOf(itemCount));
        } else {
            cartBadge.setVisibility(View.GONE);
        }
    }

    // Sepete ekleme toast mesajı
    private void showAddToCartToast(String productName) {
        Toast.makeText(getContext(), productName + " sepete eklendi", Toast.LENGTH_SHORT).show();
    }

    // Sepet fragmentına git
    private void navigateToCart() {
        if (CartManager.getInstance().isCartEmpty(shoppingListName)) {
            Toast.makeText(getContext(), "Sepetiniz boş", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            DetailFragmentDirections.ActionDetailFragmentToCartFragment action =
                    DetailFragmentDirections.actionDetailFragmentToCartFragment(shoppingListName, "");
            Navigation.findNavController(requireView()).navigate(action);
        } catch (Exception e) {
            Log.e("DetailFragment", "Navigation error: " + e.getMessage());
            Toast.makeText(getContext(), "Sepet sayfasına gidilemiyor", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Handler'ları temizle
        if (searchHandler != null) {
            searchHandler.removeCallbacksAndMessages(null);
        }
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }

        // Executor'ı kapat
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }

        // ProductAdapter'ı temizle
        if (productAdapter != null) {
            productAdapter.cleanup();
        }

        // Sepet verilerini kaydet
        CartManager.getInstance().onAppClose();
    }
}