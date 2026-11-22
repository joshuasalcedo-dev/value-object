package io.joshuasalcedo.valueobject.identifier;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A login or handle: 3 to 32 letters, digits, dots, underscores and hyphens, starting with a letter or digit, not
 * ending with a separator and with no two separators in a row. Stored lower case, so {@code "Jane.Doe"} and
 * {@code "jane.doe"} are the same username. A leading {@code @} is dropped.
 *
 * @param value the username, lower case
 * @author JoshuaSalcedo
 */
public record Username(String value) implements ValueObject, Comparable<Username> {

    private static final Pattern FORM = Pattern.compile("^[a-z0-9](?:[a-z0-9]|[._-](?=[a-z0-9])){2,31}$");

    /**
     * Creates a username: stripped, a leading {@code @} dropped, lower-cased.
     *
     * @throws ValueObjectException if it is {@code null}, blank, or not in the form above
     */
    public Username {
        if (value == null || value.isBlank()) throw new ValueObjectException("A username is required");
        value = value.strip().replaceFirst("^@", "").toLowerCase(Locale.ROOT);
        if (!FORM.matcher(value).matches()) {
            throw new ValueObjectException("A username is 3-32 letters, digits, '.', '_' or '-', starting with a letter "
                    + "or digit, with no two separators in a row: " + value);
        }
    }

    /**
     * Creates a username; the same as the constructor.
     *
     * @param value the username
     * @return the username, lower case
     * @throws ValueObjectException if it is not a username
     */
    public static Username of(String value) {
        return new Username(value);
    }

    /**
     * Parses a username; the same as the constructor.
     *
     * @param text the username, with or without a leading {@code @}
     * @return the username, lower case
     * @throws ValueObjectException if it is not a username
     */
    public static Username parse(String text) {
        return new Username(text);
    }

    /**
     * Parses a username as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as a username, or empty when it is not one
     */
    public static Optional<Username> tryParse(@Nullable String text) {
        try {
            return text == null ? Optional.empty() : Optional.of(new Username(text));
        } catch (ValueObjectException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text is a username, as {@link #parse(String)} reads it.
     *
     * @param text any text
     * @return whether it is a username
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

    @Override
    public int compareTo(Username other) {
        return value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
