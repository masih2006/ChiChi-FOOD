package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.Order;
import com.ChiChiFOOD.model.Restaurant;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class OrderDAOImpl implements OrderDAO {
    private final Session session;

    public OrderDAOImpl(Session daoSession) {
        this.session = daoSession;
    }
    @Override
public void save(Order order) {
        session.save(order);
    }

    @Override
    public void update(Order order) {
        session.update(order);
    }

    @Override
    public List<Order> findAll(){
        String hql = "FROM Order";
        Query<Order> query = session.createQuery(hql, Order.class);
        return query.list();
    }
    @Override
    public Order findById(int id){
        return session.get(Order.class, id);
    }
}
