package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.Restaurant;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class RestaurantDAOImpl implements RestaurantDAO {
    private final Session session;

    public RestaurantDAOImpl(Session session) {
        this.session = session;
    }

    public void save(Restaurant restaurant) {
        session.save(restaurant);
    }

    public void update(Restaurant restaurant) {
        session.beginTransaction();
        session.update(restaurant);
        session.getTransaction().commit();
    }

    public void delete(Restaurant restaurant) {
        session.beginTransaction();
        session.delete(restaurant);
        session.getTransaction().commit();
    }

    public Restaurant findById(Long id) {
        return session.get(Restaurant.class, id);
    }

    public List<Restaurant> getRestaurantsBySellerId(String sellerId) {
        return session.createQuery("FROM Restaurant WHERE sellerId = :sellerId", Restaurant.class)
                .setParameter("sellerId", sellerId)
                .getResultList();
    }

    public boolean existsByNameOrPhone(String name, String phone) {
        String hql = "FROM Restaurant WHERE name = :name OR phone = :phone";
        Query<Restaurant> query = session.createQuery(hql, Restaurant.class);
        query.setParameter("name", name);
        query.setParameter("phone", phone);
        return !query.list().isEmpty();
    }

    public List<Restaurant> searchByName(String keyword) {
        String hql = "FROM Restaurant WHERE lower(name) LIKE :keyword";
        Query<Restaurant> query = session.createQuery(hql, Restaurant.class);
        query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        return query.list();
    }
}
