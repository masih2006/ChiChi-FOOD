package com.ChiChiFOOD.dao;

import com.ChiChiFOOD.model.User;
import java.util.List;

public interface UserDao {
    void save(User user);
    User findById(int id);
    List<User> findAll();
    void update(User user);
    void delete(int id);
}