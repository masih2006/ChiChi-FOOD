package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.model.restaurant.Item;

import java.util.List;

public interface ItemDAO {
    public void save(Item item);
    public void update(Item item);
    public void delete(Item item);
    public Item findById(Long id);
    public boolean itemExistsByName(String name, int restaurantId);
    public Item findItemByIdAndRestaurantId(int itemId, int restaurantId) ;
    public boolean itemExistsByIdAndRestaurantId(int itemId, int restaurantId) ;

    }
