package com.ChiChiFOOD.httphandler;

import com.ChiChiFOOD.utils.TokenBlacklist;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class LogoutHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            Sender.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Sender.sendResponse(exchange, 400, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7); // حذف "Bearer "

        TokenBlacklist.blacklist(token); // اضافه به لیست سیاه

        Sender.sendResponse(exchange, 200, "Logged out successfully");
    }

//    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
//        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
//        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
//        exchange.sendResponseHeaders(statusCode, bytes.length);
//        try (OutputStream os = exchange.getResponseBody()) {
//            os.write(bytes);
//        }
//    }
}