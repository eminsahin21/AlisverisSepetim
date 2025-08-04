package com.example.alisverissepetim.view;

import android.media.Image;
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
import com.example.alisverissepetim.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private ArrayList<String> categoryList = new ArrayList<>();
    private String selectedCategory = "";
    private String currentSearchQuery = "";

    // Kategori ve market isim mapping'leri
    private Map<String, String> categoryDisplayNames = new HashMap<>();
    private Map<String, String> marketDisplayNames = new HashMap<>();

    // Performans ayarları
    private static final int ITEMS_PER_LOAD = 50;
    private static final int ITEMS_PER_BATCH = 20;
    private int currentDisplayIndex = 0;
    private boolean isLoading = false;
    private Handler mainHandler;
    private Handler searchHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainHandler = new Handler(Looper.getMainLooper());
        searchHandler = new Handler(Looper.getMainLooper());

        // Safe Args ile gelen verileri al
        if (getArguments() != null) {
            String sepetAdi = DetailFragmentArgs.fromBundle(getArguments()).getSepetAdi();
            String sepetTur = DetailFragmentArgs.fromBundle(getArguments()).getSepetTuru();
            Log.d("DetailFragment", "Gelen sepet adı: " + sepetAdi);
            Log.d("DetailFragment", "Gelen sepet türü: " + sepetTur);
        }

        // ProductAdapter'ı sepet callback'i ile başlat
        productAdapter = new ProductAdapter(displayedProducts, product -> {
            // Sepete ürün ekleme callback'i
            CartManager.getInstance().addProduct(product);
            updateCartBadge();
            showAddToCartToast(product.getUrun_adi());
        });

        initializeDisplayNames();
        initializeViews(view);
        setupRecyclerViews();
        setupSearchFunctionality();
        setupScrollListener();
        updateCartBadge(); // İlk yüklemede badge'i güncelle

        cartIcon.setOnClickListener(view1 -> {
            // Sepet fragmentına git
            navigateToCart();
        });

        // LoadingFragment'tan gelen verileri al
        loadProductsFromHolder();
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
        cartBadge = view.findViewById(R.id.cartBadge); // Badge'i bağladık
    }

    private void setupRecyclerViews() {
        recyclerViewCategory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Adaptörleri başlat
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            selectedCategory = category;
            filterAndDisplayProducts();
            categoryAdapter.setSelectedCategory(category);
        });

        recyclerViewCategory.setAdapter(categoryAdapter);
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void setupSearchFunctionality() {
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Arama işlemini 400ms gecikme ile yap (daha hızlı response)
                    searchHandler.removeCallbacksAndMessages(null);
                    searchHandler.postDelayed(() -> {
                        currentSearchQuery = s.toString().trim();
                        filterAndDisplayProducts();
                    }, 400);
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

                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount - 5) {
                            loadMoreProducts();
                        }
                    }
                }
            }
        });
    }

    private void loadProductsFromHolder() {
        // LoadingFragment'tan gelen verileri al
        List<Product> products = LoadingFragment.ProductDataHolder.getInstance().getProducts();

        if (products != null && !products.isEmpty()) {
            allProducts.clear();
            allProducts.addAll(products);

            Log.d("DetailFragment", "Veriler yüklendi: " + allProducts.size() + " ürün");

            // Kategorileri ayarla
            setupCategoriesFromProducts(allProducts);

            // İlk ürün grubunu göster
            selectedCategory = "";
            filterAndDisplayProducts();

            // Verileri temizle (memory leak'i önlemek için)
            LoadingFragment.ProductDataHolder.getInstance().clearProducts();

            Toast.makeText(getContext(), allProducts.size() + " ürün hazır!", Toast.LENGTH_SHORT).show();

        } else {
            Log.e("DetailFragment", "Ürün verileri bulunamadı!");
            Toast.makeText(getContext(), "Ürünler yüklenemedi. Lütfen tekrar deneyin.", Toast.LENGTH_LONG).show();
        }
    }

    private void setupCategoriesFromProducts(List<Product> products) {
        Set<String> uniqueCategories = new HashSet<>();
        uniqueCategories.add("Tümü");

        for (Product product : products) {
            if (product.getKategori() != null && !product.getKategori().isEmpty()) {
                uniqueCategories.add(product.getKategori());
            }
        }

        categoryList.clear();
        categoryList.addAll(uniqueCategories);

        // Kategori isimlerini güzelleştir
        beautifyCategoryNames();

        categoryAdapter.notifyDataSetChanged();

        Log.d("CATEGORIES", "Kategori sayısı: " + categoryList.size());
    }

    private void beautifyCategoryNames() {
        // Kategori listesindeki isimleri güzelleştir
        for (int i = 0; i < categoryList.size(); i++) {
            String originalName = categoryList.get(i);
            String displayName = categoryDisplayNames.get(originalName);
            if (displayName != null) {
                categoryList.set(i, displayName);
            }
        }
    }

    private void filterAndDisplayProducts() {
        if (isLoading) return;

        isLoading = true;

        // Filtreleme yap
        ArrayList<Product> filteredProducts = new ArrayList<>();

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
                filteredProducts.add(product);
            }
        }

        Log.d("FILTER", "Filtrelenen ürün sayısı: " + filteredProducts.size() +
                " (Kategori: " + selectedCategory + ", Arama: " + currentSearchQuery + ")");

        // İlk batch'i göster
        displayedProducts.clear();
        currentDisplayIndex = 0;

        int itemsToShow = Math.min(ITEMS_PER_LOAD, filteredProducts.size());
        for (int i = 0; i < itemsToShow; i++) {
            displayedProducts.add(filteredProducts.get(i));
        }

        currentDisplayIndex = itemsToShow;

        // Adapter'ı güncelle
        productAdapter.updateList(new ArrayList<>(displayedProducts));

        Log.d("DISPLAY", "Gösterilen ürün sayısı: " + displayedProducts.size());

        isLoading = false;
    }

    private String getOriginalCategoryName(String displayName) {
        // Display name'den orijinal kategori adını bul
        for (Map.Entry<String, String> entry : categoryDisplayNames.entrySet()) {
            if (entry.getValue().equals(displayName)) {
                return entry.getKey();
            }
        }
        return displayName;
    }

    private void loadMoreProducts() {
        if (isLoading) return;

        isLoading = true;

        // Mevcut filtrelenmiş ürünleri al
        ArrayList<Product> filteredProducts = new ArrayList<>();

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
                filteredProducts.add(product);
            }
        }

        // Yeni batch ekle
        int itemsToAdd = Math.min(ITEMS_PER_BATCH, filteredProducts.size() - currentDisplayIndex);

        if (itemsToAdd > 0) {
            int startPosition = displayedProducts.size();

            for (int i = currentDisplayIndex; i < currentDisplayIndex + itemsToAdd; i++) {
                displayedProducts.add(filteredProducts.get(i));
            }

            currentDisplayIndex += itemsToAdd;
            productAdapter.notifyItemRangeInserted(startPosition, itemsToAdd);

            Log.d("LOAD_MORE", "Yeni ürün eklendi: " + itemsToAdd + " - Toplam: " + displayedProducts.size());
        }

        isLoading = false;
    }

    // Sepet badge'ini güncelle
    private void updateCartBadge() {
        int itemCount = CartManager.getInstance().getTotalItemCount();
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
        if (CartManager.getInstance().isEmpty()) {
            Toast.makeText(getContext(), "Sepetiniz boş", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigation component ile sepet fragmentına git
        try {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_detailFragment_to_cartFragment);
        } catch (Exception e) {
            Log.e("DetailFragment", "Navigation error: " + e.getMessage());
            Toast.makeText(getContext(), "Sepet sayfasına gidilemiyor", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (searchHandler != null) {
            searchHandler.removeCallbacksAndMessages(null);
        }
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }

        // ProductAdapter'ı temizle
        if (productAdapter != null) {
            productAdapter.cleanup();
        }
    }
}