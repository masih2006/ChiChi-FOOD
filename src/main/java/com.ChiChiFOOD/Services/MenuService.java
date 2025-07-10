package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.dao.impl.*;
import com.ChiChiFOOD.httphandler.Sender;
import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.model.restaurant.Item;
import com.ChiChiFOOD.model.restaurant.Menu;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;

import static com.ChiChiFOOD.httphandler.Sender.sendTextResponse;
public class MenuService {
    public static void createMenu(HttpExchange exchange, JsonObject jsonRequest, String RestaurantId) throws IOException {
        String title = "";
        if (jsonRequest.has("title")) {
            title = jsonRequest.get("title").getAsString();
        }else{
            sendTextResponse(exchange,400, "invalid input");
        }
        // check validity of restaurantId:
        try {
            Long.parseLong(RestaurantId);
        }catch (Exception e) {
            sendTextResponse(exchange,400, "invalid input");
            return;
        }
        if (!exchange.getAttribute("role").equals("seller")) {
            sendTextResponse(exchange, 403, "Forbidden request");
            return;
        }
        Session RestaurantSession = HibernateUtil.getSessionFactory().openSession();
        RestaurantDAO restaurantDAO = new RestaurantDAOImpl(RestaurantSession);
        if (!restaurantDAO.restaurantExistsById(RestaurantId)) {
            sendTextResponse(exchange, 404, "resource not found");
            return;
        }
        // here we check request sender is owner of restaurant or no ?
        if (restaurantDAO.getMyRestaurantId(exchange.getAttribute("userId").toString()) != Integer.parseInt(RestaurantId) ) {
            sendTextResponse(exchange, 403, "Forbidden request");
            return;
        }
        RestaurantSession.close();
        Session session = HibernateUtil.getSessionFactory().openSession();
        MenuDAO menuDAO = new MenuDAOImpl(session);
       if (menuDAO.menuExistByTitle(title, Integer.parseInt(RestaurantId))) {
           sendTextResponse(exchange, 409, "conflict occurred");
           return;
       }
        Transaction tx = session.beginTransaction();


        try {
            Restaurant restaurant = session.get(Restaurant.class, Long.parseLong(RestaurantId));
            Menu menu = new Menu();
            menu.setTitle(title);
            menu.setRestaurant(restaurant);
            restaurant.getMenus().add(menu); // اختیاریه، فقط برای دوطرفه بودن رابطه مفیده
            menuDAO.save(menu);
            tx.commit();
            sendTextResponse(exchange, 200, "Menu created successfully");

        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
            sendTextResponse(exchange, 500, "Internal server error");
        } finally {
            session.close();
        }
    }

    public static void addItemToMenu(HttpExchange exchange, JsonObject jsonRequest, String RestaurantId, String title) throws IOException {
        int itemId = 0;
        if (jsonRequest.has("item_id") || jsonRequest.get("item_id").isJsonNull()) {
            itemId = Integer.parseInt(jsonRequest.get("item_id").getAsString());
        }else{
            sendTextResponse(exchange, 400, "invalid input");
        }
        // check validity of restaurantId:
        try {
            Long.parseLong(RestaurantId);
        }catch (Exception e) {
            sendTextResponse(exchange,400, "invalid input");
            return;
        }

        if (!exchange.getAttribute("role").equals("seller")) {
            sendTextResponse(exchange, 403, "Forbidden request");
            return;
        }
        Session RestaurantSession = HibernateUtil.getSessionFactory().openSession();
        RestaurantDAO restaurantDAO = new RestaurantDAOImpl(RestaurantSession);
        if (restaurantDAO.getMyRestaurantId(exchange.getAttribute("userId").toString()) != Integer.parseInt(RestaurantId) ) {
            sendTextResponse(exchange, 403, "Forbidden request");
            return;
        }
        RestaurantSession.close();
        Session itemsession = HibernateUtil.getSessionFactory().openSession();
        ItemDAO itemDAO = new ItemDAOImpl(itemsession);
        if (!itemDAO.itemExistsByIdAndRestaurantId(itemId,Integer.parseInt(RestaurantId))) {
            sendTextResponse(exchange, 404, "resource not found");
            return;
        }
        Session MenuSession = HibernateUtil.getSessionFactory().openSession();
        MenuDAO menuDAO = new MenuDAOImpl(MenuSession);
        if (!menuDAO.menuExistByTitle(title, Integer.parseInt(RestaurantId))) {
            sendTextResponse(exchange, 404, "resource not found");
            return;
        }
        if (menuDAO.itemExistsInMenu(title, itemId, Integer.parseInt(RestaurantId))){
            sendTextResponse(exchange, 409, "conflict occurred");
            return;
        }
        Transaction tx = MenuSession.beginTransaction();
        try {
            Menu menu = menuDAO.findByTitle(title, Integer.parseInt(RestaurantId));
            Item item = itemDAO.findItemByIdAndRestaurantId(itemId,Integer.parseInt(RestaurantId));
            menu.getItems().add(item);
            menuDAO.update(menu);
            tx.commit();
            sendTextResponse(exchange, 200, "item added to menu successfully");
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            sendTextResponse(exchange, 500, "internal server error");
        } finally {
            MenuSession.close();
            itemsession.close();
        }


    }
}
