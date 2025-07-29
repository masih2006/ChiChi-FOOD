package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.Rating;

import java.util.List;

public interface RatingDAO {
    void save(Rating rating);

    Rating getById(int id);

    List<Rating> getAll();

    List<Rating> getByOrderId(String orderId);

    void delete(int id);
}
