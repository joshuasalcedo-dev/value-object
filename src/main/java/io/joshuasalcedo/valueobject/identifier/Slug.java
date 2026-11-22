package io.joshuasalcedo.valueobject.identifier;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;

import java.text.Normalizer;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a URL-friendly slug.
 * Slugs are lowercase, use hyphens for spaces, and contain only alphanumeric characters.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Slug(String value) implements ValueObject, Comparable<Slug> {

    // Valid slug pattern: lowercase letters, numbers, and hyphens
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    // Pattern to match non-alphanumeric characters for conversion
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");

    // Maximum slug length
    private static final int MAX_LENGTH = 100;

    /** Null object representing an absent Slug. */
    public static final Slug NULL = new Slug("");

    public Slug {
        Objects.requireNonNull(value, "Slug cannot be null");
        if (!value.isEmpty()) {
            value = value.trim().toLowerCase();

            if (value.isBlank()) {
                throw new ValueObjectException("Slug cannot be blank");
            }
            if (value.length() > MAX_LENGTH) {
                throw new ValueObjectException("Slug cannot exceed " + MAX_LENGTH + " characters");
            }
            if (!SLUG_PATTERN.matcher(value).matches()) {
                throw new ValueObjectException("Invalid slug format: " + value +
                    ". Slugs must contain only lowercase letters, numbers, and hyphens.");
            }
        }
    }

    
    public boolean isNull() {
        return value.isEmpty();
    }

    /**
     * Creates a Slug from an already-valid slug string.
     *
     * @param slug the slug string
     * @return a new Slug
     */
    public static Slug of(String slug) {
        return new Slug(slug);
    }

    /**
     * Returns the null object representation of Slug.
     *
     * @return the NULL slug constant
     */
    public static Slug nullValue() {
        return NULL;
    }

    /**
     * Creates a Slug from any string by converting it to slug format.
     * Converts spaces to hyphens, removes special characters, normalizes unicode.
     *
     * @param text the text to convert
     * @return a new Slug
     */
    public static Slug fromText(String text) {
        Objects.requireNonNull(text, "Text cannot be null");

        // Normalize unicode characters (é -> e, ñ -> n, etc.)
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Convert to lowercase
        String lower = normalized.toLowerCase();

        // Replace non-alphanumeric with hyphens
        String slugified = NON_ALPHANUMERIC.matcher(lower).replaceAll("-");

        // Remove leading/trailing hyphens
        slugified = slugified.replaceAll("^-+|-+$", "");

        // Collapse multiple hyphens
        slugified = slugified.replaceAll("-{2,}", "-");

        // Truncate if too long (at word boundary if possible)
        if (slugified.length() > MAX_LENGTH) {
            slugified = slugified.substring(0, MAX_LENGTH);
            // Remove trailing hyphen if we cut mid-word
            slugified = slugified.replaceAll("-+$", "");
        }

        if (slugified.isBlank()) {
            throw new ValueObjectException("Cannot create slug from text: " + text);
        }

        return new Slug(slugified);
    }

    /**
     * Checks if a string is a valid slug format.
     *
     * @param value the string to check
     * @return true if valid slug format
     */
    public static boolean isValid(String value) {
        if (value == null || value.isBlank() || value.length() > MAX_LENGTH) {
            return false;
        }
        return SLUG_PATTERN.matcher(value.toLowerCase().trim()).matches();
    }

    /**
     * Creates a unique slug by appending a number suffix.
     *
     * @param suffix the number to append
     * @return a new Slug with the suffix
     */
    public Slug withSuffix(int suffix) {
        return new Slug(value + "-" + suffix);
    }

    /**
     * Creates a unique slug by appending a string suffix.
     *
     * @param suffix the suffix to append
     * @return a new Slug with the suffix
     */
    public Slug withSuffix(String suffix) {
        String normalizedSuffix = suffix.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (normalizedSuffix.isBlank()) {
            return this;
        }
        return new Slug(value + "-" + normalizedSuffix);
    }

    /**
     * Returns the slug as a relative URL path.
     *
     * @return the path starting with /
     */
    public String toPath() {
        return "/" + value;
    }

    /**
     * Returns the number of segments (hyphen-separated parts).
     *
     * @return the segment count
     */
    public int segmentCount() {
        return value.split("-").length;
    }

    /**
     * Returns the slug converted to camelCase.
     *
     * @return the camelCase string
     */
    public String toCamelCase() {
        String[] parts = value.split("-");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                sb.append(Character.toUpperCase(parts[i].charAt(0)));
                if (parts[i].length() > 1) {
                    sb.append(parts[i].substring(1));
                }
            }
        }
        return sb.toString();
    }

    /**
     * Returns the slug converted to PascalCase.
     *
     * @return the PascalCase string
     */
    public String toPascalCase() {
        String[] parts = value.split("-");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    sb.append(part.substring(1));
                }
            }
        }
        return sb.toString();
    }

    /**
     * Returns the slug converted to snake_case.
     *
     * @return the snake_case string
     */
    public String toSnakeCase() {
        return value.replace("-", "_");
    }

    @Override
    public int compareTo(Slug other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
