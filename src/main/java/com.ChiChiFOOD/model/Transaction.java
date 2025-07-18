package com.ChiChiFOOD.model;

import jakarta.persistence.*;

    @Entity
    @Table(name = "transactions")
    public class Transaction {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        @Column(name = "order_id")
        private int orderID;

        @Column(name = "user_id")
        private int userID;

        @Enumerated(EnumType.STRING)
        @Column(name = "payment_method")
        private PaymentMethod method;

        @Enumerated(EnumType.STRING)
        @Column(name = "status")
        private TransactionStatus status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}
