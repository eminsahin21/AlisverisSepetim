package com.example.alisverissepetim.view;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.alisverissepetim.R;
import com.example.alisverissepetim.adapter.RecyclerviewAdapter;
import com.example.alisverissepetim.model.ShoppingList;
import com.example.alisverissepetim.utils.SharedPreferencesHelper;
import java.util.ArrayList;
import java.util.List;
import com.example.alisverissepetim.manager.CartManager;

public class HomeFragment extends Fragment implements RecyclerviewAdapter.OnItemClickListener {

    RecyclerView recyclerView;
    RecyclerviewAdapter recyclerviewAdapter;
    ArrayList<ShoppingList> currentShoppingLists = new ArrayList<>();
    TextView emptyStateTextView;
    Button buttonNewList, buttonViewLists;

    // SharedPreferences helper
    private SharedPreferencesHelper shoppingListPreferences;

    // Swipe-to-delete (tam kaydırma)

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // SharedPreferences helper'ı başlat
        shoppingListPreferences = new SharedPreferencesHelper(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadSavedShoppingLists(); // Kaydedilmiş listeleri yükle
        setupRecyclerView();
        setupFragmentResultListener();
        setupClickListeners();
        setupSwipeToDelete(); // Swipe-to-delete işlemi için
        updateEmptyState();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView_home);
        emptyStateTextView = view.findViewById(R.id.textView_empty_state);
        buttonNewList = view.findViewById(R.id.button_newList_in_card);
        buttonViewLists = view.findViewById(R.id.button_viewLists_outside_card);
    }

    /**
     * Kaydedilmiş alışveriş listelerini yükle
     */
    private void loadSavedShoppingLists() {
        try {
            List<ShoppingList> savedLists = shoppingListPreferences.loadShoppingLists();
            currentShoppingLists.clear();
            currentShoppingLists.addAll(savedLists);

            Log.d("HomeFragment", "Kaydedilmiş " + savedLists.size() + " liste yüklendi");

            if (savedLists.size() > 0) {
                Toast.makeText(getContext(), savedLists.size() + " kaydedilmiş liste yüklendi",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Listeleri yüklerken hata: " + e.getMessage());
            Toast.makeText(getContext(), "Kaydedilmiş listeler yüklenemedi", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Mevcut listeleri SharedPreferences'a kaydet
     */
    private void saveShoppingLists() {
        try {
            shoppingListPreferences.saveShoppingLists(currentShoppingLists);
            Log.d("HomeFragment", "Tüm listeler kaydedildi. Liste sayısı: " + currentShoppingLists.size());
        } catch (Exception e) {
            Log.e("HomeFragment", "Listeler kaydedilirken hata: " + e.getMessage());
        }
    }

    private void setupRecyclerView() {
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
    }

    private void setupFragmentResultListener() {
        // FragmentResultListener ile SepetDialogFragment'tan gelen veriyi al
        getParentFragmentManager().setFragmentResultListener("sepetKey", this, (requestKey, bundle) -> {
            String sepetAdi = bundle.getString("sepetAdi");
            String sepetTur = bundle.getString("sepetTur");

            if (sepetAdi != null && !sepetAdi.isEmpty()) {
                // Aynı isimde liste var mı kontrol et
                if (shoppingListPreferences.isShoppingListExists(sepetAdi)) {
                    Toast.makeText(getContext(), "Bu isimde bir liste zaten var!", Toast.LENGTH_LONG).show();
                    return;
                }

                ShoppingList newSepet = new ShoppingList(sepetAdi, sepetTur);

                // RecyclerView'a ekle
                recyclerviewAdapter.addItem(newSepet);

                // SharedPreferences'a kaydet
                shoppingListPreferences.addShoppingList(newSepet);

                updateEmptyState();

                Log.d("HomeFragment", "Sepet eklendi ve kaydedildi: " + newSepet.getBasketName() +
                        " - " + newSepet.getBasketTur());

                Toast.makeText(getContext(), sepetAdi + " listesi oluşturuldu ve kaydedildi!",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Sepet adı boş olamaz!", Toast.LENGTH_SHORT).show();
            }

            Log.d("HomeFragment", "Gelen sepet: " + sepetAdi + ", Tür: " + sepetTur);
        });
    }

    private void setupClickListeners() {
        // Yeni Liste Oluştur butonu
        buttonNewList.setOnClickListener(v -> {
            SepetDialogFragment sepetDialog = new SepetDialogFragment();
            sepetDialog.show(getParentFragmentManager(), "SepetDialog");
        });

        // Mevcut Listelerim butonu - Aynı sayfada RecyclerView'a odaklan
        buttonViewLists.setOnClickListener(v -> {
            if (currentShoppingLists.isEmpty()) {
                Toast.makeText(getContext(), "Henüz hiç liste oluşturmadınız!", Toast.LENGTH_SHORT).show();
            } else {
                // RecyclerView'a scroll yap
                recyclerView.smoothScrollToPosition(0);
                Toast.makeText(getContext(), "Alışveriş listeleriniz aşağıda görüntüleniyor",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                final ShoppingList sepet = currentShoppingLists.get(position);
                final String sepetAdi = sepet.getBasketName();

                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Sepeti sil")
                        .setMessage("'" + sepetAdi + "' sepetini silmek istiyor musunuz?")
                        .setPositiveButton("Evet", (dialog, which) -> {
                            // 1) Sepetin ürünlerini temizle (UI + SharedPreferences)
                            CartManager.getInstance().clearCart(sepetAdi);

                            // 2) Liste adını SharedPreferences'tan sil
                            shoppingListPreferences.removeShoppingList(sepetAdi);

                            // 3) UI'dan kaldır
                            currentShoppingLists.remove(position);
                            recyclerviewAdapter.notifyItemRemoved(position);
                            updateEmptyState();
                        })
                        .setNegativeButton("Hayır", (dialog, which) -> {
                            // Satırı geri al
                            recyclerviewAdapter.notifyItemChanged(position);
                        })
                        .setOnCancelListener(d -> {
                            // Diyalog kapatılırsa satırı geri al
                            recyclerviewAdapter.notifyItemChanged(position);
                        })
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull android.graphics.Canvas c,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getBottom() - itemView.getTop();

                // Koyu kırmızı arkaplan (radius'lu drawable)
                android.graphics.drawable.Drawable bg = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.swipe_bg_red);
                if (bg != null) {
                    if (dX > 0) {
                        bg.setBounds(itemView.getLeft(), itemView.getTop(), (int) (itemView.getLeft() + dX), itemView.getBottom());
                        bg.draw(c);
                    } else if (dX < 0) {
                        bg.setBounds((int) (itemView.getRight() + dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                        bg.draw(c);
                    } else {
                        return;
                    }
                }

                // Beyaz çöp kutusu ikonu
                android.graphics.drawable.Drawable deleteIcon =
                        androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);
                if (deleteIcon == null) return;
                deleteIcon = androidx.core.graphics.drawable.DrawableCompat.wrap(deleteIcon.mutate());
                androidx.core.graphics.drawable.DrawableCompat.setTint(deleteIcon, android.graphics.Color.WHITE);

                int iconWidth = deleteIcon.getIntrinsicWidth();
                int iconHeight = deleteIcon.getIntrinsicHeight();
                int iconMargin = (itemHeight - iconHeight) / 2;

                if (dX > 0) {
                    // Sağda ikon
                    int iconTop = itemView.getTop() + iconMargin;
                    int iconLeft = itemView.getLeft() + iconMargin;
                    int iconRight = iconLeft + iconWidth;
                    int iconBottom = iconTop + iconHeight;
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                } else {
                    // Solda ikon
                    int iconTop = itemView.getTop() + iconMargin;
                    int iconRight = itemView.getRight() - iconMargin;
                    int iconLeft = iconRight - iconWidth;
                    int iconBottom = iconTop + iconHeight;
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                }

                deleteIcon.draw(c);
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    private void updateEmptyState() {
        if (currentShoppingLists.isEmpty()) {
            emptyStateTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Adapter'dan gelen normal tıklama
    @Override
    public void onItemClick(ShoppingList sepet) {
        // String karşılaştırmalarını .equals() ile yapın
        if ("Markalı".equals(sepet.getBasketTur())) { // NULL POINTER EXCEPTION ÖNLEMEK İÇİN "Markalı".equals(...)
            goToLoadingFragment(getView(), sepet); // getView() kullanmak daha güvenli olabilir
        } else {
            goToSimpleListFragment(getView(), sepet);
        }
    }

    // Adapter'dan gelen sepet ikonuna tıklama
    @Override
    public void onCartIconClick(ShoppingList sepet) {
        // Önce sepet var mı kontrol et
        boolean hasItems = CartManager.getInstance().hasCart(sepet.getBasketName());

        if (!hasItems) {
            Toast.makeText(getContext(), sepet.getBasketName() + " sepeti boş", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getContext(), sepet.getBasketName() + " sepetine gidiliyor...", Toast.LENGTH_SHORT).show();
        goToCartFragment(getView(), sepet.getBasketName());
    }

    /**
     * Liste silme işlemi - Adapter'dan çağrılabilir
     */
    public void deleteShoppingList(ShoppingList shoppingList) {
        try {
            // SharedPreferences'tan sil
            boolean removed = shoppingListPreferences.removeShoppingList(shoppingList.getBasketName());

            if (removed) {
                // RecyclerView'dan sil
                int position = currentShoppingLists.indexOf(shoppingList);
                if (position != -1) {
                    currentShoppingLists.remove(position);
                    recyclerviewAdapter.notifyItemRemoved(position);
                    updateEmptyState();

                    Toast.makeText(getContext(), shoppingList.getBasketName() + " listesi silindi",
                            Toast.LENGTH_SHORT).show();

                    Log.d("HomeFragment", "Liste silindi: " + shoppingList.getBasketName());
                }
            } else {
                Toast.makeText(getContext(), "Liste silinemedi", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Liste silinirken hata: " + e.getMessage());
            Toast.makeText(getContext(), "Liste silinirken hata oluştu", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Tüm listeleri temizle
     */
    public void clearAllShoppingLists() {
        try {
            shoppingListPreferences.clearAllShoppingLists();
            currentShoppingLists.clear();
            recyclerviewAdapter.notifyDataSetChanged();
            updateEmptyState();

            Toast.makeText(getContext(), "Tüm listeler temizlendi", Toast.LENGTH_SHORT).show();
            Log.d("HomeFragment", "Tüm listeler temizlendi");
        } catch (Exception e) {
            Log.e("HomeFragment", "Listeler temizlenirken hata: " + e.getMessage());
        }
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
            updateEmptyState();
        }

        // Fragment'a geri dönüldüğünde verileri tekrar yükle
        loadSavedShoppingLists();
        if (recyclerviewAdapter != null) {
            recyclerviewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Fragment'tan çıkarken listeleri kaydet
        saveShoppingLists();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Fragment yok edilirken son kez kaydet
        saveShoppingLists();
    }
}