package com.example.alisverissepetim.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton; // Eklendi
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.alisverissepetim.databinding.RecyclerRowBinding;
import com.example.alisverissepetim.model.ShoppingList;
import java.util.ArrayList;
// CartManager importu eksik, ekleyin
import com.example.alisverissepetim.manager.CartManager; // EKLENDİ

public class RecyclerviewAdapter extends RecyclerView.Adapter<RecyclerviewAdapter.RowHolder> {

    private final ArrayList<ShoppingList> shoppingList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ShoppingList sepet);
        void onCartIconClick(ShoppingList sepet);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public RecyclerviewAdapter(ArrayList<ShoppingList> shoppingList) {
        this.shoppingList = (shoppingList != null) ? shoppingList : new ArrayList<>();
    }

    @NonNull
    @Override
    public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RowHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RowHolder holder, int position) {
        ShoppingList sepet = shoppingList.get(position);
        holder.sepetAdi.setText(sepet.getBasketName());
        holder.sepetTur.setText(sepet.getBasketTur());

        // Ana öğeye tıklama olayı
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onItemClick(shoppingList.get(holder.getAdapterPosition()));
            }
        });

        // CartManager'dan ürün sayısını al
        int itemCountInCart = CartManager.getInstance().getTotalItemCount(sepet.getBasketName());

        if (itemCountInCart > 0) {
            holder.btnGoToCart.setVisibility(View.VISIBLE);
            holder.btnGoToCart.setOnClickListener(v -> {
                if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onCartIconClick(shoppingList.get(holder.getAdapterPosition()));
                }
            });
        } else {
            holder.btnGoToCart.setVisibility(View.GONE);
            holder.btnGoToCart.setOnClickListener(null); // Listener'ı temizle
        }

        // System.out.println("onBindViewHolder: " + sepet.getBasketName() + " / " + sepet.getBasketTur() + " - Ürün Sayısı: " + itemCountInCart);
    }

    @Override
    public int getItemCount() {
        return shoppingList.size();
    }

    public void addItem(ShoppingList newItem) {
        if (newItem != null) {
            shoppingList.add(newItem);
            notifyItemInserted(shoppingList.size() - 1);
        }
    }

    // Listeyi tamamen yenilemek için (örneğin onResume'da kullanılabilir)
    public void updateList(ArrayList<ShoppingList> newList) {
        shoppingList.clear();
        if (newList != null) {
            shoppingList.addAll(newList);
        }
        notifyDataSetChanged(); // Daha verimli güncellemeler için DiffUtil kullanabilirsiniz
    }


    public static class RowHolder extends RecyclerView.ViewHolder {
        TextView sepetAdi, sepetTur;
        Button btnGoToCart; // Eklendi

        public RowHolder(RecyclerRowBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot());
            sepetAdi = recyclerRowBinding.txtSepetAdi;
            sepetTur = recyclerRowBinding.txtSepetTur;
            btnGoToCart = recyclerRowBinding.btnGoToCart;
        }
    }
}
