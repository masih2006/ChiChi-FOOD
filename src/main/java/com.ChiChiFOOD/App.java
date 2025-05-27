package com.ChiChiFOOD;
import com.ChiChiFOOD.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;



import com.ChiChiFOOD.httphandler.AppServer;

public class App {
    public static void main(String[] args) {
        AppServer server = new AppServer();
        int port = 8569;  // پورت دلخواه خودت

        try {
            server.start(port);
            System.out.println("Server is running. Press Ctrl+C to stop.");

            // اینجا می‌تونی کدی بذاری که برنامه تا زمان بسته شدن سرور اجرا بمونه.
            // مثلا منتظر ورودی کاربر یا کارهای دیگه باشه.
            // یا فقط اجازه بده برنامه اجرا بمونه تا Ctrl+C بزنن.

            Thread.currentThread().join(); // نگه داشتن برنامه فعال (تا زمانی که با Ctrl+C بسته شود)

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
            System.out.println("Server stopped.");
        }
    }
}