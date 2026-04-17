package com.parking.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Simple SHA-256 password hashing utility.
 * For production use, replace with BCrypt (add bcrypt dependency).
 */
public class PasswordUtil {

    private PasswordUtil() {}   // utility class — no instances

    /**
     * Returns the SHA-256 hex digest of the given plain-text password.
     * FIX: throws RuntimeException instead of silently returning null,
     * which would have caused all login/register checks to fail silently.
     */
    public static String hashPassword(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the JVM spec — this should never happen
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}