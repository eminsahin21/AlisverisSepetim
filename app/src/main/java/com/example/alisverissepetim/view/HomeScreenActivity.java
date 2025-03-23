package com.example.alisverissepetim.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.alisverissepetim.R;
import com.example.alisverissepetim.model.ShoppingList;
import com.example.alisverissepetim.adapter.RecyclerviewAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class HomeScreenActivity extends AppCompatActivity {


    RecyclerView recyclerView;
    RecyclerviewAdapter recyclerviewAdapter;
    ArrayList<ShoppingList> shoppingList = new ArrayList<>();
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerView_home);
        recyclerviewAdapter = new RecyclerviewAdapter(shoppingList);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerviewAdapter);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.more) {
            Toast.makeText(this, "Çıkış yapılıyor...", Toast.LENGTH_SHORT).show();

            firebaseAuth.signOut();

            Intent intent = new Intent(HomeScreenActivity.this,MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    public void sepetOlustur(View view){
//        ShoppingList newItem = new ShoppingList("Ornek");
//        recyclerviewAdapter.addItem(newItem);
        SepetDialogFragment sepetDialog = new SepetDialogFragment();
        sepetDialog.show(getSupportFragmentManager(), "SepetDialog");
    }
}