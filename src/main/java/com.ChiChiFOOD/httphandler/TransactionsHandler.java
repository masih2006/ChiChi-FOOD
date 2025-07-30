package com.ChiChiFOOD.httphandler;

import com.ChiChiFOOD.Services.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TransactionsHandler implements HttpHandler {
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
            getHandler(exchange, params);
        } else if (method.equalsIgnoreCase("post")) {
            postHandler(exchange, params, jsonRequest, path);
        } else {
            Sender.sendTextResponse(exchange, 405, "Method Not Allowed");
            return;
        }
    }
    public void getHandler(HttpExchange exchange, String[] params) throws IOException {
        if (params[0].matches("transactions")){
            TransactionsService.listTransactions(exchange);
        }
        else
            Sender.sendTextResponse(exchange, 400, "Bad Request");
    }
    private void postHandler(HttpExchange exchange, String [] params,JsonObject jsonRequest,String path ) throws IOException {
        if(path.equalsIgnoreCase("/wallet/top-up") && params[1].matches("top-up")) {
            TransactionsService.wallet(exchange, jsonRequest);
        }else if (path.equalsIgnoreCase("/payment/online") && params[1].matches("online")){
            TransactionsService.pay(exchange, jsonRequest);
        } else {
            Sender.sendTextResponse(exchange, 400, "Bad Request");
            return;
        }
    }
}
