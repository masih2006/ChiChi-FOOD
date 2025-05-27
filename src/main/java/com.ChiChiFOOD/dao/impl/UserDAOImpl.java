package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.dao.impl.*;
import com.ChiChiFOOD.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class UserDAOImpl implements UserDAO {
    private final Session session;

    public UserDAOImpl(Session session) {
        this.session = session;
    }

    @Override
    public void save(User user) {
        Transaction tx = session.beginTransaction();
        session.persist(user);
        tx.commit();
    }

    @Override
    public User findById(int id) {
        return session.get(User.class, id);
    }

    @Override
    public List<User> findAll() {
        return session.createQuery("from User", User.class).list();
    }

    @Override
    public void update(User user) {
        Transaction tx = session.beginTransaction();
        session.merge(user);
        tx.commit();
    }

    @Override
    public void delete(User user) {
        Transaction tx = session.beginTransaction();
        session.remove(user);
        tx.commit();
    }
}
