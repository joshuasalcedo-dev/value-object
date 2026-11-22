package io.joshuasalcedo.valueobject.security;

import java.util.Optional;
import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A password hash in its usual text form, e.g. a bcrypt {@code $2b$12$...} or Argon2 {@code $argon2id$...}
 * string. It holds the hash only, never the password; making hashes and checking passwords against them is left
 * to a password hashing library. Its {@link #toString()} shows only the algorithm.
 *
 * <p>Only text in a recognised hash format is accepted, so a plain password is rejected:
 * <ul>
 *     <li>bcrypt: {@code $2$}, {@code $2a$}, {@code $2b$}, {@code $2x$} or {@code $2y$}, a two-digit cost,
 *         {@code $} and 53 characters of bcrypt Base64;</li>
 *     <li>Argon2 in PHC form: {@code $argon2id$}, {@code $argon2i$} or {@code $argon2d$}, an optional
 *         {@code v=19$}, {@code m=...,t=...,p=...$}, then the Base64 salt and hash separated by {@code $};</li>
 *     <li>scrypt: {@code $scrypt$} followed by two or more non-empty {@code $}-separated fields;</li>
 *     <li>PBKDF2: {@code $pbkdf2$}, {@code $pbkdf2-sha1$}, {@code $pbkdf2-sha256$} or {@code $pbkdf2-sha512$}
 *         followed by two or more non-empty {@code $}-separated fields;</li>
 *     <li>SHA-crypt: {@code $5$} (SHA-256, 43-character hash) or {@code $6$} (SHA-512, 86-character hash), an
 *         optional {@code rounds=N$}, a salt of up to 16 characters and {@code $}.</li>
 * </ul>
 *
 * @param hash the hash text, trimmed
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

    // Full formats this type accepts
    private static final Pattern BCRYPT = Pattern.compile("\\$2[abxy]?\\$\\d{2}\\$[./A-Za-z0-9]{53}");
    private static final Pattern ARGON2 = Pattern.compile(
            "\\$argon2(?:id|i|d)\\$(?:v=\\d+\\$)?m=\\d+,t=\\d+,p=\\d+\\$[A-Za-z0-9+/]+={0,2}\\$[A-Za-z0-9+/]+={0,2}");
    private static final Pattern SCRYPT = Pattern.compile("\\$scrypt\\$[^\\s$]+(?:\\$[^\\s$]+)+");
    private static final Pattern PBKDF2 = Pattern.compile("\\$pbkdf2(?:-sha(?:1|256|512))?\\$[^\\s$]+(?:\\$[^\\s$]+)+");
    private static final Pattern SHA_CRYPT = Pattern.compile(
            "\\$5\\$(?:rounds=\\d+\\$)?[./A-Za-z0-9]{1,16}\\$[./A-Za-z0-9]{43}"
                    + "|\\$6\\$(?:rounds=\\d+\\$)?[./A-Za-z0-9]{1,16}\\$[./A-Za-z0-9]{86}");

    /**
     * Wraps a hash, trimming it.
     *
     * @throws ValueObjectException if {@code hash} is {@code null}, blank, or not in one of the recognised hash
     *                              formats listed above (a plain password, for example)
     */
    public HashedPassword {
        if (hash == null) {
            throw new ValueObjectException("Password hash cannot be null");
        }
        hash = hash.trim();

        if (hash.isBlank()) {
            throw new ValueObjectException("Password hash cannot be blank");
        }
        if (!isRecognised(hash)) {
            // never echo the text: it may be a plain password
            throw new ValueObjectException("Not a recognised password hash format (bcrypt, Argon2, scrypt, PBKDF2 or SHA-crypt)");
        }
    }

    private static boolean isRecognised(String v) {
        return BCRYPT.matcher(v).matches()
                || ARGON2.matcher(v).matches()
                || SCRYPT.matcher(v).matches()
                || PBKDF2.matcher(v).matches()
                || SHA_CRYPT.matcher(v).matches();
    }

    /**
     * Wraps an existing hash; the same as the constructor. This does <b>not</b> hash a password: the text must
     * already be a hash.
     *
     * @param hash the password hash
     * @return the hashed password
     * @throws ValueObjectException if it is {@code null}, blank, or not a recognised hash format
     */
    public static HashedPassword of(String hash) {
        return new HashedPassword(hash);
    }


    /**
     * Wraps an existing hash, trimming it; the same as the constructor.
     *
     * @param text the text
     * @return the hashed password
     * @throws ValueObjectException if it is {@code null}, blank, or not a recognised hash format
     */
    public static HashedPassword parse(String text) {
        if (text == null) throw new ValueObjectException("A HashedPassword is required");
        return of(text);
    }

    /**
     * Checks whether a text, once trimmed, is in one of the recognised hash formats listed in the class
     * description; the same as {@link #isValid(String)}.
     *
     * @param value the string to check; may be {@code null}
     * @return whether it is a recognised hash
     */
    public static boolean isValidHash(@Nullable String value) {
        return value != null && isRecognised(value.trim());
    }

    /**
     * Returns the hashing algorithm, read from the hash's prefix.
     *
     * @return {@code "bcrypt-2a"}, {@code "bcrypt-2b"}, {@code "bcrypt-2y"}, {@code "bcrypt"},
     *         {@code "argon2i"}, {@code "argon2d"}, {@code "argon2id"}, {@code "argon2"}, {@code "scrypt"},
     *         {@code "pbkdf2"}, {@code "sha256-crypt"}, {@code "sha512-crypt"}, or {@code "unknown"}
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
        if (hash.startsWith("$5$")) {
            return "sha256-crypt";
        }
        if (hash.startsWith("$6$")) {
            return "sha512-crypt";
        }
        return "unknown";
    }

    /**
     * Checks whether this is a bcrypt hash: it starts with {@code $2}.
     *
     * @return whether it is bcrypt
     */
    public boolean isBcrypt() {
        return hash.startsWith(BCRYPT_PREFIX);
    }

    /**
     * Checks whether this is an Argon2 hash: it starts with {@code $argon2}.
     *
     * @return whether it is Argon2
     */
    public boolean isArgon2() {
        return hash.startsWith(ARGON2_PREFIX);
    }

    /**
     * Checks whether this is an scrypt hash: it starts with {@code $scrypt$}.
     *
     * @return whether it is scrypt
     */
    public boolean isScrypt() {
        return hash.startsWith(SCRYPT_PREFIX);
    }

    /**
     * Checks whether this is a PBKDF2 hash: it starts with {@code $pbkdf2}.
     *
     * @return whether it is PBKDF2
     */
    public boolean isPbkdf2() {
        return hash.startsWith(PBKDF2_PREFIX);
    }

    /**
     * Returns the hash masked for logs: its first two {@code $}-separated fields and the number of characters
     * after them ({@code "$2b$...(56 chars)"}), or the algorithm and the length when it has no such fields; a
     * hash of 10 characters or fewer is {@code "***"}.
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
     * Compares the hash text. Do not use this to check a password: verify the password against the hash with a
     * password hashing library instead.
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

    /**
     * Wraps a hash as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as a {@link HashedPassword}, or empty when it is not a recognised hash; never throws
     */
    public static Optional<HashedPassword> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether {@link #parse(String)} accepts a text: whether it is in a recognised hash format.
     *
     * @param text any text
     * @return whether it is a {@link HashedPassword}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
