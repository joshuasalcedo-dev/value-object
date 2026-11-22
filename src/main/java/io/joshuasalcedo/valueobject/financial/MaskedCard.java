package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A card as receipts print it: its network and last four digits, the rest hidden.
 *
 * <pre>{@code
 * MaskedCard.parse("Visa - 4242");                  // VISA ••4242
 * MaskedCard.parse("American Express ending in 1005");
 * MaskedCard.parse("**** **** **** 4242");          // UNKNOWN network, last 4242
 * }</pre>
 *
 * @param brand its network ({@link CardBrand#UNKNOWN} when not printed)
 * @param last4 its last four digits
 * @author JoshuaSalcedo
 */
public record MaskedCard(CardBrand brand, String last4) implements ValueObject {

    private static final Pattern NAMED = Pattern.compile(
            "(?i)^\\s*(visa(?: debit| electron)?|master ?card|mc|amex|american express|discover|jcb|union ?pay|diners(?: club)?|maestro)"
                    + "\\s*(?:card)?\\s*(?:[-–:•·]|ending(?: in| with)?|x+|\\*+|•+)?\\s*[*x•·]*\\s*(\\d{4})\\s*$");
    private static final Pattern STARS = Pattern.compile("^\\s*(?:[*x•·X]{2,4}[\\s-]?){1,3}[*x•·X]*\\s*(\\d{4})\\s*$");

    /**
     * @throws ValueObjectException if the last four are not four digits
     */
    public MaskedCard {
        if (brand == null) brand = CardBrand.UNKNOWN;
        if (last4 == null || !last4.matches("\\d{4}")) throw new ValueObjectException("The last four must be four digits");
    }

    /**
     * @param text e.g. {@code "Visa - 4242"}, {@code "Mastercard ending in 4444"}, {@code "**** 4242"}
     * @return the card
     * @throws ValueObjectException if it is not a masked card
     */
    public static MaskedCard parse(String text) {
        if (text == null) throw new ValueObjectException("A card is required");
        Matcher n = NAMED.matcher(text);
        if (n.matches()) return new MaskedCard(CardBrand.fromName(n.group(1)).orElse(CardBrand.UNKNOWN), n.group(2));
        Matcher s = STARS.matcher(text);
        if (s.matches()) return new MaskedCard(CardBrand.UNKNOWN, s.group(1));
        throw new ValueObjectException("Not a masked card: '" + text + "'");
    }

    /**
     * @param text any text
     * @return the masked card it is, or empty
     */
    public static Optional<MaskedCard> tryParse(@Nullable String text) {
        try {
            return text == null ? Optional.empty() : Optional.of(parse(text));
        } catch (ValueObjectException e) {
            return Optional.empty();
        }
    }

    /**
     * @param card a full card number
     * @return whether it is this card (same network, same last four)
     */
    public boolean matches(CardNumber card) {
        return card.last4().equals(last4) && (brand == CardBrand.UNKNOWN || card.brand() == brand);
    }

    /** @return e.g. {@code "Visa •••• 4242"} */
    @Override
    public String toString() {
        return (brand == CardBrand.UNKNOWN ? "Card" : brand.displayName()) + " •••• " + last4;
    }
}
