package com.example.example;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OrderDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        TextView orderIdText = findViewById(R.id.details_order_id);
        TextView pickupText = findViewById(R.id.details_pickup);
        TextView deliveryText = findViewById(R.id.details_delivery);
        TextView senderText = findViewById(R.id.details_sender);
        TextView receiverText = findViewById(R.id.details_receiver);
        TextView weightText = findViewById(R.id.details_weight);
        TextView descriptionText = findViewById(R.id.details_description);

        Intent intent = getIntent();
        if (intent != null) {
            String orderId = intent.getStringExtra("ORDER_ID");
            String status = intent.getStringExtra("STATUS");
            String pickup = intent.getStringExtra("PICKUP");
            String delivery = intent.getStringExtra("DELIVERY");
            String sender = intent.getStringExtra("SENDER");
            String receiver = intent.getStringExtra("RECEIVER");
            String weight = intent.getStringExtra("WEIGHT");
            String desc = intent.getStringExtra("DESCRIPTION");

            // Set Text
            orderIdText.setText(orderId != null ? "Order #" + orderId : "Order #");
            pickupText.setText(pickup);
            deliveryText.setText(delivery);
            senderText.setText(sender);
            receiverText.setText(receiver);
            weightText.setText(weight);
            descriptionText.setText(desc);

            // Update Tracking UI based on Status
            updateStatusTracker(status);
        }

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void updateStatusTracker(String status) {
        if (status == null) return;
        
        TextView iconPicked = findViewById(R.id.status_icon_picked);
        TextView textPicked = findViewById(R.id.status_text_picked);
        
        TextView iconTransit = findViewById(R.id.status_icon_transit);
        TextView textTransit = findViewById(R.id.status_text_transit);
        
        TextView iconDelivered = findViewById(R.id.status_icon_delivered);
        TextView textDelivered = findViewById(R.id.status_text_delivered);

        int activeColor = Color.parseColor("#2962FF"); // Blue
        int activeBg = Color.parseColor("#E3F2FD"); // Light Blue
        
        // Reset Logic: By default everything is grey (configured in XML)
        // We will just "activate" steps based on status
        
        // Case: Picked
        if (status.equalsIgnoreCase("Picked") || status.equalsIgnoreCase("In Transit") || status.equalsIgnoreCase("Delivered")) {
            activateStep(iconPicked, textPicked, activeBg, activeColor, true);
        }
        
        // Case: In Transit
        if (status.equalsIgnoreCase("In Transit") || status.equalsIgnoreCase("Delivered")) {
             activateStep(iconTransit, textTransit, activeBg, activeColor, false);
        }

        // Case: Delivered
        if (status.equalsIgnoreCase("Delivered")) {
             activateStep(iconDelivered, textDelivered, activeBg, activeColor, false);
        }
    }

    private void activateStep(TextView icon, TextView label, int bg, int color, boolean isCheckmark) {
        // Change icon background tint
        icon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        // If it's the checkmark, keep text white; otherwise assume icon color matters or just bg
        // Actually, screenshot shows Blue Circle with White Icon.
        // My XML has white text color for icons, so tinting background blue works perfect.
        
        label.setTextColor(color);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
    }
}
