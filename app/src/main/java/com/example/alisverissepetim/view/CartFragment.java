package com.example.alisverissepetim.view;

import android.os.Bundle;
import android.util.Log;
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
    private String currentShoppingListName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            CartFragmentArgs args = CartFragmentArgs.fromBundle(getArguments());
            currentShoppingListName = args.getShoppingListName(); // Safe Args ile gelen sepet adını al
            // Eğer ikinci argümanı (basketTur) kullanacaksanız onu da alın:
            // String basketType = args.getBasketTur();
            if (currentShoppingListName == null || currentShoppingListName.isEmpty()) {
                // Eğer sepet adı gelmezse bir varsayılan işlem yap veya hata göster
                Log.d("CartFragment", "Sepet adı argümanı boş veya null!");
                // Belki kullanıcıyı geri yönlendirebilirsiniz.
                // Navigation.findNavController(view).popBackStack();
                // return;
                // Ya da bir varsayılan sepet adı kullanabilirsiniz (önerilmez)
                // currentShoppingListName = "default_cart";
            }
        } else {
            Log.d("CartFragment", "Argümanlar CartFragment'a ulaşmadı!");
            // Hata yönetimi
            // Navigation.findNavController(view).popBackStack();
            // return;
        }


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

        // currentShoppingListName'i null kontrolünden sonra kullan
        if (currentShoppingListName == null || currentShoppingListName.isEmpty()) {
            Log.d("CartFragment", "setupRecyclerView: currentShoppingListName boş veya null. Adapter oluşturulamıyor.");
            // Uygun bir hata mesajı göster veya fragment'ı sonlandır
            txtEmptyCart.setText("Sepet bilgisi yüklenemedi.");
            txtEmptyCart.setVisibility(View.VISIBLE);
            recyclerViewCart.setVisibility(View.GONE);
            // Diğer UI elemanlarını da gizle
            return;
        }

        cartAdapter = new CartAdapter(
                getContext(),
                currentShoppingListName, // shoppingListName'i adapter'a ver
                CartManager.getInstance().getCartItems(currentShoppingListName), // Doğru sepetin ürünlerini al
                new CartAdapter.OnCartItemClickListener() {
                    @Override
                    public void onIncreaseClick(String productKey) {
                        CartManager.getInstance().increaseQuantity(currentShoppingListName, productKey);
                        updateCartUI();
                    }

                    @Override
                    public void onDecreaseClick(String productKey) {
                        CartManager.getInstance().decreaseQuantity(currentShoppingListName, productKey);
                        updateCartUI();
                    }
                }
        );

        recyclerViewCart.setAdapter(cartAdapter);
    }


    private void setupButtons(@NonNull View view) {
        btnCheckout.setOnClickListener(v -> {
            // currentShoppingListName null değilse devam et
            if (currentShoppingListName != null && !CartManager.getInstance().isCartEmpty(currentShoppingListName)) {
                Toast.makeText(getContext(), "Sipariş veriliyor...", Toast.LENGTH_SHORT).show();
            }
        });
        btnClearCart.setOnClickListener(v -> {
            // currentShoppingListName null değilse devam et
            if (currentShoppingListName != null && !CartManager.getInstance().isCartEmpty(currentShoppingListName)) {
                CartManager.getInstance().clearCart(currentShoppingListName);
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

        List<CartManager.CartItem> cartItems = CartManager.getInstance().getCartItems(currentShoppingListName);

        if (currentShoppingListName == null || currentShoppingListName.isEmpty()) {
            Log.w("CartFragment", "updateCartUI: currentShoppingListName boş veya null.");
            // UI'ı boş sepet durumuna getir veya hata mesajı göster
            recyclerViewCart.setVisibility(View.GONE);
            txtEmptyCart.setText("Sepet bilgisi yüklenemedi.");
            txtEmptyCart.setVisibility(View.VISIBLE);
            btnCheckout.setVisibility(View.GONE);
            btnClearCart.setVisibility(View.GONE);
            txtTotalPrice.setVisibility(View.GONE);
            return;
        }

        if (cartItems.isEmpty()) {
            // Sepet boş
            recyclerViewCart.setVisibility(View.GONE);
            txtEmptyCart.setVisibility(View.VISIBLE);
            btnCheckout.setVisibility(View.GONE);
            btnClearCart.setVisibility(View.GONE);
            txtTotalPrice.setVisibility(View.GONE);
        } else {
            cartAdapter.updateCartItems(cartItems); // Bu metod adapter içinde shoppingListName'e ihtiyaç duymayabilir, çünkü adapter zaten kendi listesini yönetir.
            double totalPrice = calculateTotalPrice(cartItems);
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