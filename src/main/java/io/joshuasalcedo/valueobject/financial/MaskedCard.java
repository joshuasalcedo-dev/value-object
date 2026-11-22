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
 * MaskedCard.parse("Visa - 4242");                  // VISA, last 4242
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
     * Checks the card; a {@code null} brand becomes {@link CardBrand#UNKNOWN}.
     *
     * @param brand its network, or {@code null} when not known
     * @param last4 its last four digits
     * @throws ValueObjectException if the last four are not four digits
     */
    public MaskedCard {
        if (brand == null) brand = CardBrand.UNKNOWN;
        if (last4 == null || !last4.matches("\\d{4}")) throw new ValueObjectException("The last four must be four digits");
    }

    /**
     * Reads a card as a receipt prints it: a network name followed by the last four digits (after a dash,
     * colon, {@code "ending in"}, or masking characters), or masking characters ({@code *}, {@code x},
     * {@code •}) followed by the last four, which gives an {@link CardBrand#UNKNOWN} network. Case is ignored.
     *
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
     * Reads a card like {@link #parse(String)}, without throwing.
     *
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
     * Tells whether a full card number could be this card: the same last four digits and, when this card's
     * network is known, the same network.
     *
     * @param card a full card number
     * @return whether it is this card (same network, or this one {@link CardBrand#UNKNOWN}; same last four)
     * @throws ValueObjectException if the card is {@code null}
     */
    public boolean matches(CardNumber card) {
        if (card == null) throw new ValueObjectException("A card number is required");
        return card.last4().equals(last4) && (brand == CardBrand.UNKNOWN || card.brand() == brand);
    }

    /**
     * Formats as the network's name, four dots and the last four; {@code "Card"} stands in for an unknown
     * network.
     *
     * @return e.g. {@code "Visa •••• 4242"}
     */
    @Override
    public String toString() {
        return (brand == CardBrand.UNKNOWN ? "Card" : brand.displayName()) + " •••• " + last4;
    }

    /**
     * Tells whether text reads as a masked card.
     *
     * @param text any text
     * @return whether it is a {@link MaskedCard}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
