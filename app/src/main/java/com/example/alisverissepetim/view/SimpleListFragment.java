package com.example.alisverissepetim.view;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.content.Context;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import com.example.alisverissepetim.R;
import com.example.alisverissepetim.adapter.SimpleProductAdapter;
import com.example.alisverissepetim.model.SimpleProductItem;
import com.example.alisverissepetim.utils.SimpleProductPref;

public class SimpleListFragment extends Fragment {

    private EditText searchEditText;
    private RecyclerView productsRecyclerView;
    private TextView emptyListTextView;
    private SimpleProductAdapter productAdapter;
    private List<SimpleProductItem> productList;

    // SharedPreferences helper
    private SimpleProductPref productPreferences;
    private String basketName;
    private String basketType;

    public SimpleListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SharedPreferences helper'ı başlat
        productPreferences = new SimpleProductPref(requireContext());

        if (getArguments() != null) {
            basketName = DetailFragmentArgs.fromBundle(getArguments()).getSepetAdi();
            basketType = DetailFragmentArgs.fromBundle(getArguments()).getSepetTuru();
            Log.d("SimpleListFragment", "Gelen sepet adı: " + basketName);
            Log.d("SimpleListFragment", "Gelen sepet türü: " + basketType);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchEditText = view.findViewById(R.id.searchSimpleEditText);
        productsRecyclerView = view.findViewById(R.id.productsRecyclerView);
        emptyListTextView = view.findViewById(R.id.emptyListTextView);

        // Kaydedilmiş ürünleri yükle
        loadSavedProducts();

        productAdapter = new SimpleProductAdapter(productList, new SimpleProductAdapter.OnProductClickListener() {
            @Override
            public void onIncreaseClick(int position) {
                SimpleProductItem item = productList.get(position);
                item.setQuantity(item.getQuantity() + 1);
                productAdapter.notifyItemChanged(position);
                updateEmptyListVisibility();

                // SharedPreferences'a kaydet
                saveProduct(item);

                Log.d("SimpleListFragment", "Ürün miktarı artırıldı: " + item.getName() + " - " + item.getQuantity());
            }

            @Override
            public void onDecreaseClick(int position) {
                SimpleProductItem item = productList.get(position);
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    productAdapter.notifyItemChanged(position);

                    // SharedPreferences'a kaydet
                    saveProduct(item);

                    Log.d("SimpleListFragment", "Ürün miktarı azaltıldı: " + item.getName() + " - " + item.getQuantity());
                } else {
                    // Adet 1 ise ürünü listeden kaldır
                    String productName = item.getName();
                    productList.remove(position);
                    productAdapter.notifyItemRemoved(position);
                    updateEmptyListVisibility();

                    // SharedPreferences'tan sil
                    removeProduct(productName);

                    Toast.makeText(getContext(), productName + " listeden çıkarıldı", Toast.LENGTH_SHORT).show();
                    Log.d("SimpleListFragment", "Ürün listeden silindi: " + productName);
                }
            }
        });

        productsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productsRecyclerView.setAdapter(productAdapter);

        // Başlangıçta boş liste mesajını güncelle
        updateEmptyListVisibility();
        setupSearchEditText();
    }

    /**
     * Kaydedilmiş ürünleri yükle
     */
    private void loadSavedProducts() {
        if (basketName != null) {
            productList = new ArrayList<>(productPreferences.loadProductsForBasket(basketName));

            Log.d("SimpleListFragment", "Sepet için yüklenen ürün sayısı: " + productList.size() +
                    " - Sepet: " + basketName);

            if (productList.size() > 0) {
                Toast.makeText(getContext(), productList.size() + " kayıtlı ürün yüklendi",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            productList = new ArrayList<>();
            Log.w("SimpleListFragment", "Sepet adı null, boş liste oluşturuldu");
        }
    }

    /**
     * Tek bir ürünü kaydet
     */
    private void saveProduct(SimpleProductItem product) {
        if (basketName != null && product != null) {
            productPreferences.updateProductInBasket(basketName, product);
        }
    }

    /**
     * Ürünü sil
     */
    private void removeProduct(String productName) {
        if (basketName != null && productName != null) {
            productPreferences.removeProductFromBasket(basketName, productName);
        }
    }

    /**
     * Tüm ürünleri kaydet
     */
    private void saveAllProducts() {
        if (basketName != null) {
            productPreferences.saveProductsForBasket(basketName, productList);
            Log.d("SimpleListFragment", "Tüm ürünler kaydedildi - Sepet: " + basketName +
                    ", Ürün sayısı: " + productList.size());
        }
    }

    private void setupSearchEditText() {
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                                event.getAction() == KeyEvent.ACTION_DOWN)) {

                    String productName = searchEditText.getText().toString().trim();
                    if (!productName.isEmpty()) {
                        addOrUpdateProduct(productName);
                        searchEditText.setText("");
                        hideKeyboard();
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    /**
     * Ürün ekle veya güncelle
     */
    private void addOrUpdateProduct(String productName) {
        // Aynı ürün var mı kontrol et
        boolean productExists = false;
        for (SimpleProductItem item : productList) {
            if (item.getName().equalsIgnoreCase(productName)) {
                item.setQuantity(item.getQuantity() + 1);
                productAdapter.notifyDataSetChanged();
                productExists = true;

                // SharedPreferences'a kaydet
                saveProduct(item);

                Toast.makeText(getContext(), productName + " miktarı artırıldı (" + item.getQuantity() + ")",
                        Toast.LENGTH_SHORT).show();

                Log.d("SimpleListFragment", "Mevcut ürün miktarı artırıldı: " + productName + " - " + item.getQuantity());
                break;
            }
        }

        // Ürün yoksa yeni ekle
        if (!productExists) {
            SimpleProductItem newProduct = new SimpleProductItem(productName, 1);
            productList.add(newProduct);
            productAdapter.notifyItemInserted(productList.size() - 1);

            // SharedPreferences'a kaydet
            saveProduct(newProduct);

            Toast.makeText(getContext(), productName + " listeye eklendi", Toast.LENGTH_SHORT).show();

            Log.d("SimpleListFragment", "Yeni ürün eklendi: " + productName);
        }

        updateEmptyListVisibility();
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && searchEditText != null) {
            inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        }
    }

    private void updateEmptyListVisibility() {
        if (productList.isEmpty()) {
            emptyListTextView.setVisibility(View.VISIBLE);
            productsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyListTextView.setVisibility(View.GONE);
            productsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Tüm ürünleri temizle
     */
    public void clearAllProducts() {
        if (basketName != null) {
            productPreferences.clearProductsForBasket(basketName);
            productList.clear();
            productAdapter.notifyDataSetChanged();
            updateEmptyListVisibility();

            Toast.makeText(getContext(), "Tüm ürünler temizlendi", Toast.LENGTH_SHORT).show();
            Log.d("SimpleListFragment", "Sepetteki tüm ürünler temizlendi: " + basketName);
        }
    }

    /**
     * Sepet istatistiklerini al
     */
    public void showBasketStats() {
        if (basketName != null) {
            int productCount = productPreferences.getProductCountForBasket(basketName);
            int totalQuantity = productPreferences.getTotalQuantityForBasket(basketName);

            String message = "Sepet: " + basketName + "\n" +
                    "Farklı ürün sayısı: " + productCount + "\n" +
                    "Toplam adet: " + totalQuantity;

            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            Log.d("SimpleListFragment", message);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Fragment'tan çıkarken tüm ürünleri kaydet
        saveAllProducts();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Fragment yok edilirken son kez kaydet
        saveAllProducts();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fragment'a geri dönüldüğünde verileri tekrar yükle
        if (basketName != null) {
            List<SimpleProductItem> savedProducts = productPreferences.loadProductsForBasket(basketName);
            if (savedProducts.size() != productList.size()) {
                productList.clear();
                productList.addAll(savedProducts);
                if (productAdapter != null) {
                    productAdapter.notifyDataSetChanged();
                    updateEmptyListVisibility();
                }
            }
        }
    }
}