package com.ChiChiFOOD.utils;

import java.util.HashSet;
import java.util.Set;

public class TokenBlacklist {
    private static final Set<String> blacklistedTokens = new HashSet<>();

    public static void blacklist(String token) {
        blacklistedTokens.add(token);
    }

    public static boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}