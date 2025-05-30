package com.ChiChiFOOD.httphandler;

import com.ChiChiFOOD.Services.AuthService;
import com.ChiChiFOOD.model.Bank;
import com.ChiChiFOOD.model.Role;
import com.ChiChiFOOD.model.User;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.ChiChiFOOD.utils.JwtUtil;
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
        String name = getString(jsonRequest, "full_name");
        String phone = getString(jsonRequest, "phone");
        String email = getString(jsonRequest, "email");
        String password = getString(jsonRequest, "password");
        String roleStr = getString(jsonRequest, "role");
        String address = getString(jsonRequest, "address");
        String profileImageBase64 = getString(jsonRequest, "profileImageBase64");
        JsonObject bankInfoJson = jsonRequest.getAsJsonObject("bank_info");
        Bank bankInfo = new Bank();
        bankInfo.setBankName(bankInfoJson.get("bank_name").getAsString());
        bankInfo.setAccountNumber(bankInfoJson.get("account_number").getAsString());

        // اگر رشته فقط space یا خالی بود، null بشه
        if (name != null && name.trim().isEmpty()) name = null;
        if (phone != null && phone.trim().isEmpty()) phone = null;
        if (email != null && email.trim().isEmpty()) email = null;
        if (password != null && password.trim().isEmpty()) password = null;
        if (roleStr != null && roleStr.trim().isEmpty()) roleStr = null;
        if (address != null && address.trim().isEmpty()) address = null;
        if (profileImageBase64 != null && profileImageBase64.trim().isEmpty()) profileImageBase64 = null;

        if (bankInfo != null) {
            // اگر بانک نیم خالی بود → null
            if (bankInfo.getBankName() == null || bankInfo.getBankName().trim().isEmpty()) {
                bankInfo.setBankName(null);
            }

            // اگر شماره حساب خالی بود → null
            if (bankInfo.getAccountNumber() == null || bankInfo.getAccountNumber().trim().isEmpty()) {
                bankInfo.setAccountNumber(null);
            }

            // اگر هر دو فیلد نال بودن → کل آبجکت bankInfo رو null کن
            if (bankInfo.getBankName() == null && bankInfo.getAccountNumber() == null) {
                bankInfo = null;
            }
        }

// فقط name و password الزامی هستن
        if (name == null || phone == null || password == null || roleStr == null || address == null) {
            sendResponse(exchange, 400, "Missing required fields: full_name and password are required.");
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
            boolean success = authService.registerUser(name, phone, email, password, role, address, profileImageBase64, bankInfo);

            if (success) {
                tx.commit();

                // مرحله لاگین: استفاده از همون AuthService
                User loggedInUser = authService.loginUser(phone, password);
                if (loggedInUser != null) {
                    String token = JwtUtil.generateToken(loggedInUser);

                    JsonObject responseJson = new JsonObject();
                    responseJson.addProperty("token", token);
                    responseJson.addProperty("message", "Registration and login successful");

                    sendResponse(exchange, 200, responseJson.toString());
                } else {
                    sendResponse(exchange, 500, "Login after registration failed");
                }
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