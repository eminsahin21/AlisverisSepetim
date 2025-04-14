package com.example.alisverissepetim.view;

import android.app.Activity;
import android.content.Intent;
import android.credentials.GetCredentialRequest;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alisverissepetim.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;

    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    private ActivityResultLauncher<Intent> googleSignInLauncher;

    public LoginFragment() {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(getActivity(),HomeScreenActivity.class);
            startActivity(intent);
            getActivity().finish();
        }

        // Google Sign-In Yapılandırması
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Firebase Console'dan alınan Web Client ID
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        try {
                            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                                    .getResult(ApiException.class);
                            if (account != null) {
                                firebaseAuthWithGoogle(account);
                            }
                        } catch (ApiException e) {
                            Log.w("Google Sign-In", "Google sign in failed", e);
                        }
                    }
                }
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);
        ImageView button_login_google = view.findViewById(R.id.signInWithGoogle);
        button_login_google.setOnClickListener(v -> signInWithGoogle());

        return view;
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

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(requireContext(), "Giriş Başarılı: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(requireContext(),HomeScreenActivity.class);
                        startActivity(intent);

                    } else {
                        Toast.makeText(requireContext(), "Giriş Başarısız", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void signInAndGoToHomePage(){

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        final boolean[] isValid = {true};

        if (email.equals("") || password.equals("")) {
            Toast.makeText(requireContext(),"Enter email or password!",Toast.LENGTH_LONG).show();
            isValid[0] = false;
            emailEditText.setBackgroundResource(R.drawable.red_border);
            passwordEditText.setBackgroundResource(R.drawable.red_border);
        }else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            isValid[0] = true;
                            emailEditText.setBackgroundResource(R.drawable.default_border);
                            passwordEditText.setBackgroundResource(R.drawable.default_border);

                            Intent intent = new Intent(requireContext(),HomeScreenActivity.class);
                            startActivity(intent);
                            requireActivity().finish();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(requireContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                            isValid[0] = false;
                            emailEditText.setBackgroundResource(R.drawable.red_border);
                            passwordEditText.setBackgroundResource(R.drawable.red_border);
                        }
                    });
        }

    }

    public void goToSignUp(View view){
        NavDirections action = LoginFragmentDirections.actionLoginFragmentToSignUpFragment();

        if (isAdded() && getView() != null) {
            Navigation.findNavController(getView()).navigate(action);
        }
    }
}