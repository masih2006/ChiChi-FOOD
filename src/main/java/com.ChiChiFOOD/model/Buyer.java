package com.ChiChiFOOD.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("Buyer")
public class    Buyer extends User {

    @ManyToMany
    @JoinTable(
            name = "buyer_favorite_restaurants",
            joinColumns = @JoinColumn(name = "buyer_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurant_id")
    )
    private List<Restaurant> favoriteRestaurants = new ArrayList<>();

    public Buyer() {
        super();
        setUserConfirmed();
    }

    public List<Restaurant> getFavoriteRestaurants() {
        return favoriteRestaurants;
    }

    public void addFavoriteRestaurant(Restaurant r) {
        favoriteRestaurants.add(r);
    }

    public void removeFavoriteRestaurant(Restaurant r) {
        favoriteRestaurants.remove(r);
    }

    public boolean isFavorite(Restaurant r) {
        return favoriteRestaurants.contains(r);
    }
}
