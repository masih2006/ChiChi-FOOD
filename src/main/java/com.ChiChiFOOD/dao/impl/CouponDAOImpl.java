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
        session.merge(coupon);
    }

    @Override
    public void delete(Coupon coupon) {
        session.remove(coupon);
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
    public boolean doesCouponIdExist(Long id) {
        String hql = "SELECT COUNT(c) FROM Coupon c WHERE c.id = :id";
        Long count = session.createQuery(hql, Long.class)
                .setParameter("id", id)
                .uniqueResult();
        return count != null && count > 0;
    }

    public Coupon getCouponByCode(String code) {
        String hql = "FROM Coupon c WHERE c.code = :code";
        return session.createQuery(hql, Coupon.class)
                .setParameter("code", code)
                .uniqueResult();
    }
    public Coupon getCouponById(Long id) {
        return session.get(Coupon.class, id);
    }



}
