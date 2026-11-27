package io.joshuasalcedo.valueobject.identifier;

import java.util.Locale;
import org.jspecify.annotations.Nullable;
import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import io.joshuasalcedo.valueobject.core.ValueObjectException;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * A URL-friendly slug: at most 100 lower-case ASCII letters and digits in words joined by single hyphens, e.g.
 * {@code "billing-report-2026"}. Use {@link #fromText(String)} to make one from any text.
 *
 * @param value the slug, trimmed and lower case
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


    /**
     * Creates a slug from text already in slug form, trimming and lower-casing it.
     *
     * @throws ValueObjectException if it is {@code null}, blank, longer than 100 characters, or not in slug form
     */
    public Slug {
        if (value == null) {
            throw new ValueObjectException("Slug cannot be null");
        }
        {
            value = value.trim().toLowerCase(Locale.ROOT);

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

    

    /**
     * Creates a slug from text already in slug form; the same as the constructor.
     *
     * @param slug the slug, e.g. {@code "billing-report"}
     * @return the slug
     * @throws ValueObjectException if it is not in slug form
     */
    public static Slug of(String slug) {
        return new Slug(slug);
    }


    /**
     * Makes a slug from any text: accents are removed ({@code "é"} to {@code "e"}), it is lower-cased, each run
     * of other characters becomes one hyphen, hyphens at the ends are dropped, and it is cut to 100 characters.
     *
     * @param text the text to convert, e.g. {@code "Café Menu: 2026!"}
     * @return the slug, e.g. {@code "cafe-menu-2026"}
     * @throws ValueObjectException if {@code text} is {@code null} or no letters or digits are left
     */
    public static Slug fromText(String text) {
        if (text == null) {
            throw new ValueObjectException("Text cannot be null");
        }

        // Normalize unicode characters (é -> e, ñ -> n, etc.)
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Convert to lowercase
        String lower = normalized.toLowerCase(Locale.ROOT);

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
     * Checks whether a text, lower-cased and trimmed, is in slug form and at most 100 characters.
     *
     * @param value the string to check; may be {@code null}
     * @return whether it is a valid slug
     */
    public static boolean isValid(@Nullable String value) {
        return tryParse(value).isPresent();
    }

    /**
     * Returns a slug with a number appended after a hyphen, e.g. {@code "report-2"}, to tell apart slugs that
     * would otherwise be the same.
     *
     * @param suffix the number to append; not negative
     * @return the new slug
     * @throws ValueObjectException if the number is negative or the result is longer than 100 characters
     */
    public Slug withSuffix(int suffix) {
        return new Slug(value + "-" + suffix);
    }

    /**
     * Returns a slug with a text appended after a hyphen. The text is lower-cased and everything but ASCII
     * letters and digits is removed from it.
     *
     * @param suffix the suffix to append, e.g. {@code "Draft"}
     * @return the new slug, or this slug when nothing is left of the suffix
     * @throws ValueObjectException if {@code suffix} is {@code null} or the result is longer than 100 characters
     */
    public Slug withSuffix(String suffix) {
        if (suffix == null) {
            throw new ValueObjectException("A suffix is required");
        }
        String normalizedSuffix = suffix.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        if (normalizedSuffix.isBlank()) {
            return this;
        }
        return new Slug(value + "-" + normalizedSuffix);
    }

    /**
     * Returns the slug as a URL path, with a {@code /} in front.
     *
     * @return the path, e.g. {@code "/billing-report"}
     */
    public String toPath() {
        return "/" + value;
    }

    /**
     * Returns the number of words (hyphen-separated parts).
     *
     * @return the segment count
     */
    public int segmentCount() {
        return value.split("-").length;
    }

    /**
     * Returns the slug in camelCase.
     *
     * @return the camelCase string, e.g. {@code "billingReport"}
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
     * Returns the slug in PascalCase.
     *
     * @return the PascalCase string, e.g. {@code "BillingReport"}
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
     * Returns the slug in snake_case, its hyphens made underscores.
     *
     * @return the snake_case string, e.g. {@code "billing_report"}
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

    /**
     * Parses text already in slug form, trimming and lower-casing it.
     *
     * @param text the slug
     * @return the slug
     * @throws ValueObjectException if it is {@code null} or not a slug
     */
    public static Slug parse(String text) {
        if (text == null) throw new ValueObjectException("A Slug is required");
        return of(text);
    }

    /**
     * Parses a slug as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as a {@link Slug}, or empty when it is not one; never throws
     */
    public static Optional<Slug> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

}
