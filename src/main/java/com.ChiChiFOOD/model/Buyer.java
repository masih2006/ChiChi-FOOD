package com.ChiChiFOOD.model;
import com.ChiChiFOOD.model.Bank;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("Buyer")
public class Buyer extends User {

    @Embedded
    private Bank bank;

    public Buyer() {
        super();
        this.bank = new Bank();
    }

    public void setBankName(String bankName) {
        bank.setBankName(bankName);
    }
    public String getBankName() {
        return bank.getBankName();
    }
}
