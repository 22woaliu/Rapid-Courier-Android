package com.example.example;

import android.content.Intent;
import android.util.Log;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private Spinner userTypeSpinner;
    private EditText fullnameInput;
    private EditText phoneInput;
    private EditText addressInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private EditText riderTokenInput;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference("Users");

        // Initialize views
        userTypeSpinner = findViewById(R.id.user_type_spinner);
        fullnameInput = findViewById(R.id.fullname_input);
        phoneInput = findViewById(R.id.phone_input);
        addressInput = findViewById(R.id.address_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        riderTokenInput = findViewById(R.id.rider_token_input);
        signupButton = findViewById(R.id.signup_button);
        TextView riderTokenLabel = findViewById(R.id.rider_token_label);

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

        // Login Button Click Listener
        Button loginButton = findViewById(R.id.login_button);
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                onBackPressed();
            });
        }

        // Sign Up Button Click Listener
        if (signupButton != null) {
            signupButton.setOnClickListener(v -> {
                performSignup();
            });
        }
    }

    private void performSignup() {
        String userType = userTypeSpinner.getSelectedItem().toString();
        String fullname = fullnameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        String riderToken = riderTokenInput.getText().toString().trim();

        // Validate inputs
        if (fullname.isEmpty()) {
            fullnameInput.setError("Full name is required");
            fullnameInput.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            phoneInput.setError("Phone number is required");
            phoneInput.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            addressInput.setError("Address is required");
            addressInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        if (userType.equals("Rider") && riderToken.isEmpty()) {
            riderTokenInput.setError("Rider token is required");
            riderTokenInput.requestFocus();
            return;
        }

        // Create email from phone number
        String email = phone + "@courier.com";

        // Register with Firebase
        registerUser(email, password, userType, fullname, phone, address, riderToken);
    }

    private void registerUser(String email, String password, String userType,
                              String fullname, String phone, String address, String riderToken) {

        signupButton.setEnabled(false);
        signupButton.setText("Signing up...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Toast.makeText(SignupActivity.this,
                                "Auth failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        signupButton.setEnabled(true);
                        signupButton.setText("Sign Up");
                        return;
                    }

                    try {
                        com.google.firebase.auth.FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String userId = firebaseUser.getUid();
                        Log.d(TAG, "AUTH SUCCESS UID = " + userId);

                        String registrationDate = new SimpleDateFormat(
                                "yyyy-MM-dd", Locale.getDefault()).format(new Date());

                        User user = new User(
                                userId,
                                email,
                                fullname,
                                userType,
                                phone,
                                registrationDate,
                                address,
                                userType.equals("Rider") ? riderToken : ""
                        );

                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                        
                        // Safety timeout: if DB doesn't respond in 15 seconds, assume failure
                        android.os.Handler timeoutHandler = new android.os.Handler();
                        Runnable timeoutRunnable = () -> {
                            if (!signupButton.isEnabled()) { // Still loading
                                Log.e(TAG, "Database operation timed out - Initiating Rollback");
                                
                                // ROLLBACK caused by Timeout
                                com.google.firebase.auth.FirebaseUser currentUser = mAuth.getCurrentUser();


                                signupButton.setEnabled(true);
                                signupButton.setText("Sign Up");
                                Toast.makeText(SignupActivity.this, 
                                    "Connection timed out. Rolling back... Please check your internet or Database URL.", 
                                    Toast.LENGTH_LONG).show();
                            }
                        };
                        timeoutHandler.postDelayed(timeoutRunnable, 15000);

                        // Save main user
                        rootRef.child("users").child(userId)
                                .setValue(user)
                                .addOnCompleteListener(dbTask -> {
                                    // Remove timeout callback since we got a response
                                    timeoutHandler.removeCallbacks(timeoutRunnable);

                                    if (dbTask.isSuccessful()) {
                                        Log.d(TAG, "✓ User saved in /users");

                                        if (userType.equals("Rider")) {
                                            Rider rider = new Rider(userId, fullname, email, riderToken);
                                            rootRef.child("riders").child(userId).setValue(rider); 
                                            // We don't wait for this one to block the UI, but it should work if the first one did
                                        } else {
                                            Client client = new Client(userId, fullname, email, phone, address);
                                            rootRef.child("clients").child(userId).setValue(client);
                                        }

                                        showSuccessAndNavigate();
                                    } else {
                                        Exception e = dbTask.getException();
                                        Log.e(TAG, "✗ User save failed. Rolling back Auth.", e);
                                        
                                        // ROLLBACK: Delete the auth user since DB save failed
                                        firebaseUser.delete();

                                        Toast.makeText(SignupActivity.this,
                                                "Registration failed: " + (e != null ? e.getMessage() : "Unknown error"),
                                                Toast.LENGTH_LONG).show();
                                        
                                        signupButton.setEnabled(true);
                                        signupButton.setText("Sign Up");
                                    }
                                });

                    } catch (Exception e) {
                        Log.e(TAG, "CRITICAL ERROR in Auth Success Block", e);
                        Toast.makeText(SignupActivity.this, "App Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        signupButton.setEnabled(true);
                        signupButton.setText("Sign Up");
                    }
                });
    }

    private void showSuccessAndNavigate() {
        Toast.makeText(SignupActivity.this, "Sign Up Successful!", Toast.LENGTH_LONG).show();
        
        // Delay navigation to show the toast message
        new android.os.Handler().postDelayed(() -> {
            // Navigate to Main Page
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 2000); // 2 second delay
    }

}