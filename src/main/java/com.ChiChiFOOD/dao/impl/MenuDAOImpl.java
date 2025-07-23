package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.restaurant.Item;
import com.ChiChiFOOD.model.restaurant.Menu;
import com.ChiChiFOOD.utils.HibernateUtil;
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
        session.update(item);
    }

    @Override
    public void delete(Menu menu) {
        Menu persistentMenu = session.get(Menu.class, menu.getId());
        if (persistentMenu != null) {
            persistentMenu.getItems().clear();
            session.flush();
            session.delete(persistentMenu);
        }
    }


    /// may have problem
    public List<Menu> findByMenuId(Long menuId) {
        return session.createQuery("FROM Menu WHERE Menu.id = :menuId", Menu.class)
                .setParameter("menuId", menuId)
                .list();
    }
    public boolean menuExistByTitle(String title, int restaurantId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Long count = session.createQuery(
                        "SELECT COUNT(m) FROM Menu m WHERE m.title = :title AND m.restaurant.id = :restaurantId", Long.class)
                .setParameter("title", title)
                .setParameter("restaurantId", restaurantId)
                .uniqueResult();
        session.close();
        return count != null && count > 0;
    }
    public Menu findByTitle(String title, int restaurantId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Menu menu = session.createQuery(
                        "SELECT m FROM Menu m LEFT JOIN FETCH m.items WHERE m.title = :title AND m.restaurant.id = :restaurantId",
                        Menu.class)
                .setParameter("title", title)
                .setParameter("restaurantId", restaurantId)
                .uniqueResult();
        session.close();
        return menu;
    }

    public boolean itemExistsInMenu(String title, int itemId, int restaurantId) {
            Menu menu = findByTitle(title, restaurantId);
            for (Item i : menu.getItems()) {
                if (i.getId() == itemId) {
                    return true;
                }
            }
            return false;
    }

}



