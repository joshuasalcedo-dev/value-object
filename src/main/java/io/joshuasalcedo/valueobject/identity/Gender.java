package io.joshuasalcedo.valueobject.identity;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;
import io.joshuasalcedo.valueobject.ValueObject;


import java.util.Arrays;
import java.util.Optional;

/**
 * A person's gender as recorded on a form, with a short code ({@code M}, {@code F}, {@code NB}, ...) and a
 * display name.
 */
public enum Gender implements ValueObject {

    /** Male ({@code M}). */
    MALE("M", "Male"),
    /** Female ({@code F}). */
    FEMALE("F", "Female"),
    /** Non-Binary ({@code NB}). */
    NON_BINARY("NB", "Non-Binary"),
    /** Prefer not to say ({@code N}). */
    PREFER_NOT_TO_SAY("N", "Prefer not to say"),
    /** Other ({@code O}). */
    OTHER("O", "Other"),

    /** Not known, or not one of the others. */
    UNKNOWN("U", "Unknown");

    /** {@return whether this is {@link #UNKNOWN}} */
    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    private final String code;
    private final String displayName;

    Gender(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    /** {@return its short code, e.g. {@code "F"} or {@code "NB"}} */
    public String code() {
        return code;
    }

    /** {@return its display name, e.g. {@code "Female"}} */
    public String displayName() {
        return displayName;
    }

    /**
     * Finds a gender by its code, ignoring case.
     *
     * @param code e.g. {@code "f"}
     * @return the gender, or empty when none has that code
     */
    public static Optional<Gender> fromCode(String code) {
        return Arrays.stream(values())
                .filter(gender -> gender.code.equalsIgnoreCase(code))
                .findFirst();
    }

    /**
     * Finds a gender by its display name, ignoring case.
     *
     * @param name e.g. {@code "non-binary"}
     * @return the gender, or empty when none has that name
     */
    public static Optional<Gender> fromName(String name) {
        return Arrays.stream(values())
                .filter(gender -> gender.displayName.equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Compares genders by display name alphabetically, ignoring case.
     *
     * @param other the Gender to compare to
     * @return negative if this comes before, zero if equal, positive if after
     */
    public int compareByName(Gender other) {
        return this.displayName.compareToIgnoreCase(other.displayName);
    }

    /**
     * Parses a gender from its code or display name, ignoring case and surrounding whitespace.
     *
     * @param text the code or name, e.g. {@code "F"}, {@code "female"}
     * @return the gender
     * @throws ValueObjectException if it is {@code null} or names no gender other than {@link #UNKNOWN}
     */
    public static Gender parse(String text) {
        if (text == null) throw new ValueObjectException("A Gender is required");
        String t = text.strip();
        return fromCode(t).or(() -> fromName(t)).filter(g -> g != UNKNOWN)
                .orElseThrow(() -> new ValueObjectException("Not a gender: '" + text + "'"));
    }

    /**
     * Parses a gender as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as a {@link Gender}, or empty when it is not one; never throws
     */
    public static Optional<Gender> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text names a gender, as {@link #parse(String)} reads it.
     *
     * @param text any text
     * @return whether it is a {@link Gender}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
