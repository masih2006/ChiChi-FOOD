package com.ChiChiFOOD.httphandler;

import com.ChiChiFOOD.Services.OrderService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class OrderHandler implements HttpHandler {
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
        }else if (method.equalsIgnoreCase("post")) {
            postHandler(exchange, params, jsonRequest, path);
        }else if (method.equalsIgnoreCase("put")){
            putHandler(exchange, params , jsonRequest);
        }else{
            Sender.sendTextResponse(exchange, 405, "Method Not Allowed");
            return;
        }
    }

    public void postHandler(HttpExchange exchange, String[] params, JsonObject jsonRequest, String path) throws IOException {
        if(path.equalsIgnoreCase("/orders")) {
            OrderService.submitOrder(exchange, jsonRequest);
        } else {
            Sender.sendTextResponse(exchange, 400, "Bad Request");
        }
    }
    public void getHandler(HttpExchange exchange, String[] params) throws IOException {
        if (params.length == 2 && params[0].matches("orders") && params[1].matches("\\d+")) {
            OrderService.specificOrder(exchange, params[1]);
        } else if (params.length == 2 && params[0].matches("orders") && params[1].matches("history")) {
            OrderService.orderHistory(exchange);
        }else if (params.length == 3 && params[0].matches("orders") && params[1].matches("getallorders")) {
            OrderService.getAllOrders(exchange,params[2]);
        }else if (params.length == 2 && params[0].matches("orders") && params[1].matches("getalluserorders")) {
            OrderService.getAllUserOrder(exchange);
        }
        else {
            Sender.sendTextResponse(exchange, 400, "Bad Request");
            return;
        }
    }
    public void putHandler(HttpExchange exchange, String[] params, JsonObject jsonRequest) throws IOException {
        if (params.length == 3 && params[1].matches("status")) {
            OrderService.changeStatus(exchange, jsonRequest, params[2]);
        }
        else {
            Sender.sendTextResponse(exchange, 400, "Bad Request");
            return;
        }
    }

}
