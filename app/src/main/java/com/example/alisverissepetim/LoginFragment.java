package com.example.alisverissepetim;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;

    public LoginFragment() {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //buraya textview setonclicklistener kodları gelcek
        TextView kayitol_text = view.findViewById(R.id.kayitol_text);
        Button button_login = view.findViewById(R.id.button_login);

        emailEditText = view.findViewById(R.id.editTextEmailAddress);
        passwordEditText = view.findViewById(R.id.editTextPassword);

        //Sign in ekranındaki Giriş butonuna tıklanınca
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInAndGoToHomePage();
            }
        });

        //En altta login ekranından sign up ekranına yönelmek için
        kayitol_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSignUp(view);
            }
        });
    }

    public void signInAndGoToHomePage(){

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.equals("") || password.equals("")) {
            Toast.makeText(requireContext(),"Enter email or password!",Toast.LENGTH_LONG).show();
        }else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Intent intent = new Intent(requireContext(),HomeScreenActivity.class);
                            startActivity(intent);
                            requireActivity().finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(requireContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
        }

    }

    public void goToSignUp(View view){
        NavDirections action = LoginFragmentDirections.actionLoginFragmentToSignUpFragment();
        Navigation.findNavController(view).navigate(action);
    }
}