package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.dao.impl.RestaurantDAO;
import com.ChiChiFOOD.dao.impl.RestaurantDAOImpl;
import com.ChiChiFOOD.dao.impl.UserDAO;
import com.ChiChiFOOD.dao.impl.UserDAOImpl;
import com.ChiChiFOOD.httphandler.Sender;
import com.ChiChiFOOD.model.Buyer;
import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.model.User;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ChiChiFOOD.httphandler.Sender.sendJsonResponse;

public class ActionService {
    public static void addRestaurantToFavorites(HttpExchange exchange, String id) throws IOException {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            RestaurantDAO restaurantDao = new RestaurantDAOImpl(session);

            if (!exchange.getAttribute("role").equals("buyer")) {
                Sender.sendTextResponse(exchange, 403, "forbidden!");
                return;
            }

            if (!restaurantDao.restaurantExistsById(id)) {
                Sender.sendTextResponse(exchange, 404, "Resource Not Found");
                return;
            }

            Restaurant restaurant = restaurantDao.findById(Long.parseLong(id));
            UserDAO userDao = new UserDAOImpl(session);
            Buyer user = (Buyer) userDao.findById(Integer.parseInt(exchange.getAttribute("userId").toString()));

            if (user == null){
                Sender.sendTextResponse(exchange, 404, "User Not Found");
                return;
            }

            if (user.getFavoriteRestaurants().contains(restaurant)){
                Sender.sendTextResponse(exchange, 409, "conflict occured");
                return;
            }

            user.addFavoriteRestaurant(restaurant);
            userDao.update(user);

            tx.commit(); // این مهم‌ترین بخش است
            Sender.sendTextResponse(exchange, 200, "added");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    public static void removeRestaurantFromFavorites(HttpExchange exchange, String id) throws IOException {
        if (id == null || id.isEmpty()) {
            Sender.sendTextResponse(exchange, 400, "missing required parameter");
            return;
        }
        if (!exchange.getAttribute("role").equals("buyer")) {
            Sender.sendTextResponse(exchange, 403, "forbidden!");
            return;
        }
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            RestaurantDAO restaurantDao = new RestaurantDAOImpl(session);
            if (!restaurantDao.restaurantExistsById(id)) {
                Sender.sendTextResponse(exchange, 404, "Resource Not Found");
                return;
            }
            Restaurant restaurant = restaurantDao.findById(Long.parseLong(id));
            Buyer user = (Buyer) session.get(Buyer.class, Long.parseLong(exchange.getAttribute("userId").toString()));
            if (!user.isFavorite(restaurant)) {
                Sender.sendTextResponse(exchange, 403, "Forbidden request");
                return;
            }
            UserDAO userDao = new UserDAOImpl(session);
            user.removeFavoriteRestaurant(restaurant);
            userDao.update(user);
            tx.commit();
            Sender.sendTextResponse(exchange, 200, "removed");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    public static void getFavorites(HttpExchange exchange) throws IOException {
        if (!exchange.getAttribute("role").equals("buyer")) {
            Sender.sendTextResponse(exchange, 403, "forbidden!");
            return;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Buyer user = (Buyer) session.get(Buyer.class, Long.parseLong(exchange.getAttribute("userId").toString()));
            List<Restaurant> favorites = user.getFavoriteRestaurants();
            List<Map<String, Object>> responseList = new ArrayList<>();
            for (Restaurant restaurant : favorites) {
                Map<String, Object> restaurantResponse = new LinkedHashMap<>();
                restaurantResponse.put("id", restaurant.getId());
                restaurantResponse.put("name", restaurant.getName());
                restaurantResponse.put("address", restaurant.getAddress());
                restaurantResponse.put("phone", restaurant.getPhone());
                restaurantResponse.put("logoBase64", restaurant.getLogoBase64());
                restaurantResponse.put("tax_fee", restaurant.getTaxFee());
                restaurantResponse.put("additional_fee", restaurant.getAdditionalFee());
                responseList.add(restaurantResponse);
            }
            String responseJson = new Gson().toJson(responseList);
            sendJsonResponse(exchange, 200, responseJson);
            return;
        }
    }
}
