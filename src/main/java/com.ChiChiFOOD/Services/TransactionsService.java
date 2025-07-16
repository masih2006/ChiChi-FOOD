package com.ChiChiFOOD.Services;


import com.ChiChiFOOD.dao.impl.TransactionDAO;
import com.ChiChiFOOD.dao.impl.TransactionDAOImpl;
import com.ChiChiFOOD.model.Transaction;
import com.ChiChiFOOD.utils.HibernateUtil;
import com.google.gson.Gson;
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
}
