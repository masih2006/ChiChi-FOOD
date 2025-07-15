package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.dao.impl.OrderDAO;
import com.ChiChiFOOD.dao.impl.OrderDAOImpl;
import com.ChiChiFOOD.dao.impl.RestaurantDAO;
import com.ChiChiFOOD.dao.impl.RestaurantDAOImpl;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import org.hibernate.Session;

import java.io.IOException;

public class OrderService {
    static Session DaoSession = HibernateUtil.getSessionFactory().openSession();
    static OrderDAO orderDAO = new OrderDAOImpl(DaoSession);

    public static void submitOrder(HttpExchange exchange, JsonObject jsonRequest) throws IOException {}

    public static void specificOrder(HttpExchange exchange, String orderID) throws IOException {}

    public static void orderHistory(HttpExchange exchange) throws IOException {}
}

