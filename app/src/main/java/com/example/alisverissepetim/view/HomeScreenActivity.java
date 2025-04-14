package com.example.alisverissepetim.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alisverissepetim.R;
import com.example.alisverissepetim.model.ShoppingList;
import com.example.alisverissepetim.adapter.RecyclerviewAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class HomeScreenActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        firebaseAuth = FirebaseAuth.getInstance();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.more) {
            Toast.makeText(HomeScreenActivity.this, "Çıkış yapılıyor...", Toast.LENGTH_SHORT).show();

            firebaseAuth.signOut();

            Intent intent = new Intent(HomeScreenActivity.this,MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}