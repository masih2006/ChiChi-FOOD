package com.ChiChiFOOD.model;

import jakarta.persistence.Embeddable;
@Embeddable
public class Bank {
    private String bankName;
    private String accountNumber;

    // Getterها و Setterها
    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
