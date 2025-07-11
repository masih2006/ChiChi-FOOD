package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.dao.impl.RestaurantDAO;
import com.ChiChiFOOD.dao.impl.RestaurantDAOImpl;
import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ChiChiFOOD.httphandler.Sender.sendJsonResponse;
import static com.ChiChiFOOD.httphandler.Sender.sendTextResponse;

public class VendorService {
    static Session DaoSession = HibernateUtil.getSessionFactory().openSession();
    static RestaurantDAO restaurantDAO = new RestaurantDAOImpl(DaoSession);
    public static void restaurantList(HttpExchange exchange, JsonObject jsonRequest) throws IOException {
        String search;
        ArrayList<String> keywords;
        try {
            search = jsonRequest.has("search") ? jsonRequest.get("search").getAsString() : null;
            if (jsonRequest.has("keywords") && jsonRequest.get("keywords").isJsonArray()) {
                JsonArray jsonArray = jsonRequest.getAsJsonArray("keywords");
                keywords = new ArrayList<>();
                for (JsonElement element : jsonArray) {
                    keywords.add(element.getAsString());
                }
            } else {
                keywords = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 400, "Invalid JSON format or data types");
            return;
        }

        List<Restaurant> restaurants = restaurantDAO.searchByName(search); // مثل before

        if (restaurants.isEmpty()) {
            sendTextResponse(exchange, 404, "No restaurant found with the given name");
            return;
        }

        Restaurant restaurant = restaurants.get(0);

        boolean hasMatchingFood = restaurant.getFoodItems().stream()
                .anyMatch(item -> item.getKeywords().containsAll(keywords));

        if (!hasMatchingFood) {
            sendTextResponse(exchange, 404, "No food matched the keywords in this restaurant");
            return;
        }

        Gson gson = new Gson();
        String jsonResponse = gson.toJson(restaurant);
        sendJsonResponse(exchange, 200, jsonResponse);
    }
}
