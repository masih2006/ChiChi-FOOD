package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.dao.impl.*;
import com.ChiChiFOOD.model.Order;
import com.ChiChiFOOD.model.OrderStatus;
import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.model.User;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ChiChiFOOD.httphandler.Sender.sendJsonResponse;
import static com.ChiChiFOOD.httphandler.Sender.sendTextResponse;

public class DeliveryService {

    public static void findCourier(HttpExchange exchange) throws IOException {
        Session DaoSession = HibernateUtil.getSessionFactory().openSession();
        OrderDAO orderDAO = new OrderDAOImpl(DaoSession);
        RestaurantDAO restaurantDAO = new RestaurantDAOImpl(DaoSession);

        try {
            List<Order> allOrders = orderDAO.findAll();

            List<Order> availableOrders = allOrders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.FINDING_COURIER)
                    .collect(Collectors.toList());

            Gson gson = new Gson();
            JsonArray ordersArray = new JsonArray();

            for (Order order : availableOrders) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", order.getId());
                obj.addProperty("delivery_address", order.getDeliveryAddress());
                obj.addProperty("customer_id", order.getCustomerID());
                obj.addProperty("vendor_id", order.getVendorID());
                obj.addProperty("vendor_name",restaurantDAO.getRestaurantName(order.getVendorID()));
                obj.addProperty("coupon_id", order.getCouponID());

                JsonArray itemArray = gson.toJsonTree(order.getItemIDs()).getAsJsonArray();
                obj.add("item_ids", itemArray);

                obj.addProperty("raw_price", order.getRawPrice());
                obj.addProperty("tax_fee", order.getTaxFee());
                obj.addProperty("additional_fee", order.getAdditionalFee());
                obj.addProperty("courier_fee", order.getCourierFee());
                obj.addProperty("pay_price", order.getPayPrice());
                obj.addProperty("courier_id", order.getCourierID());
                obj.addProperty("status", order.getStatus().getValue());
                obj.addProperty("created_at", order.getCreated_at());
                obj.addProperty("updated_at", order.getUpdated_at());

                ordersArray.add(obj);
            }
            System.out.println(ordersArray.toString());
            sendJsonResponse(exchange, 200, ordersArray.toString());

        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal server error while retrieving delivery requests");
        }
    }
    public static void deliveryHistory(HttpExchange exchange) throws IOException {

        String query = exchange.getRequestURI().getRawQuery();
        String search = null;

        if (query != null && !query.trim().isEmpty()) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                if (pair.length == 2 && pair[0].equals("search")) {
                    search = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                    break;
                }
            }
        }
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            OrderDAO orderDAO = new OrderDAOImpl(session);
            UserDAO userDAO = new UserDAOImpl(session);
            RestaurantDAO restaurantDAO = new RestaurantDAOImpl(session);
            User user = userDAO.findById(Integer.parseInt(exchange.getAttribute("userId").toString()));
            int courierId = user.getId();
            List<Order> allOrders = orderDAO.findAll();
            String finalSearch = search;
            List<Order> filteredOrders = allOrders.stream()
                    .filter(order ->  order.getCourierID() == courierId)
                    .filter(order -> {
                        if (finalSearch == null || finalSearch.isEmpty()) return true;
                        int restaurantID = order.getVendorID();
                        String restaurantName = restaurantDAO.getRestaurantName(restaurantID);
                        return restaurantName.contains(finalSearch.toLowerCase());
                    }).filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                    .collect(Collectors.toList());

            Gson gson = new Gson();
            List<Map<String, Object>> responseList = new ArrayList<>();

            for (Order order : filteredOrders) {
                Map<String, Object> orderJson = new HashMap<>();
                orderJson.put("id", order.getId());
                orderJson.put("deliveryAddress", order.getDeliveryAddress());

                int restaurantID = order.getVendorID();
                String restaurantName = restaurantDAO.getRestaurantName(restaurantID);
                orderJson.put("restaurantName", restaurantName);

                orderJson.put("courierFee", order.getCourierFee());

                responseList.add(orderJson);
            }

            String jsonResponse = gson.toJson(responseList);
            sendJsonResponse(exchange, 200, jsonResponse);

        }catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal server error while retrieving delivery requests");
            return;
        }


    }


    public static void changeStatus(HttpExchange exchange, JsonObject jsonObject, String orderID) throws IOException {
        String orderStatus;
        String courierID;
        try {
            orderStatus = jsonObject.get("status").getAsString();
            courierID = jsonObject.has("courier_id") ? jsonObject.get("courier_id").getAsString() : "0";
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
                order.setCourierID(Integer.parseInt(courierID));
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
