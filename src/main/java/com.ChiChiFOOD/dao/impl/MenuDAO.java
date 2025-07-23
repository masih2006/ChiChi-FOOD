package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.model.restaurant.Menu;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public interface  MenuDAO {

    public void save(Menu item) ;
    public Menu findById(Long id) ;
    public List<Menu> findAll() ;
    public void update(Menu item) ;
    public void delete(Menu item) ;
    public List<Menu> findByMenuId(Long menuId) ;
    public boolean menuExistByTitle(String title, int restaurantId) ;
    public boolean itemExistsInMenu(String title, int itemId, int restaurantId) ;
    public Menu findByTitle(String title, int restaurantId) ;
    }

