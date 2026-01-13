package com.example.example;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminLoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText usernameInput;
    private EditText passwordInput;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);

        // Back Button Click Listener
        Button backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                onBackPressed();
            });
        }

        // Login Button Click Listener
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                String email = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (email.isEmpty()) {
                    usernameInput.setError("Email is required");
                    usernameInput.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    passwordInput.setError("Password is required");
                    passwordInput.requestFocus();
                    return;
                }

                // Authenticate with Firebase
                loginUser(email, password);
            });
        }
    }

    private void loginUser(String email, String password) {
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(AdminLoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to Admin Dashboard
                        Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Sign in failed
                        Toast.makeText(AdminLoginActivity.this, 
                            "Authentication failed: " + task.getException().getMessage(), 
                            Toast.LENGTH_LONG).show();
                        loginButton.setEnabled(true);
                        loginButton.setText("Login");
                    }
                });
    }
}
