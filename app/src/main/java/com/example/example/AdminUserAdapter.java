package com.example.example;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;
    private OnUserDeleteListener deleteListener; // Added Listener

    // Interface for delete callback
    public interface OnUserDeleteListener {
        void onUserDelete(User user);
    }

    public AdminUserAdapter(List<User> userList, OnUserDeleteListener deleteListener) {
        this.userList = userList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.nameText.setText(user.fullname != null ? user.fullname : "Unknown");
        holder.phoneText.setText(user.phone != null ? user.phone : "N/A");
        holder.addressText.setText(user.address != null ? user.address : "N/A");
        holder.idText.setText(user.uid != null ? user.uid : "");

        String role = user.userType != null ? user.userType : "Client";
        holder.roleText.setText(role);

        // Styling based on Role
        if ("Rider".equalsIgnoreCase(role)) {
            // Green Theme
            holder.roleText.setTextColor(Color.parseColor("#1B5E20")); // Dark Green
            holder.roleText.setBackgroundColor(Color.parseColor("#E8F5E9")); // Light Green
            // Avatar
            holder.avatarImage.setColorFilter(Color.parseColor("#1B5E20"));
            holder.avatarImage.setBackgroundColor(Color.parseColor("#E8F5E9"));
        } else {
            // Client - Blue Theme (Default)
            holder.roleText.setTextColor(Color.parseColor("#1565C0")); // Dark Blue
            holder.roleText.setBackgroundColor(Color.parseColor("#E3F2FD")); // Light Blue
            // Avatar
            holder.avatarImage.setColorFilter(Color.parseColor("#1565C0"));
            holder.avatarImage.setBackgroundColor(Color.parseColor("#E3F2FD"));
        }

        // Delete Button Click
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onUserDelete(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, phoneText, addressText, roleText, idText;
        ImageView avatarImage;
        ImageButton deleteButton; // Added

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.user_name);
            phoneText = itemView.findViewById(R.id.user_phone);
            addressText = itemView.findViewById(R.id.user_address);
            roleText = itemView.findViewById(R.id.user_role);
            idText = itemView.findViewById(R.id.user_id);
            avatarImage = itemView.findViewById(R.id.user_avatar);
            deleteButton = itemView.findViewById(R.id.btn_delete_user); // Initialize
        }
    }
}
