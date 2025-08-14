package com.example.alisverissepetim.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import com.example.alisverissepetim.R;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.card.MaterialCardView;

public class SepetDialogFragment extends DialogFragment {

    private Button button1, button2, sepetListelemeButon, buttonCancel;
    private EditText inputSepetName;
    private MaterialCardView cardView1, cardView2;

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
            params.dimAmount = 0.6f;  // Arka planın ne kadar soluk olacağını ayarla (0.0 - 1.0)
            dialog.getWindow().setAttributes(params);
        }

        initViews(view);
        setupClickListeners();

        return dialog;
    }

    private void initViews(View view) {
        button1 = view.findViewById(R.id.button1);
        button2 = view.findViewById(R.id.button2);
        sepetListelemeButon = view.findViewById(R.id.make_basket);
        buttonCancel = view.findViewById(R.id.button_cancel);
        inputSepetName = view.findViewById(R.id.sepetNameDialog);

        // CardView'ları bul (eğer layout'ta varsa)
        cardView1 = (MaterialCardView) button1.getParent().getParent();
        cardView2 = (MaterialCardView) button2.getParent().getParent();
    }

    private void setupClickListeners() {
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flagForButtonControl = 1;
                changeButtonState(cardView1, cardView2, button1, button2);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flagForButtonControl = 2;
                changeButtonState(cardView2, cardView1, button2, button1);
            }
        });

        buttonCancel.setOnClickListener(v -> {
            dismiss(); // Dialog'u kapat
        });

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
            } else {
                // Hiçbiri seçilmemişse varsayılan olarak Markasız yap
                sepetTur = "Markasız";
                Toast.makeText(getContext(), "Sepet türü otomatik olarak 'Markasız' seçildi.", Toast.LENGTH_SHORT).show();
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
    }

    private void changeButtonState(MaterialCardView activeCard, MaterialCardView inactiveCard,
                                   Button activeButton, Button inactiveButton) {
        // Aktif kartı vurgula
        activeCard.setStrokeColor(getResources().getColor(R.color.green, null));
        activeCard.setStrokeWidth(4);
        activeCard.setCardElevation(8f);
        activeButton.setTextColor(getResources().getColor(R.color.green, null));

        // İnaktif kartı normale çevir
        inactiveCard.setStrokeColor(getResources().getColor(android.R.color.darker_gray, null));
        inactiveCard.setStrokeWidth(2);
        inactiveCard.setCardElevation(2f);
        inactiveButton.setTextColor(getResources().getColor(R.color.black, null));
    }
}