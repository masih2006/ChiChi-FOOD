package com.ChiChiFOOD.model;
import com.ChiChiFOOD.model.Bank;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("Buyer")
public class Buyer extends User {
    public Buyer() { super(); }
}
