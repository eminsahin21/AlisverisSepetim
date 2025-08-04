package com.example.alisverissepetim.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Glide import edildi
import com.example.alisverissepetim.R;
import com.example.alisverissepetim.model.Product;
import com.example.alisverissepetim.manager.CartManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartManager.CartItem> cartItems;
    private OnCartItemClickListener listener;
    private final Map<String, String> marketDisplayNames; // final yapıldı ve constructor'da initialize edilecek
    private final Context context; // Glide için context

    public interface OnCartItemClickListener {
        void onIncreaseClick(String productKey);
        void onDecreaseClick(String productKey);
    }

    public CartAdapter(Context context, List<CartManager.CartItem> cartItems, OnCartItemClickListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;

        // Market isimleri için map burada, sadece bir kere oluşturulacak
        this.marketDisplayNames = new HashMap<>();
        marketDisplayNames.put("carrefour", "CarrefourSA");
        marketDisplayNames.put("carrefoursa", "CarrefourSA");
        marketDisplayNames.put("bim", "BİM");
        marketDisplayNames.put("a101", "A101");
        marketDisplayNames.put("migros", "Migros");
        marketDisplayNames.put("sok", "ŞOK");
        marketDisplayNames.put("şok", "ŞOK"); // Türkçe karakterli varyasyon
        marketDisplayNames.put("real", "Real");
        marketDisplayNames.put("macro", "Macro Center");
        marketDisplayNames.put("macrocenter", "Macro Center");
        marketDisplayNames.put("onur", "Onur Market");
        marketDisplayNames.put("onur_market", "Onur Market");
        marketDisplayNames.put("tarim_kredi", "Tarım Kredi Koop."); // Daha kısa bir gösterim
        marketDisplayNames.put("tarım_kredi_market", "Tarım Kredi Koop.");
        marketDisplayNames.put("tarim kredi", "Tarım Kredi Koop.");
        marketDisplayNames.put("hakmar", "Hakmar");
        // Diğer marketler ve olası anahtar varyasyonları
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartManager.CartItem cartItem = cartItems.get(position);
        Product product = cartItem.getProduct(); // Product objesini alalım

        if (product == null) {
            // Eğer ürün bilgisi null ise, boş bir view göster veya hata mesajı
            holder.txtProductName.setText("Ürün bilgisi yok");
            holder.txtMarketName.setText("");
            holder.txtQuantity.setText("0");
            holder.txtPrice.setText("0.00 TL");
            holder.txtUnitPrice.setText("Birim: 0.00 TL");
            holder.imageProduct.setImageResource(R.drawable.ic_placeholder); // Varsayılan ürün görseli
            return; // Metodun devamını işleme
        }

        // 1. Ürün Görseli (Market Logosu Değil)
        if (product.getUrun_gorsel() != null && !product.getUrun_gorsel().isEmpty()) {
            Glide.with(context) // Adapter constructor'ından alınan context kullanılıyor
                    .load(product.getUrun_gorsel())
                    .placeholder(R.drawable.ic_placeholder) // Yüklenirken gösterilecek varsayılan ürün görseli
                    .error(R.drawable.supermarketsepet) // Hata durumunda gösterilecek varsayılan ürün görseli
                    .into(holder.imageProduct);
        } else {
            holder.imageProduct.setImageResource(R.drawable.supermarketsepet); // Varsayılan ürün görseli
        }


        // 3. Ürün Adı
        holder.txtProductName.setText(product.getUrun_adi());

        // 4. Market Adı (Düzenlenmiş)
        String marketKeyForDisplayName = product.getMarket_adi().toLowerCase().replace(" ", "_");
        String displayName = marketDisplayNames.get(marketKeyForDisplayName);
        if (displayName == null || displayName.isEmpty()) {
            displayName = product.getMarket_adi(); // Orijinal ismi fallback olarak kullan
        }
        holder.txtMarketName.setText(displayName);

        // 5. Miktar
        holder.txtQuantity.setText(String.valueOf(cartItem.getQuantity()));

        // 6. Fiyat ve Birim Fiyat
        try {
            String fiyatStr = product.getFiyat().replaceAll("[^\\d,]", ""); // Sadece rakam ve virgül kalsın
            String parsableFiyat = fiyatStr.replace(',', '.');
            System.out.println("Fiyat: " + parsableFiyat);
            double unitPrice = Double.parseDouble(parsableFiyat); // Bu birim fiyattır
            double totalPrice = unitPrice * cartItem.getQuantity();

            holder.txtPrice.setText(totalPrice + " TL");
            holder.txtUnitPrice.setText("Birim:"+unitPrice + " TL"); // Birim fiyatı göster
        } catch (NumberFormatException e) {
            holder.txtPrice.setText("G TL");
            holder.txtUnitPrice.setText("Birim: G TL");
        }

        // 7. Artırma ve Azaltma Butonları
        // Listener null değilse ve productKey geçerliyse tıklama olaylarını ayarla
        if (listener != null && cartItem.getProductKey() != null && !cartItem.getProductKey().isEmpty()) {
            holder.btnIncrease.setOnClickListener(v -> listener.onIncreaseClick(cartItem.getProductKey()));
            holder.btnDecrease.setOnClickListener(v -> listener.onDecreaseClick(cartItem.getProductKey()));
            holder.btnIncrease.setEnabled(true);
            holder.btnDecrease.setEnabled(true);
        } else {
            // Eğer listener veya productKey yoksa butonları devre dışı bırak
            holder.btnIncrease.setOnClickListener(null);
            holder.btnDecrease.setOnClickListener(null);
            holder.btnIncrease.setEnabled(false);
            holder.btnDecrease.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems == null ? 0 : cartItems.size();
    }

    public void updateCartItems(List<CartManager.CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged(); // Daha verimli güncellemeler için DiffUtil düşünülebilir
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView txtProductName, txtMarketName, txtPrice, txtUnitPrice, txtQuantity;
        ImageView imageProduct;
        ImageView btnIncrease, btnDecrease;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            txtProductName = itemView.findViewById(R.id.txtCartProductName);
            imageProduct = itemView.findViewById(R.id.imageCartProductLogo);
            txtMarketName = itemView.findViewById(R.id.txtCartMarketName);
            txtPrice = itemView.findViewById(R.id.txtCartPrice);
            txtUnitPrice = itemView.findViewById(R.id.txtCartUnitPrice);
            txtQuantity = itemView.findViewById(R.id.txtCartQuantity);
            btnIncrease = itemView.findViewById(R.id.btnCartIncrease);
            btnDecrease = itemView.findViewById(R.id.btnCartDecrease);
        }
    }
}
