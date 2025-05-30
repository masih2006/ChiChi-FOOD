package com.ChiChiFOOD.httphandler;

import com.ChiChiFOOD.Services.RestaurantService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
public class RestaurantHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String [] params = path.split("/");
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
            postHandler(exchange,params, jsonRequest, path);
        }else if (method.equalsIgnoreCase("put")){
            putHandler(exchange,params);
        }else if (method.equalsIgnoreCase("delete")){
            deleteHandler(exchange,params);
        }else if (method.equalsIgnoreCase("patch")){
            patchHandler(exchange,params);
        }else{
            Sender.sendTextResponse(exchange, 405, "Method Not Allowed");
            return;
        }

    }
    private void getHandler(HttpExchange exchange, String [] params) throws IOException {
        if (params.length == 2 && params[1].equals("mine")) {

        }else if (params.length == 3 && params[2].equals("orders")) {

        }else {
            Sender.sendTextResponse(exchange, 400, "Bad Request");
            return;
        }
    }
    private void patchHandler(HttpExchange exchange, String [] params) throws IOException {
        if (params.length == 3 && params[2].equals("orders")) {

        }else {
            Sender.sendTextResponse(exchange, 400, "Bad Request");
            return;
        }
    }
    private void putHandler(HttpExchange exchange, String [] params) throws IOException {
        if (params.length == 2 && params[1].matches("\\d+")) {

        }else if (params.length == 3 ) {
            if (params[2].equals("item")) {

            }else if (params[2].equals("menu")) {

            }
        }else {
            Sender.sendTextResponse(exchange, 400, "Bad Request");
        }
    }
    private void deleteHandler(HttpExchange exchange, String [] params) throws IOException {
        if (params.length == 5 && params [2].equals("item")) {

        }else if (params.length == 4){
            if (params[2].equals("menu")) {

            }else if (params[2].equals("item")) {

            }
        }else {
            Sender.sendTextResponse(exchange, 400, "Bad Request");
            return;
        }
    }
    private void postHandler(HttpExchange exchange, String [] params,JsonObject jsonRequest,String path ) throws IOException {
        if(path.equalsIgnoreCase("/restaurants")) {
            RestaurantService.registerRestaurant(exchange, jsonRequest);
        }else if (params.length == 3){
            if (params[2].equals("item")) {

            }else if (params[1].equals("menu")) {

            }
        }else {
            Sender.sendTextResponse(exchange, 400, "Bad Request");
            return;
        }
    }

}

