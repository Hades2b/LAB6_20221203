package com.example.lab6_20221203;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20221203.auth.AuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogle, btnGithub;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = AuthManager.getInstance();

        if (authManager.getCurrentUser() != null) {
            goToMain();
            return;
        }

        etEmail   = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin  = findViewById(R.id.btn_login);
        btnGoogle = findViewById(R.id.btn_google);
        btnGithub = findViewById(R.id.btn_github);
        TextView tvRegister = findViewById(R.id.tv_register);

        btnLogin.setOnClickListener(v -> {
            String email    = String.valueOf(etEmail.getText()).trim();
            String password = String.valueOf(etPassword.getText()).trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            authManager.loginWithEmail(email, password, new AuthManager.AuthListener() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    goToMain();
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show()
                    );
                }
            });
        });

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        btnGoogle.setOnClickListener(v ->
                authManager.startGoogleSignIn(this, new AuthManager.AuthListener() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        goToMain();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show()
                        );
                    }
                })
        );

        btnGithub.setOnClickListener(v ->
                authManager.startGitHubSignIn(this, new AuthManager.AuthListener() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        goToMain();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() ->
                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show()
                        );
                    }
                })
        );
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}