package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.User;
import java.util.List;

public interface UserDAO {
    void save(User user);
    User findById(int id);
    List<User> findAll();
    void update(User user);
    void delete(User user);
}
