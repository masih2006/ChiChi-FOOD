package com.ChiChiFOOD.utils;



import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

public class JwtUtil {
    private static final String SECRET = "123"; // در حالت واقعی باید توی فایل .env باشه
    private static final long EXPIRATION_TIME = 3600_000; // ۱ ساعت

    public static String generateToken(String userId) {
        return JWT.create()
                .withSubject(userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(SECRET));
    }

    public static String verifyToken(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET))
                .build()
                .verify(token)
                .getSubject();
    }
}