package com.harness.workshop.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Hashing {
    private Hashing() {}

    /** Returns a bucket in [0, 100). Stable for (userId, flagKey). */
    public static double bucket(String userId, String flagKey) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((userId + ":" + flagKey).getBytes(StandardCharsets.UTF_8));
            // Take first 8 bytes as unsigned long, normalize to [0,1)
            long val = 0;
            for (int i = 0; i < 8; i++) val = (val << 8) | (digest[i] & 0xffL);
            double positive = (val >>> 1) / (double)(Long.MAX_VALUE); // [0,1)
            return positive * 100.0;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
