package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.model.restaurant.Item;
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
//        session.beginTransaction();
        session.delete(item);
//        session.getTransaction().commit();
    }

    public Item findById(Long id) {
        return session.get(Item.class, id);
    }



}
