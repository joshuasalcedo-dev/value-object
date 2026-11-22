package io.joshuasalcedo.valueobject;

/**
 * Marks a type as a value object: immutable, compared by its values (not identity),
 * and valid from the moment it is constructed.
 *
 * <p>A Java {@code record} that validates in its compact constructor is the natural fit.
 */
public interface ValueObject {
}
