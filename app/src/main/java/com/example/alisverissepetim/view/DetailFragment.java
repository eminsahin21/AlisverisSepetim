package com.example.alisverissepetim.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alisverissepetim.R;
import com.example.alisverissepetim.adapter.CategoryAdapter;
import com.example.alisverissepetim.adapter.ProductAdapter;
import com.example.alisverissepetim.model.Product;

import java.util.ArrayList;
import java.util.List;

public class DetailFragment extends Fragment {

    private RecyclerView recyclerViewCategory, recyclerViewProducts;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private ArrayList<Product> allProducts = new ArrayList<>();
    private ArrayList<Product> filteredProducts = new ArrayList<>();
    private ArrayList<String> categoryList = new ArrayList<>();
    private String selectedCategory = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Safe Args ile gelen verileri al
        String sepetAdi = DetailFragmentArgs.fromBundle(getArguments()).getSepetAdi();
        String sepetTur = DetailFragmentArgs.fromBundle(getArguments()).getSepetTuru();

        // Kullan örneğin:
        System.out.println("Gelen sepet adı: " + sepetAdi);
        System.out.println("Gelen sepet türü: " + sepetTur);

        recyclerViewCategory = view.findViewById(R.id.recyclerViewCategory);
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);

        recyclerViewCategory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        setupCategoryList();
        setupProductList();

        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            selectedCategory = category;
            filterProducts();
        });

        productAdapter = new ProductAdapter(filteredProducts);

        recyclerViewCategory.setAdapter(categoryAdapter);
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void setupCategoryList() {
        categoryList.add("Temel Gıda");
        categoryList.add("Temizlik");
        categoryList.add("Et-Tavuk");
        categoryList.add("Sebze");
    }

    private void setupProductList() {
        allProducts.add(new Product("Elma", "https://example.com/elma.jpg", "Temel Gıda"));
        allProducts.add(new Product("Armut", "https://example.com/armut.jpg", "Temel Gıda"));
        allProducts.add(new Product("Deterjan", "https://example.com/deterjan.jpg", "Temizlik"));
        allProducts.add(new Product("Tavuk", "https://example.com/tavuk.jpg", "Et-Tavuk"));

        filteredProducts.addAll(allProducts); // Başta tüm ürünler
    }

    private void filterProducts() {
        filteredProducts.clear();

        for (Product p : allProducts) {
            if (selectedCategory.isEmpty() || p.getCategory().equals(selectedCategory)) {
                filteredProducts.add(p);
            }
        }

        productAdapter.updateList(new ArrayList<>(filteredProducts));
    }
}
