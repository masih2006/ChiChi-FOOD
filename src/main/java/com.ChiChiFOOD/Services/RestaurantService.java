package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.dao.impl.RestaurantDao;
import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.ChiChiFOOD.httphandler.Sender.sendTextResponse;

public class RestaurantService {

    public static void registerRestaurant(HttpExchange exchange, JsonObject jsonRequest) throws IOException {
        String name;
        String address;
        String phone;
        String logoBase64;
        int fee , additionalFee;
        try {
            name = jsonRequest.has("name") ? jsonRequest.get("name").getAsString() : null;
            address = jsonRequest.has("address") ? jsonRequest.get("address").getAsString() : null;
            phone = jsonRequest.has("phone") ? jsonRequest.get("phone").getAsString() : null;
            logoBase64 = jsonRequest.has("logoBase64") ? jsonRequest.get("logoBase64").getAsString() : null;
            fee = jsonRequest.has("tax_fee") ? jsonRequest.get("tax_fee").getAsInt() : 0;
            additionalFee = jsonRequest.has("additional_fee") ? jsonRequest.get("additional_fee").getAsInt() : 0;
        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 400, "Invalid JSON format or data types");
            return;
        }
        if (name == null || address == null || phone == null) {
            sendTextResponse(exchange, 400, "Missing required fields: name, address or phone");
            return;
        }
        System.out.println("3");
        if (!exchange.getAttribute("role").equals("seller")) {
            sendTextResponse(exchange, 403, "Forbidden request");
            return;
        }

        if (restaurantExistsByName(name) || restaurantExistsByPhone(phone)) {
            sendTextResponse(exchange, 409, "conflict occurred.");
            return;
        }
        System.out.println("2");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try{
                RestaurantDao restaurantDao = new RestaurantDao(session);
                Restaurant restaurant = new Restaurant(name, phone, address, logoBase64, fee, additionalFee);
                restaurantDao.save(restaurant);
                sendTextResponse(exchange, 200, "User registered successfully");
                return;
            }catch (Exception e) {
                e.printStackTrace();
                tx.rollback();
                sendTextResponse(exchange, 500, "Internal server error");
            }
        }
    }

    public static boolean restaurantExistsByName(String name) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Long count = session.createQuery("SELECT COUNT(r) FROM Restaurant r WHERE r.name = :name", Long.class)
                .setParameter("name", name)
                .uniqueResult();
        session.close();
        return count != null && count > 0;
    }

    public static boolean restaurantExistsByPhone(String phone) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Long count = session.createQuery("SELECT COUNT(r) FROM Restaurant r WHERE r.phone = :phone", Long.class)
                .setParameter("phone", phone)
                .uniqueResult();
        session.close();
        return count != null && count > 0;
    }





}
