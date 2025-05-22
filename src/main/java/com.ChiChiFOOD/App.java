package com.ChiChiFOOD;

import com.ChiChiFOOD.dao.UserDao;
import com.ChiChiFOOD.dao.impl.UserDaoImpl;
import com.ChiChiFOOD.model.User;

public class App {
    public static void main(String[] args) {
        UserDao userDao = new UserDaoImpl();

        // ایجاد یک شیء User جدید
        User newUser = new User();
        newUser.setName("علی");
        newUser.setPassword("123456");
        newUser.setEmail("ae.com");
        newUser.setPhone("0912xxxxxxx");
        newUser.setAddress("تهران");

        // ذخیره کاربر در پایگاه داده
        userDao.save(newUser);
        System.out.println("کاربر با ID: " + newUser.getId() + " ذخیره شد.");
        // بازیابی کاربر بر اساس ID
        User retrievedUser = userDao.findById(newUser.getId());
        if (retrievedUser != null) {
            System.out.println("کاربر بازیابی شده: " + retrievedUser.getName() + " - " + retrievedUser.getEmail());
        }

        // بستن EntityManagerFactory (توصیه می‌شود یک کلاس utility برای مدیریت آن ایجاد کنید)
        // برای سادگی در اینجا نادیده گرفته شده است.
    }
}