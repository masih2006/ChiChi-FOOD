package com.ChiChiFOOD.httphandler;

import com.ChiChiFOOD.Services.AdminService;
import com.ChiChiFOOD.Services.CouponsService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AdminHandler implements HttpHandler {
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
            getHandler(exchange, params, jsonRequest);
        }else if (method.equalsIgnoreCase("post")){
           postHandler(exchange, params, jsonRequest, path);
        }else if (method.equalsIgnoreCase("put")){
            putHandler(exchange, params, jsonRequest);
        }else if (method.equalsIgnoreCase("delete")){
           deleteHandler(exchange, params);
        }else if (method.equalsIgnoreCase("patch")){
            patchHandler(exchange, params, jsonRequest);
        }else{
            Sender.sendTextResponse(exchange, 405, "Method Not Allowed");
            return;
        }
    }
    private void getHandler(HttpExchange exchange, String[] params, JsonObject jsonRequest) throws IOException {
        if (params.length == 2){
            if (params [1].equals("users")){
                AdminService.getAllUsers(exchange, jsonRequest);
            }
            else if (params [1].equals("coupons")){
                CouponsService.getAllCoupons(exchange, jsonRequest);
            }
        }else if (params.length == 3){
            if (params [1].equals("coupons")){
                CouponsService.getCoupon(exchange, params[2]);
            }
        }
   }
   private void postHandler(HttpExchange exchange, String[] params, JsonObject jsonRequest, String path) throws IOException {
    if (params.length == 2){
        if (params [1].equals("coupons")){
            CouponsService.createCoupon(exchange, jsonRequest);
        }
    }
   }
    private void putHandler(HttpExchange exchange, String[] params, JsonObject jsonRequest) throws IOException {
        if (params.length == 3){
            if (params [1].equals("coupons")){
                CouponsService.updateCoupon(exchange,jsonRequest,params[2]);
            }
        }
    }
    private void deleteHandler(HttpExchange exchange, String[] params) throws IOException {
        if (params.length == 3){
            if (params [1].equals("coupons")){
                CouponsService.deleteCoupon(exchange, new JsonObject(), params[2]);
            }
        }
    }
    private void patchHandler(HttpExchange exchange, String[] params, JsonObject jsonRequest) throws IOException {
        if (params.length == 4){
            if (params [1].equals("users") && params [3].equals("status")){
                AdminService.confirmUser(exchange,jsonRequest,params[2]);
            }
            if (params[1].equals("restaurants") && params [2].equals("confirm") ){
                AdminService.confirmRestaurant(exchange,jsonRequest,params[3]);
            }
        }
    }
}

