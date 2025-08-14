package com.example.alisverissepetim.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Bundle;
import android.view.View;

import com.example.alisverissepetim.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        NavController navController = navHostFragment.getNavController();
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            } else {
                getSupportActionBar().show();
            }
        });
    }
}