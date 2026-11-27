package io.joshuasalcedo.valueobject.geo;

import java.util.Locale;
import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A postal or ZIP code of a country, trimmed and upper-cased. Its format is checked for about twenty countries
 * (United States, Canada, United Kingdom, Germany, Japan, Netherlands and others); for any other country it must
 * be 2 to 10 letters, digits, spaces or hyphens.
 *
 * <pre>{@code
 * PostalCode zip = PostalCode.us("94105-1804");
 * zip.baseCode();     // "94105"
 * zip.extension();    // "1804"
 * PostalCode.uk("sw1a1aa").normalized();   // "SW1A 1AA"
 * }</pre>
 *
 * @param value   the code, trimmed and upper case, as given otherwise
 * @param country the country whose format it follows
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

    /**
     * Creates a postal code, trimming and upper-casing it and checking it against its country's format.
     *
     * @throws ValueObjectException if {@code value} or {@code country} is {@code null}, or the code is blank or
     *                              not in the country's format
     */
    public PostalCode {
        if (value == null) {
            throw new ValueObjectException("Postal code cannot be null");
        }
        if (country == null) {
            throw new ValueObjectException("Country cannot be null");
        }

        value = value.trim().toUpperCase(Locale.ROOT);

        if (value.isBlank()) {
            throw new ValueObjectException("Postal code cannot be blank");
        }

        Pattern pattern = PATTERNS.getOrDefault(country, DEFAULT_PATTERN);
        if (!pattern.matcher(value).matches()) {
            throw new ValueObjectException(
                String.format(Locale.ROOT, "Invalid postal code format for %s: %s",
                    country.displayName(), value)
            );
        }
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a postal code of a country; the same as the constructor.
     *
     * @param code the postal code
     * @param country the country
     * @return the postal code
     * @throws ValueObjectException if either is {@code null}, or the code is blank or not in the country's format
     */
    public static PostalCode of(String code, Country country) {
        return new PostalCode(code, country);
    }

    /**
     * Creates a US ZIP code: five digits, optionally followed by {@code -} and four more (ZIP+4).
     *
     * @param zipCode the ZIP code, e.g. {@code "94105"} or {@code "94105-1804"}
     * @return the postal code
     * @throws ValueObjectException if it is not in that format
     */
    public static PostalCode us(String zipCode) {
        return new PostalCode(zipCode, Country.UNITED_STATES);
    }

    /**
     * Creates a UK postcode, with or without the space, in any case.
     *
     * @param postcode the UK postcode, e.g. {@code "SW1A 1AA"}
     * @return the postal code
     * @throws ValueObjectException if it is not in the UK format
     */
    public static PostalCode uk(String postcode) {
        return new PostalCode(postcode, Country.UNITED_KINGDOM);
    }

    /**
     * Creates a Canadian postal code ({@code A1A 1A1}), with a space, a hyphen or nothing in the middle.
     *
     * @param code the postal code, e.g. {@code "K1A 0B1"}
     * @return the postal code
     * @throws ValueObjectException if it is not in the Canadian format
     */
    public static PostalCode canada(String code) {
        return new PostalCode(code, Country.CANADA);
    }


    /**
     * Parses a postal code of a country; the same as {@link #of(String, Country)}.
     *
     * @param code    a postal code
     * @param country its country
     * @return the postal code
     * @throws ValueObjectException if it is not a postal code of that country
     */
    public static PostalCode parse(String code, Country country) {
        return of(code, country);
    }

    /**
     * Parses a postal code of a country as {@link #parse(String, Country)} does, without throwing.
     *
     * @param code    a postal code
     * @param country its country
     * @return it, or empty when it is not a postal code of that country; never throws
     */
    public static Optional<PostalCode> tryParse(@Nullable String code, @Nullable Country country) {
        if (code == null) return Optional.empty();
        try {
            return Optional.of(of(code, country));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Checks whether a text, trimmed and upper-cased, is in a country's postal code format.
     *
     * @param code the postal code; may be {@code null}
     * @param country the country; may be {@code null}
     * @return whether it is valid; {@code false} when either is {@code null} or the code is blank
     */
    public static boolean isValid(@Nullable String code, @Nullable Country country) {
        if (code == null || code.isBlank() || country == null) {
            return false;
        }
        Pattern pattern = PATTERNS.getOrDefault(country, DEFAULT_PATTERN);
        return pattern.matcher(code.trim().toUpperCase(Locale.ROOT)).matches();
    }

    // ==================== Query Methods ====================

    /**
     * Returns the code in its country's usual form: {@code 12345} or {@code 12345-6789} (US), {@code A1A 1A1}
     * (Canada), a space before the last three characters (UK), {@code 123-4567} (Japan), {@code 1234 AB}
     * (Netherlands); other countries' codes as stored.
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
        String clean = value.replaceAll("[ -]", "").toUpperCase(Locale.ROOT);
        if (clean.length() == 6) {
            return clean.substring(0, 3) + " " + clean.substring(3);
        }
        return value;
    }

    private String normalizeUk() {
        // Add space before last 3 characters if not present
        String clean = value.replaceAll(" ", "").toUpperCase(Locale.ROOT);
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
        String clean = value.replaceAll(" ", "").toUpperCase(Locale.ROOT);
        if (clean.length() == 6) {
            return clean.substring(0, 4) + " " + clean.substring(4);
        }
        return value;
    }

    /**
     * Returns the five-digit part of a US ZIP code.
     *
     * @return the base ZIP code; the code as stored for other countries
     */
    public String baseCode() {
        if (country == Country.UNITED_STATES) {
            return value.replaceAll("[^0-9]", "").substring(0, 5);
        }
        return value;
    }

    /**
     * Returns the four-digit extension of a US ZIP+4 code.
     *
     * @return the extension, or {@code null} when there is none or the code is not a US one
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
     * Checks whether this is a US ZIP+4 code.
     *
     * @return whether it has a ZIP+4 extension
     */
    public boolean isExtended() {
        return extension() != null;
    }

    /**
     * Returns the outward code (the part before the space) of a UK postcode, e.g. {@code "SW1A"}.
     *
     * @return the outward code; the code as stored for other countries
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
     * Returns the inward code (the last three characters) of a UK postcode, e.g. {@code "1AA"}.
     *
     * @return the inward code; an empty string for other countries
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
