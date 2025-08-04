package com.example.alisverissepetim.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alisverissepetim.R;
import com.example.alisverissepetim.adapter.CartAdapter;
import com.example.alisverissepetim.manager.CartManager;

import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView recyclerViewCart;
    private TextView txtTotalPrice, txtEmptyCart;
    private Button btnCheckout, btnClearCart;

    private CartAdapter cartAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupButtons(view); // view'ı parametre olarak gönderiyoruz
        updateCartUI();
    }

    private void initializeViews(View view) {
        recyclerViewCart = view.findViewById(R.id.recyclerViewCart);
        txtTotalPrice = view.findViewById(R.id.txtTotalPrice);
        txtEmptyCart = view.findViewById(R.id.txtEmptyCart);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        btnClearCart = view.findViewById(R.id.btnClearCart);
    }

    private void setupRecyclerView() {
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));

        // CartAdapter'ı oluştururken 'getContext()' ile Context'i ekleyin
        cartAdapter = new CartAdapter(getContext(), CartManager.getInstance().getCartItems(), new CartAdapter.OnCartItemClickListener() {
            @Override
            public void onIncreaseClick(String productKey) {
                CartManager.getInstance().increaseQuantity(productKey);
                updateCartUI();
            }

            @Override
            public void onDecreaseClick(String productKey) {
                CartManager.getInstance().decreaseQuantity(productKey);
                updateCartUI();
            }
        });

        recyclerViewCart.setAdapter(cartAdapter);
    }

    private void setupButtons(@NonNull View view) {
        btnCheckout.setOnClickListener(v -> {
            if (!CartManager.getInstance().isEmpty()) {
                Toast.makeText(getContext(), "Sipariş veriliyor...", Toast.LENGTH_SHORT).show();
                // Burada sipariş verme işlemi yapılabilir
            }
        });

        btnClearCart.setOnClickListener(v -> {
            if (!CartManager.getInstance().isEmpty()) {
                CartManager.getInstance().clearCart();
                updateCartUI();
                Toast.makeText(getContext(), "Sepet temizlendi", Toast.LENGTH_SHORT).show();
            }
        });

        // Geri butonu
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Navigation.findNavController(v).popBackStack();
            });
        }
    }

    private void updateCartUI() {
        List<CartManager.CartItem> cartItems = CartManager.getInstance().getCartItems();

        if (cartItems.isEmpty()) {
            // Sepet boş
            recyclerViewCart.setVisibility(View.GONE);
            txtEmptyCart.setVisibility(View.VISIBLE);
            btnCheckout.setVisibility(View.GONE);
            btnClearCart.setVisibility(View.GONE);
            txtTotalPrice.setVisibility(View.GONE);
        } else {
            // Sepet dolu
            recyclerViewCart.setVisibility(View.VISIBLE);
            txtEmptyCart.setVisibility(View.GONE);
            btnCheckout.setVisibility(View.VISIBLE);
            btnClearCart.setVisibility(View.VISIBLE);
            txtTotalPrice.setVisibility(View.VISIBLE);

            // Adapter'ı güncelle
            cartAdapter.updateCartItems(cartItems);

            // Toplam fiyatı hesapla
            double totalPrice = calculateTotalPrice(cartItems);
            System.out.println("Toplam: " + String.format("%.2f", totalPrice) + " TL");
            txtTotalPrice.setText("Toplam: " + String.format("%.2f", totalPrice) + " TL");
        }
    }

    private double calculateTotalPrice(List<CartManager.CartItem> cartItems) {
        double total = 0.0;
        for (CartManager.CartItem item : cartItems) {
            String fiyatGirdi = item.getProduct().getFiyat();
            if (fiyatGirdi != null && !fiyatGirdi.isEmpty()) { // isEmpty kontrolü eklendi
                // TL simgesini ve diğer gereksiz karakterleri temizle (opsiyonel ama önerilir)
                // Örnek: "40,00 TL" -> "40,00"
                fiyatGirdi = fiyatGirdi.replaceAll("[^\\d,]", ""); // Sadece rakam ve virgül kalsın

                // Virgülü nokta ile değiştir
                String parsableFiyat = fiyatGirdi.replace(',', '.');

                try {
                    double price = Double.parseDouble(parsableFiyat);
                    System.out.println("Fiyat Cart Fragment (parsed): " + price);
                    total += price * item.getQuantity();
                } catch (NumberFormatException e) {
                    // Fiyat parse edilemezse hatayı logla ve 0 olarak hesapla
                    System.err.println("Fiyat parse edilemedi: " + fiyatGirdi + " -> " + parsableFiyat);
                    e.printStackTrace(); // Hatanın detaylarını görmek için
                    total += 0.0; // Veya bir hata durumu yönetimi yapın
                }
            } else {
                System.err.println("Ürün fiyatı boş veya null: " + (item.getProduct() != null ? item.getProduct().getUrun_adi() : "Bilinmeyen Ürün"));
            }
        }
        return total;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fragment'a geri dönüldüğünde sepeti güncelle
        updateCartUI();
    }
}