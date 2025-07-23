package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.dao.impl.RestaurantDAO;
import com.ChiChiFOOD.dao.impl.RestaurantDAOImpl;
import com.ChiChiFOOD.dao.impl.UserDAO;
import com.ChiChiFOOD.dao.impl.UserDAOImpl;
import com.ChiChiFOOD.httphandler.Sender;
import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.model.User;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ChiChiFOOD.httphandler.Sender.sendJsonResponse;
import static com.ChiChiFOOD.httphandler.Sender.sendTextResponse;

public class AdminService {
    public static void getAllUsers(HttpExchange exchange, JsonObject jsonObject) throws IOException {
        if (!exchange.getAttribute("role").toString().equalsIgnoreCase("admin")) {
            Sender.sendTextResponse(exchange,403, "forbidden");
            return;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            UserDAO userDAO = new UserDAOImpl(session);
            List<User> users = userDAO.getAllUsers();
            List<Map<String, Object>> responseList = new ArrayList<>();
            for (User user : users) {
                Map<String, Object> userResponse = new LinkedHashMap<>();
                userResponse.put("id", user.getId());
                userResponse.put("full_name", user.getName());
                userResponse.put("phone", user.getPhone());
                userResponse.put("email", user.getEmail());
                userResponse.put("role", user.getRole()); // این از getClass().getSimpleName() میاد
                userResponse.put("address", user.getAddress());
                userResponse.put("profileImageBase64", user.getProfileImageBase64());
                Map<String, Object> bankInfo = new LinkedHashMap<>();
                bankInfo.put("bank_name", user.getBankName());
                bankInfo.put("account_number", user.getAccountNumber());
                userResponse.put("bank_info", bankInfo);
                responseList.add(userResponse);
            }
            String responseJson = new Gson().toJson(responseList);
            sendJsonResponse(exchange, 200, responseJson);
            return;
        }
    }

    public static void confirmUser(HttpExchange exchange, JsonObject jsonObject, String id) throws IOException {
        boolean confirmCode;
        if (jsonObject.has("status")) {
            if (jsonObject.get("status").equals("approved")) {
                confirmCode = true;
            }else {
                confirmCode = false;
            }
        }else {
            sendTextResponse(exchange, 400, "invalid field status");
            return;
        }
        if (!exchange.getAttribute("role").toString().equalsIgnoreCase("admin")) {
            sendTextResponse(exchange, 403, "forbidden");
            return;
        }
        Session session = HibernateUtil.getSessionFactory().openSession();
        UserDAO userDAO = new UserDAOImpl(session);
        User user = userDAO.findById(Integer.parseInt(id));
        if (user == null) {
            sendTextResponse(exchange, 404, "Resource not found");
            return;
        }
        if (confirmCode) {
            user.setUserConfirmed();
        }else {
            user.setUserNotConfirmed();
        }
        userDAO.update(user);
        sendTextResponse(exchange,200, "status updated");
        return;
    }
    public static void confirmRestaurant(HttpExchange exchange, JsonObject jsonObject, String id) throws IOException {
        if (!exchange.getAttribute("role").toString().equalsIgnoreCase("admin")) {
            sendTextResponse(exchange, 403, "forbidden");
            return;
        }
        Session session = HibernateUtil.getSessionFactory().openSession();
        RestaurantDAO restaurantDAO = new RestaurantDAOImpl(session);
        if (!restaurantDAO.restaurantExistsById(id)){
            sendTextResponse(exchange, 404, "Resource not found");
            return;
        }
        Restaurant restaurant = restaurantDAO.findById(Long.parseLong(id));
        restaurant.setRestaurantConfirmed(true);
        Transaction tx = session.beginTransaction();
        try {
            restaurantDAO.update(restaurant);
            tx.commit();
            sendTextResponse(exchange, 200, "status updated");
            session.close();
            return;
        }catch (Exception e){
            tx.rollback();
            sendTextResponse(exchange, 500, "internal error");
            session.close();
            return;
        }
    }

}
