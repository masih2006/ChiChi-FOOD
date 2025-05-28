package com.ChiChiFOOD.httphandler;

import com.ChiChiFOOD.utils.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthFilterHandler implements HttpHandler {
    private final HttpHandler next;

    public AuthFilterHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendResponse(exchange, 401, "Unauthorized: Missing or invalid Authorization header");
            return;
        }
        String token = authHeader.substring("Bearer ".length());


        try {
            DecodedJWT jwt = JwtUtil.verifyToken(token);
            exchange.setAttribute("userId", jwt.getSubject());  // ذخیره userId برای هندلر بعدی
            exchange.setAttribute("role", jwt.getClaim("role").asString());
            next.handle(exchange);
        } catch (Exception e) {
            sendResponse(exchange, 401, "Unauthorized: Invalid or expired token");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}