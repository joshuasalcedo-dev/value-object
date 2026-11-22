package io.joshuasalcedo.valueobject.contact;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a phone number.
 * Stores in E.164 format (country code + national number) for consistency.
 * For advanced parsing and formatting, use PhoneNumberValidatorPort with libphonenumber adapter.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record PhoneNumber(
        String countryCode,
        String nationalNumber
) implements ValueObject, Comparable<PhoneNumber> {

    // Allows digits only for normalized parts
    private static final Pattern DIGITS_ONLY = Pattern.compile("^\\d+$");

    // E.164 max length is 15 digits total
    private static final int MAX_E164_LENGTH = 15;

    /** Null object representing an absent PhoneNumber. */
    public static final PhoneNumber NULL = new PhoneNumber("", "");

    public PhoneNumber {
        Objects.requireNonNull(countryCode, "Country code cannot be null");
        Objects.requireNonNull(nationalNumber, "National number cannot be null");

        if (!countryCode.isEmpty() || !nationalNumber.isEmpty()) {
            // Normalize: remove any non-digit characters
            countryCode = countryCode.replaceAll("[^\\d]", "");
            nationalNumber = nationalNumber.replaceAll("[^\\d]", "");

            if (countryCode.isBlank()) {
                throw new ValueObjectException("Country code cannot be blank");
            }
            if (nationalNumber.isBlank()) {
                throw new ValueObjectException("National number cannot be blank");
            }
            if (countryCode.length() > 3) {
                throw new ValueObjectException("Country code cannot be longer than 3 digits: " + countryCode);
            }
            if (countryCode.length() + nationalNumber.length() > MAX_E164_LENGTH) {
                throw new ValueObjectException("Phone number exceeds E.164 maximum length of 15 digits");
            }
        }
    }

    
    public boolean isNull() {
        return countryCode.isEmpty() && nationalNumber.isEmpty();
    }

    /**
     * Creates a PhoneNumber from country code and national number.
     *
     * @param countryCode the country code (e.g., "1" for US, "44" for UK)
     * @param nationalNumber the national number
     * @return a new PhoneNumber
     */
    public static PhoneNumber of(String countryCode, String nationalNumber) {
        return new PhoneNumber(countryCode, nationalNumber);
    }

    /**
     * Creates a US phone number.
     *
     * @param nationalNumber the 10-digit national number
     * @return a new PhoneNumber with US country code
     */
    public static PhoneNumber us(String nationalNumber) {
        return new PhoneNumber("1", nationalNumber);
    }

    /**
     * Creates a UK phone number.
     *
     * @param nationalNumber the national number
     * @return a new PhoneNumber with UK country code
     */
    public static PhoneNumber uk(String nationalNumber) {
        return new PhoneNumber("44", nationalNumber);
    }

    /**
     * Returns the null object representation of PhoneNumber.
     *
     * @return the NULL phone number constant
     */
    public static PhoneNumber nullValue() {
        return NULL;
    }

    /**
     * Attempts to parse a phone number string.
     * Expects format like "+1 555-123-4567" or "+44 20 7946 0958".
     * For more robust parsing, use PhoneNumberValidatorPort.
     *
     * @param phoneString the phone string to parse
     * @return a new PhoneNumber
     * @throws ValueObjectException if parsing fails
     */
    public static PhoneNumber parse(String phoneString) {
        if (phoneString == null || phoneString.isBlank()) {
            throw new ValueObjectException("Phone string cannot be null or blank");
        }

        // Remove all non-digit characters except leading +
        String normalized = phoneString.trim();
        boolean hasPlus = normalized.startsWith("+");
        normalized = normalized.replaceAll("[^\\d]", "");

        if (normalized.length() < 4) {
            throw new ValueObjectException("Phone number too short: " + phoneString);
        }

        // If it started with +, first 1-3 digits are country code
        if (hasPlus) {
            // Try to detect country code length (1-3 digits)
            // Common patterns: 1 (US/Canada), 44 (UK), 33 (France), etc.
            String cc;
            String nn;

            if (normalized.startsWith("1") && normalized.length() >= 11) {
                cc = "1";
                nn = normalized.substring(1);
            } else if (normalized.length() >= 12) {
                // Assume 3-digit country code
                cc = normalized.substring(0, 3);
                nn = normalized.substring(3);
            } else if (normalized.length() >= 11) {
                // Assume 2-digit country code
                cc = normalized.substring(0, 2);
                nn = normalized.substring(2);
            } else {
                // Assume 1-digit country code
                cc = normalized.substring(0, 1);
                nn = normalized.substring(1);
            }

            return new PhoneNumber(cc, nn);
        }

        // No +, assume it's just a national number with no country code
        throw new ValueObjectException(
            "Phone number must include country code (e.g., +1 for US): " + phoneString
        );
    }

    /**
     * Returns the E.164 formatted number (+[country][national]).
     *
     * @return the E.164 format
     */
    public String toE164() {
        return "+" + countryCode + nationalNumber;
    }

    /**
     * Returns the full number without formatting.
     *
     * @return the concatenated country code and national number
     */
    public String toDigits() {
        return countryCode + nationalNumber;
    }

    /**
     * Returns a formatted version suitable for display.
     * Uses a basic format; for locale-specific formatting, use PhoneNumberValidatorPort.
     *
     * @return the formatted number
     */
    public String formatted() {
        // US format: +1 (XXX) XXX-XXXX
        if ("1".equals(countryCode) && nationalNumber.length() == 10) {
            return String.format("+1 (%s) %s-%s",
                nationalNumber.substring(0, 3),
                nationalNumber.substring(3, 6),
                nationalNumber.substring(6)
            );
        }
        // Default: +CC XXXX XXXX
        return "+" + countryCode + " " + nationalNumber;
    }

    /**
     * Returns a masked version for privacy.
     *
     * @return the masked number
     */
    public String masked() {
        if (nationalNumber.length() <= 4) {
            return "+" + countryCode + " ****";
        }
        String lastFour = nationalNumber.substring(nationalNumber.length() - 4);
        String stars = "*".repeat(nationalNumber.length() - 4);
        return "+" + countryCode + " " + stars + lastFour;
    }

    /**
     * Checks if this appears to be a US/Canada number.
     *
     * @return true if NANP number
     */
    public boolean isNorthAmerican() {
        return "1".equals(countryCode);
    }

    @Override
    public int compareTo(PhoneNumber other) {
        int cc = this.countryCode.compareTo(other.countryCode);
        return cc != 0 ? cc : this.nationalNumber.compareTo(other.nationalNumber);
    }

    @Override
    public String toString() {
        return toE164();
    }
}
