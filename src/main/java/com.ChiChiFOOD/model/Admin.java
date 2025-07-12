package com.ChiChiFOOD.model;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("Admin") // این مقدار در ستون "Role" ذخیره می‌شه
public class Admin extends User {
    public Admin() {
        super();
    }
}
