package com.example.example;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AdminOrderDetailsActivity extends AppCompatActivity {

    private String orderId;
    private Spinner statusSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_order_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        TextView orderIdText = findViewById(R.id.details_order_id);
        TextView dateText = findViewById(R.id.details_date);
        
        // Client Info
        TextView clientName = findViewById(R.id.client_name);
        TextView clientPhone = findViewById(R.id.client_phone);
        TextView clientAddress = findViewById(R.id.client_address);
        TextView emailText = findViewById(R.id.details_email);
        TextView userIdText = findViewById(R.id.details_user_id);
        
        // Rider Info
        TextView riderName = findViewById(R.id.rider_name);
        TextView riderPhone = findViewById(R.id.rider_phone);
        TextView riderAddress = findViewById(R.id.rider_address);
        TextView riderIdText = findViewById(R.id.details_rider_id);
        
        // Addresses (Route)
        TextView pickupText = findViewById(R.id.details_pickup);
        TextView deliveryText = findViewById(R.id.details_delivery);
        
        // Logistics Contact
        TextView senderText = findViewById(R.id.details_sender);
        TextView receiverText = findViewById(R.id.details_receiver);
        
        // Package
        TextView weightText = findViewById(R.id.details_weight);
        TextView descText = findViewById(R.id.details_desc);
        
        statusSpinner = findViewById(R.id.status_spinner);
        Button updateButton = findViewById(R.id.btn_update_status);

        // Get Data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            orderId = intent.getStringExtra("ORDER_ID");
            String status = intent.getStringExtra("STATUS");
            String date = intent.getStringExtra("DATE");
            String pickup = intent.getStringExtra("PICKUP");
            String delivery = intent.getStringExtra("DELIVERY");
            String sender = intent.getStringExtra("SENDER");
            String receiver = intent.getStringExtra("RECEIVER");
            String weight = intent.getStringExtra("WEIGHT");
            String desc = intent.getStringExtra("DESCRIPTION");
            String userEmail = intent.getStringExtra("USER_EMAIL");
            String userId = intent.getStringExtra("USER_ID");
            String riderId = intent.getStringExtra("RIDER_ID");

            orderIdText.setText(orderId != null ? "Order #" + (orderId.length() > 6 ? orderId.substring(orderId.length() -6) : orderId) : "Order #");
            dateText.setText("Created on: " + (date != null ? date : "N/A"));
            
            // Basic Client Info from Order
            emailText.setText(userEmail != null ? userEmail : "Unknown");
            userIdText.setText(userId != null ? userId : "N/A");
            
            // Basic Rider Info from Order
            riderIdText.setText(riderId != null && !riderId.isEmpty() ? riderId : "Unassigned");
            
            // Route
            pickupText.setText(pickup);
            deliveryText.setText(delivery);
            senderText.setText(sender != null ? sender : "N/A");
            receiverText.setText(receiver != null ? receiver : "N/A");
            weightText.setText(weight != null ? weight : "N/A");
            descText.setText(desc != null ? desc : "N/A");

            // Setup Spinner
            String[] statuses = {"Pending", "Picked", "In Transit", "Delivered"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            statusSpinner.setAdapter(adapter);

            // Set current selection
            if (status != null) {
                for (int i = 0; i < statuses.length; i++) {
                    if (statuses[i].equalsIgnoreCase(status)) {
                        statusSpinner.setSelection(i);
                        break;
                    }
                }
            }

            // --- FETCH DYNAMIC PROFILE DATA ---
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

            // Fetch Client Details
            if (userId != null) {
                usersRef.child(userId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("fullname").getValue(String.class);
                            String phone = snapshot.child("phone").getValue(String.class);
                            String address = snapshot.child("address").getValue(String.class);

                            clientName.setText(name != null ? name : "Unknown");
                            clientPhone.setText(phone != null ? phone : "N/A");
                            clientAddress.setText(address != null ? address : "N/A");
                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                        // Ignore
                    }
                });
            }

            // Fetch Rider Details
            if (riderId != null && !riderId.isEmpty()) {
                usersRef.child(riderId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("fullname").getValue(String.class);
                            String phone = snapshot.child("phone").getValue(String.class);
                            String address = snapshot.child("address").getValue(String.class);

                            riderName.setText(name != null ? name : "Unknown");
                            riderPhone.setText(phone != null ? phone : "N/A");
                            riderAddress.setText(address != null ? address : "N/A");
                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                        // Ignore
                    }
                });
            } else {
                riderName.setText("Unassigned");
                riderPhone.setText("N/A");
                riderAddress.setText("N/A");
            }
        }

        // Update Button Logic
        updateButton.setOnClickListener(v -> updateOrderStatus());

        // Back Button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void updateOrderStatus() {
        if (orderId == null) return;

        String selectedStatus = statusSpinner.getSelectedItem().toString();
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", selectedStatus);

        orderRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Order Status Updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
