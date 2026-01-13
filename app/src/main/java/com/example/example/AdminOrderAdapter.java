package com.example.example;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    private List<Order> orderList;

    public AdminOrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reusing the item_rider_order layout since the card design is the same
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rider_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.orderIdText.setText("Order #" + (order.orderId != null && order.orderId.length() > 6 ? order.orderId.substring(order.orderId.length() - 6) : order.orderId));
        // Using orderDate or pickupDate as available
        holder.dateText.setText(order.pickupDate != null ? order.pickupDate : order.pickupDate);
        holder.statusBadge.setText(order.status);
        holder.pickupText.setText(order.pickupAddress);
        holder.deliveryText.setText(order.deliveryAddress);
        holder.weightText.setText(order.packageWeight);
        holder.descText.setText(order.packageDescription);

        // Styling Badge
        String status = order.status != null ? order.status : "Pending";
        switch (status) {
            case "Pending":
                holder.statusBadge.setTextColor(Color.parseColor("#E65100")); // Orange Text
                holder.statusBadge.setBackgroundColor(Color.parseColor("#FFE0B2")); // Orange BG
                break;
            case "Picked":
                holder.statusBadge.setTextColor(Color.parseColor("#2962FF")); // Blue Text
                holder.statusBadge.setBackgroundColor(Color.parseColor("#E3F2FD")); // Blue BG
                break;
            case "In Transit":
                holder.statusBadge.setTextColor(Color.parseColor("#F57C00")); // Darker Orange
                holder.statusBadge.setBackgroundColor(Color.parseColor("#FFF3E0"));
                break;
            case "Delivered":
                holder.statusBadge.setTextColor(Color.parseColor("#1B5E20")); // Green Text
                holder.statusBadge.setBackgroundColor(Color.parseColor("#E8F5E9")); // Green BG
                break;
            default:
                holder.statusBadge.setTextColor(Color.parseColor("#757575"));
                holder.statusBadge.setBackgroundColor(Color.parseColor("#EEEEEE"));
                break;
        }

        // Click Listener -> Open AdminOrderDetailsActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), AdminOrderDetailsActivity.class);
            intent.putExtra("ORDER_ID", order.orderId);
            intent.putExtra("STATUS", order.status);
            intent.putExtra("DATE", order.pickupDate);
            intent.putExtra("PICKUP", order.pickupAddress);
            intent.putExtra("DELIVERY", order.deliveryAddress);
            intent.putExtra("SENDER", order.senderPhone);
            intent.putExtra("RECEIVER", order.receiverPhone);
            intent.putExtra("WEIGHT", order.packageWeight);
            intent.putExtra("DESCRIPTION", order.packageDescription);
            intent.putExtra("USER_ID", order.userId);
            intent.putExtra("USER_EMAIL", order.userEmail);
            intent.putExtra("RIDER_ID", order.riderId);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdText, dateText, statusBadge, pickupText, deliveryText, weightText, descText;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.order_id_text);
            dateText = itemView.findViewById(R.id.order_date_text);
            statusBadge = itemView.findViewById(R.id.status_badge);
            pickupText = itemView.findViewById(R.id.pickup_text);
            deliveryText = itemView.findViewById(R.id.delivery_text);
            weightText = itemView.findViewById(R.id.weight_text);
            descText = itemView.findViewById(R.id.desc_text);
        }
    }
}
