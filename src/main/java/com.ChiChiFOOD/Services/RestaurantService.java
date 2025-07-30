package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.dao.impl.*;
import com.ChiChiFOOD.model.Order;
import com.ChiChiFOOD.model.OrderStatus;
import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.model.User;
import com.ChiChiFOOD.model.restaurant.Item;
import com.ChiChiFOOD.model.restaurant.Menu;
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
import java.util.stream.Collectors;

import static com.ChiChiFOOD.httphandler.Sender.sendJsonResponse;
import static com.ChiChiFOOD.httphandler.Sender.sendTextResponse;

public class RestaurantService {
    static Session DaoSession = HibernateUtil.getSessionFactory().openSession();
    static RestaurantDAO restaurantDAO = new RestaurantDAOImpl(DaoSession);
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
        if (!exchange.getAttribute("role").equals("seller")) {
            sendTextResponse(exchange, 403, "Forbidden request");
            return;
        }
        if (restaurantDAO.existsBySellerId(Integer.parseInt(exchange.getAttribute("userId").toString()))){
            sendTextResponse(exchange, 409, "1 seller 1 restaurant !!!!");
            return;
        }
        if (restaurantDAO.restaurantExistsByName(name) || restaurantDAO.restaurantExistsByPhone(phone)) {
            sendTextResponse(exchange, 409, "conflict occurred.");
            return;
        }
        //System.out.println("2");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try{
                RestaurantDAOImpl restaurantDaoImpl = new RestaurantDAOImpl(session);
                Session session1 = HibernateUtil.getSessionFactory().openSession();
                UserDAO userDAO = new UserDAOImpl(session1);
                User tempUser = userDAO.findById(Integer.parseInt(exchange.getAttribute("userId").toString()));
                Restaurant restaurant = new Restaurant(tempUser,name, phone, address, logoBase64, fee, additionalFee);
                restaurantDaoImpl.save(restaurant);
                tx.commit();
                sendTextResponse(exchange, 200, "Restaurant registered successfully! Wait for admin confirmation");
                return;
            }catch (Exception e) {
                e.printStackTrace();
                tx.rollback();
                sendTextResponse(exchange, 500, "Internal server error");
            }
        }
    }

    public static void getRestaurants(HttpExchange exchange) throws IOException {
        String SellerId  = exchange.getAttribute("userId").toString();
        if (!exchange.getAttribute("role").equals("seller")) {
            sendTextResponse(exchange, 403, "Forbidden request");
            return;
        }
        if (!restaurantDAO.existsBySellerId(Integer.parseInt(SellerId))){
            sendTextResponse(exchange, 404, "Resource not found");
            return;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            RestaurantDAOImpl restaurantDaoImpl = new RestaurantDAOImpl(session);
            List<Restaurant> restaurants = restaurantDaoImpl.getRestaurantsBySellerId(SellerId);
            List<Map<String, Object>> responseList = new ArrayList<>();
            for (Restaurant restaurant : restaurants) {
                Map<String, Object> restaurantResponse = new LinkedHashMap<>();
                restaurantResponse.put("id", restaurant.getId());
                restaurantResponse.put("name", restaurant.getName());
                restaurantResponse.put("address", restaurant.getAddress());
                restaurantResponse.put("phone", restaurant.getPhone());
                restaurantResponse.put("logoBase64", restaurant.getLogoBase64());
                restaurantResponse.put("tax_fee", restaurant.getTaxFee());
                restaurantResponse.put("additional_fee", restaurant.getAdditionalFee());
                restaurantResponse.put("isRestaurantConfirmed", restaurant.isRestaurantConfirmed());
                responseList.add(restaurantResponse);
            }
            String responseJson = new Gson().toJson(responseList);
            sendTextResponse(exchange, 200, responseJson);
            return;
        }

    }

    public static void updateRestaurant(HttpExchange exchange, JsonObject jsonRequest, String restaurantId) throws IOException {
        String SellerId  = exchange.getAttribute("userId").toString();
        if (!exchange.getAttribute("role").equals("seller")) {
            sendTextResponse(exchange, 403, "Forbidden request");
        }
        if (!restaurantDAO.restaurantExistsById(restaurantId)) {
            sendTextResponse(exchange, 404, "Resource not found");
            return;
        }try  (Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            try {
                RestaurantDAO restaurantDao = new RestaurantDAOImpl(session);
                Restaurant restaurant = restaurantDao.findById(Long.parseLong(restaurantId));
                if (jsonRequest.has("name")) restaurant.setName(jsonRequest.get("name").getAsString());
                if (jsonRequest.has("tax_fee")) restaurant.setTaxFee(jsonRequest.get("tax_fee").getAsInt());
                if (jsonRequest.has("tax_type")) {
                    try {
                        String taxTypeStr = jsonRequest.get("tax_type").getAsString().toUpperCase();
                        Restaurant.TaxType taxType = Restaurant.TaxType.valueOf(taxTypeStr);
                        restaurant.setTaxType(taxType);
                    } catch (IllegalArgumentException e) {
                        sendTextResponse(exchange, 400, "Invalid tax_type value. Must be 'FIXED' or 'PERCENTAGE'");
                        return;
                    }
                }
                if (jsonRequest.has("phone")) restaurant.setPhone(jsonRequest.get("phone").getAsString());
                if (jsonRequest.has("address")) restaurant.setAddress(jsonRequest.get("address").getAsString());
                if (jsonRequest.has("logoBase64")) restaurant.setLogoBase64(jsonRequest.get("logoBase64").getAsString());
                if (jsonRequest.has("additional_fee")) restaurant.setAdditionalFee(jsonRequest.get("additional_fee").getAsInt());
                if (jsonRequest.has("packagingFee")) restaurant.setPackagingFee(jsonRequest.get("packagingFee").getAsInt());
                restaurantDao.update(restaurant);
                sendTextResponse(exchange, 200, "Restaurant updated successfully");
                tx.commit();
            }catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
                sendTextResponse(exchange, 500, "Internal server error");
            }
        }
    }

    public static void getAllItems(HttpExchange exchange,String restaurantId) throws IOException {
        Session session = HibernateUtil.getSessionFactory().openSession();
        RestaurantDAO restaurantDao = new RestaurantDAOImpl(session);
        if (!restaurantDao.restaurantExistsById(restaurantId)) {
            sendTextResponse(exchange, 404, "Resource not found");
            return;
        }
        List <Item> items = restaurantDao.getRestaurantItems(restaurantDao.findById(Long.parseLong(restaurantId)));
        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Item item : items) {
            Map<String, Object> itemResponse = new LinkedHashMap<>();
            itemResponse.put("id", item.getId());
            itemResponse.put("name", item.getName());
            itemResponse.put("imageBase64",item.getImageBase64() );
            itemResponse.put("price", item.getPrice());
            itemResponse.put("supply", item.getSupply());
            responseList.add(itemResponse);
        }
        String responseJson = new Gson().toJson(responseList);
        System.out.println(responseJson);
        sendTextResponse(exchange, 200, responseJson);
        session.close();
        return;
    }

    public static void getAllMenus(HttpExchange exchange, String restaurantId) throws IOException {
        Session session = HibernateUtil.getSessionFactory().openSession();
        RestaurantDAO restaurantDao = new RestaurantDAOImpl(session);

        if (!restaurantDao.restaurantExistsById(restaurantId)) {
            sendTextResponse(exchange, 404, "Resource not found");
            return;
        }

        List<Menu> menus = restaurantDao.getMenusByRestaurant(restaurantDao.findById(Long.parseLong(restaurantId)));
        List<Map<String, Object>> responseList = new ArrayList<>();

        for (Menu menu : menus) {
            Map<String, Object> menuResponse = new LinkedHashMap<>();
            menuResponse.put("id", menu.getId().toString());
            menuResponse.put("title", menu.getTitle());

            List<Map<String, Object>> itemList = new ArrayList<>();
            for (Item item : menu.getItems()) {
                Map<String, Object> itemMap = new LinkedHashMap<>();
                itemMap.put("id", item.getId());
                itemMap.put("name", item.getName());
                itemMap.put("description", item.getDescription());
                itemMap.put("price", item.getPrice());
                itemMap.put("supply", item.getSupply());
                itemMap.put("imageBase64", item.getImageBase64());
                itemList.add(itemMap);
            }

            menuResponse.put("items", itemList);
            responseList.add(menuResponse);
        }

        System.out.println(responseList);

        String responseJson = new Gson().toJson(responseList);
        sendTextResponse(exchange, 200, responseJson);
        session.close();
    }

    public static void changeStatusOfOrder(HttpExchange exchange,JsonObject jsonObject, String orderID) throws IOException {
       String orderStatus;
       try {
            orderStatus = jsonObject.get("status").getAsString();
       }catch (Exception e) {
        sendTextResponse(exchange, 400, "invalid order status");
        return;
       }
       try (Session session = HibernateUtil.getSessionFactory().openSession()){
           Transaction tx = session.beginTransaction();
           try{
               OrderDAO orderDao = new OrderDAOImpl(session);
               Order order = orderDao.findById(Integer.parseInt(orderID));
               if (order == null) {
                   sendTextResponse(exchange, 404, "Order not found");
                   return;
               }
               order.setStatus(OrderStatus.fromString(orderStatus));
               orderDao.update(order);
               sendTextResponse(exchange, 200, "Order updated successfully");
               tx.commit();
               return;
           }catch (Exception e) {
               tx.rollback();
               e.printStackTrace();
               sendTextResponse(exchange, 500, "Internal server error");
               return;
           }
       }


    }
}
