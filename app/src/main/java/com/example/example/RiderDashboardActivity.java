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

public class RiderDashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView usernameText;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rider_dashboard);
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
            
            // Default loading
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
                        // Fallback
                        String email = currentUser.getEmail();
                        String displayName = email != null ? email.split("@")[0] : "Rider";
                        usernameText.setText(displayName);
                        welcomeText.setText("Welcome, " + displayName + "!");
                    }
                } else {
                     // Fallback
                    String email = currentUser.getEmail();
                    String displayName = email != null ? email.split("@")[0] : "Rider";
                    usernameText.setText(displayName);
                    welcomeText.setText("Welcome, " + displayName + "!");
                }
            });
        
            // Initialize Stats Views
            TextView totalOrdersText = findViewById(R.id.total_orders_text);
            TextView pickedOrdersText = findViewById(R.id.picked_orders_text);
            TextView transitOrdersText = findViewById(R.id.transit_orders_text);
            TextView deliveredOrdersText = findViewById(R.id.delivered_orders_text);

            // Set initial state to 0
            totalOrdersText.setText("0");
            pickedOrdersText.setText("0");
            transitOrdersText.setText("0");
            deliveredOrdersText.setText("0");

            // Attach Database Listener for Orders
            String currentUserId = currentUser.getUid();
            com.google.firebase.database.DatabaseReference ordersRef = 
                    com.google.firebase.database.FirebaseDatabase.getInstance().getReference("orders");

            ordersRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                    int total = 0;
                    int picked = 0;
                    int inTransit = 0;
                    int delivered = 0;

                    for (com.google.firebase.database.DataSnapshot orderSnap : snapshot.getChildren()) {
                        String status = orderSnap.child("status").getValue(String.class);
                        String riderId = orderSnap.child("riderId").getValue(String.class);
                        
                        // Case 1: Order is assigned to current Rider
                        if (riderId != null && riderId.equals(currentUserId)) {
                            total++;
                            if (status != null) {
                                switch (status) {
                                    case "Picked":
                                        picked++;
                                        break;
                                    case "In Transit":
                                        inTransit++;
                                        break;
                                    case "Delivered":
                                        delivered++;
                                        break;
                                }
                            }
                        } 
                        // Case 2: Order is Pending and Unassigned (Available for any rider)
                        // We count this towards "Total" so the rider knows there is work available.
                        else if ("Pending".equals(status) && (riderId == null || riderId.isEmpty())) {
                            total++;
                        }
                    }

                    // Update UI
                    totalOrdersText.setText(String.valueOf(total));
                    pickedOrdersText.setText(String.valueOf(picked));
                    transitOrdersText.setText(String.valueOf(inTransit));
                    deliveredOrdersText.setText(String.valueOf(delivered));
                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                    Toast.makeText(RiderDashboardActivity.this, "Failed to load stats", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Logout Button Click Listener
        Button logoutButton = findViewById(R.id.logout_button);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(RiderDashboardActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // View Orders Card Click Listener
        CardView viewOrdersCard = findViewById(R.id.view_orders_card);
        if (viewOrdersCard != null) {
            viewOrdersCard.setOnClickListener(v -> {
                Intent intent = new Intent(RiderDashboardActivity.this, RiderOrdersActivity.class);
                startActivity(intent);
            });
        }
    }
}
