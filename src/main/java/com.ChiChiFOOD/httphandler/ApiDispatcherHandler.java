package com.ChiChiFOOD.httphandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApiDispatcherHandler implements HttpHandler {

    private final Map<String, HttpHandler> routes = new HashMap<>();

    public ApiDispatcherHandler() {
        routes.put("/auth/register", new RegisterHandler());

//        routes.put("/login", new LoginHandler());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        HttpHandler handler = routes.get(path);
        if (handler != null) {
            handler.handle(exchange);
        } else {
            String response = "404 Not Found";
            exchange.sendResponseHeaders(404, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }
}