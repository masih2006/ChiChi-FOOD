package com.ChiChiFOOD.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    private DiscountType type;
    @Enumerated(EnumType.STRING)
    private CouponScope scope;
    @Column(nullable = false)
    private int value;
    @Column(nullable = false)
    private int minPrice;
    @Column(nullable = false)
    private int userCount;
    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate endDate;
    @Column(nullable = true)
    private String restaurantId;
    @Column(nullable = true)
    private String itemId;

    public enum CouponScope {
        GENERAL,
        SHIPPING_ONLY,
        ITEM_SPECIFIC
    }

    public enum DiscountType {
        FIXED,
        PERCENTAGE
    }
    public Coupon() {
        this.scope = CouponScope.GENERAL;
    }


    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public DiscountType getType() {
        return type;
    }

    public void setType(DiscountType type) {
        this.type = type;
    }

    public void setType(String typeStr) {
        this.type = DiscountType.valueOf(typeStr.toUpperCase());
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(int minPrice) {
        this.minPrice = minPrice;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}

