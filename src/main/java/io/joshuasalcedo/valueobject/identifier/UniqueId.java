package io.joshuasalcedo.valueobject.identifier;

import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * A unique identifier: a typed wrapper around a {@link UUID}, made at random, from a UUID string or from a name.
 *
 * @param value the UUID
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record UniqueId(UUID value) implements ValueObject, Comparable<UniqueId> {

    private static final Pattern CANONICAL =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");


    /**
     * Wraps a UUID.
     *
     * @throws ValueObjectException if {@code value} is {@code null}
     */
    public UniqueId {
        if (value == null) {
            throw new ValueObjectException("UniqueId value cannot be null");
        }
    }

    

    /**
     * Creates a random (version 4) identifier.
     *
     * @return a new random identifier
     */
    public static UniqueId generate() {
        return new UniqueId(UUID.randomUUID());
    }

    /**
     * Wraps an existing UUID; the same as the constructor.
     *
     * @param uuid the UUID
     * @return the identifier
     * @throws ValueObjectException if {@code uuid} is {@code null}
     */
    public static UniqueId of(UUID uuid) {
        return new UniqueId(uuid);
    }

    /**
     * Creates an identifier from a UUID string, trimmed: 32 hex digits in either case, in the 8-4-4-4-12 dashed
     * form. The short forms {@link UUID#fromString(String)} also tolerates (such as {@code "1-2-3-4-5"}) are
     * rejected.
     *
     * @param value the UUID string, e.g. {@code "123e4567-e89b-12d3-a456-426614174000"}
     * @return the identifier
     * @throws ValueObjectException if the string is {@code null} or not a valid UUID
     */
    public static UniqueId of(String value) {
        if (value == null) {
            throw new ValueObjectException("UniqueId string cannot be null");
        }
        String t = value.trim();
        if (!CANONICAL.matcher(t).matches()) {
            throw new ValueObjectException("Invalid UUID format: " + value);
        }
        try {
            return new UniqueId(UUID.fromString(t));
        } catch (IllegalArgumentException e) {
            throw new ValueObjectException("Invalid UUID format: " + value, e);
        }
    }


    /**
     * Parses an identifier from a UUID string, trimmed.
     *
     * @param text the text
     * @return the identifier
     * @throws ValueObjectException if it is {@code null} or not a valid UUID
     */
    public static UniqueId parse(String text) {
        if (text == null) throw new ValueObjectException("A UniqueId is required");
        return of(text);
    }


    /**
     * Checks whether a text, once trimmed, is a UUID in the dashed form {@link #of(String)} reads.
     *
     * @param value the string to check; may be {@code null}
     * @return whether it is a valid UUID
     */
    public static boolean isValid(@Nullable String value) {
        return tryParse(value).isPresent();
    }

    /**
     * Creates a name-based (version 3, MD5) identifier as RFC 4122 (section 4.3) defines it: the MD5 hash of the
     * namespace's 16 bytes followed by the name in UTF-8, with the version and variant bits set. The same
     * namespace and name always give the same identifier, and it matches other RFC 4122 implementations: the
     * DNS namespace ({@code 6ba7b810-9dad-11d1-80b4-00c04fd430c8}) and {@code "www.example.com"} give
     * {@code 5df41881-3aed-3515-88a7-2f4a814cf09e}.
     *
     * @param namespace the namespace UUID
     * @param name the name within the namespace
     * @return the identifier
     * @throws ValueObjectException if either is {@code null}
     */
    public static UniqueId fromName(UUID namespace, String name) {
        if (namespace == null || name == null) {
            throw new ValueObjectException("A namespace and a name are required");
        }
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(16 + nameBytes.length);
        buffer.putLong(namespace.getMostSignificantBits());
        buffer.putLong(namespace.getLeastSignificantBits());
        buffer.put(nameBytes);
        // nameUUIDFromBytes takes the MD5 of the bytes and sets version 3 and the IETF variant
        return new UniqueId(UUID.nameUUIDFromBytes(buffer.array()));
    }

    /**
     * Returns the UUID as text, lower case with dashes.
     *
     * @return the UUID string in standard format
     */
    public String asString() {
        return value.toString();
    }

    /**
     * Returns the UUID as 32 lower-case hex digits, without dashes.
     *
     * @return the compact UUID string
     */
    public String compact() {
        return value.toString().replace("-", "");
    }

    /**
     * Returns the most significant 64 bits of the UUID.
     *
     * @return the most significant 64 bits
     */
    public long mostSignificantBits() {
        return value.getMostSignificantBits();
    }

    /**
     * Returns the least significant 64 bits of the UUID.
     *
     * @return the least significant 64 bits
     */
    public long leastSignificantBits() {
        return value.getLeastSignificantBits();
    }

    /**
     * Returns the UUID version, as {@link UUID#version()}.
     *
     * @return the version: 4 for random, 3 for name-based
     */
    public int version() {
        return value.version();
    }

    /**
     * Returns the UUID variant, as {@link UUID#variant()}.
     *
     * @return the variant: 2 for the IETF (RFC 4122) layout
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

    /**
     * Parses an identifier as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as a {@link UniqueId}, or empty when it is not one; never throws
     */
    public static Optional<UniqueId> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

}
