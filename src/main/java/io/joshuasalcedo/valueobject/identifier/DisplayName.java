package io.joshuasalcedo.valueobject.identifier;

import io.joshuasalcedo.valueobject.core.ValueObject;
import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * A name to show for someone or something: trimmed, inner spaces collapsed, 1 to 64 characters, no control
 * characters. Any script, any punctuation; for a person's parts (first, middle, last), see
 * {@link io.joshuasalcedo.valueobject.contact.FullName}.
 *
 * @param value the name, e.g. {@code "Andrea B."}
 * @author JoshuaSalcedo
 */
public record DisplayName(String value) implements ValueObject, Comparable<DisplayName> {

    /** The most characters a display name has. */
    public static final int MAX_LENGTH = 64;

    /**
     * Creates a display name: stripped, each run of whitespace (tabs and line breaks too) made one space.
     *
     * @throws ValueObjectException if it is {@code null}, blank, longer than {@value #MAX_LENGTH}, or has
     *                              control characters
     */
    public DisplayName {
        if (value == null || value.isBlank()) throw new ValueObjectException("A display name is required");
        value = value.strip().replaceAll("\\s+", " ");
        if (value.length() > MAX_LENGTH)
            throw new ValueObjectException("A display name is at most " + MAX_LENGTH + " characters");
        if (value.chars().anyMatch(Character::isISOControl))
            throw new ValueObjectException("A display name cannot hold control characters");
    }

    /**
     * Creates a display name; the same as the constructor.
     *
     * @param value the name
     * @return the display name
     * @throws ValueObjectException if it is not a display name
     */
    public static DisplayName of(String value) {
        return new DisplayName(value);
    }

    /**
     * Parses a display name; the same as the constructor.
     *
     * @param text the name
     * @return the display name
     * @throws ValueObjectException if it is not a display name
     */
    public static DisplayName parse(String text) {
        return new DisplayName(text);
    }

    /**
     * Parses a display name as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as a display name, or empty when it is not one
     */
    public static Optional<DisplayName> tryParse(@Nullable String text) {
        try {
            return text == null ? Optional.empty() : Optional.of(new DisplayName(text));
        } catch (ValueObjectException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text is a display name, as {@link #parse(String)} reads it.
     *
     * @param text any text
     * @return whether it is a display name
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

    @Override
    public int compareTo(DisplayName other) {
        return value.compareToIgnoreCase(other.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
