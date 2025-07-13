package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.dao.impl.*;
import com.ChiChiFOOD.httphandler.Sender;
import com.ChiChiFOOD.model.Coupon;
import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mysql.cj.xdevapi.SessionFactory;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ChiChiFOOD.httphandler.Sender.sendJsonResponse;
import static com.ChiChiFOOD.httphandler.Sender.sendTextResponse;

public class CouponsService {
    public static void createCoupon(HttpExchange exchange, JsonObject jsonObject) throws IOException {
        String couponCode;
        Coupon coupon = new Coupon();
        try {
            String code = jsonObject.has("coupon_code") ? jsonObject.get("coupon_code").getAsString() : null;
            String typeStr = jsonObject.has("type") ? jsonObject.get("type").getAsString() : null;
            String scopeStr = jsonObject.has("scope") ? jsonObject.get("scope").getAsString() : null;
            int value = jsonObject.has("value") ? jsonObject.get("value").getAsInt() : 0;
            int minPrice = jsonObject.has("min_price") ? jsonObject.get("min_price").getAsInt() : 0;
            int userCount = jsonObject.has("user_count") ? jsonObject.get("user_count").getAsInt() : 0;
            String startDateStr = jsonObject.has("start_date") ? jsonObject.get("start_date").getAsString() : null;
            String endDateStr = jsonObject.has("end_date") ? jsonObject.get("end_date").getAsString() : null;
            String restaurantId = jsonObject.has("restaurant_id") ? jsonObject.get("restaurant_id").getAsString() : null;
            String itemId = jsonObject.has("item_id") ? jsonObject.get("item_id").getAsString() : null;

            if (code == null ||value == 0|| userCount == 0|| typeStr == null || startDateStr == null || endDateStr == null) {
                sendTextResponse(exchange, 400, "Missing required fields");
                return;
            }

            coupon.setCode(code);
            coupon.setType(Coupon.DiscountType.valueOf(typeStr.toUpperCase()));
            if (scopeStr != null) {
                coupon.setScope(Coupon.CouponScope.valueOf(scopeStr.toUpperCase()));
                if (scopeStr == "ITEM_SPECIFIC"){
                    if (restaurantId == null || itemId == null) {
                        sendTextResponse(exchange, 400, "Missing required fields");
                        return;
                    }
                    Session session = HibernateUtil.getSessionFactory().openSession();
                    ItemDAO itemDAO = new ItemDAOImpl(session);
                    if (!itemDAO.itemExistsByIdAndRestaurantId(Integer.parseInt(itemId), Integer.parseInt(restaurantId))) {
                        sendTextResponse(exchange, 404, "Resource not found");
                        return;
                    }
                    session.close();
                }
            }
            coupon.setValue(value);
            coupon.setMinPrice(minPrice);
            coupon.setUserCount(userCount);
            coupon.setStartDate(LocalDate.parse(startDateStr));
            coupon.setEndDate(LocalDate.parse(endDateStr));

            if (restaurantId != null)
                coupon.setRestaurantId(restaurantId);
            if (itemId != null)
                coupon.setItemId(itemId);

        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 400, "Invalid JSON format or data types");
            return;
        }
        if (!exchange.getAttribute("role").toString().equalsIgnoreCase("admin")) {
            sendTextResponse(exchange,403, "forbidden");
            return;
        }
        Session session = HibernateUtil.getSessionFactory().openSession();
        CouponDAO couponDAO = new CouponDAOImpl(session);
        if (couponDAO.doesCouponCodeExist(coupon.getCode())) {
            sendTextResponse(exchange, 409, "conflict");
            return;
        }
        Transaction tx =session.beginTransaction();
        try{
            couponDAO.save(coupon);
            sendTextResponse(exchange, 400, "done");
            tx.commit();
            return;
        }catch (Exception e){
            sendTextResponse(exchange, 500, "Internal Server Error");
            tx.rollback();
            return;
        }
    }

    public static void getAllCoupons(HttpExchange exchange, JsonObject jsonObject) throws IOException {
        if (!exchange.getAttribute("role").toString().equalsIgnoreCase("admin")) {
            sendTextResponse(exchange, 403, "forbidden");
            return;
        }
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            CouponDAO couponDAO = new CouponDAOImpl(session);
            List<Coupon> coupons = couponDAO.getAllCoupons();
            List<Map<String, Object>> responseList = new ArrayList<>();
            for (Coupon coupon : coupons) {
                Map<String, Object> couponResponse = new LinkedHashMap<>();
                couponResponse.put("id", coupon.getId());
                couponResponse.put("coupon_code", coupon.getCode());
                couponResponse.put("type", coupon.getType());
                couponResponse.put("value", coupon.getValue());
                couponResponse.put("min_price", coupon.getMinPrice());
                couponResponse.put("user_count", coupon.getUserCount());
                couponResponse.put("start_date", coupon.getStartDate().toString());
                couponResponse.put("end_date",  coupon.getEndDate().toString());
                responseList.add(couponResponse);
            }
            String responseJson = new Gson().toJson(responseList);
            sendJsonResponse(exchange, 200, responseJson);
            return;
        }
    }

    public static void deleteCoupon(HttpExchange exchange, JsonObject jsonObject, String id) throws IOException {
        if (!exchange.getAttribute("role").toString().equalsIgnoreCase("admin")) {
            sendTextResponse(exchange, 403, "forbidden");
            return;
        }
        if (id == null || id.isEmpty()) {
            sendTextResponse(exchange, 400, "Missing required fields");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        CouponDAO couponDAO = new CouponDAOImpl(session);
        if (!couponDAO.doesCouponIdExist(Long.parseLong(id))) {
            sendTextResponse(exchange, 404, "Resource not found");
            return;
        }
        Transaction tx =session.beginTransaction();
        try {
            Coupon coupon = couponDAO.getCouponById(Long.parseLong(id));
            couponDAO.delete(coupon);
            sendTextResponse(exchange, 400, "done");
            tx.commit();
            return;
        }catch (Exception e){
            sendTextResponse(exchange, 500, "Internal Server Error");
            tx.rollback();
            return;
        }
    }

    public static void updateCoupon(HttpExchange exchange, JsonObject jsonObject, String id) throws IOException {
        if (!exchange.getAttribute("role").toString().equalsIgnoreCase("admin")) {
            sendTextResponse(exchange, 403, "forbidden");
            return;
        }
        if (id == null || id.isEmpty()) {
            sendTextResponse(exchange, 400, "Missing required fields");
            return;
        }
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            CouponDAO couponDAO = new CouponDAOImpl(session);
            if (!couponDAO.doesCouponIdExist(Long.parseLong(id))) {
                sendTextResponse(exchange, 404, "Resource not found");
                return;
            }
            Coupon coupon = couponDAO.getCouponById(Long.parseLong(id));

            if (jsonObject.has("coupon_code")) {
                coupon.setCode(jsonObject.get("coupon_code").getAsString());
            }

            if (jsonObject.has("type")) {
                try {
                    coupon.setType(jsonObject.get("type").getAsString());
                } catch (IllegalArgumentException e) {
                    sendTextResponse(exchange, 400, "Invalid discount type");
                    return;
                }
            }

            if (jsonObject.has("scope")) {
                try {
                    coupon.setScope(jsonObject.get("scope").getAsString());
                } catch (IllegalArgumentException e) {
                    sendTextResponse(exchange, 400, "Invalid coupon scope");
                    return;
                }
            }

            if (jsonObject.has("value")) {
                coupon.setValue(jsonObject.get("value").getAsInt());
            }

            if (jsonObject.has("min_price")) {
                coupon.setMinPrice(jsonObject.get("min_price").getAsInt());
            }

            if (jsonObject.has("user_count")) {
                coupon.setUserCount(jsonObject.get("user_count").getAsInt());
            }

            if (jsonObject.has("start_date")) {
                try {
                    String startDateStr = jsonObject.get("start_date").getAsString();
                    coupon.setStartDate(LocalDate.parse(startDateStr));
                } catch (Exception e) {
                    sendTextResponse(exchange, 400, "Invalid start date format");
                    return;
                }
            }

            if (jsonObject.has("end_date")) {
                try {
                    String endDateStr = jsonObject.get("end_date").getAsString();
                    coupon.setEndDate(LocalDate.parse(endDateStr));
                } catch (Exception e) {
                    sendTextResponse(exchange, 400, "Invalid end date format");
                    return;
                }
            }

            if (jsonObject.has("restaurant_id")) {
                coupon.setRestaurantId(jsonObject.get("restaurant_id").getAsString());
            }

            if (jsonObject.has("item_id")) {
                coupon.setItemId(jsonObject.get("item_id").getAsString());
            }

            couponDAO.update(coupon);
            tx.commit();
            sendTextResponse(exchange, 200, "done");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            sendTextResponse(exchange, 500, "Internal Server Error");
        } finally {
            session.close();
        }
    }

    public static void getCoupon(HttpExchange exchange, String id) throws IOException {
        if (!exchange.getAttribute("role").toString().equalsIgnoreCase("admin")) {
            sendTextResponse(exchange, 403, "forbidden");
            return;
        }
        if (id == null || id.isEmpty()) {
            sendTextResponse(exchange, 400, "Missing required fields");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        CouponDAO couponDAO = new CouponDAOImpl(session);
        if (!couponDAO.doesCouponIdExist(Long.parseLong(id))) {
            sendTextResponse(exchange, 404, "Resource not found");
            session.close();
            return;
        }

        Coupon coupon = couponDAO.getCouponById(Long.parseLong(id));
        try {
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("id", coupon.getId());
            responseJson.addProperty("coupon_code", coupon.getCode());
            responseJson.addProperty("type", coupon.getType().name().toLowerCase());
            responseJson.addProperty("value", coupon.getValue());
            responseJson.addProperty("min_price", coupon.getMinPrice());
            responseJson.addProperty("user_count", coupon.getUserCount());
            responseJson.addProperty("start_date", coupon.getStartDate().toString());
            responseJson.addProperty("end_date", coupon.getEndDate().toString());
            sendJsonResponse(exchange, 200, responseJson.toString());
        }catch (Exception e){
            sendTextResponse(exchange, 500, "Internal Server Error");
            return;
        }finally {
            session.close();
        }

    }

}

