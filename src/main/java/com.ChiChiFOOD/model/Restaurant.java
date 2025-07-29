package com.ChiChiFOOD.model;

import com.ChiChiFOOD.model.restaurant.*;
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

    private String logoBase64;
    private Integer taxFee;
    private Integer additionalFee;

    private boolean isRestaurantConfirmed = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sellerId", nullable = false, unique = true)
    private User seller;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Item> items = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Menu> menus = new ArrayList<>();

    // ---------- Constructors ----------
    public Restaurant() {}

    public Restaurant(User seller, String name, String phone, String address) {
        this.seller = seller;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public Restaurant(User seller, String name, String phone, String address, String logoBase64) {
        this(seller, name, phone, address);
        this.logoBase64 = logoBase64;
    }

    public Restaurant(User seller, String name, String phone, String address, String logoBase64, Integer taxFee, Integer additionalFee) {
        this(seller, name, phone, address, logoBase64);
        this.taxFee = taxFee;
        this.additionalFee = additionalFee;
    }

    // ---------- Getters & Setters ----------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLogoBase64() { return logoBase64; }
    public void setLogoBase64(String logoBase64) { this.logoBase64 = logoBase64; }

    public Integer getTaxFee() { return taxFee; }
    public void setTaxFee(Integer taxFee) { this.taxFee = taxFee; }

    public Integer getAdditionalFee() { return additionalFee; }
    public void setAdditionalFee(Integer additionalFee) { this.additionalFee = additionalFee; }

    public boolean isRestaurantConfirmed() { return isRestaurantConfirmed; }
    public void setRestaurantConfirmed(boolean confirmed) { isRestaurantConfirmed = confirmed; }

    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    public List<Item> getFoodItems() { return items; }
    public void setFoodItems(List<Item> items) { this.items = items; }

    public void addFoodItem(Item item) {
        items.add(item);
        item.setRestaurant(this);
    }

    public void removeFoodItem(Item item) {
        items.remove(item);
        item.setRestaurant(null);
    }

    public List<Menu> getMenus() { return menus; }
    public void setMenus(List<Menu> menus) { this.menus = menus; }
}
