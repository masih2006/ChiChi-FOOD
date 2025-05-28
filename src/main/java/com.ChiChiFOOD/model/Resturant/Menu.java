package com.ChiChiFOOD.model.Resturant;

import com.ChiChiFOOD.model.Restaurant;
import jakarta.persistence.*;

@Entity
@Table
public class Menu {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")  // این ستون توی دیتابیس هست
    private Restaurant restaurant;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
