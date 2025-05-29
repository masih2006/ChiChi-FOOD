package com.ChiChiFOOD.model;

import jakarta.persistence.*;
import com.ChiChiFOOD.model.Resturant.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurant")
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = true)
    private String logoBase64;

    @Column(nullable = true)
    private int taxFee;

    @Column(nullable = true)
    private int additionalFee;

    private boolean isResturantConfirmed = false;
 // dardast sakht kargaran mashgool karand....
//    @OneToMany (mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//
//    private List <Menu> menus = new ArrayList<>();

    public Restaurant() {
        // سازنده پیش‌فرض
    }

    public Restaurant(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public Restaurant(String name, String phone, String address, String logoBase64) {
        this(name, phone, address); // فراخوانی سازنده قبلی
        this.logoBase64 = logoBase64;
    }

    public Restaurant(String name, String phone, String address, String logoBase64, int taxFee, int additionalFee) {
        this(name, phone, address, logoBase64); // فراخوانی سازنده قبلی
        this.taxFee = taxFee;
        this.additionalFee = additionalFee;
    }


    public void setResturantConfirmed (){
        isResturantConfirmed = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public int getTaxFee() {
        return taxFee;
    }

    public void setTaxFee(int taxFee) {
        this.taxFee = taxFee;
    }

    public int getAddtionalFee() {
        return additionalFee;
    }

    public void setAddtionalFee(int addtionalFee) {
        this.additionalFee = addtionalFee;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
