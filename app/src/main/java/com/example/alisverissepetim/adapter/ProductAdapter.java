package com.example.alisverissepetim.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.alisverissepetim.R;
import com.example.alisverissepetim.model.Product;
import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private ArrayList<Product> productList;
    private OnProductClickListener onProductClickListener;

    // Interface for button clicks
    public interface OnProductClickListener {
        void onAddToCartClick(Product product);
    }

    public ProductAdapter(ArrayList<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.onProductClickListener = listener;
    }

    // Eski constructor (geriye uyumluluk için)
    public ProductAdapter(ArrayList<Product> productList) {
        this.productList = productList;
        this.onProductClickListener = null;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Ürün bilgilerini set et
        holder.txtProductName.setText(product.getUrun_adi());
        holder.txtProductPrice.setText(product.getFiyat());
        holder.txtMarketName.setText(product.getMarket_adi());

        // Sepete ekle butonuna tıklama
        holder.btnAdd.setOnClickListener(v -> {
            if (onProductClickListener != null) {
                onProductClickListener.onAddToCartClick(product);
            }
        });

        // Market adını düzeltilmiş isimle set et
        String displayMarketName = getMarketDisplayName(product.getMarket_adi());
        holder.txtMarketName.setText(displayMarketName);

        // Market logosunu drawable'dan yükle
        int marketLogoResId = getMarketLogoResource(product.getMarket_adi());
        if (marketLogoResId != 0) {
            holder.imageMarketLogo.setImageResource(marketLogoResId);
        } else {
            // Eşleşen logo bulunamazsa varsayılan bir resim göster
            holder.imageMarketLogo.setImageResource(R.drawable.ic_placeholder);
        }

        // Ürün resmini yükle
        if (product.getUrun_gorsel() != null && !product.getUrun_gorsel().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getUrun_gorsel())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .into(holder.imageProduct);
        } else {
            holder.imageProduct.setImageResource(R.drawable.supermarketsepet);
        }
    }

    // Market logosu için drawable ID'si döndüren yardımcı metod
    private int getMarketLogoResource(String marketName) {
        if (marketName == null || marketName.isEmpty()) {
            return 0;
        }

        switch (marketName.toLowerCase()) {
            case "carrefour":
                return R.drawable.carrefour_logo;
            case "bim":
                return R.drawable.bim_logo;
            case "hakmar":
                return R.drawable.hakmar_logo;
            case "a101":
                return R.drawable.a101_logo;
            case "migros":
                return R.drawable.migros_logo;
            case "sok":
                return R.drawable.sok_logo;
            case "tarim_kredi":
                return R.drawable.tarim_kredi_logo;
            default:
                return R.drawable.supermarketsepet; // Eşleşme yoksa 0 döndür
        }
    }

    // Market adını düzenleyen yardımcı metod
    private String getMarketDisplayName(String marketName) {
        if (marketName == null || marketName.isEmpty()) {
            return "";
        }

        switch (marketName) {
            case "carrefour":
                return "CarrefourSA";
            case "bim":
                return "BİM";
            case "a101":
                return "A101";
            case "migros":
                return "Migros";
            case "sok":
                return "ŞOK";
            case "hakmar":
                return "Hakmar";
            case "real":
                return "Real";
            case "macro":
                return "Macro Center";
            case "onur":
                return "Onur Market";
            case "tarim_kredi":
                return "Tarım Kredi Market";
            default:
                return marketName; // Eşleşme yoksa orijinal ismi döndür
        }
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    // Listeyi güncelle
    public void updateList(ArrayList<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    // Memory leak önlemek için cleanup
    public void cleanup() {
        if (productList != null) {
            productList.clear();
        }
        onProductClickListener = null;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView txtProductName, txtProductPrice, txtMarketName;
        ImageView imageProduct, imageMarketLogo, btnAdd;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
            txtMarketName = itemView.findViewById(R.id.txtMarketName);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            imageMarketLogo = itemView.findViewById(R.id.imageMarketLogo);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}