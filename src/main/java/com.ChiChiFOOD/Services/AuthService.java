package com.ChiChiFOOD.Services;

import com.ChiChiFOOD.model.*;
import com.ChiChiFOOD.utils.HibernateUtil;
import org.hibernate.Session;
import com.ChiChiFOOD.dao.impl.UserDAOImpl;
import com.ChiChiFOOD.dao.impl.UserDAO;

public class AuthService {
    private final UserDAO userDAO;
    private final Session session;

    public AuthService(Session session) {
        this.session = session;
        this.userDAO = new UserDAOImpl(session);
    }

    public boolean registerUser(String name, String phone, String email, String password, Role role, String address, String profileImageBase64, Bank bank) {
        if (isEmailExists(email)) {
            System.out.println("Email already exists.");
            return false;
        } else if (isPhoneNumberExists(phone)) {
            System.out.println("Phone number already exists.");
            return false;
        }

        User user;
        switch (role) {
            case BUYER:
                user = new Buyer();
                break;
            case SELLER:
                user = new Seller();
                break;
            case COURIER:
                user = new Courier();
                break;
            default:
                System.out.println("Role invalid.");
                return false;
        }

        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(password);
        user.setAddress(address);
        user.setProfileImageBase64(profileImageBase64);
        user.setBank(bank);
        session.persist(user);
        System.out.println("Register success.");
        return true;
    }
    public User loginUser(String phone, String password) {
        return userDAO.findByPhoneAndPassword(phone, password);
    }

    private boolean isEmailExists(String email) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Long count = session.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .uniqueResult();
        session.close();
        return count != null && count > 0;
    }

    private boolean isPhoneNumberExists(String phone) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Long count = session.createQuery("SELECT COUNT(u) FROM User u WHERE u.phone = :phone", Long.class)
                .setParameter("phone", phone)
                .uniqueResult();
        session.close();
        return count != null && count > 0;
    }
}