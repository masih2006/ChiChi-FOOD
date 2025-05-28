package com.ChiChiFOOD.utils;



import com.ChiChiFOOD.model.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class JwtUtil {
    private static final String SECRET = "123"; // در حالت واقعی باید توی فایل .env باشه
    private static final long EXPIRATION_TIME = 3600_000; // ۱ ساعت

    public static String generateToken(User user) {
        String role = "" ;
        if (user instanceof Seller) {
            role = "seller";
        }else if (user instanceof Buyer){
            role = "buyer";
        }else if (user instanceof Courier){
            role = "courier";
        }
//        else if (user instanceof Admin){
//            role = "admin";
//        }
        return JWT.create()
                .withSubject(String.valueOf(user.getId()))
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(SECRET));
    }

    public static DecodedJWT verifyToken(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET))
                .build()
                .verify(token);


    }
}