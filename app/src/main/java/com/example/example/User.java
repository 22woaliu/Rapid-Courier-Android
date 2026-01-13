package com.example.example;

public class User {
    public String uid, email, fullname, userType, phone, registrationDate, address, riderToken;

    public User() {} // REQUIRED

    public User(String uid, String email, String fullname, String userType,
                String phone, String registrationDate, String address, String riderToken) {
        this.uid = uid;
        this.email = email;
        this.fullname = fullname;
        this.userType = userType;
        this.phone = phone;
        this.registrationDate = registrationDate;
        this.address = address;
        this.riderToken = riderToken;
    }
}
