package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing a distance measurement.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Distance(
        BigDecimal value,
        DistanceUnit unit
) implements ValueObject, Measurable<Distance, DistanceUnit>, Comparable<Distance> {

    private static final int SCALE = 6;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    public static final Distance ZERO = new Distance(BigDecimal.ZERO, DistanceUnit.METER);

    public Distance {
        Objects.requireNonNull(value, "Distance value cannot be null");
        Objects.requireNonNull(unit, "Distance unit cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("Distance cannot be negative: " + value);
        }
    }

    // ==================== Factory Methods ====================

    public static Distance of(BigDecimal value, DistanceUnit unit) {
        return new Distance(value, unit);
    }

    public static Distance of(double value, DistanceUnit unit) {
        return new Distance(BigDecimal.valueOf(value), unit);
    }

    public static Distance of(long value, DistanceUnit unit) {
        return new Distance(BigDecimal.valueOf(value), unit);
    }

    public static Distance zero() {
        return ZERO;
    }

    // Convenience methods for common units
    public static Distance millimeters(double value) {
        return of(value, DistanceUnit.MILLIMETER);
    }

    public static Distance centimeters(double value) {
        return of(value, DistanceUnit.CENTIMETER);
    }

    public static Distance meters(double value) {
        return of(value, DistanceUnit.METER);
    }

    public static Distance kilometers(double value) {
        return of(value, DistanceUnit.KILOMETER);
    }

    public static Distance inches(double value) {
        return of(value, DistanceUnit.INCH);
    }

    public static Distance feet(double value) {
        return of(value, DistanceUnit.FOOT);
    }

    public static Distance yards(double value) {
        return of(value, DistanceUnit.YARD);
    }

    public static Distance miles(double value) {
        return of(value, DistanceUnit.MILE);
    }

    // ==================== Conversion Methods ====================

    @Override
    public Distance convertTo(DistanceUnit targetUnit) {
        if (this.unit == targetUnit) {
            return this;
        }
        BigDecimal converted = unit.convertTo(value, targetUnit);
        return new Distance(converted.setScale(SCALE, ROUNDING), targetUnit);
    }

    public Distance toMeters() {
        return convertTo(DistanceUnit.METER);
    }

    public Distance toKilometers() {
        return convertTo(DistanceUnit.KILOMETER);
    }

    public Distance toMiles() {
        return convertTo(DistanceUnit.MILE);
    }

    public Distance toFeet() {
        return convertTo(DistanceUnit.FOOT);
    }

    // ==================== Arithmetic Operations ====================

    @Override
    public Distance add(Distance other) {
        Distance otherConverted = other.convertTo(this.unit);
        return new Distance(this.value.add(otherConverted.value), this.unit);
    }

    @Override
    public Distance subtract(Distance other) {
        Distance otherConverted = other.convertTo(this.unit);
        BigDecimal result = this.value.subtract(otherConverted.value);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("Result would be negative");
        }
        return new Distance(result, this.unit);
    }

    @Override
    public Distance multiply(BigDecimal multiplier) {
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("Multiplier cannot be negative");
        }
        return new Distance(value.multiply(multiplier), unit);
    }

    public Distance multiply(double multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    @Override
    public Distance divide(BigDecimal divisor) {
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValueObjectException("Divisor must be positive");
        }
        return new Distance(value.divide(divisor, SCALE, ROUNDING), unit);
    }

    public Distance divide(double divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }

    /**
     * Returns how many times this distance contains another.
     *
     * @param other the other distance
     * @return the ratio
     */
    public BigDecimal ratio(Distance other) {
        Distance otherConverted = other.convertTo(this.unit);
        if (otherConverted.value.compareTo(BigDecimal.ZERO) == 0) {
            throw new ValueObjectException("Cannot divide by zero");
        }
        return this.value.divide(otherConverted.value, SCALE, ROUNDING);
    }

    // ==================== Comparison ====================

    @Override
    public int compareTo(Distance other) {
        Distance thisInMeters = this.toMeters();
        Distance otherInMeters = other.toMeters();
        return thisInMeters.value.compareTo(otherInMeters.value);
    }

    public boolean isGreaterThan(Distance other) {
        return compareTo(other) > 0;
    }

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

    @Override
    public String toString() {
        return formatted();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Distance distance)) return false;
        // Compare in base units for equality
        return this.toMeters().value.compareTo(distance.toMeters().value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(toMeters().value.stripTrailingZeros());
    }
}
