package com.example.alisverissepetim;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class SignUpFragment extends Fragment {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;
    public SignUpFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //buraya textview setonclicklistener kodları gelcek
        TextView girisyap_text = view.findViewById(R.id.girisyap_text);
        Button button_signup = view.findViewById(R.id.button_signup);

        emailEditText = view.findViewById(R.id.editTextEpostaText);
        passwordEditText = view.findViewById(R.id.editTextPasswordText);

        button_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpAndGoToLoginFragment();
            }
        });

        girisyap_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLogin(view);
            }
        });

    }

    public void signUpAndGoToLoginFragment(){

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.equals("") || password.equals("")) {
            Toast.makeText(requireContext(),"Enter email or password!",Toast.LENGTH_LONG).show();
        }else {
            mAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    NavDirections action = SignUpFragmentDirections.actionSignUpFragmentToLoginFragment();
                    Navigation.findNavController(requireView()).navigate(action);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(requireContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void goToLogin(View view){
        NavDirections action = SignUpFragmentDirections.actionSignUpFragmentToLoginFragment();
        Navigation.findNavController(view).navigate(action);
    }
}