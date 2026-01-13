package com.example.example;

public class Rider {
    public String uid;
    public String name;
    public String email;
    public String riderToken;
    public boolean isOnline;
    public double currentLatitude;
    public double currentLongitude;
    public String vehicleType;
    public String licensePlate;

    public Rider() {
        // Default constructor for Firebase
    }

    public Rider(String uid, String name, String email, String riderToken) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.riderToken = riderToken;
        this.isOnline = false;
        this.currentLatitude = 0.0;
        this.currentLongitude = 0.0;
        this.vehicleType = "car";
        this.licensePlate = "";
    }
}
