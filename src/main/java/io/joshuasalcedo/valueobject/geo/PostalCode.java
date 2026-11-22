package io.joshuasalcedo.valueobject.geo;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a postal/ZIP code.
 * Validates format based on country-specific rules.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record PostalCode(
        String value,
        Country country
) implements ValueObject, Comparable<PostalCode> {

    // Country-specific postal code patterns
    private static final Map<Country, Pattern> PATTERNS = Map.ofEntries(
        Map.entry(Country.UNITED_STATES, Pattern.compile("^\\d{5}(-\\d{4})?$")),
        Map.entry(Country.CANADA, Pattern.compile("^[A-Za-z]\\d[A-Za-z][ -]?\\d[A-Za-z]\\d$")),
        Map.entry(Country.UNITED_KINGDOM, Pattern.compile("^[A-Za-z]{1,2}\\d[A-Za-z\\d]?[ ]?\\d[A-Za-z]{2}$")),
        Map.entry(Country.GERMANY, Pattern.compile("^\\d{5}$")),
        Map.entry(Country.FRANCE, Pattern.compile("^\\d{5}$")),
        Map.entry(Country.AUSTRALIA, Pattern.compile("^\\d{4}$")),
        Map.entry(Country.JAPAN, Pattern.compile("^\\d{3}-?\\d{4}$")),
        Map.entry(Country.BRAZIL, Pattern.compile("^\\d{5}-?\\d{3}$")),
        Map.entry(Country.INDIA, Pattern.compile("^\\d{6}$")),
        Map.entry(Country.CHINA, Pattern.compile("^\\d{6}$")),
        Map.entry(Country.ITALY, Pattern.compile("^\\d{5}$")),
        Map.entry(Country.SPAIN, Pattern.compile("^\\d{5}$")),
        Map.entry(Country.NETHERLANDS, Pattern.compile("^\\d{4}[ ]?[A-Za-z]{2}$")),
        Map.entry(Country.SWEDEN, Pattern.compile("^\\d{3}[ ]?\\d{2}$")),
        Map.entry(Country.SWITZERLAND, Pattern.compile("^\\d{4}$")),
        Map.entry(Country.MEXICO, Pattern.compile("^\\d{5}$")),
        Map.entry(Country.SOUTH_KOREA, Pattern.compile("^\\d{5}$")),
        Map.entry(Country.RUSSIA, Pattern.compile("^\\d{6}$")),
        Map.entry(Country.POLAND, Pattern.compile("^\\d{2}-?\\d{3}$")),
        Map.entry(Country.BELGIUM, Pattern.compile("^\\d{4}$"))
    );

    // Default pattern for countries without specific validation
    private static final Pattern DEFAULT_PATTERN = Pattern.compile("^[A-Za-z0-9 -]{2,10}$");

    public PostalCode {
        Objects.requireNonNull(value, "Postal code cannot be null");
        Objects.requireNonNull(country, "Country cannot be null");

        value = value.trim().toUpperCase();

        if (value.isBlank()) {
            throw new ValueObjectException("Postal code cannot be blank");
        }

        Pattern pattern = PATTERNS.getOrDefault(country, DEFAULT_PATTERN);
        if (!pattern.matcher(value).matches()) {
            throw new ValueObjectException(
                String.format("Invalid postal code format for %s: %s",
                    country.displayName(), value)
            );
        }
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a PostalCode with country.
     *
     * @param code the postal code
     * @param country the country
     * @return a new PostalCode
     */
    public static PostalCode of(String code, Country country) {
        return new PostalCode(code, country);
    }

    /**
     * Creates a US ZIP code.
     *
     * @param zipCode the ZIP code (5 or 9 digits)
     * @return a new PostalCode
     */
    public static PostalCode us(String zipCode) {
        return new PostalCode(zipCode, Country.UNITED_STATES);
    }

    /**
     * Creates a UK postcode.
     *
     * @param postcode the UK postcode
     * @return a new PostalCode
     */
    public static PostalCode uk(String postcode) {
        return new PostalCode(postcode, Country.UNITED_KINGDOM);
    }

    /**
     * Creates a Canadian postal code.
     *
     * @param code the postal code
     * @return a new PostalCode
     */
    public static PostalCode canada(String code) {
        return new PostalCode(code, Country.CANADA);
    }

    /**
     * Parses a postal code, returning null if invalid.
     *
     * @param code the postal code
     * @param country the country
     * @return the PostalCode or null
     */
    public static @Nullable PostalCode parse(String code, Country country) {
        try {
            return new PostalCode(code, country);
        } catch (ValueObjectException e) {
            return null;
        }
    }

    /**
     * Checks if a postal code is valid for a country.
     *
     * @param code the postal code
     * @param country the country
     * @return true if valid
     */
    public static boolean isValid(String code, Country country) {
        if (code == null || code.isBlank() || country == null) {
            return false;
        }
        Pattern pattern = PATTERNS.getOrDefault(country, DEFAULT_PATTERN);
        return pattern.matcher(code.trim().toUpperCase()).matches();
    }

    // ==================== Query Methods ====================

    /**
     * Returns the normalized (formatted) postal code.
     *
     * @return the formatted code
     */
    public String normalized() {
        // Country-specific normalization
        return switch (country) {
            case UNITED_STATES -> normalizeUs();
            case CANADA -> normalizeCanada();
            case UNITED_KINGDOM -> normalizeUk();
            case JAPAN -> normalizeJapan();
            case NETHERLANDS -> normalizeNetherlands();
            default -> value;
        };
    }

    private String normalizeUs() {
        // Format as 12345 or 12345-6789
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.length() == 9) {
            return digits.substring(0, 5) + "-" + digits.substring(5);
        }
        return digits;
    }

    private String normalizeCanada() {
        // Format as A1A 1A1
        String clean = value.replaceAll("[ -]", "").toUpperCase();
        if (clean.length() == 6) {
            return clean.substring(0, 3) + " " + clean.substring(3);
        }
        return value;
    }

    private String normalizeUk() {
        // Add space before last 3 characters if not present
        String clean = value.replaceAll(" ", "").toUpperCase();
        if (clean.length() >= 5) {
            return clean.substring(0, clean.length() - 3) + " " + clean.substring(clean.length() - 3);
        }
        return value;
    }

    private String normalizeJapan() {
        // Format as 123-4567
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.length() == 7) {
            return digits.substring(0, 3) + "-" + digits.substring(3);
        }
        return value;
    }

    private String normalizeNetherlands() {
        // Format as 1234 AB
        String clean = value.replaceAll(" ", "").toUpperCase();
        if (clean.length() == 6) {
            return clean.substring(0, 4) + " " + clean.substring(4);
        }
        return value;
    }

    /**
     * For US ZIP codes, returns just the 5-digit part.
     *
     * @return the base ZIP code
     */
    public String baseCode() {
        if (country == Country.UNITED_STATES) {
            return value.replaceAll("[^0-9]", "").substring(0, 5);
        }
        return value;
    }

    /**
     * For US ZIP+4, returns the extension if present.
     *
     * @return the ZIP+4 extension or null
     */
    public @Nullable String extension() {
        if (country == Country.UNITED_STATES) {
            String digits = value.replaceAll("[^0-9]", "");
            if (digits.length() == 9) {
                return digits.substring(5);
            }
        }
        return null;
    }

    /**
     * Checks if this is a ZIP+4 code (US only).
     *
     * @return true if ZIP+4
     */
    public boolean isExtended() {
        return extension() != null;
    }

    /**
     * For UK postcodes, returns the outward code (first part).
     *
     * @return the outward code
     */
    public String outwardCode() {
        if (country == Country.UNITED_KINGDOM) {
            String normalized = normalizeUk();
            int spaceIndex = normalized.indexOf(' ');
            return spaceIndex > 0 ? normalized.substring(0, spaceIndex) : normalized;
        }
        return value;
    }

    /**
     * For UK postcodes, returns the inward code (last part).
     *
     * @return the inward code
     */
    public String inwardCode() {
        if (country == Country.UNITED_KINGDOM) {
            String normalized = normalizeUk();
            int spaceIndex = normalized.indexOf(' ');
            return spaceIndex > 0 ? normalized.substring(spaceIndex + 1) : "";
        }
        return "";
    }

    @Override
    public int compareTo(PostalCode other) {
        int countryCompare = this.country.compareTo(other.country);
        if (countryCompare != 0) {
            return countryCompare;
        }
        return this.value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return normalized();
    }
}
