package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.model.restaurant.Item;
import com.ChiChiFOOD.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class ItemDAOImpl implements ItemDAO {
    private final Session session;

    public ItemDAOImpl(Session session) {
        this.session = session;
    }

    public void save(Item item) {
        session.save(item);
    }

    public void update(Item item) {
        session.update(item);
    }

    public void delete(Item item) {
        session.delete(item);
    }

    public Item findById(Long id) {
        return session.get(Item.class, id);
    }

    public boolean itemExistsByName(String name, int restaurantId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Long count = session.createQuery(
                        "SELECT COUNT(i) FROM Item i WHERE i.name = :name AND i.restaurant.id = :restaurantId", Long.class)
                .setParameter("name", name)
                .setParameter("restaurantId", restaurantId)
                .uniqueResult();
        session.close();
        return count != null && count > 0;
    }
    public Item findItemByIdAndRestaurantId(int itemId, int restaurantId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Item item = session.createQuery(
                        "FROM Item i WHERE i.id = :itemId AND i.restaurant.id = :restaurantId", Item.class)
                .setParameter("itemId", itemId)
                .setParameter("restaurantId", restaurantId)
                .uniqueResult();
        session.close();
        return item;
    }
    public boolean itemExistsByIdAndRestaurantId(int itemId, int restaurantId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Long count = session.createQuery(
                        "SELECT COUNT(i) FROM Item i WHERE i.id = :itemId AND i.restaurant.id = :restaurantId", Long.class)
                .setParameter("itemId", itemId)
                .setParameter("restaurantId", restaurantId)
                .uniqueResult();
        session.close();
        return count != null && count > 0;
    }





}
