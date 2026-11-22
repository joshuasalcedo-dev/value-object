package io.joshuasalcedo.valueobject.identity;

import io.joshuasalcedo.valueobject.ValueObject;


import java.util.Arrays;
import java.util.Optional;

public enum Gender implements ValueObject {

    MALE("M", "Male"),
    FEMALE("F", "Female"),
    NON_BINARY("NB", "Non-Binary"),
    PREFER_NOT_TO_SAY("N", "Prefer not to say"),
    OTHER("O", "Other"),

    UNKNOWN("U", "Unknown");

    /**
     * @return whether this is {@link #UNKNOWN}
     */
    public boolean isNull() {
        return this == UNKNOWN;
    }

    private final String code;
    private final String displayName;

    Gender(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String code() {
        return code;
    }

    public String displayName() {
        return displayName;
    }

    public static Optional<Gender> fromCode(String code) {
        return Arrays.stream(values())
                .filter(gender -> gender.code.equalsIgnoreCase(code))
                .findFirst();
    }

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
     * Compares genders by display name alphabetically.
     *
     * @param other the Gender to compare to
     * @return negative if this comes before, zero if equal, positive if after
     */
    public int compareByName(Gender other) {
        return this.displayName.compareToIgnoreCase(other.displayName);
    }

    /**
     * Returns the null object representation of Gender.
     *
     * @return the UNKNOWN gender constant
     */
    public static Gender nullValue() {
        return UNKNOWN;
    }
}
