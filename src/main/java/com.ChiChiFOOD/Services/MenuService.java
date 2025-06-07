package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.dao.impl.MenuDAO;
import com.ChiChiFOOD.dao.impl.MenuDAOImpl;
import com.ChiChiFOOD.dao.impl.RestaurantDAOImpl;
import com.ChiChiFOOD.httphandler.Sender;
import com.ChiChiFOOD.model.Restaurant;
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
        if (!exchange.getAttribute("role").equals("seller")) {
            sendTextResponse(exchange, 403, "Forbidden request");
            return;
        }
        if (!RestaurantService.restaurantExistsById(RestaurantId)) {
            sendTextResponse(exchange, 404, "resource not found");
            return;
        }
        // check 409
        Session session = HibernateUtil.getSessionFactory().openSession();
        MenuDAO menuDAO = new MenuDAOImpl(session);
        Menu tempmenu = new Menu();
        tempmenu = menuDAO.findById(Long.parseLong(RestaurantId));
        if (!(tempmenu == null)) {
            sendTextResponse(exchange, 409, "conflict occurred");
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

        session.close();
    }
}
