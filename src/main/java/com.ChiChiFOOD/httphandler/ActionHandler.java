package com.ChiChiFOOD.httphandler;

import com.ChiChiFOOD.Services.ActionService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ActionHandler implements HttpHandler {
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
            postHandler(exchange, params, jsonRequest);
        }else if (method.equalsIgnoreCase("put")){
            //putHandler(exchange, params, jsonRequest);
        }else if (method.equalsIgnoreCase("delete")){
            deleteHandler(exchange, params);
        }else if (method.equalsIgnoreCase("patch")){
            //patchHandler(exchange, params);
        }else{
            Sender.sendTextResponse(exchange, 405, "Method Not Allowed");
            return;
        }
    }
    public void getHandler(HttpExchange exchange, String[] params) throws IOException {
        if (params.length == 1 & params[0].equalsIgnoreCase("favorites")) {
            ActionService.getFavorites(exchange);
        }
    }
    public void postHandler(HttpExchange exchange, String[] params, JsonObject jsonRequest) throws IOException {
        if (params.length == 2) {
            if (params [0].equalsIgnoreCase("favorites") && params[1].matches("\\d+")) {
                ActionService.addRestaurantToFavorites(exchange,params[1]);
            }
        }
    }
    public void deleteHandler(HttpExchange exchange, String[] params) throws IOException {
        if (params.length == 2) {
            if (params [0].equalsIgnoreCase("favorites") && params[1].matches("\\d+")) {
                ActionService.removeRestaurantFromFavorites(exchange,params[1]);
            }
        }
    }
}
