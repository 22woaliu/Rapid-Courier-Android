package com.example.example;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import java.util.List;

public class RiderOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RiderOrderAdapter adapter;
    private List<Order> fullOrderList;
    private List<Order> displayedOrderList;
    private DatabaseReference ordersRef;
    private String currentUserId;
    
    private android.widget.TextView filterText; // To update the header text

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rider_orders);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI
        recyclerView = findViewById(R.id.rider_orders_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Filters Trigger
        LinearLayout filterTrigger = findViewById(R.id.filter_dropdown_trigger);
        filterText = findViewById(R.id.filter_text);
        
        filterTrigger.setOnClickListener(v -> showFilterMenu(v));

        fullOrderList = new ArrayList<>();
        displayedOrderList = new ArrayList<>();
        adapter = new RiderOrderAdapter(displayedOrderList);
        recyclerView.setAdapter(adapter);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        fetchOrders();
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
            else applyFilter(title); // Pending, Picked, In Transit, Delivered
            
            return true;
        });

        popup.show();
    }

    private void applyFilter(String filter) {
        displayedOrderList.clear();
        for (Order order : fullOrderList) {
            boolean isAssignedToMe = order.riderId != null && order.riderId.equals(currentUserId);
            // Available means Pending and NO rider assigned
            boolean isPendingUnassigned = "Pending".equals(order.status) && (order.riderId == null || order.riderId.isEmpty());

            if (filter.equals("All")) {
                // Show everything relevant to this rider (Available OR Assigned to me)
                if (isAssignedToMe || isPendingUnassigned) {
                    displayedOrderList.add(order);
                }
            } else if (filter.equals("Pending")) {
                // Show Pending Unassigned OR Pending Assigned to me
                 if ("Pending".equals(order.status) && (isPendingUnassigned || isAssignedToMe)) {
                    displayedOrderList.add(order);
                }
            } else {
                // For Picked, In Transit, Delivered -> MUST be assigned to me
                if (isAssignedToMe && filter.equalsIgnoreCase(order.status)) {
                    displayedOrderList.add(order);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void fetchOrders() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullOrderList.clear();
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Order order = orderSnap.getValue(Order.class);
                    if (order != null) {
                        fullOrderList.add(order);
                    }
                }
                // Apply default filter (All)
                 applyFilter("All");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RiderOrdersActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
