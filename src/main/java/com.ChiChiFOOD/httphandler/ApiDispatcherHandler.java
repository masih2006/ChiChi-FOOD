package com.ChiChiFOOD.httphandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApiDispatcherHandler implements HttpHandler {

    private final Map<String, HttpHandler> routes = new HashMap<>();

    public ApiDispatcherHandler() {
        // ثبت مسیرها
        routes.put("/api/register", new RegisterHandler());

//        routes.put("/login", new LoginHandler());
        // اینجا می‌تونی هندلرهای دیگه هم اضافه کنی مثل:
        // routes.put("/restaurants", new RestaurantHandler());
        // routes.put("/orders", new OrderHandler());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        HttpHandler handler = routes.get(path);
        if (handler != null) {
            handler.handle(exchange);
        } else {
            // اگر مسیر وجود نداشت
            String response = "404 Not Found";
            exchange.sendResponseHeaders(404, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }
}