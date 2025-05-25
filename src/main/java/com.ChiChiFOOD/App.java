package com.ChiChiFOOD;

import com.ChiChiFOOD.model.MenuService;
import com.ChiChiFOOD.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class App {
    public static void main(String[] args) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            MenuService menuService = new MenuService(session);

            boolean running = true;
            while (running) {
                Transaction tx = session.beginTransaction();
                try {
                    running = menuService.start(); // فرض می‌کنیم start() حالا boolean برمی‌گرداند که مشخص می‌کند ادامه بده یا نه
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    e.printStackTrace();
                }
            }

        } finally {
            session.close();
            HibernateUtil.shutdown();
        }
    }
}