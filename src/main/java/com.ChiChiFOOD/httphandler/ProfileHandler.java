package com.ChiChiFOOD.httphandler;

import com.ChiChiFOOD.model.User;
import com.ChiChiFOOD.utils.JwtUtil;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.Gson;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;
import org.json.JSONException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProfileHandler implements HttpHandler {

    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
    // niaz nist
//        if (!exchange.getRequestHeaders().containsKey("Authorization")) {
//            sendResponse(exchange, 401, "Authorization header missing");
//            return;
//        }

        String token = exchange.getRequestHeaders().getFirst("Authorization").replace("Bearer ", "");
        DecodedJWT decodedJWT;
        try {
            decodedJWT = JwtUtil.verifyToken(token);
        } catch (Exception e) {
            sendResponse(exchange, 401, "Invalid token");
            return;
        }

        int userId = Integer.parseInt(decodedJWT.getSubject());
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = session.get(User.class, userId);

        if (user == null) {
            sendResponse(exchange, 404, "User not found");
            return;
        }

        switch (method) {
            case "GET":
                Map <String, Object> userResponse = new LinkedHashMap<>();
                userResponse.put("id", user.getId());
                userResponse.put("name", user.getName());
                userResponse.put("phone", user.getPhone());
                userResponse.put("email", user.getEmail());
                userResponse.put("role", decodedJWT.getClaim("role").asString());
                userResponse.put("address", user.getAddress());
                userResponse.put("profileImageBase64", user.getProfileImageBase64());
                userResponse.put("bankName",user.getBankName());
                userResponse.put("AccountNumber",user.getAccountNumber());
                String json = gson.toJson(userResponse);
                sendJsonResponse(exchange, 200, json);
                session.close();
                break;
            case "PUT":
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    JSONObject jsonObject = new JSONObject(body);

                    session.beginTransaction();

                    if (jsonObject.has("full_name")) user.setName(jsonObject.getString("full_name"));
                    if (jsonObject.has("email")) user.setEmail(jsonObject.getString("email"));
                    if (jsonObject.has("phone")) user.setPhone(jsonObject.getString("phone"));
                    if (jsonObject.has("address")) user.setAddress(jsonObject.getString("address"));
                    if (jsonObject.has("profileImageBase64")) user.setProfileImageBase64(jsonObject.getString("profileImageBase64"));

                    if (jsonObject.has("bank_info")) {
                        JSONObject bankInfo = jsonObject.getJSONObject("bank_info");
                        if (bankInfo.has("bank_name")) user.setBankName(bankInfo.getString("bank_name"));
                        if (bankInfo.has("account_number")) user.setAccountNumber(bankInfo.getString("account_number"));
                    }

                    session.getTransaction().commit();
                    sendResponse(exchange, 200, "Profile updated successfully");

                } catch (JSONException e) {
                    sendResponse(exchange, 400, "Invalid JSON format");
                } finally {
                    session.close();
                }
                break;

            default:
                sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        exchange.sendResponseHeaders(statusCode, message.length());
        OutputStream os = exchange.getResponseBody();
        os.write(message.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}