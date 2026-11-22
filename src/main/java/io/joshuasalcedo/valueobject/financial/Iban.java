package io.joshuasalcedo.valueobject.financial;

import java.util.Optional;
import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.geo.Country;
import org.jspecify.annotations.Nullable;

import java.math.BigInteger;
import java.util.Map;

/**
 * An International Bank Account Number (ISO 13616): a country code, two check digits, then the national account
 * number. Checked with mod-97 and, for the countries listed, the country's length.
 *
 * <pre>{@code
 * Iban iban = Iban.parse("GB82 WEST 1234 5698 7654 32");
 * iban.countryCode();   // "GB"
 * iban.formatted();     // "GB82 WEST 1234 5698 7654 32"
 * }</pre>
 *
 * @param value the IBAN, upper case, no spaces
 * @author JoshuaSalcedo
 */
public record Iban(String value) implements ValueObject, Comparable<Iban> {

    /** IBAN lengths by country (SWIFT IBAN registry). */
    private static final Map<String, Integer> LENGTHS = Map.ofEntries(
            Map.entry("AD", 24), Map.entry("AE", 23), Map.entry("AT", 20), Map.entry("AZ", 28), Map.entry("BA", 20),
            Map.entry("BE", 16), Map.entry("BG", 22), Map.entry("BH", 22), Map.entry("BR", 29), Map.entry("CH", 21),
            Map.entry("CR", 22), Map.entry("CY", 28), Map.entry("CZ", 24), Map.entry("DE", 22), Map.entry("DK", 18),
            Map.entry("DO", 28), Map.entry("EE", 20), Map.entry("EG", 29), Map.entry("ES", 24), Map.entry("FI", 18),
            Map.entry("FO", 18), Map.entry("FR", 27), Map.entry("GB", 22), Map.entry("GE", 22), Map.entry("GI", 23),
            Map.entry("GL", 18), Map.entry("GR", 27), Map.entry("GT", 28), Map.entry("HR", 21), Map.entry("HU", 28),
            Map.entry("IE", 22), Map.entry("IL", 23), Map.entry("IQ", 23), Map.entry("IS", 26), Map.entry("IT", 27),
            Map.entry("JO", 30), Map.entry("KW", 30), Map.entry("KZ", 20), Map.entry("LB", 28), Map.entry("LC", 32),
            Map.entry("LI", 21), Map.entry("LT", 20), Map.entry("LU", 20), Map.entry("LV", 21), Map.entry("MC", 27),
            Map.entry("MD", 24), Map.entry("ME", 22), Map.entry("MK", 19), Map.entry("MR", 27), Map.entry("MT", 31),
            Map.entry("MU", 30), Map.entry("NL", 18), Map.entry("NO", 15), Map.entry("PK", 24), Map.entry("PL", 28),
            Map.entry("PS", 29), Map.entry("PT", 25), Map.entry("QA", 29), Map.entry("RO", 24), Map.entry("RS", 22),
            Map.entry("SA", 24), Map.entry("SC", 31), Map.entry("SE", 24), Map.entry("SI", 19), Map.entry("SK", 24),
            Map.entry("SM", 27), Map.entry("TN", 24), Map.entry("TR", 26), Map.entry("UA", 29), Map.entry("VA", 22),
            Map.entry("VG", 24), Map.entry("XK", 20));

    /**
     * Checks the IBAN after removing spaces and dashes and upper-casing it: two letters, two check digits and
     * 11 to 30 letters or digits, the country's length when the country is listed, and the mod-97 check.
     *
     * @param value an IBAN, spaces and dashes allowed, any case
     * @throws ValueObjectException if it is {@code null} or not a valid IBAN
     */
    public Iban {
        if (value == null) throw new ValueObjectException("An IBAN is required");
        value = value.replaceAll("[\\s-]", "").toUpperCase(java.util.Locale.ROOT);
        if (!value.matches("[A-Z]{2}\\d{2}[A-Z0-9]{11,30}")) throw new ValueObjectException("Not an IBAN: " + value);
        Integer length = LENGTHS.get(value.substring(0, 2));
        if (length != null && value.length() != length)
            throw new ValueObjectException("An IBAN from " + value.substring(0, 2) + " has " + length + " characters, not " + value.length());
        if (!mod97(value)) throw new ValueObjectException("Not a valid IBAN (check digits do not match): " + value);
    }

    /**
     * Reads an IBAN, removing spaces and dashes and upper-casing it.
     *
     * @param text an IBAN, spaces allowed
     * @return the IBAN
     * @throws ValueObjectException if it is not a valid IBAN
     */
    public static Iban parse(String text) {
        return new Iban(text);
    }

    /**
     * Tells whether text is a valid IBAN, as {@link #parse(String)} checks it.
     *
     * @param text any text
     * @return whether it is a valid IBAN
     */
    public static boolean isValid(@Nullable String text) {
        if (text == null) return false;
        try {
            new Iban(text);
            return true;
        } catch (ValueObjectException e) {
            return false;
        }
    }

    private static boolean mod97(String iban) {
        String moved = iban.substring(4) + iban.substring(0, 4);
        StringBuilder digits = new StringBuilder();
        for (char c : moved.toCharArray()) digits.append(Character.isDigit(c) ? String.valueOf(c) : String.valueOf(c - 'A' + 10));
        return new BigInteger(digits.toString()).mod(BigInteger.valueOf(97)).intValue() == 1;
    }

    /**
     * Returns the first two letters: the ISO 3166 country code.
     *
     * @return its country code, e.g. {@code "GB"}
     */
    public String countryCode() {
        return value.substring(0, 2);
    }

    /**
     * Looks up the country of the country code.
     *
     * @return its country, or {@link Country#UNKNOWN} when not in {@link Country}
     */
    public Country country() {
        Country c = Country.fromCode(countryCode());
        return c == null ? Country.UNKNOWN : c;
    }

    /**
     * Returns the two check digits after the country code.
     *
     * @return its check digits, e.g. {@code "82"}
     */
    public String checkDigits() {
        return value.substring(2, 4);
    }

    /**
     * Returns everything after the check digits: the Basic Bank Account Number.
     *
     * @return the national account number (BBAN): bank, branch and account
     */
    public String bban() {
        return value.substring(4);
    }

    /**
     * Returns the IBAN in groups of four from the left, the last group possibly shorter.
     *
     * @return it in groups of four, as printed: {@code "GB82 WEST 1234 5698 7654 32"}
     */
    public String formatted() {
        return value.replaceAll("(.{4})(?=.)", "$1 ");
    }

    @Override
    public int compareTo(Iban other) {
        return value.compareTo(other.value);
    }

    /**
     * Returns the IBAN in printed groups of four, as {@link #formatted()}.
     *
     * @return the formatted IBAN
     */
    @Override
    public String toString() {
        return formatted();
    }

    /**
     * Reads an IBAN like {@link #parse(String)}, without throwing.
     *
     * @param text any text
     * @return it as a {@link Iban}, or empty when it is not one; never throws
     */
    public static Optional<Iban> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

}
