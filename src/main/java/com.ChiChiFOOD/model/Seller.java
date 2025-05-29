package com.ChiChiFOOD.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@DiscriminatorValue("Seller")
public class Seller extends User {

    @Column(unique=false, nullable=true)
    private String ResturantName;


    public Seller() {
        super();
    }



    public String getResturantName() {
        return ResturantName;
    }

    public void setResturantName(String resturantName) {
        ResturantName = resturantName;
    }
}
// resturant , food , cart ,