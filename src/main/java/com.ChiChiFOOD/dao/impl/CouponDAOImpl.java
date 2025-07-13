package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.Coupon;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class CouponDAOImpl implements CouponDAO {

    private final Session session;

    public CouponDAOImpl(Session session) {
        this.session = session;
    }

    @Override
    public void save(Coupon coupon) {
        session.persist(coupon);
    }

    @Override
    public void update(Coupon coupon) {
        Transaction transaction = session.beginTransaction();
        session.merge(coupon);
        transaction.commit();
    }

    @Override
    public void delete(Coupon coupon) {
        Transaction transaction = session.beginTransaction();
        session.remove(coupon);
        transaction.commit();
    }

    @Override
    public boolean doesCouponCodeExist(String code) {
        String hql = "SELECT COUNT(c) FROM Coupon c WHERE c.code = :code";
        Long count = session.createQuery(hql, Long.class)
                .setParameter("code", code)
                .uniqueResult();
        return count != null && count > 0;
    }
    @Override
    public List<Coupon> getAllCoupons() {
        String hql = "FROM Coupon";
        return session.createQuery(hql, Coupon.class).getResultList();
    }

}
