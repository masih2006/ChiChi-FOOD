package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.model.restaurant.FoodItem;
import org.hibernate.Session;

import java.util.ArrayList;

public class ItemService {
    private final Session SESSION;

    public ItemService(Session session) {
        this.SESSION = session;
    }

    public boolean addItem(String name, String imageBase64, String description, int price, int supply, ArrayList<String> keywords) {
        try {
            if (isItemExists(name)) {
                System.out.println("Item already exists.");
                return false;
            }

            FoodItem item = new FoodItem();
            item.setName(name);
            item.setImageBase64(imageBase64);
            item.setDescription(description);
            item.setPrice(price);
            item.setSupply(supply);
            item.setKeywords(keywords);
            SESSION.persist(item);
            System.out.println("Added success.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteItemFromRestaurant(int restaurantId, int itemId) {
        try {
            FoodItem item = SESSION.get(FoodItem.class, itemId);
            if (item == null) return false;

            // بررسی تعلق آیتم به رستوران
            if (item.getRestaurant() == null || item.getRestaurant().getId() != restaurantId) {
                return false;
            }

            // حذف از لیست رستوران
            item.getRestaurant().removeFoodItem(item);

            // حذف آیتم از دیتابیس
            SESSION.remove(item);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isItemExists(String name) {
        try {
            Long count = SESSION.createQuery("SELECT COUNT(f) FROM FoodItem f WHERE f.name = :name", Long.class)
                    .setParameter("name", name)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
