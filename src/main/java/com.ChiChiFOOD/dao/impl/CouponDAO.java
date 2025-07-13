package com.ChiChiFOOD.dao.impl;

import com.ChiChiFOOD.model.Coupon;

import java.util.List;

public interface CouponDAO {
    public void save(Coupon coupon);
    public void update(Coupon coupon);
    public void delete(Coupon coupon);
    public boolean doesCouponCodeExist(String code);
    List<Coupon> getAllCoupons();
    public boolean doesCouponIdExist(Long id) ;
    public Coupon getCouponByCode(String code) ;
    public Coupon getCouponById(Long id) ;

    }
