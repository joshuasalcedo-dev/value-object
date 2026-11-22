package io.joshuasalcedo.valueobject.identifier;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

/**
 * A product's barcode number (GS1 GTIN): EAN-8, UPC-A (12 digits), EAN-13 or GTIN-14, with its check digit.
 *
 * <pre>{@code
 * Gtin g = Gtin.parse("4006381333931");
 * g.kind();      // EAN_13
 * Gtin.parse("036000291452").toGtin14();   // "00036000291452"
 * }</pre>
 *
 * @param value its digits
 * @author JoshuaSalcedo
 */
public record Gtin(String value) implements ValueObject, Comparable<Gtin> {

    /** The barcode families, by length. */
    public enum Kind { EAN_8, UPC_A, EAN_13, GTIN_14 }

    /**
     * @throws ValueObjectException if it is not 8, 12, 13 or 14 digits with a right check digit
     */
    public Gtin {
        if (value == null) throw new ValueObjectException("A barcode number is required");
        value = value.replaceAll("[\\s-]", "");
        if (!value.matches("\\d{8}|\\d{12,14}")) throw new ValueObjectException("A GTIN is 8, 12, 13 or 14 digits: " + value);
        if (!checkDigitOk(value)) throw new ValueObjectException("Not a valid GTIN (check digit does not match): " + value);
    }

    /**
     * @param text the digits, spaces and dashes allowed
     * @return it
     * @throws ValueObjectException if it is not a GTIN
     */
    public static Gtin parse(String text) {
        return new Gtin(text);
    }

    /**
     * @param text any text
     * @return whether it is a GTIN
     */
    public static boolean isValid(@Nullable String text) {
        if (text == null) return false;
        try {
            new Gtin(text);
            return true;
        } catch (ValueObjectException e) {
            return false;
        }
    }

    /** The GS1 check: weights 3 and 1 from the right, excluding the check digit. */
    static boolean checkDigitOk(String digits) {
        int sum = 0;
        for (int i = digits.length() - 2, w = 3; i >= 0; i--, w = w == 3 ? 1 : 3) sum += (digits.charAt(i) - '0') * w;
        int check = (10 - sum % 10) % 10;
        return check == digits.charAt(digits.length() - 1) - '0';
    }

    /** @return its family, by length */
    public Kind kind() {
        return switch (value.length()) {
            case 8 -> Kind.EAN_8;
            case 12 -> Kind.UPC_A;
            case 13 -> Kind.EAN_13;
            default -> Kind.GTIN_14;
        };
    }

    /** @return it as 14 digits, padded with zeros on the left */
    public String toGtin14() {
        return "0".repeat(14 - value.length()) + value;
    }

    /** @return the GS1 country prefix (first three digits of the 13-digit form), e.g. {@code "480"} for the Philippines */
    public String prefix() {
        return toGtin14().substring(1, 4);
    }

    @Override
    public int compareTo(Gtin other) {
        return toGtin14().compareTo(other.toGtin14());
    }

    @Override
    public String toString() {
        return value;
    }
}
