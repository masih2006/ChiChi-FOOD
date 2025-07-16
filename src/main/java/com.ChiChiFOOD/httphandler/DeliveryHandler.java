package com.ChiChiFOOD.httphandler;

import com.ChiChiFOOD.Services.DeliveryService;
import com.ChiChiFOOD.Services.VendorService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DeliveryHandler implements HttpHandler {
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
        }else if (method.equalsIgnoreCase("patch")){
            patchHandler(exchange, params);
        }else{
            Sender.sendTextResponse(exchange, 405, "Method Not Allowed");
            return;
        }
    }

    public void getHandler(HttpExchange exchange, String[] params) throws IOException {
        if (params.length == 2 && params[1].matches("\\d+")){
            DeliveryService.(exchange);
        }
        else
            Sender.sendTextResponse(exchange, 400, "Bad Request");
    }

    public void patchHandler(HttpExchange exchange, String [] params) throws IOException {
        if (params.length == 3 && params[2].equals("orders")) {
            DeliveryService.();
        }else {
            Sender.sendTextResponse(exchange, 400, "Bad Request");
            return;
        }
    }
}
