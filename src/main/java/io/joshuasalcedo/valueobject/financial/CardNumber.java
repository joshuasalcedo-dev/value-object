package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

/**
 * A full payment card number (PAN): 12 to 19 digits that pass the Luhn check. Its {@code toString} is masked, so a
 * log line never shows the whole number; {@link #value()} has it.
 *
 * <pre>{@code
 * CardNumber c = CardNumber.parse("4242 4242 4242 4242");
 * c.brand();      // VISA
 * c.last4();      // "4242"
 * c.masked();     // "**** **** **** 4242"
 * }</pre>
 *
 * @param value the digits, no spaces
 * @author JoshuaSalcedo
 */
public record CardNumber(String value) implements ValueObject {

    /**
     * @throws ValueObjectException if it is not 12-19 digits passing the Luhn check
     */
    public CardNumber {
        if (value == null) throw new ValueObjectException("A card number is required");
        value = value.replaceAll("[\\s-]", "");
        if (!value.matches("\\d{12,19}")) throw new ValueObjectException("A card number is 12 to 19 digits");
        if (!luhn(value)) throw new ValueObjectException("Not a valid card number (Luhn check failed)");
    }

    /**
     * @param text the number, spaces and dashes allowed
     * @return it
     * @throws ValueObjectException if it is not a card number
     */
    public static CardNumber parse(String text) {
        return new CardNumber(text);
    }

    /**
     * @param text any text
     * @return whether it is a card number
     */
    public static boolean isValid(@Nullable String text) {
        if (text == null) return false;
        String d = text.replaceAll("[\\s-]", "");
        return d.matches("\\d{12,19}") && luhn(d);
    }

    /** The Luhn (mod 10) check every card number carries in its last digit. */
    static boolean luhn(String digits) {
        int sum = 0;
        boolean twice = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (twice) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            twice = !twice;
        }
        return sum % 10 == 0;
    }

    /** @return the network it belongs to */
    public CardBrand brand() {
        return CardBrand.of(value);
    }

    /** @return its last four digits */
    public String last4() {
        return value.substring(value.length() - 4);
    }

    /** @return its first six digits, the issuer (bank) number */
    public String iin() {
        return value.substring(0, 6);
    }

    /** @return it with all but the last four digits hidden, in groups of four */
    public String masked() {
        String hidden = "*".repeat(value.length() - 4) + last4();
        return group(hidden);
    }

    /** @return it in its printed groups: 4-6-5 for American Express, else groups of four */
    public String formatted() {
        return group(value);
    }

    private String group(String s) {
        if (brand() == CardBrand.AMERICAN_EXPRESS && s.length() == 15) {
            return s.substring(0, 4) + " " + s.substring(4, 10) + " " + s.substring(10);
        }
        return s.replaceAll("(.{4})(?=.)", "$1 ");
    }

    /** @return it as the card it is, but masked: brand and last four */
    public MaskedCard toMasked() {
        return new MaskedCard(brand(), last4());
    }

    /** @return the masked number, never the whole one */
    @Override
    public String toString() {
        return masked();
    }
}
