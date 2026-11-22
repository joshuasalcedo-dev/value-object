package io.joshuasalcedo.valueobject.measurement;

import org.jspecify.annotations.Nullable;
import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A weight (mass): a non-negative amount of a {@link WeightUnit}. The value is kept as given. Conversions are
 * exact when the result has a finite decimal expansion, else rounded half-even to 34 significant digits
 * ({@link java.math.MathContext#DECIMAL128}). Two weights are equal, and compare, by their exact amount in kilograms,
 * whatever their units and however many trailing zeros they carry.
 *
 * <pre>{@code
 * Weight w = Weight.pounds(10);
 * w.toKilograms();           // 4.5359237 kg (exact)
 * w.add(Weight.ounces(8)).formatted();       // "10.50 lb"
 * }</pre>
 *
 * @param value the amount of the unit, zero or more
 * @param unit  the unit
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Weight(
        BigDecimal value,
        WeightUnit unit
) implements ValueObject, Measurable<Weight, WeightUnit>, Comparable<Weight> {

    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    /** Zero: {@code new Weight(BigDecimal.ZERO, WeightUnit.KILOGRAM)}. */
    public static final Weight ZERO = new Weight(BigDecimal.ZERO, WeightUnit.KILOGRAM);

    /**
     * Creates a weight. The value is kept as given, without rounding.
     *
     * @param value the amount of the unit, zero or more
     * @param unit  the unit
     * @throws ValueObjectException if either is {@code null}, or the value is negative
     */
    public Weight {
        if (value == null) throw new ValueObjectException("Weight value cannot be null");
        if (unit == null) throw new ValueObjectException("Weight unit cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("Weight cannot be negative: " + value);
        }
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a weight.
     *
     * @param value the amount of the unit, zero or more
     * @param unit  the unit
     * @return the weight
     * @throws ValueObjectException if either is {@code null}, or the value is negative
     */
    public static Weight of(BigDecimal value, WeightUnit unit) {
        return new Weight(value, unit);
    }

    /**
     * Creates a weight from a {@code double}, converted through its decimal string form.
     *
     * @param value the amount of the unit, zero or more
     * @param unit  the unit
     * @return the weight
     * @throws ValueObjectException if the unit is {@code null}, or the value is negative, NaN or infinite
     */
    public static Weight of(double value, WeightUnit unit) {
        return new Weight(decimal(value), unit);
    }

    /**
     * Creates a weight from a whole amount of the unit.
     *
     * @param value the amount of the unit, zero or more
     * @param unit  the unit
     * @return the weight
     * @throws ValueObjectException if the unit is {@code null}, or the value is negative
     */
    public static Weight of(long value, WeightUnit unit) {
        return new Weight(BigDecimal.valueOf(value), unit);
    }

    /**
     * Returns a zero weight, in kilograms.
     *
     * @return {@link #ZERO}
     */
    public static Weight zero() {
        return ZERO;
    }

    // Convenience methods for common units
    /**
     * Creates a weight in milligrams.
     *
     * @param value the number of milligrams, zero or more
     * @return the weight
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static Weight milligrams(double value) {
        return of(value, WeightUnit.MILLIGRAM);
    }

    /**
     * Creates a weight in grams.
     *
     * @param value the number of grams, zero or more
     * @return the weight
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static Weight grams(double value) {
        return of(value, WeightUnit.GRAM);
    }

    /**
     * Creates a weight in kilograms.
     *
     * @param value the number of kilograms, zero or more
     * @return the weight
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static Weight kilograms(double value) {
        return of(value, WeightUnit.KILOGRAM);
    }

    /**
     * Creates a weight in metric tons.
     *
     * @param value the number of metric tons, zero or more
     * @return the weight
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static Weight metricTons(double value) {
        return of(value, WeightUnit.METRIC_TON);
    }

    /**
     * Creates a weight in ounces.
     *
     * @param value the number of ounces, zero or more
     * @return the weight
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static Weight ounces(double value) {
        return of(value, WeightUnit.OUNCE);
    }

    /**
     * Creates a weight in pounds.
     *
     * @param value the number of pounds, zero or more
     * @return the weight
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static Weight pounds(double value) {
        return of(value, WeightUnit.POUND);
    }

    /**
     * Creates a weight in stones.
     *
     * @param value the number of stones, zero or more
     * @return the weight
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static Weight stones(double value) {
        return of(value, WeightUnit.STONE);
    }

    // ==================== Conversion Methods ====================

    /**
     * Converts to another unit, exactly when the result has a finite decimal expansion, else rounded half-even to 34
     * significant digits ({@link java.math.MathContext#DECIMAL128}), with trailing zeros dropped. The same unit returns this weight
     * unchanged.
     *
     * @param targetUnit the unit to convert to
     * @return the weight in {@code targetUnit}
     * @throws ValueObjectException if the unit is {@code null}
     */
    @Override
    public Weight convertTo(WeightUnit targetUnit) {
        if (targetUnit == null) throw new ValueObjectException("Target unit cannot be null");
        if (this.unit == targetUnit) {
            return this;
        }
        return new Weight(unit.convertTo(value, targetUnit), targetUnit);
    }

    /**
     * Converts to grams, exactly where it can be (else to 34 significant digits); one already in grams is returned as is.
     *
     * @return the weight in grams
     */
    public Weight toGrams() {
        return convertTo(WeightUnit.GRAM);
    }

    /**
     * Converts to kilograms, exactly where it can be (else to 34 significant digits); one already in kilograms is returned as is.
     *
     * @return the weight in kilograms
     */
    public Weight toKilograms() {
        return convertTo(WeightUnit.KILOGRAM);
    }

    /**
     * Converts to pounds, exactly where it can be (else to 34 significant digits); one already in pounds is returned as is.
     *
     * @return the weight in pounds
     */
    public Weight toPounds() {
        return convertTo(WeightUnit.POUND);
    }

    /**
     * Converts to ounces, exactly where it can be (else to 34 significant digits); one already in ounces is returned as is.
     *
     * @return the weight in ounces
     */
    public Weight toOunces() {
        return convertTo(WeightUnit.OUNCE);
    }

    // ==================== Arithmetic Operations ====================

    /**
     * Adds another weight: the exact sum in kilograms, converted to this one's unit (exact where it can be, else to 34
     * significant digits).
     *
     * @param other the weight to add
     * @return the sum, in this weight's unit
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    @Override
    public Weight add(Weight other) {
        if (other == null) throw new ValueObjectException("Cannot add null");
        return new Weight(fromBase(inBase().add(other.inBase()), unit), this.unit);
    }

    /**
     * Subtracts another weight: the exact difference in kilograms, converted to this one's unit (exact where it can be,
     * else to 34 significant digits).
     *
     * @param other the weight to subtract
     * @return the difference, in this weight's unit
     * @throws ValueObjectException if {@code other} is {@code null}, or the result would be negative
     */
    @Override
    public Weight subtract(Weight other) {
        if (other == null) throw new ValueObjectException("Cannot subtract null");
        BigDecimal result = fromBase(inBase().subtract(other.inBase()), unit);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("Result would be negative");
        }
        return new Weight(result, this.unit);
    }

    /**
     * Multiplies by a factor, without rounding.
     *
     * @param multiplier the factor, zero or more
     * @return the product, in this weight's unit
     * @throws ValueObjectException if the factor is {@code null} or negative
     */
    @Override
    public Weight multiply(BigDecimal multiplier) {
        if (multiplier == null) throw new ValueObjectException("Multiplier cannot be null");
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("Multiplier cannot be negative");
        }
        return new Weight(value.multiply(multiplier), unit);
    }

    /**
     * Multiplies by a factor, without rounding.
     *
     * @param multiplier the factor, zero or more
     * @return the product, in this weight's unit
     * @throws ValueObjectException if the factor is negative, NaN or infinite
     */
    public Weight multiply(double multiplier) {
        return multiply(decimal(multiplier));
    }

    /**
     * Divides by a divisor, exactly when the result has a finite decimal expansion, else rounded half-even to 34
     * significant digits ({@link java.math.MathContext#DECIMAL128}).
     *
     * @param divisor the divisor, above zero
     * @return the quotient, in this weight's unit
     * @throws ValueObjectException if the divisor is {@code null}, zero or negative
     */
    @Override
    public Weight divide(BigDecimal divisor) {
        if (divisor == null || divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValueObjectException("Divisor must be positive");
        }
        return new Weight(exactOrDecimal128(value, divisor), unit);
    }

    /**
     * Divides by a divisor, as {@link #divide(BigDecimal)}.
     *
     * @param divisor the divisor, above zero
     * @return the quotient, in this weight's unit
     * @throws ValueObjectException if the divisor is zero, negative, NaN or infinite
     */
    public Weight divide(double divisor) {
        return divide(decimal(divisor));
    }

    /**
     * Returns how many times this weight contains another.
     *
     * @param other the other weight
     * @return the ratio, exact when it has a finite decimal expansion, else rounded half-even to 34 significant
     *         digits
     * @throws ValueObjectException if {@code other} is {@code null} or zero
     */
    public BigDecimal ratio(Weight other) {
        if (other == null) throw new ValueObjectException("Cannot divide by null");
        BigDecimal otherBase = other.inBase();
        if (otherBase.compareTo(BigDecimal.ZERO) == 0) {
            throw new ValueObjectException("Cannot divide by zero");
        }
        return exactOrDecimal128(inBase(), otherBase);
    }

    // ==================== Comparison ====================

    /**
     * Compares by the exact amount in kilograms, whatever the units.
     *
     * @param other the weight to compare to
     * @return negative, zero or positive as this is less than, equal to or
     *         more than {@code other}
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    @Override
    public int compareTo(Weight other) {
        if (other == null) throw new ValueObjectException("Cannot compare with null");
        return inBase().compareTo(other.inBase());
    }

    /**
     * Tells whether this weight is more than another, whatever their units.
     *
     * @param other the weight to compare to
     * @return whether this is greater than {@code other}
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    public boolean isGreaterThan(Weight other) {
        return compareTo(other) > 0;
    }

    /**
     * Tells whether this weight is less than another, whatever their units.
     *
     * @param other the weight to compare to
     * @return whether this is less than {@code other}
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    public boolean isLessThan(Weight other) {
        return compareTo(other) < 0;
    }

    // ==================== Formatting ====================

    /**
     * Formats with default precision.
     *
     * @return the formatted weight
     */
    public String formatted() {
        return formatted(2);
    }

    /**
     * Formats with specified decimal places.
     *
     * @param decimals the number of decimal places
     * @return the formatted weight
     */
    public String formatted(int decimals) {
        return value.setScale(decimals, ROUNDING).toPlainString() + " " + unit.symbol();
    }

    /**
     * Formats with 2 decimal places, as {@link #formatted()}.
     *
     * @return the value and unit symbol
     */
    @Override
    public String toString() {
        return formatted();
    }

    /**
     * Tells whether another object is a weight of the same amount, compared exactly in kilograms, so 1 of a
     * unit equals its equivalent in another unit, and trailing zeros do not matter.
     *
     * @param o the object to compare to
     * @return whether it is an equal weight
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Weight weight)) return false;
        return inBase().compareTo(weight.inBase()) == 0;
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}: of the amount in kilograms.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return inBase().stripTrailingZeros().hashCode();
    }

    private BigDecimal inBase() {
        return value.multiply(unit.toBaseUnitFactor());
    }

    private static BigDecimal fromBase(BigDecimal inBase, WeightUnit unit) {
        return exactOrDecimal128(inBase, unit.toBaseUnitFactor());
    }

    private static BigDecimal exactOrDecimal128(BigDecimal dividend, BigDecimal divisor) {
        BigDecimal q;
        try {
            q = dividend.divide(divisor);
        } catch (ArithmeticException nonTerminating) {
            q = dividend.divide(divisor, java.math.MathContext.DECIMAL128);
        }
        q = q.stripTrailingZeros();
        return q.scale() < 0 ? q.setScale(0) : q;
    }

    private static BigDecimal decimal(double d) {
        if (Double.isNaN(d) || Double.isInfinite(d)) throw new ValueObjectException("Not a finite number: " + d);
        return BigDecimal.valueOf(d);
    }

    /**
     * Reads a weight printed as an amount and a unit, e.g. {@code "2.5 kg"}: the amount may have thousands commas, the
     * space before the unit is optional, and the unit is anything {@link WeightUnit#parse} reads.
     *
     * @param text the text
     * @return the weight
     * @throws ValueObjectException if it is not an amount followed by a known unit
     */
    public static Weight parse(String text) {
        if (text == null) throw new ValueObjectException("Weight text is required");
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^\\s*([-+]?(?:\\d{1,3}(?:,\\d{3})+|\\d+)(?:\\.\\d+)?)\\s*(\\S.*?)\\s*$").matcher(text);
        if (!m.matches()) throw new ValueObjectException("Not a weight: " + text);
        return of(new BigDecimal(m.group(1).replace(",", "")), WeightUnit.parse(m.group(2)));
    }

    /**
     * Reads a weight printed as an amount and a unit, if it is one.
     *
     * @param text the text, may be null
     * @return the weight, or empty
     */
    public static java.util.Optional<Weight> tryParse(@Nullable String text) {
        if (text == null) return java.util.Optional.empty();
        try {
            return java.util.Optional.of(parse(text));
        } catch (ValueObjectException e) {
            return java.util.Optional.empty();
        }
    }

    /**
     * Tells whether text is a weight printed as an amount and a unit.
     *
     * @param text the text, may be null
     * @return whether {@link #parse} reads it
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }
}
