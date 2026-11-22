package io.joshuasalcedo.valueobject.identifier;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a unique identifier.
 * Wraps UUID for type safety and provides convenient provider methods.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record UniqueId(UUID value) implements ValueObject, Comparable<UniqueId> {

    /** Null object representing an absent UniqueId (nil UUID). */
    public static final UniqueId NULL = new UniqueId(new UUID(0, 0));

    public UniqueId {
        Objects.requireNonNull(value, "UniqueId value cannot be null");
    }

    
    public boolean isNull() {
        return value.getMostSignificantBits() == 0 && value.getLeastSignificantBits() == 0;
    }

    /**
     * Creates a new random UniqueId.
     *
     * @return a new random UniqueId
     */
    public static UniqueId generate() {
        return new UniqueId(UUID.randomUUID());
    }

    /**
     * Creates a UniqueId from an existing UUID.
     *
     * @param uuid the UUID
     * @return a new UniqueId
     */
    public static UniqueId of(UUID uuid) {
        return new UniqueId(uuid);
    }

    /**
     * Creates a UniqueId from a string representation.
     *
     * @param value the UUID string
     * @return a new UniqueId
     * @throws ValueObjectException if the string is not a valid UUID
     */
    public static UniqueId of(String value) {
        Objects.requireNonNull(value, "UniqueId string cannot be null");
        try {
            return new UniqueId(UUID.fromString(value.trim()));
        } catch (IllegalArgumentException e) {
            throw new ValueObjectException("Invalid UUID format: " + value, e);
        }
    }

    /**
     * Parses a UUID string, returning null if invalid.
     *
     * @param value the UUID string
     * @return the UniqueId or null
     */
    public static @Nullable UniqueId parse(String value) {
        try {
            return of(value);
        } catch (ValueObjectException e) {
            return null;
        }
    }

    /**
     * Returns the null object representation of UniqueId.
     *
     * @return the NULL unique id constant (nil UUID)
     */
    public static UniqueId nullValue() {
        return NULL;
    }

    /**
     * Checks if a string is a valid UUID format.
     *
     * @param value the string to check
     * @return true if valid UUID format
     */
    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            UUID.fromString(value.trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Creates a name-based (version 3) UUID using MD5 hashing.
     *
     * @param namespace the namespace UUID
     * @param name the name within the namespace
     * @return a new UniqueId
     */
    public static UniqueId fromName(UUID namespace, String name) {
        return new UniqueId(UUID.nameUUIDFromBytes(
            (namespace.toString() + name).getBytes()
        ));
    }

    /**
     * Returns the string representation of the UUID.
     *
     * @return the UUID string in standard format
     */
    public String asString() {
        return value.toString();
    }

    /**
     * Returns the UUID as a compact string (no dashes).
     *
     * @return the compact UUID string
     */
    public String compact() {
        return value.toString().replace("-", "");
    }

    /**
     * Returns the most significant bits.
     *
     * @return the most significant 64 bits
     */
    public long mostSignificantBits() {
        return value.getMostSignificantBits();
    }

    /**
     * Returns the least significant bits.
     *
     * @return the least significant 64 bits
     */
    public long leastSignificantBits() {
        return value.getLeastSignificantBits();
    }

    /**
     * Returns the UUID version.
     *
     * @return the version
     */
    public int version() {
        return value.version();
    }

    /**
     * Returns the UUID variant.
     *
     * @return the variant
     */
    public int variant() {
        return value.variant();
    }

    @Override
    public int compareTo(UniqueId other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
