package com.example.alisverissepetim.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.example.alisverissepetim.databinding.RecyclerRowBinding;
import com.example.alisverissepetim.model.ShoppingList;
import java.util.ArrayList;
import com.example.alisverissepetim.R;



public class RecyclerviewAdapter extends RecyclerView.Adapter<RecyclerviewAdapter.RowHolder> {

    private final ArrayList<ShoppingList> shoppingList;

    private OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(ShoppingList sepet);
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

        // Tıklama olayı
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onItemClick(shoppingList.get(holder.getAdapterPosition()));
            }
        });

        System.out.println("onBindViewHolder: " + sepet.getBasketName() + " / " + sepet.getBasketTur());
    }

    @Override
    public int getItemCount() {
        return shoppingList.size();
    }

    public void addItem(ShoppingList newItem) {
        if (newItem != null) {
            shoppingList.add(newItem);
            notifyItemInserted(shoppingList.size() - 1); // Daha performanslı güncelleme
        }
    }

    public static class RowHolder extends RecyclerView.ViewHolder {
        TextView sepetAdi, sepetTur;

        public RowHolder(RecyclerRowBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot());
            sepetAdi = recyclerRowBinding.txtSepetAdi;
            sepetTur = recyclerRowBinding.txtSepetTur;
        }
    }
} 