package com.example.alisverissepetim.view;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.alisverissepetim.R;
import com.example.alisverissepetim.adapter.RecyclerviewAdapter;
import com.example.alisverissepetim.model.ShoppingList;
// CartManager importu gerekebilir, adapter içinde kullanılıyor ama fragment'ta direkt gerekirse diye
// import com.example.alisverissepetim.manager.CartManager;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements RecyclerviewAdapter.OnItemClickListener {

    RecyclerView recyclerView;
    RecyclerviewAdapter recyclerviewAdapter;
    ArrayList<ShoppingList> currentShoppingLists = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView_home);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        recyclerviewAdapter = new RecyclerviewAdapter(currentShoppingLists);
        recyclerviewAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(recyclerviewAdapter);

        // Animasyonlar
        if (getContext() != null) {
            recyclerView.setLayoutAnimation(
                    AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down)
            );
            recyclerView.scheduleLayoutAnimation();
        }


        // FragmentResultListener ile SepetDialogFragment'tan gelen veriyi al
        getParentFragmentManager().setFragmentResultListener("sepetKey", this, (requestKey, bundle) -> {
            String sepetAdi = bundle.getString("sepetAdi");
            String sepetTur = bundle.getString("sepetTur");

            if (sepetAdi != null && !sepetAdi.isEmpty()) {
                ShoppingList newSepet = new ShoppingList(sepetAdi, sepetTur);
                recyclerviewAdapter.addItem(newSepet);
                Log.d("HomeFragment", "Sepet eklendi: " + newSepet.getBasketName() + " - " + newSepet.getBasketTur());
            } else {
                Toast.makeText(getContext(), "Sepet adı boş olamaz!", Toast.LENGTH_SHORT).show();
            }
            Log.d("HomeFragment", "Gelen sepet: " + sepetAdi+ ", Tür: " + sepetTur);
        });

        ImageView sepetOlusturButton = view.findViewById(R.id.sepet_olustur_button);
        sepetOlusturButton.setOnClickListener(v -> {
            SepetDialogFragment sepetDialog = new SepetDialogFragment();
            sepetDialog.show(getParentFragmentManager(), "SepetDialog");
        });
    }

    // Adapter'dan gelen normal tıklama
    @Override
    public void onItemClick(ShoppingList sepet) {
        // String karşılaştırmalarını .equals() ile yapın
        if ("Markalı".equals(sepet.getBasketTur())){ // NULL POINTER EXCEPTION ÖNLEMEK İÇİN "Markalı".equals(...)
            goToLoadingFragment(getView(), sepet); // getView() kullanmak daha güvenli olabilir
        } else {
            goToSimpleListFragment(getView(), sepet);
        }
    }

    // Adapter'dan gelen sepet ikonuna tıklama
    @Override
    public void onCartIconClick(ShoppingList sepet) {
        Toast.makeText(getContext(), sepet.getBasketName() + " sepetine gidiliyor...", Toast.LENGTH_SHORT).show();
        goToCartFragment(getView(), sepet.getBasketName());
    }

    private void goToSimpleListFragment(View view, ShoppingList sepet) {
        if (view == null) return; // View null ise işlem yapma
        // HomeFragment'tan SimpleListFragment'a yönlendirme
        NavDirections action = HomeFragmentDirections
                .actionHomeFragmentToSimpleListFragment(sepet.getBasketName(), sepet.getBasketTur());
        Navigation.findNavController(view).navigate(action);
    }

    private void goToLoadingFragment(View view, ShoppingList sepet) {
        if (view == null) return;
        // HomeFragment'tan LoadingFragment'a yönlendirme
        NavDirections action = HomeFragmentDirections
                .actionHomeFragmentToLoadingFragment(sepet.getBasketName(), sepet.getBasketTur());
        Navigation.findNavController(view).navigate(action);
    }

    private void goToCartFragment(View view, String shoppingListName) {
        if (view == null) return;
        try {
            HomeFragmentDirections.ActionHomeFragmentToCartFragment action =
                    HomeFragmentDirections.actionHomeFragmentToCartFragment(shoppingListName, ""); // basketTur için boş string veya null
            Navigation.findNavController(view).navigate(action);
        } catch (Exception e) {
            Log.e("HomeFragment", "CartFragment'a navigasyon hatası: " + e.getMessage());
            Toast.makeText(getContext(), "Sepet sayfasına gidilemiyor.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerviewAdapter != null) {
            recyclerviewAdapter.notifyDataSetChanged();
        }
    }
}
