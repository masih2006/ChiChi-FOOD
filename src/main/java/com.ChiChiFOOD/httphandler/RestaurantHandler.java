package com.ChiChiFOOD.httphandler;

import com.ChiChiFOOD.dao.impl.RestaurantDao;
import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RestaurantHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String [] params = path.split("/");
        String method = exchange.getRequestMethod();
        Gson gson = new Gson();
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
        JsonObject jsonRequest;
        try {
            jsonRequest = gson.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 400, "Invalid JSON");
            return;
        }


        System.out.println(path);
        if (method.equalsIgnoreCase("GET")) {
            if (params.length == 2 && params[1].equals("mine")) {

                }else if (params.length == 3 && params[2].equals("orders")) {

           }else {
                sendTextResponse(exchange, 400, "Bad Request");
                return;
            }
        }else if (method.equalsIgnoreCase("post")){
            if(path.equalsIgnoreCase("/restaurants")) {
                registerRestaurant(exchange, jsonRequest);
            }else if (params.length == 3){
                if (params[2].equals("item")) {

                }else if (params[1].equals("menu")) {

                }
            }else {
                sendTextResponse(exchange, 400, "Bad Request");
                return;
            }
        }else if (method.equalsIgnoreCase("put")){
            if (params.length == 2 && params[1].matches("\\d+")) {

            }else if (params.length == 3 ) {
                if (params[2].equals("item")) {

                }else if (params[2].equals("menu")) {

                }
            }else {
                sendTextResponse(exchange, 400, "Bad Request");
            }
        }else if (method.equalsIgnoreCase("delete")){
        if (params.length == 5 && params [2].equals("item")) {

        }else if (params.length == 4){
            if (params[2].equals("menu")) {

            }else if (params[2].equals("item")) {
                
            }
        }else {
            sendTextResponse(exchange, 400, "Bad Request");
            return;
        }
        }else if (method.equalsIgnoreCase("patch")){
            if (params.length == 3 && params[2].equals("orders")) {

            }else {
                sendTextResponse(exchange, 400, "Bad Request");
                return;
            }
        }else{
            sendTextResponse(exchange, 405, "Method Not Allowed");
            return;
        }




    }

    private void registerRestaurant(HttpExchange exchange, JsonObject jsonRequest) throws IOException {
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
        // in baiad dorost she
//        if (restaurantExistsByNameOrPhone(name, phone)) {
//            sendTextResponse(exchange, 409, "conflict occurred.");
//            return;
//        }
        System.out.println("doool");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            RestaurantDao restaurantDao = new RestaurantDao(session);
            Restaurant restaurant = new Restaurant(name, phone, address);

            restaurantDao.save(restaurant);
            sendTextResponse(exchange, 200, "User registered successfully");
        }catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal server error");

        }

    }
    private void sendTextResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
    public boolean restaurantExistsByNameOrPhone(String name, String phone) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        String hql = "FROM Restaurant WHERE name = :name OR phone = :phone";
        List<?> result = session.createQuery(hql)
                .setParameter("name", name)
                .setParameter("phone", phone)
                .list();
        session.close();
        return !result.isEmpty();  // یعنی یکی با این مشخصات پیدا شده

    }


}

