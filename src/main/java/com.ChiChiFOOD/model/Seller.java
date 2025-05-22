package com.ChiChiFOOD.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "sellers")
public class Seller extends User {
    private String address;
//kkk
    public Seller() {
    }

    public Seller(String name, String lastName, String phoneNumber, String email, String password, String profileImageBase64, String address) {
        super(name, lastName, phoneNumber, email, password, profileImageBase64);
        this.address = address;
        super.setRole(Role.seller);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
