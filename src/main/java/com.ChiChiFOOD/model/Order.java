package com.ChiChiFOOD.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "customer_id", nullable = false)
    private int customerID;

    @Column(name = "vendor_id", nullable = false)
    private int vendorID;

    @Column(name = "coupon_id")
    private int couponID;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "item_id")
    private List<Integer> itemIDs;

    @Column(name = "raw_price", nullable = false)
    private int rawPrice;

    @Column(name = "tax_fee", nullable = false)
    private int taxFee;

    @Column(name = "additional_fee")
    private int additionalFee;

    @Column(name = "courier_fee")
    private int courierFee;

    @Column(name = "pay_price")
    private int payPrice;

    @Column(name = "courier_id")
    private int courierID;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    @Column(name = "created_at")
    private String created_at;

    @Column(name = "updated_at")
    private String updated_at;


    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public int getCourierID() {
        return courierID;
    }

    public void setCourierID(int courierID) {
        this.courierID = courierID;
    }

    public int getPayPrice() {
        return payPrice;
    }

    public void setPayPrice(int payPrice) {
        this.payPrice = payPrice;
    }

    public int getCourierFee() {
        return courierFee;
    }

    public void setCourierFee(int courierFee) {
        this.courierFee = courierFee;
    }

    public int getAdditionalFee() {
        return additionalFee;
    }

    public void setAdditionalFee(int additionalFee) {
        this.additionalFee = additionalFee;
    }

    public int getTaxFee() {
        return taxFee;
    }

    public void setTaxFee(int taxFee) {
        this.taxFee = taxFee;
    }

    public int getRawPrice() {
        return rawPrice;
    }

    public void setRawPrice(int rawPrice) {
        this.rawPrice = rawPrice;
    }

    public List<Integer> getItemIDs() {
        return itemIDs;
    }

    public void setItemIDs(List<Integer> itemIDs) {
        this.itemIDs = itemIDs;
    }

    public int getCouponID() {
        return couponID;
    }

    public void setCouponID(int couponID) {
        this.couponID = couponID;
    }

    public int getVendorID() {
        return vendorID;
    }

    public void setVendorID(int vendorID) {
        this.vendorID = vendorID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }}
