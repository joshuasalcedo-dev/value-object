package io.joshuasalcedo.valueobject.security;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Value object representing a hashed password.
 * This stores only the hash - never the plain text password.
 * Use PasswordHasherPort to create hashes and verify passwords.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record HashedPassword(
         String hash) implements ValueObject {

    // Common hash format prefixes
    private static final String BCRYPT_PREFIX = "$2";
    private static final String ARGON2_PREFIX = "$argon2";
    private static final String SCRYPT_PREFIX = "$scrypt$";
    private static final String PBKDF2_PREFIX = "$pbkdf2";

    public HashedPassword {
        Objects.requireNonNull(hash, "Password hash cannot be null");
        hash = hash.trim();

        if (hash.isBlank()) {
            throw new ValueObjectException("Password hash cannot be blank");
        }
    }

    /**
     * Creates a HashedPassword from an existing hash string.
     * Note: This does NOT hash a password - the input must already be a hash.
     * Use PasswordHasherPort.hash() to create a hash from a plain password.
     *
     * @param hash the password hash
     * @return a new HashedPassword
     */
    public static HashedPassword of(String hash) {
        return new HashedPassword(hash);
    }

    /**
     * Parses a hash string, returning null if invalid.
     *
     * @param hash the hash string
     * @return the HashedPassword or null
     */
    public static @Nullable HashedPassword parse(String hash) {
        try {
            return new HashedPassword(hash);
        } catch (ValueObjectException e) {
            return null;
        }
    }

    /**
     * Checks if a string looks like a valid password hash.
     * This checks for common hash format prefixes.
     *
     * @param value the string to check
     * @return true if it appears to be a hash
     */
    public static boolean isValidHash(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String v = value.trim();
        return v.startsWith(BCRYPT_PREFIX) ||
               v.startsWith(ARGON2_PREFIX) ||
               v.startsWith(SCRYPT_PREFIX) ||
               v.startsWith(PBKDF2_PREFIX) ||
               // Generic check for hash-like strings (hex or base64)
               v.matches("^\\$[a-z0-9]+\\$.+") ||
               (v.length() >= 32 && v.matches("^[A-Za-z0-9+/=]+$"));
    }

    /**
     * Returns the algorithm identifier based on hash format.
     *
     * @return the algorithm name or "unknown"
     */
    public String algorithm() {
        if (hash.startsWith(BCRYPT_PREFIX)) {
            if (hash.startsWith("$2a$")) return "bcrypt-2a";
            if (hash.startsWith("$2b$")) return "bcrypt-2b";
            if (hash.startsWith("$2y$")) return "bcrypt-2y";
            return "bcrypt";
        }
        if (hash.startsWith(ARGON2_PREFIX)) {
            if (hash.startsWith("$argon2i$")) return "argon2i";
            if (hash.startsWith("$argon2d$")) return "argon2d";
            if (hash.startsWith("$argon2id$")) return "argon2id";
            return "argon2";
        }
        if (hash.startsWith(SCRYPT_PREFIX)) {
            return "scrypt";
        }
        if (hash.startsWith(PBKDF2_PREFIX)) {
            return "pbkdf2";
        }
        return "unknown";
    }

    /**
     * Checks if this hash uses BCrypt.
     *
     * @return true if BCrypt
     */
    public boolean isBcrypt() {
        return hash.startsWith(BCRYPT_PREFIX);
    }

    /**
     * Checks if this hash uses Argon2.
     *
     * @return true if Argon2
     */
    public boolean isArgon2() {
        return hash.startsWith(ARGON2_PREFIX);
    }

    /**
     * Checks if this hash uses SCrypt.
     *
     * @return true if SCrypt
     */
    public boolean isScrypt() {
        return hash.startsWith(SCRYPT_PREFIX);
    }

    /**
     * Checks if this hash uses PBKDF2.
     *
     * @return true if PBKDF2
     */
    public boolean isPbkdf2() {
        return hash.startsWith(PBKDF2_PREFIX);
    }

    /**
     * Returns a masked version of the hash for logging/display.
     * Shows algorithm prefix and length, hides the actual hash.
     *
     * @return the masked hash
     */
    public String masked() {
        if (hash.length() <= 10) {
            return "***";
        }
        int prefixEnd = hash.indexOf('$', 1);
        if (prefixEnd > 0) {
            prefixEnd = hash.indexOf('$', prefixEnd + 1);
            if (prefixEnd > 0) {
                return hash.substring(0, prefixEnd + 1) + "...(" + (hash.length() - prefixEnd - 1) + " chars)";
            }
        }
        return algorithm() + ":...(" + hash.length() + " chars)";
    }

    /**
     * Returns the length of the hash string.
     *
     * @return the hash length
     */
    public int length() {
        return hash.length();
    }

    /**
     * IMPORTANT: Password hashes should NEVER be compared directly.
     * Use PasswordHasherPort.verify() instead.
     * This method exists for consistency but comparing hashes is not recommended.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashedPassword(String hash1))) return false;
        return hash.equals(hash1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    /**
     * Returns a safe representation (not the actual hash).
     */
    @Override

    public @NonNull String toString() {
        return "HashedPassword[" + algorithm() + "]";
    }
}
