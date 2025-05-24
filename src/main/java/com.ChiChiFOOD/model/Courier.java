package com.ChiChiFOOD.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "couriers")
public class Courier extends User {
    public Courier() {}
}
