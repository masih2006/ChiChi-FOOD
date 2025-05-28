package com.ChiChiFOOD.httphandler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
            sendTextResponse(exchange, 400, "Invalid JSON");
            return;
        }




        if (method.equalsIgnoreCase("GET")) {
            if (params.length == 2 && params[1].equals("mine")) {

                }else if (params.length == 3 && params[2].equals("orders")) {

           }else {
                sendTextResponse(exchange, 400, "Bad Request");
                return;
            }
        }else if (method.equalsIgnoreCase("post")){
            if(path.equalsIgnoreCase("restaurants")) {
                registerRestaurant(exchange, jsonRequest);
            }else if (params.length == 3){
                if (params[2].equals("item")) {

                }else if (params[1].equals("menu")) {

                }
            }else {
                sendTextResponse(exchange, 400, "Bad Request");
                return;
            }
        }else if (method.equalsIgnoreCase("put")){
            if (params.length == 2 && params[1].matches("\\d+")) {

            }else if (params.length == 3 ) {
                if (params[2].equals("item")) {

                }else if (params[2].equals("menu")) {

                }
            }else {
                sendTextResponse(exchange, 400, "Bad Request");
            }
        }else if (method.equalsIgnoreCase("delete")){
        if (params.length == 5 && params [2].equals("item")) {

        }else if (params.length == 4){
            if (params[2].equals("menu")) {

            }else if (params[2].equals("item")) {
                
            }
        }else {
            sendTextResponse(exchange, 400, "Bad Request");
            return;
        }
        }else if (method.equalsIgnoreCase("patch")){
            if (params.length == 3 && params[2].equals("orders")) {

            }else {
                sendTextResponse(exchange, 400, "Bad Request");
                return;
            }
        }else{
            sendTextResponse(exchange, 405, "Method Not Allowed");
            return;
        }




    }

    private void registerRestaurant(HttpExchange exchange, JsonObject jsonRequest) throws IOException {
    try {
        String name = jsonRequest.get("name").getAsString();
        String address = jsonRequest.get("address").getAsString();
        String phone = jsonRequest.get("phone").getAsString();
        String logoBase64 = jsonRequest.get("logoBase64").getAsString();
        int fee = jsonRequest.get("tax_fee").getAsInt();
        int additionalFee = jsonRequest.get("additiona_fee").getAsInt();
        if (name == null || phone == null ||  address == null) {
            sendTextResponse(exchange, 400, "Missing required fields");
            return;
        }}catch (Exception e) {
        sendTextResponse(exchange, 400, "invalid JSON");
        e.printStackTrace();
        return;
    }
    if (!exchange.getAttribute("role").equals("seller")) {
        sendTextResponse(exchange, 403, "Forbidden request");
        return;
    }



    }
    private void sendTextResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

}

