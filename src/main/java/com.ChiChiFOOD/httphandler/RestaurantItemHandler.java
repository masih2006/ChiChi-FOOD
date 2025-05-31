package com.ChiChiFOOD.httphandler;


import com.ChiChiFOOD.Services.ItemService;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.hibernate.Session;
import org.hibernate.Transaction;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;


public class RestaurantItemHandler implements HttpHandler {
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] params = Arrays.stream(path.split("/"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        JsonObject jsonRequest = null;
        // فقط برای متدهایی که معمولاً body دارند json بخوان
        if (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("PATCH")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                jsonRequest = gson.fromJson(reader, JsonObject.class);
            } catch (Exception e) {
                e.printStackTrace();
                Sender.sendResponse(exchange, 400, "Invalid JSON");
                return;
            }
        }

        switch (method.toUpperCase()) {
            case "POST":
                postHandler(exchange, params, jsonRequest);
                break;
            case "PUT":
                putHandler(exchange, params, jsonRequest);
                break;
            case "DELETE":
                deleteHandler(exchange, params);
                break;
            default:
                Sender.sendResponse(exchange, 405, "Method Not Allowed");
        }
    }
    private void postHandler(HttpExchange exchange, String[] params, JsonObject jsonRequest) throws IOException {
        // اضافه کردن آیتم به رستوران
        if (params.length == 3 && params[0].equalsIgnoreCase("restaurants") && params[1].matches("\\d+") && params[2].equalsIgnoreCase("item")) {
            int restaurantId = Integer.parseInt(params[1]);
            addItem(exchange, restaurantId, jsonRequest);
        } else {
            Sender.sendResponse(exchange, 400, "Bad Request");
        }
    }

    private void putHandler(HttpExchange exchange, String[] params, JsonObject jsonRequest) throws IOException {
        // ویرایش آیتم رستوران
        if (params.length == 4 && params[0].equalsIgnoreCase("restaurants") && params[1].matches("\\d+") && params[2].equalsIgnoreCase("item") && params[3].matches("\\d+")) {
            int restaurantId = Integer.parseInt(params[1]);
            int itemId = Integer.parseInt(params[3]);
            updateItem(exchange, restaurantId, itemId, jsonRequest);
        } else {
            Sender.sendResponse(exchange, 400, "Bad Request");
        }
    }

    private void deleteHandler(HttpExchange exchange, String[] params) throws IOException {
        // حذف آیتم: /restaurants/{restaurantId}/item/{itemId}
        if (params.length == 4 &&
                params[0].equalsIgnoreCase("restaurants") &&
                params[1].matches("\\d+") &&
                params[2].equalsIgnoreCase("item") &&
                params[3].matches("\\d+")) {

            int restaurantId = Integer.parseInt(params[1]);
            int itemId = Integer.parseInt(params[3]);
            deleteItem(exchange, restaurantId, itemId);
        } else {
            Sender.sendResponse(exchange, 400, "Bad Request - Invalid URL format");
        }
    }

    // متد افزودن آیتم (موجود از قبل، کمی تغییر یافته)
    private void addItem(HttpExchange exchange, int restaurantId, JsonObject jsonRequest) throws IOException {
        String name = getString(jsonRequest, "name");
        String imageBase64 = getString(jsonRequest, "imageBase64");
        String description = getString(jsonRequest, "description");

        Integer price, supply;
        try {
            price = Integer.parseInt(getString(jsonRequest, "price"));
            supply = Integer.parseInt(getString(jsonRequest, "supply"));
        } catch (NumberFormatException e) {
            Sender.sendResponse(exchange, 400, "Invalid number format for price or supply");
            return;
        }

        JsonArray keywordsArray = jsonRequest.getAsJsonArray("keywords");
        ArrayList<String> keywords = new ArrayList<>();
        if (keywordsArray != null) {
            for (JsonElement keyword : keywordsArray) keywords.add(keyword.getAsString());
        } else {
            Sender.sendResponse(exchange, 400, "Missing required fields: keywords are required.");
            return;
        }

        if (name == null || name.trim().isEmpty() || description == null || description.trim().isEmpty() || keywords.isEmpty()) {
            Sender.sendResponse(exchange, 400, "Missing required fields: name, description, price, supply and keywords are required.");
            return;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            ItemService itemService = new ItemService(session);
            boolean success = itemService.addItem(name, imageBase64, description, price, supply, keywords);
            if (success) {
                tx.commit();
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("message", "Item registered successfully");
                Sender.sendResponse(exchange, 200, responseJson.toString());
            } else {
                tx.rollback();
                Sender.sendResponse(exchange, 400, "Failed to add item");
            }
        }
    }

    // متدهای update و delete (باید پیاده‌سازی کنی)

    private void updateItem(HttpExchange exchange, int restaurantId, int itemId, JsonObject jsonRequest) throws IOException {
        // مشابه addItem ولی با آپدیت آیتم با شناسه itemId
        // پیاده‌سازی متناسب با ItemService
        Sender.sendResponse(exchange, 501, "Not implemented yet");
    }

    private void deleteItem(HttpExchange exchange, int restaurantId, int itemId) throws IOException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            ItemService itemService = new ItemService(session);

            boolean success = itemService.deleteItemFromRestaurant(restaurantId, itemId);

            if (success) {
                tx.commit();
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("message", "Item deleted successfully");
                Sender.sendResponse(exchange, 200, responseJson.toString());
            } else {
                tx.rollback();
                Sender.sendResponse(exchange, 404, "Item not found or does not belong to restaurant");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Sender.sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private String getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }
}
