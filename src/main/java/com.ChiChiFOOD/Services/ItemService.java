package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.dao.impl.*;
import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.model.restaurant.Item;
import com.ChiChiFOOD.model.restaurant.Menu;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.ChiChiFOOD.httphandler.Sender.sendTextResponse;

public class ItemService {
    private final Session SESSION;

    public ItemService(Session session) {
        this.SESSION = session;
    }

    public static void addItem(HttpExchange exchange, JsonObject jsonRequest, String restaurantId) throws IOException {
        String name;
        String imageBase64;
        String description;
        Integer price;
        Integer supply;
        JsonArray keywordsJsonArray;
        ArrayList<String> keywords = new ArrayList<>();
        try {
            name = jsonRequest.has("name") ? jsonRequest.get("name").getAsString() : null;
            imageBase64 = jsonRequest.has("imageBase64") ? jsonRequest.get("imageBase64").getAsString() : null;
            description = jsonRequest.has("description") ? jsonRequest.get("description").getAsString() : null;
            price = jsonRequest.has("price") ? jsonRequest.get("price").getAsInt() : 0;
            supply = jsonRequest.has("supply") ? jsonRequest.get("supply").getAsInt() : 0;
            keywordsJsonArray = jsonRequest.has("keywords") ? jsonRequest.get("keywords").getAsJsonArray() : null;
            for (int i = 0; i < keywordsJsonArray.size(); i++) keywords.add(keywordsJsonArray.get(i).getAsString());
        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 400, "Invalid JSON format or data types");
            return;
        }
        if (name == null || description == null || price == null) {
            sendTextResponse(exchange, 400, "Missing required fields: name, address or phone");
            return;
        }
        if (!exchange.getAttribute("role").equals("seller")) {
            sendTextResponse(exchange, 403, "Forbidden request");
            return;
        }
        /////////// in haro ba ejazat man ezafe karadam
        Session DAOsession = HibernateUtil.getSessionFactory().openSession();
        ItemDAO itemDAO = new ItemDAOImpl(DAOsession);
        if (itemDAO.itemExistsByName(name, Integer.parseInt(restaurantId))) {
            sendTextResponse(exchange, 409, "conflict occurred");
        }
        DAOsession.close();
        Session DAOsession2 = HibernateUtil.getSessionFactory().openSession();
        RestaurantDAO restaurantDAO = new RestaurantDAOImpl(DAOsession2);
        // here we check request sender is owner of restaurant or no ?
        if (restaurantDAO.getMyRestaurantId(exchange.getAttribute("userId").toString()) != Integer.parseInt(restaurantId) ) {
            sendTextResponse(exchange, 403, "Forbidden request");
            return;
        }
        ///  ta inja
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Restaurant restaurant = new RestaurantDAOImpl(session).findById(Long.parseLong(restaurantId));
                if (restaurant == null) {
                    sendTextResponse(exchange, 404, "Restaurant not found");
                    return;
                }
                ItemDAOImpl itemDAOImpl = new ItemDAOImpl(session);
                Item item = new Item();
                item.setName(name);
                item.setImageBase64(imageBase64);
                item.setDescription(description);
                item.setPrice(price);
                item.setSupply(supply);
                item.setKeywords(keywords);
                item.setRestaurant(restaurant);
                itemDAOImpl.save(item);
                sendTextResponse(exchange, 200, "Item registered successfully");
                return;
            } catch (Exception e) {
                e.printStackTrace();
                tx.rollback();
                sendTextResponse(exchange, 500, "Internal server error");
            }
        }
    }

    public static void updateItem(HttpExchange exchange, JsonObject jsonRequest, String restaurantId, String itemId) throws IOException {
        if (!exchange.getAttribute("role").equals("seller")) {
            sendTextResponse(exchange, 403, "Forbidden request");
        }
        if (!isItemExists(itemId)) {
            sendTextResponse(exchange, 404, "Resource not found");
            return;
        }
        try  (Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            try {
                ItemDAO itemDao = new ItemDAOImpl(session);
                Item item = itemDao.findById(Long.parseLong(itemId));
                if (jsonRequest.has("name")) item.setName(jsonRequest.get("name").getAsString());
                if (jsonRequest.has("imageBase64")) item.setImageBase64(jsonRequest.get("imageBase64").getAsString());
                if (jsonRequest.has("description")) item.setDescription(jsonRequest.get("description").getAsString());
                if (jsonRequest.has("price")) item.setPrice(jsonRequest.get("price").getAsInt());
                if (jsonRequest.has("supply")) item.setSupply(jsonRequest.get("supply").getAsInt());
                if (jsonRequest.has("keywords")) {
                    JsonArray keywordsJsonArray = jsonRequest.get("keywords").getAsJsonArray();
                    ArrayList<String> keywords = new ArrayList<>();
                    for (int i = 0; i < keywordsJsonArray.size(); i++) keywords.add(keywordsJsonArray.get(i).getAsString());
                    item.setKeywords(keywords);
                }
            itemDao.update(item);
                sendTextResponse(exchange, 200, "Item updated successfully");
                tx.commit();
            }catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
                sendTextResponse(exchange, 500, "Internal server error");
            }
        }
    }

    public static void deleteItem(HttpExchange exchange, String restaurantId, String itemId) throws IOException {
        if (!exchange.getAttribute("role").equals("seller")) {
            sendTextResponse(exchange, 403, "Forbidden request");
            return;
        }

        if (!isItemExists(itemId)) {
            sendTextResponse(exchange, 404, "Item not found");
            return;
        }

        Session RestaurantSession = HibernateUtil.getSessionFactory().openSession();
        RestaurantDAO restaurantDAO = new RestaurantDAOImpl(RestaurantSession);
        if (restaurantDAO.getMyRestaurantId(exchange.getAttribute("userId").toString()) != Integer.parseInt(restaurantId) ) {
            sendTextResponse(exchange, 403, "Forbidden request");
            return;
        }
        RestaurantSession.close();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                ItemDAO itemDao = new ItemDAOImpl(session);
                Item item = itemDao.findById(Long.parseLong(itemId));
                int restId = Integer.parseInt(restaurantId);
                System.out.println(item.getRestaurant().getId());
                System.out.println(restId);
                if (item.getRestaurant() == null || !Objects.equals(item.getRestaurant().getId(), restId)) {
                    sendTextResponse(exchange, 403, "Item does not belong to the specified restaurant");
                    return;
                }
                itemDao.delete(item);
                tx.commit();
                sendTextResponse(exchange, 200, "Item deleted successfully");
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
                sendTextResponse(exchange, 500, "Internal server error");
            }
        }
    }

    public static void deleteItemFromMenu(HttpExchange exchange, String restaurantId, String title, String itemId) throws IOException {
        Session session = HibernateUtil.getSessionFactory().openSession();

        MenuDAO menuDAO = new MenuDAOImpl(session);
        ItemDAO itemDAO = new ItemDAOImpl(session);
        RestaurantDAO restaurantDAO = new RestaurantDAOImpl(session);

        try {
            Long.parseLong(restaurantId);
            Long.parseLong(itemId);
        } catch (Exception e) {
            sendTextResponse(exchange, 400, "invalid input");
            return;
        }

        if (!exchange.getAttribute("role").equals("seller")) {
            sendTextResponse(exchange, 403, "Forbidden request");
            return;
        }

        if (!restaurantDAO.restaurantExistsById(restaurantId)) {
            sendTextResponse(exchange, 404, "resource not found");
            return;
        }

        if (restaurantDAO.getMyRestaurantId(exchange.getAttribute("userId").toString()) != Integer.parseInt(restaurantId)) {
            sendTextResponse(exchange, 403, "Forbidden request");
            return;
        }

        if (!menuDAO.menuExistByTitle(title, Integer.parseInt(restaurantId))) {
            sendTextResponse(exchange, 404, "resource not found");
            return;
        }

        if (!itemDAO.itemExistsInMenu(title, Long.parseLong(itemId), Integer.parseInt(restaurantId))) {
            sendTextResponse(exchange, 404, "resource not found");
            return;
        }

        Transaction tx = session.beginTransaction();
        try {
            // همه چی با یک session لود میشه
            Menu menu = menuDAO.findByTitle(title, Integer.parseInt(restaurantId));
            Item item = itemDAO.findById(Long.parseLong(itemId));

            // حذف آیتم از منو
            menu.getItems().removeIf(i -> i.getId() == item.getId());
            item.getMenus().removeIf(m -> m.getId().equals(menu.getId()));

            // نیازی به update نیست چون session دنبال می‌کنه
            session.merge(menu);
            session.merge(item);

            tx.commit();
            sendTextResponse(exchange, 200, "Item removed from menu successfully");
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal server error");
        } finally {
            session.close();
        }
    }



    public static boolean isItemExists(String id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Long count = session.createQuery("SELECT COUNT(i) FROM Item i WHERE i.id = :id", Long.class)
                .setParameter("id", id)
                .uniqueResult();
        session.close();
        return count != null && count > 0;
    }
}


