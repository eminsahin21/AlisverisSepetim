package com.example.alisverissepetim.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.alisverissepetim.R;
import com.example.alisverissepetim.adapter.RecyclerviewAdapter;
import com.example.alisverissepetim.model.ShoppingList;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class HomeFragment extends Fragment{

    RecyclerView recyclerView;
    RecyclerviewAdapter recyclerviewAdapter;
    ArrayList<ShoppingList> shoppingList = new ArrayList<>();
    FirebaseAuth firebaseAuth;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView_home);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerviewAdapter = new RecyclerviewAdapter(shoppingList);
        recyclerView.setAdapter(recyclerviewAdapter);
        recyclerView.setLayoutAnimation(
                AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down)
        );
        recyclerView.scheduleLayoutAnimation();


        // FragmentResultListener ile SepetDialogFragment'tan gelen veriyi al
        getParentFragmentManager().setFragmentResultListener("sepetKey", this, (requestKey, bundle) -> {
            String sepetAdi = bundle.getString("sepetAdi");
            String sepetTur = bundle.getString("sepetTur");



            if (sepetAdi != null && !sepetAdi.isEmpty()) {
                ShoppingList newSepet = new ShoppingList(sepetAdi, sepetTur);
                recyclerviewAdapter.addItem(newSepet);
                System.out.println("Sepet eklendi: " + newSepet.getBasketName() + " - " + newSepet.getBasketTur());
            } else {
                Toast.makeText(getContext(), "Sepet adı boş olamaz!", Toast.LENGTH_SHORT).show();
            }

            System.out.println("Gelen sepet: " + sepetAdi+ ", Tür: " + sepetTur);
            Log.i("HomeFragment", "FragmentResultListener tetiklendi");
        });

        ImageView sepetOlusturButton = view.findViewById(R.id.sepet_olustur_button);
        sepetOlusturButton.setOnClickListener(v -> {
            SepetDialogFragment sepetDialog = new SepetDialogFragment();
            sepetDialog.show(getParentFragmentManager(), "SepetDialog");
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}