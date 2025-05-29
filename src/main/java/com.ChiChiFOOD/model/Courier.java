package com.ChiChiFOOD.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@DiscriminatorValue("Courier")
public class Courier extends User {
    public Courier() {
        super();
    }

}
