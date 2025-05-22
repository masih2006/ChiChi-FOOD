package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.dao.UserDao;
import com.ChiChiFOOD.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class UserDaoImpl implements UserDao {

    private final EntityManagerFactory entityManagerFactory;

    public UserDaoImpl() {
        // "chichifood-persistence-unit" را با نام persistence-unit خود در persistence.xml جایگزین کنید
        entityManagerFactory = Persistence.createEntityManagerFactory("ChiCHiFOOD_persistence_unit");
    }

    private EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    @Override
    public void save(User user) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    @Override
    public User findById(int id) {
        EntityManager em = getEntityManager();
        User user = null;
        try {
            user = em.find(User.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        EntityManager em = getEntityManager();
        List<User> users = null;
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
            users = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return users;
    }

    @Override
    public void update(User user) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.merge(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(int id) {
        EntityManager em = getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            User user = em.find(User.class, id);
            if (user != null) {
                em.remove(user);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}