package com.example.example;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserOrderAdapter adapter;
    private List<Order> orderList;
    private List<Order> fullOrderList = new ArrayList<>(); // Store all user orders
    private android.widget.TextView filterText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_orders);
        
        android.view.View rootView = findViewById(R.id.main);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
        
        // Setup Window Insets for root layout (LinearLayout)
        // Since main ID isn't in XML provided, let's just find root view content
        // findViewById(android.R.id.content).setPadding(0, 0, 0, 0); // Removed redundant line since we fixed the ID

        mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI
        recyclerView = findViewById(R.id.orders_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        orderList = new ArrayList<>();
        adapter = new UserOrderAdapter(orderList);
        recyclerView.setAdapter(adapter);

        // Back Button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Filter Trigger
        android.widget.LinearLayout filterTrigger = findViewById(R.id.filter_dropdown_trigger);
        filterText = findViewById(R.id.filter_text);
        filterTrigger.setOnClickListener(v -> showFilterMenu(v));

        // Fetch Data
        fetchOrders(currentUserId);
    }

    private void showFilterMenu(android.view.View view) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, view);
        popup.getMenu().add("All Orders");
        popup.getMenu().add("Pending");
        popup.getMenu().add("Picked");
        popup.getMenu().add("In Transit");
        popup.getMenu().add("Delivered");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            filterText.setText(title);
            
            if (title.equals("All Orders")) applyFilter("All");
            else applyFilter(title);
            
            return true;
        });

        popup.show();
    }

    private void applyFilter(String status) {
        orderList.clear();
        for (Order order : fullOrderList) {
            if (status.equals("All")) {
                orderList.add(order);
            } else {
                if (order.status != null && order.status.equalsIgnoreCase(status)) {
                    orderList.add(order);
                }
            }
        }
        // Keep sorting logic
        Collections.reverse(orderList);
        adapter.notifyDataSetChanged();
    }

    private void fetchOrders(String userId) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullOrderList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order != null && userId.equals(order.userId)) {
                        fullOrderList.add(order);
                    }
                }
                // Apply default filter (All)
                applyFilter("All");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserOrdersActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
