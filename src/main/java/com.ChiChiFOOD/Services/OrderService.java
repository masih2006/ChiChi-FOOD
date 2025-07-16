package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.dao.impl.*;
import com.ChiChiFOOD.model.Order;
import com.ChiChiFOOD.model.restaurant.Item;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.ChiChiFOOD.httphandler.Sender.sendJsonResponse;
import static com.ChiChiFOOD.httphandler.Sender.sendTextResponse;

public class OrderService {
    static Session DaoSession = HibernateUtil.getSessionFactory().openSession();
    static OrderDAO orderDAO = new OrderDAOImpl(DaoSession);
    static ItemDAO itemDAO = new ItemDAOImpl(DaoSession);

    public static void submitOrder(HttpExchange exchange, JsonObject jsonRequest) throws IOException {
        String deliveryAddress;
        int vendorId;
        Integer couponId = null;
        List<Integer> itemIDs = new ArrayList<>();

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
                itemIDs.add(itemId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 400, "Invalid JSON format or data types");
            return;
        }

        try {
            Order order = new Order();
            order.setDeliveryAddress(deliveryAddress);
            order.setVendorID(vendorId);
            order.setCouponID(couponId);
            order.setItemIDs(itemIDs);

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
            if (couponId != null) response.addProperty("couponID", couponId);

            Gson gson = new Gson();
            JsonArray jsonItems = gson.toJsonTree(fullItems).getAsJsonArray();
            response.add("items", jsonItems);

            sendJsonResponse(exchange, 200, response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal server error while submitting order");
        }
    }

    public static void specificOrder(HttpExchange exchange, String orderID) throws IOException {
        int id;

        try {
            // تبدیل orderID به عدد صحیح
            id = Integer.parseInt(orderID);
        } catch (NumberFormatException e) {
            sendTextResponse(exchange, 400, "Invalid order ID format");
            return;
        }

        try {
            // گرفتن سفارش از دیتابیس
            Order order = orderDAO.findById(id);

            if (order == null) {
                sendTextResponse(exchange, 404, "Order not found");
                return;
            }

            // ساخت JSON خروجی با تمام فیلدها
            JsonObject response = new JsonObject();
            response.addProperty("id", order.getId());
            response.addProperty("delivery_address", order.getDeliveryAddress());
            response.addProperty("customer_id", order.getCustomerID());
            response.addProperty("vendor_id", order.getVendorID());
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

            sendJsonResponse(exchange, 200, response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal server error while retrieving order");
        }
    }

    public static void orderHistory(HttpExchange exchange) throws IOException {

    }
}

