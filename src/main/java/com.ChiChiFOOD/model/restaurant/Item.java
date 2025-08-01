package com.ChiChiFOOD.model.restaurant;

import com.ChiChiFOOD.model.Rating;
import com.ChiChiFOOD.model.Restaurant;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.List;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;

@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column
    private String imageBase64;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int supply;

    @ElementCollection
    private List<String> keywords = new ArrayList<>();

    @ManyToMany(mappedBy = "items")
    private List<Rating> ratings = new ArrayList<>();

    public void addRating(Rating rating) {
        if (!ratings.contains(rating)) {
            ratings.add(rating);
            rating.getItems().add(this);
        }
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }

@ManyToMany(mappedBy = "items")
private List<Menu> menus = new ArrayList<>();

    public List<Menu> getMenus() {
        return menus;
    }

    public void setMenus(List<Menu> menus) {
        this.menus = menus;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

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
    public String getImageBase64() {
        return imageBase64;
    }
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getPrice() {
        return price;
    }
    public void setPrice(int price) {
        this.price = price;
    }
    public int getSupply() {
        return supply;
    }
    public void setSupply(int supply) {
        this.supply = supply;
    }
    public List<String> getKeywords() {
        return keywords;
    }
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
    public Restaurant getRestaurant() {
        return restaurant;
    }
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
    public Map<String, Object> toJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.id);
        map.put("name", this.name);
        map.put("imageBase64", this.imageBase64);
        map.put("description", this.description);
        map.put("vendor_id", this.restaurant.getId());
        map.put("price", this.price);
        map.put("supply", this.supply);
        map.put("keywords", this.keywords);
        return map;
    }
}