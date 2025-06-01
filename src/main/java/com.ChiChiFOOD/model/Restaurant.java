package com.ChiChiFOOD.model;

import com.ChiChiFOOD.model.restaurant.Item;
import jakarta.persistence.*;
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
    private Integer taxFee;

    @Column(nullable = true)
    private Integer additionalFee;

    @Column(nullable = false)
    private String sellerId;

    private boolean isRestaurantConfirmed = false;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Item> items = new ArrayList<>();

    public Restaurant() {
    }

    public Restaurant(String sellerId, String name, String phone, String address) {
        this.sellerId = sellerId;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public Restaurant(String sellerId, String name, String phone, String address, String logoBase64) {
        this(sellerId, name, phone, address);
        this.logoBase64 = logoBase64;
    }

    public Restaurant(String sellerId, String name, String phone, String address, String logoBase64, Integer taxFee, Integer additionalFee) {
        this(sellerId, name, phone, address, logoBase64);
        this.taxFee = taxFee;
        this.additionalFee = additionalFee;
    }

    public void setRestaurantConfirmed() {
        this.isRestaurantConfirmed = true;
    }

    // getter و setter ها

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Integer getTaxFee() {
        return taxFee;
    }

    public void setTaxFee(Integer taxFee) {
        this.taxFee = taxFee;
    }

    public Integer getAdditionalFee() {
        return additionalFee;
    }

    public void setAdditionalFee(Integer additionalFee) {
        this.additionalFee = additionalFee;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public boolean isRestaurantConfirmed() {
        return isRestaurantConfirmed;
    }

    public void setRestaurantConfirmed(boolean restaurantConfirmed) {
        isRestaurantConfirmed = restaurantConfirmed;
    }

    public List<Item> getFoodItems() {
        return items;
    }

    public void setFoodItems(List<Item> items) {
        this.items = items;
    }

    public void addFoodItem(Item item) {
        items.add(item);
        item.setRestaurant(this);
    }

    public void removeFoodItem(Item item) {
        items.remove(item);
        item.setRestaurant(null);
    }
}