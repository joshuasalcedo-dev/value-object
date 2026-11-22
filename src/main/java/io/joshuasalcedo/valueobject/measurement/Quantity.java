package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An amount of something in a {@link UnitOfMeasure}: {@code 10 pcs}, {@code 2.5 kg}, {@code 3 hrs}. A count with no
 * unit printed is in {@link UnitOfMeasure#ONE} ({@code C62}), as e-invoices write it.
 *
 * <pre>{@code
 * Quantity.parse("2.5 kg");        // 2.5 KILOGRAM
 * Quantity.parse("Nos 3");         // 3 PIECE  (unit first, as some POs print it)
 * Quantity.parse("12");            // 12 ONE
 * Quantity.parse("2 kg").to(UnitOfMeasure.GRAM);   // 2000 g
 * }</pre>
 *
 * @param amount how much, zero or more
 * @param unit   its unit
 * @author JoshuaSalcedo
 */
public record Quantity(BigDecimal amount, UnitOfMeasure unit) implements ValueObject, Comparable<Quantity> {

    private static final String NUMBER = "(\\d[\\d,]*(?:\\.\\d+)?|\\.\\d+)";
    private static final Pattern AMOUNT_FIRST = Pattern.compile("^" + NUMBER + "\\s*(?:x\\s*)?([\\p{L}][\\p{L}0-9²³ .-]{0,20})?$");
    private static final Pattern UNIT_FIRST = Pattern.compile("^([\\p{L}][\\p{L}0-9²³ .-]{0,20}?)\\s*" + NUMBER + "$");

    /**
     * @throws ValueObjectException if the amount is missing or negative
     */
    public Quantity {
        if (amount == null) throw new ValueObjectException("A quantity needs an amount");
        if (amount.signum() < 0) throw new ValueObjectException("A quantity cannot be negative: " + amount);
        if (unit == null) unit = UnitOfMeasure.ONE;
        amount = amount.stripTrailingZeros();
        if (amount.scale() < 0) amount = amount.setScale(0);
    }

    /**
     * @param amount how much
     * @param unit   its unit
     * @return the quantity
     */
    public static Quantity of(BigDecimal amount, UnitOfMeasure unit) {
        return new Quantity(amount, unit);
    }

    /**
     * @param amount how much
     * @param unit   its unit
     * @return the quantity
     */
    public static Quantity of(long amount, UnitOfMeasure unit) {
        return new Quantity(BigDecimal.valueOf(amount), unit);
    }

    /**
     * @param amount a count
     * @return it, in {@link UnitOfMeasure#ONE}
     */
    public static Quantity of(BigDecimal amount) {
        return new Quantity(amount, UnitOfMeasure.ONE);
    }

    /**
     * @param text {@code "10 pcs"}, {@code "2.5kg"}, {@code "1,200 m"}, {@code "Nos 3"}, {@code "12"}
     * @return the quantity
     * @throws ValueObjectException if it is not an amount with a known unit (or none)
     */
    public static Quantity parse(String text) {
        if (text == null) throw new ValueObjectException("A quantity is required");
        String t = text.strip();
        Matcher a = AMOUNT_FIRST.matcher(t);
        if (a.matches()) {
            UnitOfMeasure u = a.group(2) == null ? UnitOfMeasure.ONE : UnitOfMeasure.parse(a.group(2).strip());
            return new Quantity(number(a.group(1)), u);
        }
        Matcher b = UNIT_FIRST.matcher(t);
        if (b.matches()) return new Quantity(number(b.group(2)), UnitOfMeasure.parse(b.group(1).strip()));
        throw new ValueObjectException("Not a quantity: '" + text + "'");
    }

    /**
     * @param text any text
     * @return the quantity it is, or empty
     */
    public static Optional<Quantity> tryParse(@Nullable String text) {
        if (text == null || text.isBlank()) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (ValueObjectException e) {
            return Optional.empty();
        }
    }

    private static BigDecimal number(String s) {
        return new BigDecimal(s.replace(",", ""));
    }

    /**
     * @param target a unit of the same category
     * @return this amount in it
     * @throws ValueObjectException if the units do not convert
     */
    public Quantity to(UnitOfMeasure target) {
        return new Quantity(unit.convert(amount, target), target);
    }

    /**
     * @param other a quantity of the same category
     * @return the sum, in this quantity's unit
     */
    public Quantity plus(Quantity other) {
        return new Quantity(amount.add(other.to(unit).amount), unit);
    }

    /**
     * @param factor how many times
     * @return this quantity times it
     */
    public Quantity times(BigDecimal factor) {
        return new Quantity(amount.multiply(factor), unit);
    }

    /** @return whether it is zero */
    public boolean isZero() {
        return amount.signum() == 0;
    }

    /** Compares in the base unit when the units convert, else by amount then unit. */
    @Override
    public int compareTo(Quantity other) {
        if (unit.category() == other.unit.category() && unit.convertible() && other.unit.convertible()) {
            return amount.multiply(unit.toBaseUnitFactor()).compareTo(other.amount.multiply(other.unit.toBaseUnitFactor()));
        }
        int byAmount = amount.compareTo(other.amount);
        return byAmount != 0 ? byAmount : unit.compareTo(other.unit);
    }

    /** @return e.g. {@code "2.5 kg"}; a plain count is just the number */
    @Override
    public String toString() {
        return unit == UnitOfMeasure.ONE ? amount.toPlainString() : amount.toPlainString() + " " + unit.symbol();
    }
}
