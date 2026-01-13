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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminOrderAdapter adapter;
    private List<Order> fullOrderList;
    private List<Order> displayedOrderList;
    private DatabaseReference ordersRef;

    private android.widget.TextView filterText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_orders);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI
        recyclerView = findViewById(R.id.admin_orders_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Filters Trigger
        LinearLayout filterTrigger = findViewById(R.id.filter_dropdown_trigger);
        filterText = findViewById(R.id.filter_text);
        
        filterTrigger.setOnClickListener(v -> showFilterMenu(v));

        fullOrderList = new ArrayList<>();
        displayedOrderList = new ArrayList<>();
        adapter = new AdminOrderAdapter(displayedOrderList);
        recyclerView.setAdapter(adapter);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Initialize Firebase
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

    private void applyFilter(String status) {
        displayedOrderList.clear();
        if (status.equals("All")) {
            displayedOrderList.addAll(fullOrderList);
        } else {
            for (Order order : fullOrderList) {
                if (order.status != null && order.status.equalsIgnoreCase(status)) {
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
                // Re-apply filter (default All)
                applyFilter("All");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminOrdersActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
