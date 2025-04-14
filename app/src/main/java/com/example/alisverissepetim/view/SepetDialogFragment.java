package com.example.alisverissepetim.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import com.example.alisverissepetim.R;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alisverissepetim.adapter.RecyclerviewAdapter;
import com.example.alisverissepetim.model.ShoppingList;

import java.io.Console;
import java.util.ArrayList;

public class SepetDialogFragment extends DialogFragment {

    private Button button1, button2 , sepetListelemeButon;
    private EditText inputSepetName;

    private int flagForButtonControl = 0;

    private String sepetAdi;
    String sepetTur = "";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        flagForButtonControl = 0;


        // View'i şişir
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.dialog_sepet, null);
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


        button1 = view.findViewById(R.id.button1);
        button2 = view.findViewById(R.id.button2);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flagForButtonControl = 1;
                changeButtonState(button1, button2);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flagForButtonControl = 2;
                changeButtonState(button2, button1);
            }
        });

        sepetListelemeButon = view.findViewById(R.id.make_basket);
        inputSepetName = view.findViewById(R.id.sepetNameDialog);

        sepetListelemeButon.setOnClickListener(v -> {
            sepetAdi = inputSepetName.getText().toString().trim();

            if (sepetAdi.isEmpty()) {
                Toast.makeText(getContext(), "Sepet adı boş olamaz!", Toast.LENGTH_SHORT).show();
                return; // Dialog kapanmasın
            }


            if (flagForButtonControl == 1) {
                sepetTur = "Markalı";
            } else if (flagForButtonControl == 2) {
                sepetTur = "Markasız";
            }else {
                sepetTur = "Markasız";
            }

            // Veriyi HomeFragment'a göndermek için bundle oluştur
            Bundle result = new Bundle();
            result.putString("sepetAdi", sepetAdi);
            result.putString("sepetTur", sepetTur);

            // HomeFragment'a sonucu ilet
            getParentFragmentManager().setFragmentResult("sepetKey", result);
            System.out.println("SepetDialog tetiklendi: " + sepetAdi + " - " + sepetTur);

            // Dialog'u kapat
            dismiss();
        });

        return dialog;
    }


    private void changeButtonState(Button activeButton, Button inactiveButton) {
        activeButton.setBackgroundResource(R.drawable.button_pressed_bg); // Yeşil arka plan
        activeButton.setTextColor(getResources().getColor(R.color.white));

        inactiveButton.setBackgroundResource(R.drawable.markali_markasiz_button_bg); // Normal beyaz arka plan
        inactiveButton.setTextColor(getResources().getColor(R.color.green));
    }
}
