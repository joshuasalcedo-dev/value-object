package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;

import io.joshuasalcedo.valueobject.measurement.Quantity;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * One line of a bill, invoice or receipt: what was charged, how many, at what price, for how much.
 *
 * @param description what it is, e.g. {@code "Compute hours, Jan 1 – Jan 31"}
 * @param quantity    how many, in what unit ({@code C62} for a plain count), if printed
 * @param unitPrice   the price of one, if printed
 * @param amount      the line's total, if printed
 * @param other       the line's other columns (SKU, tax code, ...), by column name
 * @author JoshuaSalcedo
 */
public record LineItem(String description, Optional<Quantity> quantity, Optional<Money> unitPrice,
                       Optional<Money> amount, Map<String, String> other) implements ValueObject {

    /**
     * @throws ValueObjectException if it has neither a description nor an amount
     */
    public LineItem {
        description = description == null ? "" : description.strip();
        quantity = quantity == null ? Optional.empty() : quantity;
        unitPrice = unitPrice == null ? Optional.empty() : unitPrice;
        amount = amount == null ? Optional.empty() : amount;
        other = other == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(other));
        if (description.isEmpty() && amount.isEmpty() && unitPrice.isEmpty())
            throw new ValueObjectException("A line item needs a description or an amount");
    }

    /**
     * @param description what it is
     * @param amount      what it cost
     * @return a line with only a description and an amount
     */
    public static LineItem of(String description, Money amount) {
        return new LineItem(description, Optional.empty(), Optional.empty(), Optional.of(amount), Map.of());
    }

    /**
     * @return the amount, or quantity × unit price when only those were printed
     */
    public Optional<Money> total() {
        if (amount.isPresent()) return amount;
        return unitPrice.flatMap(p -> quantity.map(q -> p.multiply(q.amount())));
    }
}
