package com.ChiChiFOOD.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "couriers")
public class Courier extends User {
    private String address; // اضافه کردن فیلد address برای یکپارچگی


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
