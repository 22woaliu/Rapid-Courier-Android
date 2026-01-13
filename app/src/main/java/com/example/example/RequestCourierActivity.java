package com.example.example;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RequestCourierActivity extends AppCompatActivity {

    private EditText pickupAddressInput;
    private EditText deliveryAddressInput;
    private EditText senderPhoneInput;
    private EditText receiverPhoneInput;
    private EditText packageWeightInput;
    private EditText packageDescriptionInput;
    private Button cancelButton;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_courier);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        pickupAddressInput = findViewById(R.id.pickup_address_input);
        deliveryAddressInput = findViewById(R.id.delivery_address_input);
        senderPhoneInput = findViewById(R.id.sender_phone_input);
        receiverPhoneInput = findViewById(R.id.receiver_phone_input);
        packageWeightInput = findViewById(R.id.package_weight_input);
        packageDescriptionInput = findViewById(R.id.package_description_input);
        
        cancelButton = findViewById(R.id.cancel_button);
        confirmButton = findViewById(R.id.confirm_button);
        ImageButton backButton = findViewById(R.id.back_button);

        // Click Listeners
        backButton.setOnClickListener(v -> finish());
        
        cancelButton.setOnClickListener(v -> finish());

        confirmButton.setOnClickListener(v -> {
            String pickup = pickupAddressInput.getText().toString().trim();
            String delivery = deliveryAddressInput.getText().toString().trim();
            String senderP = senderPhoneInput.getText().toString().trim();
            String receiverP = receiverPhoneInput.getText().toString().trim();
            String weight = packageWeightInput.getText().toString().trim();
            String description = packageDescriptionInput.getText().toString().trim();

            if (pickup.isEmpty() || delivery.isEmpty() || senderP.isEmpty() || receiverP.isEmpty() || weight.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Navigate to Confirm Page with Data
            android.content.Intent intent = new android.content.Intent(RequestCourierActivity.this, ConfirmOrderActivity.class);
            intent.putExtra("PICKUP", pickup);
            intent.putExtra("DELIVERY", delivery);
            intent.putExtra("SENDER_PHONE", senderP);
            intent.putExtra("RECEIVER_PHONE", receiverP);
            intent.putExtra("WEIGHT", weight);
            intent.putExtra("DESCRIPTION", description);
            startActivity(intent);
        });
    }
}
