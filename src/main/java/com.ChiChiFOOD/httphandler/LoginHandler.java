package com.ChiChiFOOD.httphandler;

import com.ChiChiFOOD.model.AuthService;
import com.ChiChiFOOD.model.User;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.ChiChiFOOD.utils.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.hibernate.Session;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class LoginHandler implements HttpHandler {
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendTextResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        JsonObject jsonRequest;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            jsonRequest = gson.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            sendTextResponse(exchange, 400, "Invalid JSON");
            return;
        }

        String phone = getString(jsonRequest, "phone");
        String password = getString(jsonRequest, "password");

        if (phone == null || password == null) {
            sendTextResponse(exchange, 400, "Missing credentials");
            return;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            AuthService authService = new AuthService(session);
            User user = authService.loginUser(phone, password);

            if (user != null) {
                String token = JwtUtil.generateToken(String.valueOf(user.getId()));
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("token", token);
                responseJson.addProperty("message", "Login successful");

                sendJsonResponse(exchange, 200, responseJson.toString());
            } else {
                sendTextResponse(exchange, 401, "Invalid email/phone or password");
            }
        }
    }

    private String getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private void sendTextResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}