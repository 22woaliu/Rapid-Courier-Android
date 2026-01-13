package com.example.example;

import android.graphics.Color;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private List<User> fullUserList; // Stores ALL users
    private List<User> displayedList; // Stores FILTERED users
    private DatabaseReference usersRef;
    private TextView statsText;
    
    private Button btnAll, btnClients, btnRiders;
    private String currentFilter = "All"; // All, Client, Rider

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_users);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI
        recyclerView = findViewById(R.id.users_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        statsText = findViewById(R.id.users_stats_text);
        
        // Filters
        btnAll = findViewById(R.id.filter_all);
        btnClients = findViewById(R.id.filter_clients);
        btnRiders = findViewById(R.id.filter_riders);

        fullUserList = new ArrayList<>();
        displayedList = new ArrayList<>();
        adapter = new AdminUserAdapter(displayedList, this::showDeleteConfirmationDialog);
        recyclerView.setAdapter(adapter);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Initialize Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        
        setupFilterListeners();
        fetchUsers();
    }

    private void setupFilterListeners() {
        btnAll.setOnClickListener(v -> applyFilter("All"));
        btnClients.setOnClickListener(v -> applyFilter("Client"));
        btnRiders.setOnClickListener(v -> applyFilter("Rider"));
    }

    private void applyFilter(String type) {
        currentFilter = type;
        updateFilterButtonsUI();

        displayedList.clear();
        if (type.equals("All")) {
            displayedList.addAll(fullUserList);
        } else {
            for (User user : fullUserList) {
                if (user.userType != null && user.userType.equalsIgnoreCase(type)) {
                    displayedList.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateFilterButtonsUI() {
        int selectedColor = Color.parseColor("#1976D2");
        int unselectedColor = Color.parseColor("#E0E0E0");
        int white = Color.WHITE;
        int gray = Color.parseColor("#545F71");

        btnAll.setBackgroundTintList(ColorStateList.valueOf(currentFilter.equals("All") ? selectedColor : unselectedColor));
        btnAll.setTextColor(currentFilter.equals("All") ? white : gray);

        btnClients.setBackgroundTintList(ColorStateList.valueOf(currentFilter.equals("Client") ? selectedColor : unselectedColor));
        btnClients.setTextColor(currentFilter.equals("Client") ? white : gray);

        btnRiders.setBackgroundTintList(ColorStateList.valueOf(currentFilter.equals("Rider") ? selectedColor : unselectedColor));
        btnRiders.setTextColor(currentFilter.equals("Rider") ? white : gray);
    }

    private void fetchUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullUserList.clear();
                int clientCount = 0;
                int riderCount = 0;

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    User user = userSnap.getValue(User.class);
                    if (user != null) {
                        fullUserList.add(user);
                        
                        if ("Rider".equalsIgnoreCase(user.userType)) {
                            riderCount++;
                        } else {
                            clientCount++;
                        }
                    }
                }
                
                // Update Header Stats (Always shows total counts)
                int total = fullUserList.size();
                String stats = clientCount + " Clients • " + riderCount + " Riders • " + total + " Total";
                statsText.setText(stats);
                
                // Re-apply current filter to update list
                applyFilter(currentFilter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminUsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(User user) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.fullname + "? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(User user) {
        if (user == null || user.uid == null) return;

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        // 1. Delete from users node (Common)
        rootRef.child("users").child(user.uid).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminUsersActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminUsersActivity.this, "Failed to delete user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // 2. Role specific deletion
        if ("Client".equalsIgnoreCase(user.userType)) {
            // Delete Client Profile
            rootRef.child("clients").child(user.uid).removeValue();

            // Cascading Delete: Delete all orders by this client
            DatabaseReference ordersRef = rootRef.child("orders");
            ordersRef.orderByChild("userId").equalTo(user.uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot orderSnap : snapshot.getChildren()) {
                        orderSnap.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Log error if needed
                }
            });

        } else if ("Rider".equalsIgnoreCase(user.userType)) {
            // Delete Rider Profile
            rootRef.child("riders").child(user.uid).removeValue();
            
            // NOTE: We do NOT delete orders delivered by this rider, to preserve history for clients.
        }
    }
}
