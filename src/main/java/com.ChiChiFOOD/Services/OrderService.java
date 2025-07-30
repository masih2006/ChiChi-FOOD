package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.dao.impl.*;
import com.ChiChiFOOD.model.Order;
import com.ChiChiFOOD.model.OrderStatus;
import com.ChiChiFOOD.model.restaurant.Item;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.ChiChiFOOD.httphandler.Sender.sendJsonResponse;
import static com.ChiChiFOOD.httphandler.Sender.sendTextResponse;

public class OrderService {
    static Session DaoSession = HibernateUtil.getSessionFactory().openSession();
    static OrderDAO orderDAO = new OrderDAOImpl(DaoSession);
    static ItemDAO itemDAO = new ItemDAOImpl(DaoSession);
    static UserDAO userDAO = new UserDAOImpl(DaoSession);
    static RestaurantDAO restaurantDAO = new RestaurantDAOImpl(DaoSession);

    public static String getCurrentTime(){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
    public static void submitOrder(HttpExchange exchange, JsonObject jsonRequest) throws IOException {
        String deliveryAddress;
        int vendorId;
        int customerId;
        int courierFee;
        int rawPrice = 0;
        int randomStep = (int) (Math.random() * 16);
        int payPrice = 0;
        int additionalFee = 0;
        courierFee = 15000 + randomStep * 1000;

        Integer couponId = null;
        List<Integer> itemIDs = new ArrayList<>();
        customerId = Integer.parseInt(exchange.getAttribute("userId").toString());
        try {
            if (!jsonRequest.has("delivery_address") || !jsonRequest.has("vendor_id") || !jsonRequest.has("items")) {
                sendTextResponse(exchange, 400, "Missing required fields");
                return;
            }

            deliveryAddress = jsonRequest.get("delivery_address").getAsString();
            vendorId = jsonRequest.get("vendor_id").getAsInt();

            if (jsonRequest.has("coupon_id") && !jsonRequest.get("coupon_id").isJsonNull()) {
                couponId = jsonRequest.get("coupon_id").getAsInt();
            }

            JsonArray jsonItems = jsonRequest.getAsJsonArray("items");
            for (JsonElement element : jsonItems) {
                JsonObject itemObj = element.getAsJsonObject();

                if (!itemObj.has("item_id")) {
                    sendTextResponse(exchange, 400, "Each item must have item_id and quantity");
                    return;
                }

                int itemId = itemObj.get("item_id").getAsInt();
                    rawPrice += itemDAO.findPriceById(itemId) * itemObj.get("quantity").getAsInt();
                itemIDs.add(itemId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 400, "Invalid JSON format or data types");
            return;
        }
        Transaction transaction = DaoSession.beginTransaction();
        try {
            if (restaurantDAO.findById(Long.parseLong(vendorId + "")).getTaxType().equals("PERCENTAGE")){
                payPrice = rawPrice * restaurantDAO.findById(Long.parseLong(vendorId + "")).getTaxFee() / 100 + courierFee + rawPrice + restaurantDAO.findById(Long.parseLong(vendorId + "")).getAdditionalFee();
            }else if (restaurantDAO.findById(Long.parseLong(vendorId + "")).getTaxType().equals("FIXED")){
                payPrice = rawPrice + courierFee + restaurantDAO.findById(Long.parseLong(vendorId + "")).getTaxFee() + restaurantDAO.findById(Long.parseLong(vendorId + "")).getAdditionalFee();
            }
            System.out.println("== itemIDs to save: " + itemIDs);
            Order order = new Order();
            order.setDeliveryAddress(deliveryAddress);
            order.setVendorID(vendorId);
            order.setCouponID(couponId);
            order.setCustomerID(customerId);
            order.setItemIDs(itemIDs);
            order.setCourierFee(courierFee);
            order.setRawPrice(rawPrice);
            order.setPayPrice(payPrice);
            order.setAdditionalFee(additionalFee);
            order.setCreated_at(getCurrentTime());
            order.setUpdated_at(getCurrentTime());
            order.setStatus(OrderStatus.SUBMITTED);
            orderDAO.save(order);
            List<Item> fullItems = new ArrayList<>();
            for (Integer id : itemIDs) {
                Item item = itemDAO.findById(id);
                if (item != null) {
                    fullItems.add(item);
                }
            }

            JsonObject response = new JsonObject();
            response.addProperty("deliveryAddress", deliveryAddress);
            response.addProperty("vendorID", vendorId);
            response.addProperty("id", order.getId());
            response.addProperty("pay_price", order.getPayPrice());
            response.addProperty("wallet_ballance", userDAO.findById(customerId).getWalletBalance());
            System.out.println(order.getId() +" "+ order.getPayPrice());
            if (couponId != null) response.addProperty("couponID", couponId);

            Gson gson = new Gson();
            JsonArray itemArray = new JsonArray();
            for (Item item : fullItems) {
                JsonObject itemJson = new JsonObject();
                itemJson.addProperty("id", item.getId());
                itemJson.addProperty("name", item.getName());
                itemJson.addProperty("description", item.getDescription());
                itemJson.addProperty("price", item.getPrice());
                itemJson.addProperty("supply", item.getSupply());
                itemJson.addProperty("imageBase64", item.getImageBase64());
                itemArray.add(itemJson);
            }
            response.add("items", itemArray);


            sendJsonResponse(exchange, 200, response.toString());
            transaction.commit();
            return;
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal server error while submitting order");
        }
    }

    public static void specificOrder(HttpExchange exchange, String orderID) throws IOException {
        int id;
        Session session = HibernateUtil.getSessionFactory().openSession();
        RestaurantDAO restaurantDAO = new RestaurantDAOImpl(session);


        try {
            id = Integer.parseInt(orderID);
        } catch (NumberFormatException e) {
            sendTextResponse(exchange, 400, "Invalid order ID format");
            return;
        }

        try {
            Order order = orderDAO.findById(id);
            if (order == null) {
                sendTextResponse(exchange, 404, "Order not found");
                return;
            }

            JsonObject response = new JsonObject();
            response.addProperty("id", order.getId());
            response.addProperty("delivery_address", order.getDeliveryAddress());
            response.addProperty("customer_id", order.getCustomerID());
            response.addProperty("vendor_id", order.getVendorID());
            response.addProperty("vendorName", restaurantDAO.getRestaurantName(order.getVendorID()));

            response.addProperty("coupon_id", order.getCouponID());

            Gson gson = new Gson();
            JsonArray itemArray = gson.toJsonTree(order.getItemIDs()).getAsJsonArray();
            response.add("item_ids", itemArray);

            response.addProperty("raw_price", order.getRawPrice());
            response.addProperty("tax_fee", order.getTaxFee());
            response.addProperty("additional_fee", order.getAdditionalFee());
            response.addProperty("courier_fee", order.getCourierFee());
            response.addProperty("pay_price", order.getPayPrice());
            response.addProperty("courier_id", order.getCourierID());
            response.addProperty("status", order.getStatus().getValue());
            response.addProperty("created_at", order.getCreated_at());
            response.addProperty("updated_at", order.getUpdated_at());

            JsonArray itemsArray = new JsonArray();
            for (Integer itemId : order.getItemIDs()) {
                Item item = itemDAO.findById(itemId);
                if (item != null) {
                    JsonObject itemJson = new JsonObject();
                    itemJson.addProperty("id", item.getId());
                    itemJson.addProperty("name", item.getName());
                    itemJson.addProperty("description", item.getDescription());
                    itemJson.addProperty("price", item.getPrice());
                    itemJson.addProperty("supply", item.getSupply());
                    itemJson.addProperty("imageBase64", item.getImageBase64());
                    itemsArray.add(itemJson);
                }
            }
            response.add("items", itemsArray);
            System.out.println(response.toString());
            sendJsonResponse(exchange, 200, response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal server error while retrieving order");
        }
    }

    public static void getAllOrders(HttpExchange exchange, String restaurantID) throws IOException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            RestaurantDAO restaurantDAO = new RestaurantDAOImpl(session);
            ItemDAO itemDAO = new ItemDAOImpl(session);
            OrderDAO orderDAO = new OrderDAOImpl(session);

            if (!restaurantDAO.restaurantExistsById(restaurantID)) {
                sendTextResponse(exchange, 404, "Restaurant does not exist");
                return;
            }

            List<Order> orders = orderDAO.getOrdersByRestaurant(restaurantID);
            List<Map<String, Object>> responseList = new ArrayList<>();

            for (Order order : orders) {
                System.out.println("== Retrieved itemIDs: " + order.getItemIDs());
                Map<String, Object> orderResponse = new LinkedHashMap<>();
                orderResponse.put("id", order.getId());
                orderResponse.put("deliveryAddress", order.getDeliveryAddress());
                orderResponse.put("customerID", order.getCustomerID());
                orderResponse.put("vendorID", order.getVendorID());
                orderResponse.put("couponID", order.getCouponID());
                orderResponse.put("rawPrice", order.getRawPrice());
                orderResponse.put("taxFee", order.getTaxFee());
                orderResponse.put("additionalFee", order.getAdditionalFee());
                orderResponse.put("courierFee", order.getCourierFee());
                orderResponse.put("payPrice", order.getPayPrice());
                orderResponse.put("courierID", order.getCourierID());
                orderResponse.put("status", order.getStatus().toString());
                orderResponse.put("createdAt", order.getCreated_at());
                orderResponse.put("updatedAt", order.getUpdated_at());

                List<Map<String, Object>> itemList = new ArrayList<>();
                for (Integer itemId : order.getItemIDs()) {
                    Item item = itemDAO.findById(itemId);
                    if (item != null) {
                        Map<String, Object> itemMap = new LinkedHashMap<>();
                        itemMap.put("id", item.getId());
                        itemMap.put("name", item.getName());
                        itemMap.put("description", item.getDescription());
                        itemMap.put("price", item.getPrice());
                        itemMap.put("supply", item.getSupply());
                        itemMap.put("imageBase64", item.getImageBase64());
                        itemList.add(itemMap);
                    }
                }

                orderResponse.put("items", itemList);
                responseList.add(orderResponse);
            }

            Gson gson = new Gson();
            sendJsonResponse(exchange, 200, gson.toJson(responseList));
        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal server error while retrieving orders");
        }
    }

    public static void getAllUserOrder(HttpExchange exchange) throws IOException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            RestaurantDAO restaurantDAO = new RestaurantDAOImpl(session);
            UserDAO userDAO = new UserDAOImpl(session);
            ItemDAO itemDAO = new ItemDAOImpl(session);
            OrderDAO orderDAO = new OrderDAOImpl(session);

            String userID = exchange.getAttribute("userId").toString();
            List<Order> orders = orderDAO.getAllUserOrder(userID);
            List<Map<String, Object>> responseList = new ArrayList<>();
            System.out.println("done" + userID);

            for (Order order : orders) {


                System.out.println(order.getStatus().toString());
                System.out.println("== Retrieved itemIDs: " + order.getItemIDs());
                Map<String, Object> orderResponse = new LinkedHashMap<>();
                orderResponse.put("id", order.getId());
                orderResponse.put("deliveryAddress", order.getDeliveryAddress());
                orderResponse.put("customerID", order.getCustomerID());
                orderResponse.put("vendorID", order.getVendorID());
                orderResponse.put("vendorName", restaurantDAO.getRestaurantName(order.getVendorID()));
                orderResponse.put("couponID", order.getCouponID());
                orderResponse.put("rawPrice", order.getRawPrice());
                orderResponse.put("taxFee", order.getTaxFee());
                orderResponse.put("additionalFee", order.getAdditionalFee());
                orderResponse.put("courierFee", order.getCourierFee());
                orderResponse.put("payPrice", order.getPayPrice());
                orderResponse.put("courierID", order.getCourierID());
                orderResponse.put("status", order.getStatus().toString());
                orderResponse.put("createdAt", order.getCreated_at());
                orderResponse.put("updatedAt", order.getUpdated_at());

                List<Map<String, Object>> itemList = new ArrayList<>();
                for (Integer itemId : order.getItemIDs()) {
                    Item item = itemDAO.findById(itemId);
                    if (item != null) {
                        Map<String, Object> itemMap = new LinkedHashMap<>();
                        itemMap.put("id", item.getId());
                        itemMap.put("name", item.getName());
                        itemMap.put("description", item.getDescription());
                        itemMap.put("price", item.getPrice());
                        itemMap.put("supply", item.getSupply());
                        itemMap.put("imageBase64", item.getImageBase64());
                        itemList.add(itemMap);
                    }
                }
                orderResponse.put("items", itemList);
                responseList.add(orderResponse);
            }

            Gson gson = new Gson();
            System.out.println(responseList.toString());
            sendJsonResponse(exchange, 200, gson.toJson(responseList));
        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal server error while retrieving orders");
        }
    }

    public static void changeStatus(HttpExchange exchange, JsonObject jsonObject, String ID) throws IOException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            OrderDAO orderDAO = new OrderDAOImpl(session);
            Order order = orderDAO.findById(Integer.valueOf(ID));
            if (order == null) {
                sendTextResponse(exchange, 404 , "order not found");
                return;
            }
            String status;
            if (jsonObject.has("status")) {
                status = jsonObject.get("status").getAsString();
            }else {
                sendTextResponse(exchange, 404 , "invalid json");
                return;
            }
            order.setUpdated_at(getCurrentTime());
            order.setStatus(OrderStatus.valueOf(status));
            orderDAO.update(order);
            transaction.commit();
            sendTextResponse(exchange, 200, "Order updated successfully");
            return;
        }catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal server error while changing status");
            return;
        }
    }

    public static void orderHistory(HttpExchange exchange) throws IOException {



    }

}

