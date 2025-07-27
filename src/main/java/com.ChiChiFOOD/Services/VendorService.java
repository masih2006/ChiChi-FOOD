package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.dao.impl.ItemDAOImpl;
import com.ChiChiFOOD.dao.impl.RestaurantDAO;
import com.ChiChiFOOD.dao.impl.RestaurantDAOImpl;
import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.model.restaurant.Item;
import com.ChiChiFOOD.model.restaurant.Menu;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;

import java.io.IOException;
import java.util.*;

import static com.ChiChiFOOD.httphandler.Sender.sendJsonResponse;
import static com.ChiChiFOOD.httphandler.Sender.sendTextResponse;

public class VendorService {
    static Session DaoSession = HibernateUtil.getSessionFactory().openSession();
    static RestaurantDAO restaurantDAO = new RestaurantDAOImpl(DaoSession);
    public static void restaurantsList(HttpExchange exchange, JsonObject jsonRequest) throws IOException {
        String search = null;

        try {
            if (jsonRequest.has("search") && !jsonRequest.get("search").isJsonNull()) {
                search = jsonRequest.get("search").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 400, "Invalid JSON format or data types");
            return;
        }

        List<Restaurant> restaurants = restaurantDAO.searchByName(search);

        if (restaurants.isEmpty()) {
            sendTextResponse(exchange, 404, "No restaurant found with the given name");
            return;
        }

        List<Map<String, Object>> restaurantJsonList = new ArrayList<>();

        for (Restaurant restaurant : restaurants) {
            Map<String, Object> restaurantJson = new HashMap<>();
            restaurantJson.put("id", restaurant.getId());
            restaurantJson.put("name", restaurant.getName());
            restaurantJson.put("address", restaurant.getAddress());
            restaurantJson.put("phone", restaurant.getPhone());
            restaurantJson.put("logoBase64", restaurant.getLogoBase64() != null ? restaurant.getLogoBase64() : "");
            restaurantJson.put("tax_fee", restaurant.getTaxFee() != null ? restaurant.getTaxFee() : 0);
            restaurantJson.put("additional_fee", restaurant.getAdditionalFee() != null ? restaurant.getAdditionalFee() : 0);

            restaurantJsonList.add(restaurantJson);
        }

        Gson gson = new Gson();
        String jsonResponse = gson.toJson(restaurantJsonList);
        sendJsonResponse(exchange, 200, jsonResponse);
    }



    public static void restaurantMenus(HttpExchange exchange) throws IOException {

        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length < 3) {
            sendTextResponse(exchange, 400, "Invalid vendor id");
            return;
        }

        String vendorId = pathParts[2];

        if (!restaurantDAO.restaurantExistsById(vendorId)) {
            sendTextResponse(exchange, 404, "Restaurant not found");
            return;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            RestaurantDAOImpl restaurantDaoImpl = new RestaurantDAOImpl(session);
            ItemDAOImpl itemDaoImpl = new ItemDAOImpl(session);

            Restaurant restaurant = restaurantDaoImpl.findById(Long.parseLong(vendorId));
            if (restaurant == null) {
                sendTextResponse(exchange, 404, "Restaurant not found");
                return;
            }

            Map<String, Object> response = new LinkedHashMap<>();

            Map<String, Object> vendorMap = new LinkedHashMap<>();
            vendorMap.put("id", restaurant.getId());
            vendorMap.put("name", restaurant.getName());
            vendorMap.put("address", restaurant.getAddress());
            vendorMap.put("phone", restaurant.getPhone());
            vendorMap.put("logoBase64", restaurant.getLogoBase64());
            vendorMap.put("tax_fee", restaurant.getTaxFee());
            vendorMap.put("additional_fee", restaurant.getAdditionalFee());
            response.put("vendor", vendorMap);

            List<Menu> menus = restaurant.getMenus();
            List<String> menuTitles = new ArrayList<>();

            for (Menu menu : menus) {
                String title = menu.getTitle();
                menuTitles.add(title);

                List<Map<String, Object>> itemList = new ArrayList<>();
                for (Item item : menu.getItems()) {
                    itemList.add(item.toJson());
                }

                response.put(title, itemList);
            }

            response.put("menu_titles", menuTitles);

            String responseJson = new Gson().toJson(response);
            System.out.println(responseJson);
            sendJsonResponse(exchange, 200, responseJson);

        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal server error");
        }
    }




}
