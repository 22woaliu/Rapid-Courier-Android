package com.example.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserLoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Spinner userTypeSpinner;
    private EditText phoneInput;
    private EditText passwordInput;
    private Button loginButton;
    private EditText riderTokenInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        userTypeSpinner = findViewById(R.id.user_type_spinner);
        phoneInput = findViewById(R.id.phone_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        TextView riderTokenLabel = findViewById(R.id.rider_token_label);
        riderTokenInput = findViewById(R.id.rider_token_input);

        // Setup User Type Spinner
        String[] userTypes = {"Client", "Rider"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userTypeSpinner.setAdapter(adapter);

        // Spinner Item Selected Listener
        userTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (userTypes[position].equals("Rider")) {
                    riderTokenLabel.setVisibility(View.VISIBLE);
                    riderTokenInput.setVisibility(View.VISIBLE);
                } else {
                    riderTokenLabel.setVisibility(View.GONE);
                    riderTokenInput.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                riderTokenLabel.setVisibility(View.GONE);
                riderTokenInput.setVisibility(View.GONE);
            }
        });

        // Back Button Click Listener
        Button backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                onBackPressed();
            });
        }

        // Sign Up Button Click Listener
        Button signupButton = findViewById(R.id.signup_button);
        if (signupButton != null) {
            signupButton.setOnClickListener(v -> {
                Intent intent = new Intent(UserLoginActivity.this, SignupActivity.class);
                startActivity(intent);
            });
        }

        // Login Button Click Listener
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                String userType = userTypeSpinner.getSelectedItem().toString();
                String phone = phoneInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                String riderToken = riderTokenInput.getText().toString().trim();

                if (phone.isEmpty()) {
                    phoneInput.setError("Phone number is required");
                    phoneInput.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    passwordInput.setError("Password is required");
                    passwordInput.requestFocus();
                    return;
                }

                if (userType.equals("Rider") && riderToken.isEmpty()) {
                    riderTokenInput.setError("Rider token is required");
                    riderTokenInput.requestFocus();
                    return;
                }

                // Authenticate with Firebase using email directly (constructed from phone)
                String email = phone + "@courier.com";
                loginUser(email, password, userType, riderToken);
            });
        }
    }

    private void loginUser(String email, String password, String userType, String riderToken) {
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 1. Auth Success - Now Validate User Data from Database
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String uid = firebaseUser.getUid();
                        
                        com.google.firebase.database.DatabaseReference rootRef = 
                                com.google.firebase.database.FirebaseDatabase.getInstance().getReference();

                        // Check User Type in 'users' node
                        rootRef.child("users").child(uid).get().addOnCompleteListener(userTask -> {
                            if (!userTask.isSuccessful()) {
                                handleError("Failed to fetch user profile: " + userTask.getException().getMessage());
                                return;
                            }
                            
                            if (!userTask.getResult().exists()) {
                                handleError("User profile not found. Please contact support.");
                                return;
                            }

                            // 2. Validate User Type
                            String storedUserType = userTask.getResult().child("userType").getValue(String.class);
                            if (storedUserType == null || !storedUserType.equals(userType)) {
                                handleError("Access Denied: Account is not a " + userType);
                                mAuth.signOut(); // Security: Sign out immediately
                                return;
                            }

                            // 3. If Rider, Validate Token
                            if (userType.equals("Rider")) {
                                rootRef.child("riders").child(uid).child("riderToken").get().addOnCompleteListener(tokenTask -> {
                                    if (tokenTask.isSuccessful() && tokenTask.getResult().exists()) {
                                        String storedToken = tokenTask.getResult().getValue(String.class);
                                        
                                        if (storedToken != null && storedToken.equals(riderToken)) {
                                            // SUCCESS: Token Matches
                                            proceedToDashboard(userType, riderToken);
                                        } else {
                                            handleError("Invalid Rider Token");
                                            mAuth.signOut();
                                        }
                                    } else {
                                        handleError("Rider profile error. Please contact admin.");
                                        mAuth.signOut();
                                    }
                                });
                            } else {
                                // Clients don't need token validation
                                proceedToDashboard(userType, null);
                            }
                        });

                    } else {
                        // Sign in failed
                        handleError("Authentication failed: " + task.getException().getMessage());
                    }
                });
    }

    private void proceedToDashboard(String userType, String riderToken) {
        Toast.makeText(UserLoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
        
        if (userType.equals("Client")) {
            Intent intent = new Intent(UserLoginActivity.this, UserDashboardActivity.class);
            startActivity(intent);
            finish();
        } else if (userType.equals("Rider")) {
            Intent intent = new Intent(UserLoginActivity.this, RiderDashboardActivity.class);
            intent.putExtra("riderToken", riderToken);
            startActivity(intent);
            finish();
        }
    }

    private void handleError(String message) {
        Toast.makeText(UserLoginActivity.this, message, Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
        loginButton.setText("Login");
    }
}
