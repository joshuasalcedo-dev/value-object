package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.core.ValueObject;
import io.joshuasalcedo.valueobject.core.ValueObjectException;
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
     * Checks the quantity and normalises the amount: trailing zeros are dropped ({@code 2.50} becomes
     * {@code 2.5}, {@code 1E+3} becomes {@code 1000}), and a {@code null} unit becomes {@link UnitOfMeasure#ONE}.
     *
     * @param amount how much, zero or more
     * @param unit   its unit, or {@code null} for a plain count
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
     * Creates a quantity.
     *
     * @param amount how much, zero or more
     * @param unit   its unit
     * @return the quantity
     * @throws ValueObjectException if the amount is missing or negative
     */
    public static Quantity of(BigDecimal amount, UnitOfMeasure unit) {
        return new Quantity(amount, unit);
    }

    /**
     * Creates a quantity of a whole amount.
     *
     * @param amount how much, zero or more
     * @param unit   its unit
     * @return the quantity
     * @throws ValueObjectException if the amount is negative
     */
    public static Quantity of(long amount, UnitOfMeasure unit) {
        return new Quantity(BigDecimal.valueOf(amount), unit);
    }

    /**
     * Creates a plain count, with no unit.
     *
     * @param amount a count, zero or more
     * @return it, in {@link UnitOfMeasure#ONE}
     * @throws ValueObjectException if the amount is missing or negative
     */
    public static Quantity of(BigDecimal amount) {
        return new Quantity(amount, UnitOfMeasure.ONE);
    }

    /**
     * Reads a quantity as documents print it: an amount then a unit (an {@code x} between is allowed, as in
     * {@code "3 x box"}), a unit then an amount, or an amount alone, a plain count. Grouping commas are ignored;
     * the unit is read by {@link UnitOfMeasure#parse(String)}. Negative amounts are not read.
     *
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
     * Reads a quantity like {@link #parse(String)}, without throwing.
     *
     * @param text any text
     * @return the quantity it is, or empty when it is {@code null}, blank or not a quantity
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
        try {
            return new BigDecimal(s.replace(",", ""));
        } catch (NumberFormatException e) {
            throw new ValueObjectException("Not an amount: '" + s + "'");
        }
    }

    /**
     * Converts to another unit of the same category, as {@link UnitOfMeasure#convert(BigDecimal, UnitOfMeasure)}
     * (rounded half-even to 12 decimal places).
     *
     * @param target a unit of the same category
     * @return this amount in it
     * @throws ValueObjectException if the target is {@code null} or the units do not convert
     */
    public Quantity to(UnitOfMeasure target) {
        return new Quantity(unit.convert(amount, target), target);
    }

    /**
     * Adds another quantity, converted to this one's unit.
     *
     * @param other a quantity of the same category
     * @return the sum, in this quantity's unit
     * @throws ValueObjectException if the other is {@code null} or its unit does not convert to this one
     */
    public Quantity plus(Quantity other) {
        if (other == null) throw new ValueObjectException("Cannot add null");
        return new Quantity(amount.add(other.to(unit).amount), unit);
    }

    /**
     * Multiplies the amount, keeping the unit.
     *
     * @param factor how many times, zero or more
     * @return this quantity times it
     * @throws ValueObjectException if the factor is {@code null} or negative
     */
    public Quantity times(BigDecimal factor) {
        if (factor == null) throw new ValueObjectException("A factor is required");
        return new Quantity(amount.multiply(factor), unit);
    }

    /**
     * Tells whether the amount is zero.
     *
     * @return whether it is zero
     */
    public boolean isZero() {
        return amount.signum() == 0;
    }

    /**
     * Compares in the base unit when both units are of one category and convert, else by amount, then by unit
     * (in {@link UnitOfMeasure} declaration order). So {@code 2 kg} and {@code 2000 g} compare as equal, though
     * {@link #equals(Object)} tells them apart: equality is by amount and unit, so the ordering is not
     * consistent with equals (a sorted set keeps only one of {@code 2 kg} and {@code 2000 g}).
     *
     * @param other the quantity to compare to
     * @return negative, zero or positive as this is less than, equal to or more than {@code other}
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    @Override
    public int compareTo(Quantity other) {
        if (other == null) throw new ValueObjectException("Cannot compare with null");
        if (unit.category() == other.unit.category() && unit.convertible() && other.unit.convertible()) {
            return amount.multiply(unit.toBaseUnitFactor()).compareTo(other.amount.multiply(other.unit.toBaseUnitFactor()));
        }
        int byAmount = amount.compareTo(other.amount);
        return byAmount != 0 ? byAmount : unit.compareTo(other.unit);
    }

    /**
     * Formats as the plain amount, a space and the unit's symbol.
     *
     * @return e.g. {@code "2.5 kg"}; a plain count is just the number
     */
    @Override
    public String toString() {
        return unit == UnitOfMeasure.ONE ? amount.toPlainString() : amount.toPlainString() + " " + unit.symbol();
    }

    /**
     * Tells whether text reads as a quantity, as {@link #parse(String)} reads it.
     *
     * @param text any text
     * @return whether it is a {@link Quantity}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
