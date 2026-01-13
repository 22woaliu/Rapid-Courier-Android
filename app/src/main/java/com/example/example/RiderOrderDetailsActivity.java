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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RiderOrderDetailsActivity extends AppCompatActivity {

    private String orderId;
    private Spinner statusSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rider_order_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        TextView orderIdText = findViewById(R.id.details_order_id);
        TextView dateText = findViewById(R.id.details_date);
        TextView pickupText = findViewById(R.id.details_pickup);
        TextView deliveryText = findViewById(R.id.details_delivery);
        TextView senderText = findViewById(R.id.details_sender);
        TextView receiverText = findViewById(R.id.details_receiver);
        TextView weightText = findViewById(R.id.details_weight);
        TextView descriptionText = findViewById(R.id.details_description);
        
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

            orderIdText.setText(orderId != null ? "Order #" + (orderId.length() > 6 ? orderId.substring(orderId.length() -6) : orderId) : "Order #");
            dateText.setText("Created on " + date);
            pickupText.setText(pickup);
            deliveryText.setText(delivery);
            senderText.setText(sender);
            receiverText.setText(receiver);
            weightText.setText(weight);
            descriptionText.setText(desc);

            // Setup Spinner
            String[] statuses = {"Picked", "In Transit", "Delivered"};
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
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", selectedStatus);
        
        // IMPORTANT: Also assign the rider logic here
        // We always ensure the riderID is set to current rider when they update it
        // This handles "Accepting" a Pending order implicitly
        updates.put("riderId", currentUserId);

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
