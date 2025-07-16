package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.Transaction;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class TransactionDAOImpl implements TransactionDAO {
    private final Session session;

    public TransactionDAOImpl(Session session) {
        this.session = session;
    }

    @Override
    public void save(Transaction transaction) {
        session.save(transaction);
    }

    @Override
    public List<Transaction> findAll() {
        String hql = "FROM Transaction";
        Query<Transaction> query = session.createQuery(hql, Transaction.class);
        return query.list();
    }

}
