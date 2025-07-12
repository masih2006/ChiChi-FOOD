package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.dao.impl.UserDAO;
import com.ChiChiFOOD.model.User;
import org.hibernate.Session;

import java.util.List;

public class UserDAOImpl implements UserDAO {
    private final Session session;

    public UserDAOImpl(Session session) {
        this.session = session;
    }

    @Override
    public void save(User user) {
        session.persist(user);
    }

    @Override
    public User findById(int id) {
        return session.get(User.class, id);
    }

    @Override
    public List<User> findAll() {
        return session.createQuery("FROM User", User.class).list();
    }

    @Override
    public void update(User user) {
        session.merge(user);
    }

    @Override
    public void delete(User user) {
        session.remove(user);
    }

    @Override
    public User findByPhoneAndPassword(String phone, String password) {
        return session.createQuery(
                        "FROM User u WHERE u.phone = :phone AND u.password = :password", User.class)
                .setParameter("phone", phone)
                .setParameter("password", password)
                .uniqueResult();
    }
    public List<User> getAllUsers() {
        String hql = "FROM User";
        return session.createQuery(hql, User.class)
                .getResultList();
    }

}