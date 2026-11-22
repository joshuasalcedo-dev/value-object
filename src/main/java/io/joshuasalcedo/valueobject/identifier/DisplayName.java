package io.joshuasalcedo.valueobject.identifier;

import io.joshuasalcedo.valueobject.ValueObject;


/**
 *
 *
 * @author Joshua Vasquez Salcedo
 * @since 2026-02-11
 */
public record DisplayName(String value) implements ValueObject {

    /** Null object representing an absent DisplayName. */
    public static final DisplayName NULL = new DisplayName("");

    
    public boolean isNull() {
        return value.isEmpty();
    }

    public static DisplayName of(String value ) {
        return new DisplayName(value);
    }

    /**
     * Returns the null object representation of DisplayName.
     *
     * @return the NULL display name constant
     */
    public static DisplayName nullValue() {
        return NULL;
    }
}
