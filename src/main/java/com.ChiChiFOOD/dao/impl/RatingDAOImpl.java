package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.Rating;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class RatingDAOImpl implements RatingDAO {
    private final Session session;

    public RatingDAOImpl(Session session) {
        this.session = session;
    }

    @Override
    public void save(Rating rating) {
        session.save(rating);
    }

    @Override
    public Rating getById(int id) {
        return session.get(Rating.class, id);
    }

    @Override
    public List<Rating> getAll() {
        String hql = "FROM Rating";
        Query<Rating> query = session.createQuery(hql, Rating.class);
        return query.list();
    }

    @Override
    public List<Rating> getByOrderId(String orderId) {
        String hql = "FROM Rating r WHERE r.orderID = :orderId";
        Query<Rating> query = session.createQuery(hql, Rating.class);
        query.setParameter("orderId", orderId);
        return query.list();
    }

    @Override
    public void delete(int id) {
        Rating rating = getById(id);
        if (rating != null) {
            session.delete(rating);
        }
    }
}
