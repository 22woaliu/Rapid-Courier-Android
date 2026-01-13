package com.example.example;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserDashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView usernameText;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Initialize views
        usernameText = findViewById(R.id.username_text);
        welcomeText = findViewById(R.id.welcome_text);

        // Set user information
        // Set user information
        if (currentUser != null) {
            String uid = currentUser.getUid();
            
            // Default loading or fallback
            usernameText.setText("Loading...");
            welcomeText.setText("Welcome!");

            // Fetch Real Name from Database
            com.google.firebase.database.DatabaseReference userRef = 
                    com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users").child(uid);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    String fullname = task.getResult().child("fullname").getValue(String.class);
                    if (fullname != null && !fullname.isEmpty()) {
                        usernameText.setText(fullname);
                        welcomeText.setText("Welcome, " + fullname + "!");
                    } else {
                        // Fallback to email if name missing
                        String email = currentUser.getEmail();
                        String displayName = email != null ? email.split("@")[0] : "User";
                        usernameText.setText(displayName);
                        welcomeText.setText("Welcome, " + displayName + "!");
                    }
                } else {
                    // Fallback on error
                    String email = currentUser.getEmail();
                    String displayName = email != null ? email.split("@")[0] : "User";
                    usernameText.setText(displayName);
                    welcomeText.setText("Welcome, " + displayName + "!");
                }
            });
        }

        // Logout Button Click Listener
        Button logoutButton = findViewById(R.id.logout_button);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(UserDashboardActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // Request Courier Card Click Listener
        CardView requestCourierCard = findViewById(R.id.request_courier_card);
        if (requestCourierCard != null) {
            requestCourierCard.setOnClickListener(v -> {
                Toast.makeText(UserDashboardActivity.this, "Request Courier feature coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // View Status Card Click Listener

        requestCourierCard = findViewById(R.id.request_courier_card);
        if (requestCourierCard != null) {
            requestCourierCard.setOnClickListener(v -> {
                Intent intent = new Intent(UserDashboardActivity.this, RequestCourierActivity.class);
                startActivity(intent);
            });
        }

        CardView viewStatusCard = findViewById(R.id.view_status_card);
        if (viewStatusCard != null) {
            viewStatusCard.setOnClickListener(v -> {
                Intent intent = new Intent(UserDashboardActivity.this, UserOrdersActivity.class);
                startActivity(intent);
            });
        }
    }
}
