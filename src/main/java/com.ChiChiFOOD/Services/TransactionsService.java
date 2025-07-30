package com.ChiChiFOOD.Services;


import com.ChiChiFOOD.dao.impl.*;
import com.ChiChiFOOD.model.*;
import com.ChiChiFOOD.model.restaurant.Item;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static com.ChiChiFOOD.httphandler.Sender.sendJsonResponse;
import static com.ChiChiFOOD.httphandler.Sender.sendTextResponse;

public class TransactionsService {
    static Session DaoSession = HibernateUtil.getSessionFactory().openSession();
    static TransactionDAO transactionDAO = new TransactionDAOImpl(DaoSession);
    static OrderDAO orderDAO = new OrderDAOImpl(DaoSession);
    static UserDAO userDAO = new UserDAOImpl(DaoSession);

    public static void listTransactions(HttpExchange exchange) throws IOException {
        List<Transaction> transactions;
        try {
            transactions = transactionDAO.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 500, "Internal server error while fetching transactions");
            return;
        }

        if (transactions == null || transactions.isEmpty()) {
            sendTextResponse(exchange, 404, "No transactions found");
            return;
        }

        Gson gson = new Gson();
        String jsonResponse = gson.toJson(transactions);
        sendJsonResponse(exchange, 200, jsonResponse);
    }

    public static void pay(HttpExchange exchange, JsonObject jsonRequest) throws IOException {
        int orderId;
        String methodText;

        try {
            orderId = jsonRequest.has("order_id") ? jsonRequest.get("order_id").getAsInt() : -1;
            methodText = jsonRequest.has("method") ? jsonRequest.get("method").getAsString().toLowerCase() : null;
        } catch (Exception e) {
            e.printStackTrace();
            sendTextResponse(exchange, 400, "Invalid JSON format or data types");
            return;
        }

        if (orderId == -1 || methodText == null) {
            sendTextResponse(exchange, 400, "Missing required fields: order_id or method");
            return;
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.fromString(methodText);
        } catch (IllegalArgumentException e) {
            sendTextResponse(exchange, 400, "Invalid payment method: " + methodText);
            return;
        }

        int userId = Integer.parseInt(exchange.getAttribute("userId").toString());

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            org.hibernate.Transaction tx = session.beginTransaction();
            try {
                TransactionDAOImpl transactionDao = new TransactionDAOImpl(session);
                OrderDAOImpl orderDao = new OrderDAOImpl(session);

                Order order = orderDao.findById(orderId);
                if (order == null) {
                    sendTextResponse(exchange, 404, "Order not found");
                    tx.rollback();
                    return;
                }

                if (order.getCustomerID() != userId) {
                    sendTextResponse(exchange, 403, "You are not allowed to pay for this order");
                    tx.rollback();
                    return;
                }

                if (!order.getStatus().equals(OrderStatus.SUBMITTED)) {
                    sendTextResponse(exchange, 409, "Order is not in a payable state");
                    tx.rollback();
                    return;
                }
                if (method.toString().equalsIgnoreCase("wallet")) {
                    int temp = userDAO.findById(userId).getWalletBalance();
                    userDAO.findById(userId).setWalletBalance(temp-orderDAO.findById(orderId).getPayPrice());
                }
                Transaction transaction = new Transaction();
                transaction.setOrderID(orderId);
                transaction.setUserID(userId);
                transaction.setMethod(method);
                transaction.setStatus(TransactionStatus.SUCCESS);
                transaction.setMoney(orderDAO.findById(orderId).getPayPrice());
                transactionDao.save(transaction);

                order.setStatus(OrderStatus.WAITING_VENDOR);
                order.setUpdated_at(String.valueOf(System.currentTimeMillis()));
                orderDao.update(order);

                tx.commit();

                JsonObject response = new JsonObject();
                response.addProperty("id", transaction.getId());
                response.addProperty("order_id", transaction.getOrderID());
                response.addProperty("user_id", transaction.getUserID());
                response.addProperty("money", transaction.getMoney());
                response.addProperty("method", transaction.getMethod().getValue());
                response.addProperty("status", transaction.getStatus().name().toLowerCase());

                sendJsonResponse(exchange, 200, response.toString());
            } catch (Exception e) {
                e.printStackTrace();
                tx.rollback();
                sendTextResponse(exchange, 500, "Internal server error");
            }
        }
    }
    public static void wallet(HttpExchange exchange, JsonObject jsonRequest) throws IOException {
        int amount;

        try {
            if (!jsonRequest.has("amount")) {
                sendTextResponse(exchange, 400, "Missing required field: amount");
                return;
            }
            amount = jsonRequest.get("amount").getAsInt();
            if (amount <= 0) {
                sendTextResponse(exchange, 400, "Amount must be greater than zero");
                return;
            }
        } catch (Exception e) {
            sendTextResponse(exchange, 400, "Invalid JSON format or amount");
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(exchange.getAttribute("userId").toString());
        } catch (Exception e) {
            sendTextResponse(exchange, 401, "Unauthorized");
            return;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            org.hibernate.Transaction tx = session.beginTransaction();
            try {
                Buyer user = session.get(Buyer.class, userId);
                if (user == null) {
                    sendTextResponse(exchange, 404, "User not found");
                    return;
                }

                int currentBalance = user.getWalletBalance();
                user.setWalletBalance(currentBalance + amount);

                session.update(user);
                tx.commit();

                sendTextResponse(exchange, 200, "Wallet topped up successfully");
            } catch (Exception e) {
                e.printStackTrace();
                tx.rollback();
                sendTextResponse(exchange, 500, "Internal server error");
            }
        }
    }
}
