package com.ChiChiFOOD.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("Seller")
public class Seller extends User {

    @OneToOne(mappedBy = "seller", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private Restaurant restaurant;


    public Seller() {
        super();
        setUserNotConfirmed();
    }



    public String getResturantName() {
        return restaurant.getName();
    }

    public void setResturantName(String resturantName) {
        restaurant.setName(resturantName);
    }
}
// resturant , food , cart ,