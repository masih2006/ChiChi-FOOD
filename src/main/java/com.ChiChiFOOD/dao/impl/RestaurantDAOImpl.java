package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.model.restaurant.Item;
import com.ChiChiFOOD.model.restaurant.Menu;
import com.ChiChiFOOD.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.ArrayList;
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
        session.update(restaurant);
    }

    public void delete(Restaurant restaurant) {
        session.beginTransaction();
        session.delete(restaurant);
        session.getTransaction().commit();
    }

    public Restaurant findById(Long id) {
        return session.get(Restaurant.class, id);
    }

    public boolean existsBySellerId(int sellerId) {
        String hql = "SELECT COUNT(r) FROM Restaurant r WHERE r.seller.id = :sellerId";
        Query<Long> query = session.createQuery(hql, Long.class);
        query.setParameter("sellerId", sellerId);
        return query.uniqueResult() > 0;
    }

    public int getMyRestaurantId(String sellerId) {
        String hql = "SELECT r.id FROM Restaurant r WHERE r.seller.id = :sellerId";
        return session.createQuery(hql, Integer.class)
                .setParameter("sellerId", sellerId)
                .uniqueResult();
    }

    public List<Restaurant> getRestaurantsBySellerId(String sellerId) {
        String hql = "FROM Restaurant r WHERE r.seller.id = :sellerId";
        return session.createQuery(hql, Restaurant.class)
                .setParameter("sellerId", sellerId)
                .getResultList();
    }

    public List<Restaurant> searchByName(String keyword) {
        String hql = "FROM Restaurant WHERE lower(name) LIKE :keyword";
        Query<Restaurant> query = session.createQuery(hql, Restaurant.class);
        query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        return query.list();
    }

    public List<Restaurant> findAll() {
        String hql = "FROM Restaurant";
        Query<Restaurant> query = session.createQuery(hql, Restaurant.class);
        return query.list();
    }

    public  boolean restaurantExistsByName(String name) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Long count = session.createQuery("SELECT COUNT(r) FROM Restaurant r WHERE r.name = :name", Long.class)
                .setParameter("name", name)
                .uniqueResult();
        session.close();
        return count != null && count > 0;
    }

    public boolean restaurantExistsByPhone(String phone) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Long count = session.createQuery("SELECT COUNT(r) FROM Restaurant r WHERE r.phone = :phone", Long.class)
                .setParameter("phone", phone)
                .uniqueResult();
        session.close();
        return count != null && count > 0;
    }

    public  boolean restaurantExistsById(String id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Long count = session.createQuery("SELECT COUNT(r) FROM Restaurant r WHERE r.id = :id", Long.class)
                .setParameter("id", id)
                .uniqueResult();
        session.close();
        return count != null && count > 0;
    }

    //maybe incorrect
    public List<Item> getRestaurantItems(Restaurant restaurant){
            String hql = "FROM Item WHERE restaurant = :restaurant";
            Query<Item> query = session.createQuery(hql, Item.class);
            query.setParameter("restaurant", restaurant);
            return query.list();
    }
    public List<Menu> getMenusByRestaurant(Restaurant restaurant) {
        String hql = "FROM Menu WHERE restaurant = :restaurant";
        Query<Menu> query = session.createQuery(hql, Menu.class);
        query.setParameter("restaurant", restaurant);
        return query.list();
    }
    public String getRestaurantName(int id) {
        String hql = "SELECT r.name FROM Restaurant r WHERE r.id = :id";
        Query<String> query = session.createQuery(hql, String.class);
        query.setParameter("id", id);
        return query.uniqueResult();
    }


    @Override
    public List<Restaurant> getAllRestaurants() {
        String hql = "FROM Restaurant";
        Query<Restaurant> query = session.createQuery(hql, Restaurant.class);
        return query.list();
    }

}
