package com.example.alisverissepetim.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.alisverissepetim.databinding.RecyclerRowBinding;
import com.example.alisverissepetim.model.ShoppingList;
import java.util.ArrayList;

public class RecyclerviewAdapter extends RecyclerView.Adapter<RecyclerviewAdapter.RowHolder> {

    private ArrayList<ShoppingList> shoppingList = new ArrayList<>();

    public void clearItems() {
        shoppingList.clear(); // Listeyi temizle
        notifyDataSetChanged(); // RecyclerView'i güncelle
    }

    public RecyclerviewAdapter(ArrayList<ShoppingList> shoppingList) {
        this.shoppingList = shoppingList;
    }

    @NonNull
    @Override
    public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RowHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RowHolder holder, int position) {
        ShoppingList item = shoppingList.get(position);
        holder.recyclerRowBinding.sepetName.setText(item.basketName);
    }

    @Override
    public int getItemCount() {
        return shoppingList.size();
    }

    public void addItem(ShoppingList newItem) {
        shoppingList.add(newItem);
        notifyDataSetChanged();
    }

    public static class RowHolder extends RecyclerView.ViewHolder {
        RecyclerRowBinding recyclerRowBinding;

        public RowHolder(RecyclerRowBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot());
            this.recyclerRowBinding = recyclerRowBinding;
        }
    }
}
