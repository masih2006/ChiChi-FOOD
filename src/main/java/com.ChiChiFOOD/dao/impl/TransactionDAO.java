package com.ChiChiFOOD.dao.impl;


import com.ChiChiFOOD.model.Order;
import com.ChiChiFOOD.model.Transaction;

import java.util.List;

public interface TransactionDAO {
    public void save(Transaction transaction);
    public List<Transaction> findAll();
}
