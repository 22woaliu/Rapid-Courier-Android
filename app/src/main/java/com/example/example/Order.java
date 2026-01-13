package com.example.example;

public class Order {
    public String orderId;
    public String userId;
    public String userEmail; // Added
    public String riderId;
    public String status;
    public String pickupAddress;
    public String deliveryAddress;
    public String senderPhone; // Added
    public String receiverPhone; // Added
    public String packageWeight; // Added
    public String packageDescription; // Added
    public String pickupDate; 
    public long timestamp;

    public Order() {
        // Default constructor required for calls to DataSnapshot.getValue(Order.class)
    }

    public Order(String orderId, String userId, String userEmail, String status, String pickupAddress, String deliveryAddress, 
                 String senderPhone, String receiverPhone, String packageWeight, String packageDescription, String pickupDate) {
        this.orderId = orderId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.status = status;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.senderPhone = senderPhone;
        this.receiverPhone = receiverPhone;
        this.packageWeight = packageWeight;
        this.packageDescription = packageDescription;
        this.pickupDate = pickupDate;
        this.timestamp = System.currentTimeMillis();
    }
}
