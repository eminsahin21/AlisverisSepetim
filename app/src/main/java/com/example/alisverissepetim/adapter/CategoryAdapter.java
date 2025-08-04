package com.example.alisverissepetim.adapter;

import android.graphics.Color; // Renk ayarları için
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.alisverissepetim.R;
import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private ArrayList<String> categoryList;
    private OnCategoryClickListener listener;
    private String selectedCategory = "Tümü"; // Varsayılan olarak "Tümü" kategorisi seçili olacak

    // --- OnCategoryClickListener Arayüzü ---
    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryName);
    }

    // --- Constructor ---
    public CategoryAdapter(ArrayList<String> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    // --- Seçili Kategoriyi Ayarlama Metodu ---
    // DetailFragment'tan çağrılarak seçilen kategoriyi günceller ve UI'ı yeniler
    public void setSelectedCategory(String category) {
        this.selectedCategory = category;
        notifyDataSetChanged(); // Adapter'ı yeniden çizerek görünümü günceller
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categoryList.get(position);
        holder.categoryName.setText(category);

        // --- Seçili Kategoriye Göre Görsel Güncelleme ---
        if (category.equals(selectedCategory)) {
            // Seçili kategoriye özel arkaplan ve metin rengi
            // Örneğin, arkaplanı vurgula ve metni beyaz yap
            holder.itemView.setBackgroundResource(R.drawable.bg_category_selected);
            holder.categoryName.setTextColor(Color.WHITE);
        } else {
            // Seçili olmayan kategoriler için varsayılan arkaplan ve metin rengi
            // Örneğin, varsayılan arkaplan ve metni siyah yap
            holder.itemView.setBackgroundResource(R.drawable.bg_category_default);
            holder.categoryName.setTextColor(Color.BLACK);
        }

        // --- Tıklama Dinleyicisi ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
            // Tıklama sonrası seçili kategoriyi güncelle ve UI'ı yenile
            setSelectedCategory(category);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    // --- ViewHolder Sınıfı ---
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.txtCategoryName);
        }
    }
}