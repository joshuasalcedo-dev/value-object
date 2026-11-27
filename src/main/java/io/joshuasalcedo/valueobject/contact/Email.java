package io.joshuasalcedo.valueobject.contact;

import java.util.Locale;
import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

/**
 * An email address, trimmed and lower-cased. Checks the shape only ({@code local@domain.tld}, with letters,
 * digits and {@code . _ % + -} in the local part and a top-level domain of two or more letters); it does not
 * check that the address can receive mail.
 *
 * @param value the address, trimmed and lower case
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Email(String value) implements ValueObject, Comparable<Email> {

    // Basic email pattern - covers most valid emails
    private static final List<String> PERSONAL_DOMAINS =
            List.of("gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "icloud.com", "aol.com");

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );


    /**
     * Creates an email address, trimming and lower-casing it.
     *
     * @throws ValueObjectException if {@code value} is {@code null}, blank or not shaped like an email address
     */
    public Email {
        if (value == null) {
            throw new ValueObjectException("Email cannot be null");
        }
        {
            value = value.trim().toLowerCase(Locale.ROOT);
            if (value.isBlank()) {
                throw new ValueObjectException("Email cannot be blank");
            }
            if (!EMAIL_PATTERN.matcher(value).matches()) {
                throw new ValueObjectException("Invalid email format: " + value);
            }
        }
    }

    

    /**
     * Creates an email address; the same as the constructor.
     *
     * @param email the address, e.g. {@code "Andrea@Example.com"}
     * @return the email, lower case
     * @throws ValueObjectException if it is {@code null}, blank or not shaped like an email address
     */
    public static Email of(String email) {
        return new Email(email);
    }


    /**
     * Parses an email address, trimming and lower-casing it.
     *
     * @param text the text
     * @return the email
     * @throws ValueObjectException if it is {@code null}, blank or not shaped like an email address
     */
    public static Email parse(String text) {
        if (text == null) throw new ValueObjectException("A Email is required");
        return of(text);
    }


    /**
     * Checks whether a text, once trimmed, is shaped like an email address.
     *
     * @param email the text to check; may be {@code null}
     * @return whether it is a valid email address
     */
    public static boolean isValid(@Nullable String email) {
        return tryParse(email).isPresent();
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
     * Returns the address masked for display: the first and last character of the local part are kept
     * ({@code "j***n@example.com"}); a local part of one or two characters keeps only the first
     * ({@code "j***@example.com"}).
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
     * Checks whether the domain is a common free mail domain, or a subdomain of one: {@code gmail.com},
     * {@code yahoo.com}, {@code outlook.com}, {@code hotmail.com}, {@code icloud.com} or {@code aol.com}. A domain
     * that only ends with the same letters, such as {@code notgmail.com}, does not count.
     *
     * @return whether it is a personal email domain
     */
    public boolean isPersonalDomain() {
        String d = domain().toLowerCase(Locale.ROOT);
        for (String personal : PERSONAL_DOMAINS) {
            if (d.equals(personal) || d.endsWith("." + personal)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether this looks like a business email: the opposite of {@link #isPersonalDomain()}.
     *
     * @return whether it is likely a business email
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

    /**
     * Parses an email address as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as an {@link Email}, or empty when it is not one; never throws
     */
    public static Optional<Email> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

}
