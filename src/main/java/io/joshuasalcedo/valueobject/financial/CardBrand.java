package io.joshuasalcedo.valueobject.financial;

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

    VISA("Visa", List.of(13, 16, 19), "4"),
    MASTERCARD("Mastercard", List.of(16), "51-55", "2221-2720"),
    AMERICAN_EXPRESS("American Express", List.of(15), "34", "37"),
    DISCOVER("Discover", List.of(16, 19), "6011", "644-649", "65"),
    JCB("JCB", List.of(16, 17, 18, 19), "3528-3589"),
    UNIONPAY("UnionPay", List.of(16, 17, 18, 19), "62"),
    DINERS_CLUB("Diners Club", List.of(14, 16), "300-305", "36", "38", "39"),
    MAESTRO("Maestro", List.of(12, 13, 14, 15, 16, 17, 18, 19), "50", "56-58", "6304", "6759", "6761-6763"),
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
     * @param digits a card number's digits
     * @return the network its first digits and length belong to, or {@link #UNKNOWN}
     */
    public static CardBrand of(String digits) {
        String d = digits.replaceAll("\\D", "");
        for (CardBrand b : values()) {
            if (b != UNKNOWN && b.lengths.contains(d.length()) && b.matchesPrefix(d)) return b;
        }
        return UNKNOWN;
    }

    /**
     * @param name a network's name as printed: "Visa", "AMEX", "American Express", "MC", ...
     * @return that network, or empty
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

    /** @return its name, e.g. {@code "American Express"} */
    public String displayName() {
        return displayName;
    }

    /** @return whether it is {@link #UNKNOWN} */
    public boolean isNull() {
        return this == UNKNOWN;
    }
}
