package com.example.example;

public class Client {
    public String uid;
    public String name;
    public String email;
    public String phone;
    public String address;
    public int totalRides;

    public Client() {
        // Default constructor for Firebase
    }

    public Client(String uid, String name, String email, String phone, String address) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.totalRides = 0;
    }
}
