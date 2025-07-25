package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.Order;
import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.model.restaurant.Item;

import java.util.List;

public interface OrderDAO {
    public void save(Order order);
    public void update(Order order);
    public List<Order> findAll();
    public Order findById(int id);
    public List<Order> getOrdersByRestaurant(String vendorID);

}
