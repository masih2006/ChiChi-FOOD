package com.ChiChiFOOD.httphandler;

import com.ChiChiFOOD.model.AuthService;
import com.ChiChiFOOD.model.Role;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class RegisterHandler implements HttpHandler {
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
        JsonObject jsonRequest;
        try {
            jsonRequest = gson.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 400, "Invalid JSON");
            return;
        }
        String name = getString(jsonRequest, "name");
        String phone = getString(jsonRequest, "phone");
        String email = getString(jsonRequest, "email");
        String password = getString(jsonRequest, "password");
        String roleStr = getString(jsonRequest, "role");
        String address = getString(jsonRequest, "address");

        if (name == null || phone == null || email == null || password == null || roleStr == null || address == null) {
            sendResponse(exchange, 400, "Missing required fields");
            return;
        }

        Role role;
        try {
            role = Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "Invalid role");
            return;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            AuthService authService = new AuthService(session);
            boolean success = authService.registerUser(name, phone, email, password, role, address);

            if (success) {
                tx.commit();
                sendResponse(exchange, 200, "User registered successfully");
            } else {
                tx.rollback();
                sendResponse(exchange, 409, "User with this phone already exists");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal server error");
            e.printStackTrace();
        }
    }

    private String getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}