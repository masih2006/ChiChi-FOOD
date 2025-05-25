package com.ChiChiFOOD;
import com.ChiChiFOOD.dao.impl.UserDAO;
import com.ChiChiFOOD.dao.impl.UserDAOImpl;
import com.ChiChiFOOD.model.*;
import com.ChiChiFOOD.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class App {
    public static void main(String[] args) {

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        Buyer courier = new Buyer();
        courier.setName("mamad");
        courier.setEmail("mamad1234@aur.ac.ir");
        courier.setPassword("123456");
        courier.setAddress("babhuOz");
        courier.setPhone("09131000001");
        courier.setBankName("SAderat");
        session.persist(courier);
        tx.commit();
        session.close();

    }

        // بستن EntityManagerFactory (توصیه می‌شود یک کلاس utility برای مدیریت آن ایجاد کنید)
        // برای سادگی در اینجا نادیده گرفته شده است.
    }
