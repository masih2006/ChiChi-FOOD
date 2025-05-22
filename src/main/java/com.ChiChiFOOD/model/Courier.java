package com.ChiChiFOOD.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "couriers")
public class Courier extends User {
    private String address; // اضافه کردن فیلد address برای یکپارچگی

    public Courier() {
    }

    public Courier(String name, String lastName, String phoneNumber, String email, String password, String profileImageBase64, String address) {
        super(name, lastName, phoneNumber, email, password, profileImageBase64);
        this.address = address;
        super.setRole(Role.courier);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
