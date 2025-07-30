package com.ChiChiFOOD.model;

import com.google.gson.annotations.Expose;
import jakarta.persistence.Embeddable;

@Embeddable
public class Bank {
    @Expose
    private String bankName;
    @Expose
    private String accountNumber;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
