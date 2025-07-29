package com.ChiChiFOOD.httphandler;

import com.ChiChiFOOD.Services.RatingService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RatingHandler implements HttpHandler {
    @Override
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
            System.out.println("kire");
            postHandler(exchange, params, jsonRequest, path);
        }else if (method.equalsIgnoreCase("put")){
            putHandler(exchange, params, jsonRequest);
        }else if (method.equalsIgnoreCase("delete")){
            deleteHandler(exchange, params);
        }else{
            Sender.sendTextResponse(exchange, 405, "Method Not Allowed");
            return;
        }
    }
    public void getHandler(HttpExchange exchange, String[] params) throws IOException {
        if (params.length == 3 && params[1].equals("items")){
            RatingService.getRatingForSpecificItem(exchange, Integer.parseInt(params[2]));
        }
        if (params.length == 2){
            RatingService.getRatingForSpecificItem(exchange, Integer.parseInt(params[1]));
        }
    }
    public void postHandler(HttpExchange exchange, String[] params, JsonObject jsonRequest, String path) throws IOException {
        if (params.length == 1 ) {
            RatingService.createRating(exchange, jsonRequest);
        }
    }
    public void putHandler(HttpExchange exchange, String[] params, JsonObject jsonRequest) throws IOException {
        if (params.length == 2  ) {
            RatingService.updateRating(exchange, Integer.parseInt(params[1]), jsonRequest);
        }
    }
    public void deleteHandler(HttpExchange exchange, String[] params) {
        if (params.length == 2 ) {
            RatingService.deleteRating(exchange, Integer.parseInt(params[1]));
        }
    }
}
