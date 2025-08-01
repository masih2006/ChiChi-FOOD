package com.ChiChiFOOD.httphandler;

import com.ChiChiFOOD.Services.ItemService;
import com.ChiChiFOOD.Services.RestaurantService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ItemHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] params = Arrays.stream(path.split("/"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        String method = exchange.getRequestMethod();
        Gson gson = new Gson();
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
        JsonObject jsonRequest;
        try {
            jsonRequest = gson.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            Sender.sendTextResponse(exchange, 400, "Invalid JSON");
            return;
        }
        if (method.equalsIgnoreCase("GET")) {
            getHandler(exchange,params);
        }else if (method.equalsIgnoreCase("post")){
            postHandler(exchange, params, jsonRequest, path);
        }else{
            Sender.sendTextResponse(exchange, 405, "Method Not Allowed");
            return;
        }
    }

    public void postHandler(HttpExchange exchange, String[] params, JsonObject jsonRequest, String path) throws IOException {
        if(path.equalsIgnoreCase("/items")) {
            ItemService.searchItems(exchange, jsonRequest);
        } else {
            Sender.sendTextResponse(exchange, 400, "Bad Request");
        }
    }
    public void getHandler(HttpExchange exchange, String[] params) throws IOException {
        if (params.length == 2 && params[0].matches("items") && params[1].matches("\\d+")) {
            ItemService.oneItem(exchange, params[1]);
        }else {
            Sender.sendTextResponse(exchange, 400, "Bad Request");
            return;
        }
    }
}
