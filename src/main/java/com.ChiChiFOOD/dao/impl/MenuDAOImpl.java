package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.restaurant.Menu;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class MenuDAOImpl implements MenuDAO {
    private final Session session;

    public MenuDAOImpl(Session session) {
        this.session = session;
    }

    public void save(Menu item) {
        session.persist(item);
    }

    public Menu findById(Long id) {
        return session.get(Menu.class, id);
    }

    public List<Menu> findAll() {
        return session.createQuery("FROM Menu", Menu.class).list();
    }

    public void update(Menu item) {
        Transaction tx = session.beginTransaction();
        session.update(item);
        tx.commit();
    }

    public void delete(Menu item) {
        Transaction tx = session.beginTransaction();
        session.remove(item);
        tx.commit();
    }
    /// may have problem
    public List<Menu> findByMenuId(Long menuId) {
        return session.createQuery("FROM Menu WHERE Menu.id = :menuId", Menu.class)
                .setParameter("menuId", menuId)
                .list();
    }
}
