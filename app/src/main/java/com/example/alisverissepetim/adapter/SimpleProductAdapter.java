package com.example.alisverissepetim.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.alisverissepetim.model.SimpleProductItem;
import java.util.List;
import com.example.alisverissepetim.R;

public class SimpleProductAdapter extends RecyclerView.Adapter<SimpleProductAdapter.ProductViewHolder> {

    private List<SimpleProductItem> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onIncreaseClick(int position);
        void onDecreaseClick(int position);
    }

    public SimpleProductAdapter(List<SimpleProductItem> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_list_items, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        SimpleProductItem product = productList.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private TextView productNameTextView;
        private TextView quantityTextView;
        private ImageButton decreaseButton; // ImageButton olarak değiştirdim
        private ImageButton increaseButton; // ImageButton olarak değiştirdim

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            decreaseButton = itemView.findViewById(R.id.decreaseButton);
            increaseButton = itemView.findViewById(R.id.increaseButton);
        }

        public void bind(SimpleProductItem product, OnProductClickListener listener) {
            productNameTextView.setText(product.getName());
            quantityTextView.setText(String.valueOf(product.getQuantity()));

            increaseButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onIncreaseClick(position);
                }
            });

            decreaseButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDecreaseClick(position);
                }
            });
        }
    }
}