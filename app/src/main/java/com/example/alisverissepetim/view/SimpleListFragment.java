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
import java.util.ArrayList;
import java.util.List;
import com.example.alisverissepetim.R;
import com.example.alisverissepetim.adapter.SimpleProductAdapter;
import com.example.alisverissepetim.model.SimpleProductItem;

public class SimpleListFragment extends Fragment {

    private EditText searchEditText;
    private RecyclerView productsRecyclerView;
    private TextView emptyListTextView;
    private SimpleProductAdapter productAdapter;
    private List<SimpleProductItem> productList;

    public SimpleListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String sepetAdi = DetailFragmentArgs.fromBundle(getArguments()).getSepetAdi();
            String sepetTur = DetailFragmentArgs.fromBundle(getArguments()).getSepetTuru();
            Log.d("SimpleListFragment", "Gelen sepet adı: " + sepetAdi);
            Log.d("SimpleListFragment", "Gelen sepet türü: " + sepetTur);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchEditText = view.findViewById(R.id.searchSimpleEditText);
        productsRecyclerView = view.findViewById(R.id.productsRecyclerView);
        emptyListTextView = view.findViewById(R.id.emptyListTextView);

        productList = new ArrayList<>();
        productAdapter = new SimpleProductAdapter(productList, new SimpleProductAdapter.OnProductClickListener() {
            @Override
            public void onIncreaseClick(int position) {
                SimpleProductItem item = productList.get(position);
                item.setQuantity(item.getQuantity() + 1);
                productAdapter.notifyItemChanged(position);
                updateEmptyListVisibility();
            }

            @Override
            public void onDecreaseClick(int position) {
                SimpleProductItem item = productList.get(position);
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    productAdapter.notifyItemChanged(position);
                } else {
                    // Adet 1 ise ürünü listeden kaldır
                    productList.remove(position);
                    productAdapter.notifyItemRemoved(position);
                    updateEmptyListVisibility();
                }
            }
        });

        productsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productsRecyclerView.setAdapter(productAdapter);

        // Başlangıçta boş liste mesajını göster
        updateEmptyListVisibility();

        setupSearchEditText();
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
                        // Aynı ürün var mı kontrol et
                        boolean productExists = false;
                        for (SimpleProductItem item : productList) {
                            if (item.getName().equalsIgnoreCase(productName)) {
                                item.setQuantity(item.getQuantity() + 1);
                                productAdapter.notifyDataSetChanged();
                                productExists = true;
                                break;
                            }
                        }

                        // Ürün yoksa yeni ekle
                        if (!productExists) {
                            productList.add(new SimpleProductItem(productName, 1));
                            productAdapter.notifyItemInserted(productList.size() - 1);
                        }

                        updateEmptyListVisibility();
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

    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    searchEditText.getWindowToken(), 0);
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
}