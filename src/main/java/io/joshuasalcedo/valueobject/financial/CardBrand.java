package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.ValueObject;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * A payment card network, recognised by the first digits of the card number (its IIN) and its length.
 *
 * @author JoshuaSalcedo
 */
public enum CardBrand implements ValueObject {

    /** Visa ({@code Visa}). */
    VISA("Visa", List.of(13, 16, 19), "4"),
    /** Mastercard. */
    MASTERCARD("Mastercard", List.of(16), "51-55", "2221-2720"),
    /** American Express. */
    AMERICAN_EXPRESS("American Express", List.of(15), "34", "37"),
    /** Discover. */
    DISCOVER("Discover", List.of(16, 19), "6011", "644-649", "65"),
    /** Jcb ({@code JCB}). */
    JCB("JCB", List.of(16, 17, 18, 19), "3528-3589"),
    /** UnionPay. */
    UNIONPAY("UnionPay", List.of(16, 17, 18, 19), "62"),
    /** Diners Club. */
    DINERS_CLUB("Diners Club", List.of(14, 16), "300-305", "36", "38", "39"),
    /** Maestro. */
    MAESTRO("Maestro", List.of(12, 13, 14, 15, 16, 17, 18, 19), "50", "56-58", "6304", "6759", "6761-6763"),
    /** Not known, or not one of the others. */
    UNKNOWN("Card", List.of());

    private final String displayName;
    private final List<Integer> lengths;
    private final String[] prefixes;

    CardBrand(String displayName, List<Integer> lengths, String... prefixes) {
        this.displayName = displayName;
        this.lengths = lengths;
        this.prefixes = prefixes;
    }

    /**
     * Recognises the network of a card number from its first digits (IIN range) and its length. Anything that
     * is not a digit is ignored first; the Luhn check is not made.
     *
     * @param digits a card number's digits
     * @return the network its first digits and length belong to, or {@link #UNKNOWN}
     * @throws ValueObjectException if the digits are {@code null}
     */
    public static CardBrand of(String digits) {
        if (digits == null) throw new ValueObjectException("Card digits are required");
        String d = digits.replaceAll("\\D", "");
        for (CardBrand b : values()) {
            if (b != UNKNOWN && b.lengths.contains(d.length()) && b.matchesPrefix(d)) return b;
        }
        return UNKNOWN;
    }

    /**
     * Looks up a network by its name, ignoring case and anything that is not a letter. Besides the full names
     * it knows {@code "AMEX"}, {@code "MC"}, {@code "Master"}, {@code "Visa Debit"}, {@code "Visa Electron"},
     * {@code "China UnionPay"}, {@code "CUP"} and {@code "Diners"}.
     *
     * @param name a network's name as printed: {@code "Visa"}, {@code "AMEX"}, {@code "American Express"},
     *             {@code "MC"}, ...
     * @return that network, or empty when the name is {@code null} or not known
     */
    public static Optional<CardBrand> fromName(@Nullable String name) {
        if (name == null) return Optional.empty();
        String n = name.strip().toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        return Optional.ofNullable(switch (n) {
            case "visa", "visadebit", "visaelectron" -> VISA;
            case "mastercard", "mc", "master" -> MASTERCARD;
            case "amex", "americanexpress" -> AMERICAN_EXPRESS;
            case "discover" -> DISCOVER;
            case "jcb" -> JCB;
            case "unionpay", "chinaunionpay", "cup" -> UNIONPAY;
            case "diners", "dinersclub" -> DINERS_CLUB;
            case "maestro" -> MAESTRO;
            default -> null;
        });
    }

    private boolean matchesPrefix(String digits) {
        for (String p : prefixes) {
            if (p.contains("-")) {
                String[] r = p.split("-");
                int len = r[0].length();
                if (digits.length() < len) continue;
                int head = Integer.parseInt(digits.substring(0, len));
                if (head >= Integer.parseInt(r[0]) && head <= Integer.parseInt(r[1])) return true;
            } else if (digits.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the network's name as printed; {@code "Card"} for {@link #UNKNOWN}.
     *
     * @return its name, e.g. {@code "American Express"}
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Tells whether this is the {@link #UNKNOWN} placeholder.
     *
     * @return whether it is {@link #UNKNOWN}
     */
    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    /**
     * Reads a network from its name, as {@link #fromName(String)}, or else from a card number, as
     * {@link #of(String)}.
     *
     * @param text the network's name or a card number
     * @return the network
     * @throws ValueObjectException if it is {@code null}, or neither a known name nor a number of a known network
     */
    public static CardBrand parse(String text) {
        if (text == null) throw new ValueObjectException("A CardBrand is required");
        CardBrand b = fromName(text).orElseGet(() -> of(text));
        if (b == UNKNOWN) throw new ValueObjectException("Not a card network: '" + text + "'");
        return b;
    }

    /**
     * Reads a network like {@link #parse(String)}, without throwing.
     *
     * @param text any text
     * @return it as a {@link CardBrand}, or empty when it is not one; never throws
     */
    public static Optional<CardBrand> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether text is a known network's name or a number of a known network.
     *
     * @param text any text
     * @return whether it is a {@link CardBrand}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
