package com.ChiChiFOOD;
import com.ChiChiFOOD.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;



import com.ChiChiFOOD.httphandler.AppServer;

public class App {
    private final static int PORT = 8569;  // پورت دلخواه خودت

    public static void main(String[] args) {

        AppServer server = new AppServer();

        try {
            server.start(PORT);
            System.out.println("Server is running. ");


            Thread.currentThread().join(); // نگه داشتن برنامه فعال (تا زمانی که با Ctrl+C بسته شود)

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
            System.out.println("Server stopped.");
        }
    }
}