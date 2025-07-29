package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.dao.impl.*;
import com.ChiChiFOOD.model.Order;
import com.ChiChiFOOD.model.Rating;
import com.ChiChiFOOD.model.Restaurant;
import com.ChiChiFOOD.model.restaurant.Item;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ChiChiFOOD.httphandler.Sender.sendTextResponse;

public class RatingService {
    public static void createRating(HttpExchange exchange, JsonObject jsonObject) throws IOException {
        int rating;
        int orderID;
        String comment;
        List<String> pictures = new ArrayList<>();

        try {
            rating = jsonObject.has("rating") ? jsonObject.get("rating").getAsInt() : 0;
            orderID = jsonObject.has("order_id") ? jsonObject.get("order_id").getAsInt() : 0;
            comment = jsonObject.has("comment") ? jsonObject.get("comment").getAsString() : "";
            if (jsonObject.has("imageBase64") && jsonObject.get("imageBase64").isJsonArray()) {
                JsonArray imageArray = jsonObject.getAsJsonArray("imageBase64");
                for (JsonElement element : imageArray) {
                    pictures.add(element.getAsString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 400, "Invalid JSON format or data types");
            return;
        }

        if (rating == 0 || orderID == 0) {
            sendTextResponse(exchange, 400, "Rating or orderID is required");
            return;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            OrderDAO orderDAO = new OrderDAOImpl(session);
            ItemDAO itemDAO = new ItemDAOImpl(session);
            RestaurantDAO restaurantDAO = new RestaurantDAOImpl(session);
            RatingDAO ratingDAO = new RatingDAOImpl(session);

            Order order = orderDAO.findById(orderID);
            if (order == null) {
                sendTextResponse(exchange, 400, "Order not found");
                return;
            }

            int restaurantID = order.getVendorID();
            Restaurant restaurant = restaurantDAO.findById((long) restaurantID);
            if (restaurant == null) {
                sendTextResponse(exchange, 400, "Restaurant not found");
                return;
            }

            Rating rating1 = new Rating();
            rating1.setScore(rating);
            rating1.setComment(comment);
            rating1.setOrderID(orderID);
            rating1.setRestaurantId(restaurantID);
            rating1.setImagePaths(pictures);
            ratingDAO.save(rating1);

            // رستوران
            restaurant.addRating(rating1);
            restaurantDAO.update(restaurant);

            // آیتم‌ها
            for (Integer itemID : order.getItemIDs()) {
                Item item = itemDAO.findById(itemID);
                if (item == null) {
                    transaction.rollback();
                    sendTextResponse(exchange, 400, "Item not found: " + itemID);
                    return;
                }

                // فقط اگر قبلاً اضافه نشده باشه
                if (!item.getRatings().contains(rating1)) {
                    item.addRating(rating1);
                    itemDAO.update(item);
                }
            }

            transaction.commit();
            sendTextResponse(exchange, 200, "Rating submitted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Server error");
        }
    }

    public static void getRating(HttpExchange exchange, int ratingID) throws IOException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            RatingDAO ratingDAO = new RatingDAOImpl(session);
            OrderDAO orderDAO = new OrderDAOImpl(session);

            Rating rating = ratingDAO.getById(ratingID);

            if (rating == null) {
                sendTextResponse(exchange, 404, "Rating not found");
                return;
            }

            JsonObject response = new JsonObject();

            response.addProperty("id", rating.getId());
            response.addProperty("order_id", rating.getOrderID());
            response.addProperty("rating", rating.getScore());
            response.addProperty("comment", rating.getComment());

            // افزودن تصاویر به صورت آرایه
            JsonArray imageArray = new JsonArray();
            for (String img : rating.getImagePaths()) {
                imageArray.add(img);
            }
            response.add("imageBase64", imageArray);

            // اطلاعات کاربر و زمان از Order
            Order order = orderDAO.findById(rating.getOrderID());
            if (order != null) {
                response.addProperty("user_id", order.getCustomerID());
                response.addProperty("created_at", order.getCreated_at().toString());
            } else {
                response.addProperty("user_id", 0);
                response.addProperty("created_at", "");
            }

            sendTextResponse(exchange, 200, response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal Server Error");
        }
    }

    public static void deleteRating(HttpExchange exchange, int ratingID) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            RatingDAO ratingDAO = new RatingDAOImpl(session);
            ItemDAO itemDAO = new ItemDAOImpl(session);
            RestaurantDAO restaurantDAO = new RestaurantDAOImpl(session);

            Rating rating = ratingDAO.getById(ratingID);
            if (rating == null) {
                sendTextResponse(exchange, 404, "Rating not found");
                return;
            }
            List<Item> relatedItems = new ArrayList<>(rating.getItems());
            for (Item item : relatedItems) {
                item.getRatings().remove(rating);
                session.update(item);
            }
            rating.getItems().clear();

            Restaurant restaurant = rating.getRestaurant();
            if (restaurant != null) {
                restaurant.getRatings().remove(rating);
                session.update(restaurant);
                rating.setRestaurant(null);
            }
            session.delete(rating);

            tx.commit();
            sendTextResponse(exchange, 200, "Rating deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                sendTextResponse(exchange, 500, "Server error during rating deletion");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public static void updateRating(HttpExchange exchange, int ratingID, JsonObject jsonObject) throws IOException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            RatingDAO ratingDAO = new RatingDAOImpl(session);
            OrderDAO orderDAO = new OrderDAOImpl(session);

            Rating rating = ratingDAO.getById(ratingID);
            if (rating == null) {
                sendTextResponse(exchange, 404, "Rating not found");
                return;
            }

            // به‌روزرسانی مقادیر موجود در JSON
            if (jsonObject.has("rating")) {
                rating.setScore(jsonObject.get("rating").getAsInt());
            }

            if (jsonObject.has("comment")) {
                rating.setComment(jsonObject.get("comment").getAsString());
            }

            if (jsonObject.has("imageBase64") && jsonObject.get("imageBase64").isJsonArray()) {
                List<String> images = new ArrayList<>();
                for (JsonElement el : jsonObject.getAsJsonArray("imageBase64")) {
                    images.add(el.getAsString());
                }
                rating.setImagePaths(images);
            }

            session.update(rating);
            tx.commit();

            // پاسخ خروجی به فرمت مشخص‌شده
            Order order = orderDAO.findById(rating.getOrderID());

            JsonObject response = new JsonObject();
            response.addProperty("id", rating.getId());
            response.addProperty("order_id", rating.getOrderID());
            response.addProperty("rating", rating.getScore());
            response.addProperty("comment", rating.getComment());
            response.addProperty("user_id", order.getCustomerID());
            response.addProperty("created_at", order.getCreated_at());

            sendTextResponse(exchange, 200, response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal server error during update");
        }
    }


    public static void getRatingForSpecificItem(HttpExchange exchange, int itemID) throws IOException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            ItemDAO itemDAO = new ItemDAOImpl(session);
            OrderDAO orderDAO = new OrderDAOImpl(session);
            Item item = itemDAO.findById(itemID);

            if (item == null) {
                sendTextResponse(exchange, 404, "Not found item");
                return;
            }

            List<Rating> ratings = item.getRatings();

            JsonArray commentsArray = new JsonArray();
            double totalScore = 0;

            for (Rating rating : ratings) {
                JsonObject commentObj = new JsonObject();

                commentObj.addProperty("id", rating.getId());
                commentObj.addProperty("order_id", rating.getOrderID());
                commentObj.addProperty("rating", rating.getScore());
                commentObj.addProperty("comment", rating.getComment());

                JsonArray images = new JsonArray();
                for (String img : rating.getImagePaths()) {
                    images.add(img);
                }
                commentObj.add("imageBase64", images);

                Order order = orderDAO.findById(rating.getOrderID());
                if (order != null) {
                    commentObj.addProperty("user_id", order.getCustomerID());
                    if (order.getCreated_at() == null)
                        commentObj.addProperty("created_at", "0");
                    else
                         commentObj.addProperty("created_at", order.getCreated_at().toString());
                } else {
                    commentObj.addProperty("user_id", 0);
                    commentObj.addProperty("created_at", "");
                }

                totalScore += rating.getScore();
                commentsArray.add(commentObj);
            }

            double avgRating = ratings.isEmpty() ? 0 : totalScore / ratings.size();

            JsonObject response = new JsonObject();
            response.addProperty("avg_rating", avgRating);
            response.add("comments", commentsArray);

            String json = response.toString();
            sendTextResponse(exchange, 200, json);

        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal Server Error");
        }
    }
}
