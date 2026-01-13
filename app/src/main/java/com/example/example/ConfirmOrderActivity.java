package com.example.example;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfirmOrderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirm_order);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get Data from Intent
        Intent intent = getIntent();
        String pickup = intent.getStringExtra("PICKUP");
        String delivery = intent.getStringExtra("DELIVERY");
        String senderP = intent.getStringExtra("SENDER_PHONE");
        String receiverP = intent.getStringExtra("RECEIVER_PHONE");
        String weight = intent.getStringExtra("WEIGHT");
        String description = intent.getStringExtra("DESCRIPTION");

        // Initialize Views & Set Text
        ((TextView) findViewById(R.id.summary_pickup)).setText(pickup);
        ((TextView) findViewById(R.id.summary_delivery)).setText(delivery);
        ((TextView) findViewById(R.id.summary_sender_phone)).setText(senderP);
        ((TextView) findViewById(R.id.summary_receiver_phone)).setText(receiverP);
        ((TextView) findViewById(R.id.summary_weight)).setText(weight);
        ((TextView) findViewById(R.id.summary_description)).setText(description);

        // Back Buttons
        ImageButton backButtonTop = findViewById(R.id.back_button);
        Button backButtonBottom = findViewById(R.id.btn_back_bottom);
        
        backButtonTop.setOnClickListener(v -> finish());
        backButtonBottom.setOnClickListener(v -> finish());

        // Confirm Button (Database Write Logic)
        Button confirmFinalButton = findViewById(R.id.btn_confirm_final);
        confirmFinalButton.setOnClickListener(v -> {
            saveOrderToDatabase(pickup, delivery, senderP, receiverP, weight, description);
        });
    }

    private void saveOrderToDatabase(String pickup, String delivery, String senderP, String receiverP, String weight, String description) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        String orderId = ordersRef.push().getKey();

        if (orderId == null) {
            Toast.makeText(this, "Error generating order ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentDate = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());

        Order newOrder = new Order(
                orderId,
                currentUser.getUid(),
                currentUser.getEmail(),
                "Pending",
                pickup,
                delivery,
                senderP,
                receiverP,
                weight,
                description,
                currentDate
        );

        ordersRef.child(orderId).setValue(newOrder).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Order Confirmed Successfully!", Toast.LENGTH_LONG).show();
                // Navigate back to Dashboard (clearing stack so user can't go back to confirm page)
                Intent intent = new Intent(ConfirmOrderActivity.this, UserDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); 
            } else {
                Toast.makeText(this, "Failed to confirm order: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
