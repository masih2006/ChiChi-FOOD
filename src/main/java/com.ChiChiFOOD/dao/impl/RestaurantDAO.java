package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.Restaurant;

import java.util.List;

public interface RestaurantDAO {
    public void save(Restaurant restaurant);
    public void update(Restaurant restaurant);
    public void delete(Restaurant restaurant);
    public Restaurant findById(Long id);
    public List<Restaurant> getRestaurantsBySellerId(String sellerId);
    public boolean existsByNameOrPhone(String name, String phone);
    public List<Restaurant> searchByName(String keyword);
}
