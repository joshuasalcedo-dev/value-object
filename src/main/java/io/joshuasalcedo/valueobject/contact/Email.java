package io.joshuasalcedo.valueobject.contact;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing an email address.
 * Performs basic validation on structure but does not verify deliverability.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Email(String value) implements ValueObject, Comparable<Email> {

    // Basic email pattern - covers most valid emails
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    /** Null object representing an absent Email. */
    public static final Email NULL = new Email("");

    public Email {
        Objects.requireNonNull(value, "Email cannot be null");
        if (!value.isEmpty()) {
            value = value.trim().toLowerCase();
            if (value.isBlank()) {
                throw new ValueObjectException("Email cannot be blank");
            }
            if (!EMAIL_PATTERN.matcher(value).matches()) {
                throw new ValueObjectException("Invalid email format: " + value);
            }
        }
    }

    
    public boolean isNull() {
        return value.isEmpty();
    }

    /**
     * Creates an Email from a string value.
     *
     * @param email the email string
     * @return a new Email instance
     */
    public static Email of(String email) {
        return new Email(email);
    }

    /**
     * Parses an email string, returning null for invalid emails instead of throwing.
     *
     * @param email the email string
     * @return the Email or null if invalid
     */
    public static @Nullable Email parse(String email) {
        try {
            return new Email(email);
        } catch (ValueObjectException e) {
            return null;
        }
    }

    /**
     * Returns the null object representation of Email.
     *
     * @return the NULL email constant
     */
    public static Email nullValue() {
        return NULL;
    }

    /**
     * Checks if a string is a valid email format.
     *
     * @param email the email string to check
     * @return true if valid format
     */
    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Returns the local part of the email (before @).
     *
     * @return the local part
     */
    public String localPart() {
        return value.substring(0, value.indexOf('@'));
    }

    /**
     * Returns the domain part of the email (after @).
     *
     * @return the domain
     */
    public String domain() {
        return value.substring(value.indexOf('@') + 1);
    }

    /**
     * Returns a masked version of the email for display (e.g., j***@example.com).
     *
     * @return the masked email
     */
    public String masked() {
        String local = localPart();
        if (local.length() <= 2) {
            return local.charAt(0) + "***@" + domain();
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain();
    }

    /**
     * Checks if this is a common personal email domain.
     *
     * @return true if personal domain (gmail, yahoo, outlook, etc.)
     */
    public boolean isPersonalDomain() {
        String d = domain().toLowerCase();
        return d.endsWith("gmail.com") || d.endsWith("yahoo.com") ||
               d.endsWith("outlook.com") || d.endsWith("hotmail.com") ||
               d.endsWith("icloud.com") || d.endsWith("aol.com");
    }

    /**
     * Checks if this appears to be a business/corporate email.
     *
     * @return true if likely a business email
     */
    public boolean isBusinessDomain() {
        return !isPersonalDomain();
    }

    @Override
    public int compareTo(Email other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
