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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = true)
    private String logoBase64;

    @Column(nullable = true)
    private int taxFee;

    @Column(nullable = true)
    private int addtionalFee;

    private boolean isResturantConfirmed = false;

    @OneToMany (mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)

    private List <Menu> menus = new ArrayList<>();

    public Restaurant() {}


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
        return addtionalFee;
    }

    public void setAddtionalFee(int addtionalFee) {
        this.addtionalFee = addtionalFee;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
