package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.utils.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public interface RestaurantDAO {
    public void save(Restaurant restaurant);
    public void update(Restaurant restaurant);
    public void delete(Restaurant restaurant);
    public Restaurant findById(Long id);
    public List<Restaurant> getRestaurantsBySellerId(String sellerId);
    public boolean existsBySellerId(int sellerId);
    public List<Restaurant> searchByName(String keyword);
    public boolean restaurantExistsByPhone(String phone);
    public  boolean restaurantExistsById(String id);
    public  boolean restaurantExistsByName(String name);
    public int getMyRestaurantId(String sellerId) ;
}
