package io.joshuasalcedo.valueobject.financial;

import java.util.Optional;
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
     * Checks the number after removing spaces and dashes.
     *
     * @param value the number, spaces and dashes allowed
     * @throws ValueObjectException if it is {@code null}, or not 12-19 digits passing the Luhn check
     */
    public CardNumber {
        if (value == null) throw new ValueObjectException("A card number is required");
        value = value.replaceAll("[\\s-]", "");
        if (!value.matches("\\d{12,19}")) throw new ValueObjectException("A card number is 12 to 19 digits");
        if (!luhn(value)) throw new ValueObjectException("Not a valid card number (Luhn check failed)");
    }

    /**
     * Reads a card number, removing spaces and dashes.
     *
     * @param text the number, spaces and dashes allowed
     * @return the card number
     * @throws ValueObjectException if it is not a card number
     */
    public static CardNumber parse(String text) {
        return new CardNumber(text);
    }

    /**
     * Tells whether text is 12-19 digits, spaces and dashes aside, that pass the Luhn check.
     *
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

    /**
     * Recognises the card's network from its first digits and length, as {@link CardBrand#of(String)}.
     *
     * @return the network it belongs to, or {@link CardBrand#UNKNOWN}
     */
    public CardBrand brand() {
        return CardBrand.of(value);
    }

    /**
     * Returns the last four digits, the part receipts print.
     *
     * @return its last four digits
     */
    public String last4() {
        return value.substring(value.length() - 4);
    }

    /**
     * Returns the first six digits: the issuer identification number (IIN, or BIN) of the card's bank.
     *
     * @return its first six digits
     */
    public String iin() {
        return value.substring(0, 6);
    }

    /**
     * Returns the number with every digit but the last four replaced by {@code *}, grouped as
     * {@link #formatted()} groups it.
     *
     * @return it with all but the last four digits hidden, e.g. {@code "**** **** **** 4242"}
     */
    public String masked() {
        String hidden = "*".repeat(value.length() - 4) + last4();
        return group(hidden);
    }

    /**
     * Returns the whole number in its printed groups: 4-6-5 for a 15-digit American Express card, 4-6-4 for a
     * 14-digit Diners Club card, else groups of four counted from the right, so the last four digits always
     * stay together and only the first group may be shorter (a 19-digit number is 3-4-4-4-4).
     *
     * @return it in its printed groups, e.g. {@code "4242 4242 4242 4242"}
     */
    public String formatted() {
        return group(value);
    }

    private String group(String s) {
        CardBrand brand = brand();
        if (brand == CardBrand.AMERICAN_EXPRESS && s.length() == 15) {
            return s.substring(0, 4) + " " + s.substring(4, 10) + " " + s.substring(10);
        }
        if (brand == CardBrand.DINERS_CLUB && s.length() == 14) {
            return s.substring(0, 4) + " " + s.substring(4, 10) + " " + s.substring(10);
        }
        StringBuilder out = new StringBuilder(s.length() + 4);
        int head = s.length() % 4;
        if (head > 0) out.append(s, 0, head);
        for (int i = head; i < s.length(); i += 4) {
            if (!out.isEmpty()) out.append(' ');
            out.append(s, i, i + 4);
        }
        return out.toString();
    }

    /**
     * Returns the card as receipts print it: its network and last four digits.
     *
     * @return it as the card it is, but masked: brand and last four
     */
    public MaskedCard toMasked() {
        return new MaskedCard(brand(), last4());
    }

    /**
     * Returns the masked number, as {@link #masked()}, so logs never show the whole one.
     *
     * @return the masked number, never the whole one
     */
    @Override
    public String toString() {
        return masked();
    }

    /**
     * Reads a card number like {@link #parse(String)}, without throwing.
     *
     * @param text any text
     * @return it as a {@link CardNumber}, or empty when it is not one; never throws
     */
    public static Optional<CardNumber> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

}
