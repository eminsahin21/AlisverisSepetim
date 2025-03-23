package com.example.alisverissepetim.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import com.example.alisverissepetim.R;
import com.google.android.engage.shopping.datamodel.ShoppingList;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.alisverissepetim.adapter.RecyclerviewAdapter;

public class SepetDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);


        // View'i şişir
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sepet, null);
        dialog.setContentView(view);


        // Dialog'un dışına tıklayınca kapanmasını sağla
        dialog.setCanceledOnTouchOutside(true);

        // Pencereyi özelleştir (Arka planı soluklaştır)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.dimAmount = 0.5f;  // Arka planın ne kadar soluk olacağını ayarla (0.0 - 1.0)
            dialog.getWindow().setAttributes(params);
        }

        // Buton işlevi
        Button closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> dismiss());

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button sepetOlusturButton = view.findViewById(R.id.make_basket);


        return dialog;
    }
}
