package io.joshuasalcedo.valueobject.contact;

import java.util.Locale;
import org.jspecify.annotations.Nullable;
import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import io.joshuasalcedo.valueobject.core.ValueObjectException;

import java.util.regex.Pattern;

/**
 * A phone number in E.164 terms: a country calling code of 1 to 3 digits and a national number, at most 15
 * digits together. Both parts are kept as digits only; any other characters are dropped. It does not check that
 * the number is assigned or valid for its country.
 *
 * @param countryCode    the country calling code, digits only, e.g. {@code "1"} or {@code "44"}
 * @param nationalNumber the national number, digits only, without the country code
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


    /**
     * Creates a phone number, dropping every non-digit from both parts.
     *
     * @throws ValueObjectException if either part is {@code null} or has no digits, the country code has more than 3 digits, or
     *                              there are more than 15 digits together
     */
    public PhoneNumber {
        if (countryCode == null) {
            throw new ValueObjectException("Country code cannot be null");
        }
        if (nationalNumber == null) {
            throw new ValueObjectException("National number cannot be null");
        }

        {
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

    

    /**
     * Creates a phone number from a country code and a national number; the same as the constructor.
     *
     * @param countryCode    the country calling code, e.g. {@code "1"} for the US, {@code "44"} for the UK
     * @param nationalNumber the national number; non-digits are dropped
     * @return the phone number
     * @throws ValueObjectException if a part has no digits or the number is too long
     */
    public static PhoneNumber of(String countryCode, String nationalNumber) {
        return new PhoneNumber(countryCode, nationalNumber);
    }

    /**
     * Creates a phone number with country code {@code 1} (United States, Canada and the rest of the North
     * American Numbering Plan).
     *
     * @param nationalNumber the national number, normally 10 digits; non-digits are dropped
     * @return the phone number
     * @throws ValueObjectException if it has no digits or is too long
     */
    public static PhoneNumber us(String nationalNumber) {
        return new PhoneNumber("1", nationalNumber);
    }

    /**
     * Creates a phone number with country code {@code 44} (United Kingdom).
     *
     * @param nationalNumber the national number, without the leading 0; non-digits are dropped
     * @return the phone number
     * @throws ValueObjectException if it has no digits or is too long
     */
    public static PhoneNumber uk(String nationalNumber) {
        return new PhoneNumber("44", nationalNumber);
    }


    /**
     * Parses an international phone number that starts with {@code +}, such as {@code "+1 555-123-4567"} or
     * {@code "+63 917 123 4567"}. Surrounding whitespace and separators are ignored. The country calling code is
     * read from the leading digits using the ITU-T E.164 assignments, which are prefix-free: {@code 1} and
     * {@code 7} are one digit, the two-digit codes are {@code 20}, {@code 27}, {@code 30}-{@code 34}, {@code 36},
     * {@code 39}, {@code 40}, {@code 41}, {@code 43}-{@code 49}, {@code 51}-{@code 58}, {@code 60}-{@code 66},
     * {@code 81}, {@code 82}, {@code 84}, {@code 86}, {@code 90}-{@code 95} and {@code 98}, and every other code
     * has three digits.
     *
     * @param phoneString the text, with a leading {@code +}
     * @return the phone number
     * @throws ValueObjectException if it is {@code null}, blank, has fewer than 4 digits, has no leading
     *                              {@code +}, starts with 0, or is too long
     */
    public static PhoneNumber parse(String phoneString) {
        if (phoneString == null || phoneString.isBlank()) {
            throw new ValueObjectException("Phone string cannot be null or blank");
        }

        String normalized = phoneString.trim();
        if (!normalized.startsWith("+")) {
            throw new ValueObjectException(
                "Phone number must include country code (e.g., +1 for US): " + phoneString
            );
        }
        normalized = normalized.replaceAll("\\D", "");

        if (normalized.length() < 4) {
            throw new ValueObjectException("Phone number too short: " + phoneString);
        }
        if (normalized.charAt(0) == '0') {
            throw new ValueObjectException("Country calling codes do not start with 0: " + phoneString);
        }

        int ccLength = countryCodeLength(normalized);
        return new PhoneNumber(normalized.substring(0, ccLength), normalized.substring(ccLength));
    }

    /**
     * Returns how many leading digits of an E.164 digit string form its country calling code.
     *
     * @param digits the digits, at least one
     * @return 1, 2 or 3
     */
    private static int countryCodeLength(String digits) {
        char first = digits.charAt(0);
        if (first == '1' || first == '7') {
            return 1;
        }
        int two = Integer.parseInt(digits.substring(0, 2));
        boolean twoDigit = switch (two / 10) {
            case 2 -> two == 20 || two == 27;
            case 3 -> (two >= 30 && two <= 34) || two == 36 || two == 39;
            case 4 -> two != 42;
            case 5 -> two >= 51 && two <= 58;
            case 6 -> two <= 66;
            case 8 -> two == 81 || two == 82 || two == 84 || two == 86;
            case 9 -> (two >= 90 && two <= 95) || two == 98;
            default -> false;
        };
        return twoDigit ? 2 : 3;
    }

    /**
     * Returns the number in E.164 form, {@code +} then the country code and the national number.
     *
     * @return the E.164 format
     */
    public String toE164() {
        return "+" + countryCode + nationalNumber;
    }

    /**
     * Returns the number as digits only, without the {@code +}.
     *
     * @return the concatenated country code and national number
     */
    public String toDigits() {
        return countryCode + nationalNumber;
    }

    /**
     * Returns the number formatted for display: {@code +1 (555) 123-4567} for a 10-digit North American number,
     * otherwise {@code +CC} and the national number separated by a space.
     *
     * @return the formatted number
     */
    public String formatted() {
        // US format: +1 (XXX) XXX-XXXX
        if ("1".equals(countryCode) && nationalNumber.length() == 10) {
            return String.format(Locale.ROOT, "+1 (%s) %s-%s",
                nationalNumber.substring(0, 3),
                nationalNumber.substring(3, 6),
                nationalNumber.substring(6)
            );
        }
        // Default: +CC XXXX XXXX
        return "+" + countryCode + " " + nationalNumber;
    }

    /**
     * Returns the number masked for privacy: the country code and the last four digits stay, the rest become
     * {@code *} ({@code +1 ******4567}); a national number of four digits or fewer becomes {@code ****}.
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
     * Checks whether the country code is {@code 1}, the North American Numbering Plan (US, Canada and others).
     *
     * @return whether it is a North American number
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

    /**
     * Parses a phone number as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as a {@link PhoneNumber}, or empty when it is not one; never throws
     */
    public static Optional<PhoneNumber> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text parses as a phone number with {@link #parse(String)}.
     *
     * @param text any text
     * @return whether it is a {@link PhoneNumber}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
