package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.dao.impl.OrderDAO;
import com.ChiChiFOOD.dao.impl.OrderDAOImpl;
import com.ChiChiFOOD.model.Order;
import com.ChiChiFOOD.model.OrderStatus;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.ChiChiFOOD.httphandler.Sender.sendJsonResponse;
import static com.ChiChiFOOD.httphandler.Sender.sendTextResponse;

public class DeliveryService {
    static Session DaoSession = HibernateUtil.getSessionFactory().openSession();
    static OrderDAO orderDAO = new OrderDAOImpl(DaoSession);
    public static void findCourier(HttpExchange exchange) throws IOException {
        try {
            List<Order> allOrders = orderDAO.findAll(); // همه سفارش‌ها از دیتابیس

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

            sendJsonResponse(exchange, 200, ordersArray.toString());

        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal server error while retrieving delivery requests");
        }
    }
    public static void deliveryHistory(HttpExchange exchange) throws IOException {

    }
    public static void changeStatus(HttpExchange exchange, String ordrtID) throws IOException {}


}
