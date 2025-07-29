package com.ChiChiFOOD.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "Role")
public abstract class User {
    @Expose
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Expose
    @Column(nullable = false)
    @SerializedName("full_name")
    private String name;
    @Expose
    @Column(unique = true, nullable = false)
    private String phone;
    @Expose
    @Column(nullable = false)
    private int walletBalance = 0;
    @Expose
    @Column(unique = true, nullable = false)
    private String email;
    @Expose
    @Column( nullable = false)
    private int isUserConfirmed;
    @Expose
    @Column(nullable = true)
    private String address;
    ;
    @Column(nullable = false)
    private String password;

    @SerializedName("role")
    public String getRole() {
        return this.getClass().getSimpleName();
    }


    @Expose
    @Column
    private String profileImageBase64;


    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    @Expose
    @Embedded
    private Bank bank;


    public User() {
        this.bank = new Bank();
        isUserConfirmed = 0;
    }
    public int getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(int walletBalance) {
        this.walletBalance = walletBalance;
    }


    public String getProfileImageBase64() {
        return profileImageBase64;
    }

    public void setProfileImageBase64(String profileImageBase64) {
        this.profileImageBase64 = profileImageBase64;
    }

    public void setAccountNumber(String accountNumber) {
        bank.setAccountNumber(accountNumber);
    }

    public String getAccountNumber() {
        return bank.getAccountNumber();
    }

    public void setBankName(String bankName) {
        bank.setBankName(bankName);
    }

    public String getBankName() {
        return bank.getBankName();
    }

    public int getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setUserConfirmed(){
        isUserConfirmed = 1;
    }
    public void setUserNotConfirmed(){
        isUserConfirmed = 0;
    }
    public int getIsUserConfirmed() {
        return isUserConfirmed;
    }


}
