package com.example.example;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserOrderAdapter extends RecyclerView.Adapter<UserOrderAdapter.OrderViewHolder> {

    private List<Order> orderList;

    public UserOrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.orderIdText.setText(order.orderId != null ? "Order #" + order.orderId : "Order #???");
        holder.orderDateText.setText(order.pickupDate != null ? order.pickupDate : "Date Unknown");
        holder.pickupAddressText.setText(order.pickupAddress);
        holder.deliveryAddressText.setText(order.deliveryAddress);
        
        // Status Badge Logic
        String status = order.status != null ? order.status : "Pending";
        holder.statusBadge.setText(status);

        // Dynamic Color Styling for Status
        switch (status) {
            case "Picked":
                // Blueish
                holder.statusBadge.setTextColor(Color.parseColor("#2E5C9A"));
                holder.statusBadge.setBackgroundColor(Color.parseColor("#D1E4FA"));
                break;
            case "Delivered":
                // Greenish
                holder.statusBadge.setTextColor(Color.parseColor("#1B5E20"));
                holder.statusBadge.setBackgroundColor(Color.parseColor("#C8E6C9"));
                break;
            case "In Transit":
                // Orangeish
                holder.statusBadge.setTextColor(Color.parseColor("#BF360C"));
                holder.statusBadge.setBackgroundColor(Color.parseColor("#FFCCBC"));
                break;
            default:
                // Grey/Default
                holder.statusBadge.setTextColor(Color.parseColor("#424242"));
                holder.statusBadge.setBackgroundColor(Color.parseColor("#EEEEEE"));
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(holder.itemView.getContext(), OrderDetailsActivity.class);
            intent.putExtra("ORDER_ID", order.orderId);
            intent.putExtra("STATUS", order.status);
            intent.putExtra("PICKUP", order.pickupAddress);
            intent.putExtra("DELIVERY", order.deliveryAddress);
            intent.putExtra("SENDER", order.senderPhone);
            intent.putExtra("RECEIVER", order.receiverPhone);
            intent.putExtra("WEIGHT", order.packageWeight);
            intent.putExtra("DESCRIPTION", order.packageDescription);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdText, orderDateText, statusBadge, pickupAddressText, deliveryAddressText;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.order_id_text);
            orderDateText = itemView.findViewById(R.id.order_date_text);
            statusBadge = itemView.findViewById(R.id.status_badge);
            pickupAddressText = itemView.findViewById(R.id.pickup_address_text);
            deliveryAddressText = itemView.findViewById(R.id.delivery_address_text);
        }
    }
}
