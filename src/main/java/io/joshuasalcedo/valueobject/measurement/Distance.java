package io.joshuasalcedo.valueobject.measurement;

import org.jspecify.annotations.Nullable;
import io.joshuasalcedo.valueobject.core.ValueObject;

import io.joshuasalcedo.valueobject.core.ValueObjectException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A distance: a non-negative amount of a {@link DistanceUnit}. The value is kept as given. Conversions are
 * exact when the result has a finite decimal expansion, else rounded half-even to 34 significant digits
 * ({@link java.math.MathContext#DECIMAL128}). Two distances are equal, and compare, by their exact amount in meters,
 * whatever their units and however many trailing zeros they carry.
 *
 * <pre>{@code
 * Distance d = Distance.kilometers(5);
 * d.toMiles().formatted(6);   // "3.106856 mi" (the value has 34 significant digits)
 * d.add(Distance.meters(500)).formatted();   // "5.50 km"
 * }</pre>
 *
 * @param value the amount of the unit, zero or more
 * @param unit  the unit
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Distance(
        BigDecimal value,
        DistanceUnit unit
) implements ValueObject, Measurable<Distance, DistanceUnit>, Comparable<Distance> {

    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    /** Zero: {@code new Distance(BigDecimal.ZERO, DistanceUnit.METER)}. */
    public static final Distance ZERO = new Distance(BigDecimal.ZERO, DistanceUnit.METER);

    /**
     * Creates a distance. The value is kept as given, without rounding.
     *
     * @param value the amount of the unit, zero or more
     * @param unit  the unit
     * @throws ValueObjectException if either is {@code null}, or the value is negative
     */
    public Distance {
        if (value == null) throw new ValueObjectException("Distance value cannot be null");
        if (unit == null) throw new ValueObjectException("Distance unit cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("Distance cannot be negative: " + value);
        }
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a distance.
     *
     * @param value the amount of the unit, zero or more
     * @param unit  the unit
     * @return the distance
     * @throws ValueObjectException if either is {@code null}, or the value is negative
     */
    public static Distance of(BigDecimal value, DistanceUnit unit) {
        return new Distance(value, unit);
    }

    /**
     * Creates a distance from a {@code double}, converted through its decimal string form.
     *
     * @param value the amount of the unit, zero or more
     * @param unit  the unit
     * @return the distance
     * @throws ValueObjectException if the unit is {@code null}, or the value is negative, NaN or infinite
     */
    public static Distance of(double value, DistanceUnit unit) {
        return new Distance(decimal(value), unit);
    }

    /**
     * Creates a distance from a whole amount of the unit.
     *
     * @param value the amount of the unit, zero or more
     * @param unit  the unit
     * @return the distance
     * @throws ValueObjectException if the unit is {@code null}, or the value is negative
     */
    public static Distance of(long value, DistanceUnit unit) {
        return new Distance(BigDecimal.valueOf(value), unit);
    }

    /**
     * Returns a zero distance, in meters.
     *
     * @return {@link #ZERO}
     */
    public static Distance zero() {
        return ZERO;
    }

    // Convenience methods for common units
    /**
     * Creates a distance in millimeters.
     *
     * @param value the number of millimeters, zero or more
     * @return the distance
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static Distance millimeters(double value) {
        return of(value, DistanceUnit.MILLIMETER);
    }

    /**
     * Creates a distance in centimeters.
     *
     * @param value the number of centimeters, zero or more
     * @return the distance
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static Distance centimeters(double value) {
        return of(value, DistanceUnit.CENTIMETER);
    }

    /**
     * Creates a distance in meters.
     *
     * @param value the number of meters, zero or more
     * @return the distance
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static Distance meters(double value) {
        return of(value, DistanceUnit.METER);
    }

    /**
     * Creates a distance in kilometers.
     *
     * @param value the number of kilometers, zero or more
     * @return the distance
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static Distance kilometers(double value) {
        return of(value, DistanceUnit.KILOMETER);
    }

    /**
     * Creates a distance in inches.
     *
     * @param value the number of inches, zero or more
     * @return the distance
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static Distance inches(double value) {
        return of(value, DistanceUnit.INCH);
    }

    /**
     * Creates a distance in feet.
     *
     * @param value the number of feet, zero or more
     * @return the distance
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static Distance feet(double value) {
        return of(value, DistanceUnit.FOOT);
    }

    /**
     * Creates a distance in yards.
     *
     * @param value the number of yards, zero or more
     * @return the distance
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static Distance yards(double value) {
        return of(value, DistanceUnit.YARD);
    }

    /**
     * Creates a distance in miles.
     *
     * @param value the number of miles, zero or more
     * @return the distance
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static Distance miles(double value) {
        return of(value, DistanceUnit.MILE);
    }

    // ==================== Conversion Methods ====================

    /**
     * Converts to another unit, exactly when the result has a finite decimal expansion, else rounded half-even to 34
     * significant digits ({@link java.math.MathContext#DECIMAL128}), with trailing zeros dropped. The same unit returns this distance
     * unchanged.
     *
     * @param targetUnit the unit to convert to
     * @return the distance in {@code targetUnit}
     * @throws ValueObjectException if the unit is {@code null}
     */
    @Override
    public Distance convertTo(DistanceUnit targetUnit) {
        if (targetUnit == null) throw new ValueObjectException("Target unit cannot be null");
        if (this.unit == targetUnit) {
            return this;
        }
        return new Distance(unit.convertTo(value, targetUnit), targetUnit);
    }

    /**
     * Converts to meters, exactly where it can be (else to 34 significant digits); one already in meters is returned as is.
     *
     * @return the distance in meters
     */
    public Distance toMeters() {
        return convertTo(DistanceUnit.METER);
    }

    /**
     * Converts to kilometers, exactly where it can be (else to 34 significant digits); one already in kilometers is returned as is.
     *
     * @return the distance in kilometers
     */
    public Distance toKilometers() {
        return convertTo(DistanceUnit.KILOMETER);
    }

    /**
     * Converts to miles, exactly where it can be (else to 34 significant digits); one already in miles is returned as is.
     *
     * @return the distance in miles
     */
    public Distance toMiles() {
        return convertTo(DistanceUnit.MILE);
    }

    /**
     * Converts to feet, exactly where it can be (else to 34 significant digits); one already in feet is returned as is.
     *
     * @return the distance in feet
     */
    public Distance toFeet() {
        return convertTo(DistanceUnit.FOOT);
    }

    // ==================== Arithmetic Operations ====================

    /**
     * Adds another distance: the exact sum in meters, converted to this one's unit (exact where it can be, else to 34
     * significant digits).
     *
     * @param other the distance to add
     * @return the sum, in this distance's unit
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    @Override
    public Distance add(Distance other) {
        if (other == null) throw new ValueObjectException("Cannot add null");
        return new Distance(fromBase(inBase().add(other.inBase()), unit), this.unit);
    }

    /**
     * Subtracts another distance: the exact difference in meters, converted to this one's unit (exact where it can be,
     * else to 34 significant digits).
     *
     * @param other the distance to subtract
     * @return the difference, in this distance's unit
     * @throws ValueObjectException if {@code other} is {@code null}, or the result would be negative
     */
    @Override
    public Distance subtract(Distance other) {
        if (other == null) throw new ValueObjectException("Cannot subtract null");
        BigDecimal result = fromBase(inBase().subtract(other.inBase()), unit);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("Result would be negative");
        }
        return new Distance(result, this.unit);
    }

    /**
     * Multiplies by a factor, without rounding.
     *
     * @param multiplier the factor, zero or more
     * @return the product, in this distance's unit
     * @throws ValueObjectException if the factor is {@code null} or negative
     */
    @Override
    public Distance multiply(BigDecimal multiplier) {
        if (multiplier == null) throw new ValueObjectException("Multiplier cannot be null");
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("Multiplier cannot be negative");
        }
        return new Distance(value.multiply(multiplier), unit);
    }

    /**
     * Multiplies by a factor, without rounding.
     *
     * @param multiplier the factor, zero or more
     * @return the product, in this distance's unit
     * @throws ValueObjectException if the factor is negative, NaN or infinite
     */
    public Distance multiply(double multiplier) {
        return multiply(decimal(multiplier));
    }

    /**
     * Divides by a divisor, exactly when the result has a finite decimal expansion, else rounded half-even to 34
     * significant digits ({@link java.math.MathContext#DECIMAL128}).
     *
     * @param divisor the divisor, above zero
     * @return the quotient, in this distance's unit
     * @throws ValueObjectException if the divisor is {@code null}, zero or negative
     */
    @Override
    public Distance divide(BigDecimal divisor) {
        if (divisor == null || divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValueObjectException("Divisor must be positive");
        }
        return new Distance(exactOrDecimal128(value, divisor), unit);
    }

    /**
     * Divides by a divisor, as {@link #divide(BigDecimal)}.
     *
     * @param divisor the divisor, above zero
     * @return the quotient, in this distance's unit
     * @throws ValueObjectException if the divisor is zero, negative, NaN or infinite
     */
    public Distance divide(double divisor) {
        return divide(decimal(divisor));
    }

    /**
     * Returns how many times this distance contains another.
     *
     * @param other the other distance
     * @return the ratio, exactly when the result has a finite decimal expansion, else rounded half-even to 34
     * significant digits ({@link java.math.MathContext#DECIMAL128})
     * @throws ValueObjectException if {@code other} is {@code null} or zero
     */
    public BigDecimal ratio(Distance other) {
        if (other == null) throw new ValueObjectException("Cannot divide by null");
        BigDecimal otherBase = other.inBase();
        if (otherBase.compareTo(BigDecimal.ZERO) == 0) {
            throw new ValueObjectException("Cannot divide by zero");
        }
        return exactOrDecimal128(inBase(), otherBase);
    }

    // ==================== Comparison ====================

    /**
     * Compares by the exact amount in meters, whatever the units.
     *
     * @param other the distance to compare to
     * @return negative, zero or positive as this is less than, equal to or
     *         more than {@code other}
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    @Override
    public int compareTo(Distance other) {
        if (other == null) throw new ValueObjectException("Cannot compare with null");
        return inBase().compareTo(other.inBase());
    }

    /**
     * Tells whether this distance is more than another, whatever their units.
     *
     * @param other the distance to compare to
     * @return whether this is greater than {@code other}
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    public boolean isGreaterThan(Distance other) {
        return compareTo(other) > 0;
    }

    /**
     * Tells whether this distance is less than another, whatever their units.
     *
     * @param other the distance to compare to
     * @return whether this is less than {@code other}
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    public boolean isLessThan(Distance other) {
        return compareTo(other) < 0;
    }

    // ==================== Formatting ====================

    /**
     * Formats with default precision.
     *
     * @return the formatted distance
     */
    public String formatted() {
        return formatted(2);
    }

    /**
     * Formats with specified decimal places.
     *
     * @param decimals the number of decimal places
     * @return the formatted distance
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
     * Tells whether another object is a distance of the same amount, compared exactly in meters, so 1 of a
     * unit equals its equivalent in another unit, and trailing zeros do not matter.
     *
     * @param o the object to compare to
     * @return whether it is an equal distance
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Distance distance)) return false;
        return inBase().compareTo(distance.inBase()) == 0;
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}: of the amount in meters.
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

    private static BigDecimal fromBase(BigDecimal inBase, DistanceUnit unit) {
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
     * Reads a distance printed as an amount and a unit, e.g. {@code "5.2 km"}: the amount may have thousands commas, the
     * space before the unit is optional, and the unit is anything {@link DistanceUnit#parse} reads.
     *
     * @param text the text
     * @return the distance
     * @throws ValueObjectException if it is not an amount followed by a known unit
     */
    public static Distance parse(String text) {
        if (text == null) throw new ValueObjectException("Distance text is required");
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^\\s*([-+]?(?:\\d{1,3}(?:,\\d{3})+|\\d+)(?:\\.\\d+)?)\\s*(\\S.*?)\\s*$").matcher(text);
        if (!m.matches()) throw new ValueObjectException("Not a distance: " + text);
        return of(new BigDecimal(m.group(1).replace(",", "")), DistanceUnit.parse(m.group(2)));
    }

    /**
     * Reads a distance printed as an amount and a unit, if it is one.
     *
     * @param text the text, may be null
     * @return the distance, or empty
     */
    public static java.util.Optional<Distance> tryParse(@Nullable String text) {
        if (text == null) return java.util.Optional.empty();
        try {
            return java.util.Optional.of(parse(text));
        } catch (ValueObjectException e) {
            return java.util.Optional.empty();
        }
    }

    /**
     * Tells whether text is a distance printed as an amount and a unit.
     *
     * @param text the text, may be null
     * @return whether {@link #parse} reads it
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }
}
