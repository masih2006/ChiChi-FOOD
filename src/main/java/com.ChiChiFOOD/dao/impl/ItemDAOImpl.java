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

    @Override
    public void delete(Item item) {
        item.getMenus().forEach(menu -> menu.getItems().remove(item));
        item.getMenus().clear();
        session.merge(item);
        session.remove(item);
    }
@Override
    public int findPriceById(int itemId){
    Session session = HibernateUtil.getSessionFactory().openSession();
        Integer price = session.createQuery(
                        "SELECT i.price FROM Item i WHERE i.id = :itemId", Integer.class)
                .setParameter("itemId", itemId)
                .uniqueResult();
        session.close();
    return price;
}


    @Override
    public Item findById(int id) {
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

    public boolean itemExistsInMenu(String menuTitle, Long itemId, int restaurantId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Long count = session.createQuery(
                        "SELECT COUNT(mi) FROM Menu m JOIN m.items mi WHERE m.title = :title AND m.restaurant.id = :restaurantId AND mi.id = :itemId", Long.class)
                .setParameter("title", menuTitle)
                .setParameter("restaurantId", restaurantId)
                .setParameter("itemId", itemId)
                .uniqueResult();
        session.close();
        return count != null && count > 0;
    }




}
