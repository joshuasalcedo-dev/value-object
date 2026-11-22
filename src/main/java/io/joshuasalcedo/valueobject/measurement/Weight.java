package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing a weight/mass measurement.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Weight(
        BigDecimal value,
        WeightUnit unit
) implements ValueObject, Measurable<Weight, WeightUnit>, Comparable<Weight> {

    private static final int SCALE = 6;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    public static final Weight ZERO = new Weight(BigDecimal.ZERO, WeightUnit.KILOGRAM);

    public Weight {
        Objects.requireNonNull(value, "Weight value cannot be null");
        Objects.requireNonNull(unit, "Weight unit cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("Weight cannot be negative: " + value);
        }
    }

    // ==================== Factory Methods ====================

    public static Weight of(BigDecimal value, WeightUnit unit) {
        return new Weight(value, unit);
    }

    public static Weight of(double value, WeightUnit unit) {
        return new Weight(BigDecimal.valueOf(value), unit);
    }

    public static Weight of(long value, WeightUnit unit) {
        return new Weight(BigDecimal.valueOf(value), unit);
    }

    public static Weight zero() {
        return ZERO;
    }

    // Convenience methods for common units
    public static Weight milligrams(double value) {
        return of(value, WeightUnit.MILLIGRAM);
    }

    public static Weight grams(double value) {
        return of(value, WeightUnit.GRAM);
    }

    public static Weight kilograms(double value) {
        return of(value, WeightUnit.KILOGRAM);
    }

    public static Weight metricTons(double value) {
        return of(value, WeightUnit.METRIC_TON);
    }

    public static Weight ounces(double value) {
        return of(value, WeightUnit.OUNCE);
    }

    public static Weight pounds(double value) {
        return of(value, WeightUnit.POUND);
    }

    public static Weight stones(double value) {
        return of(value, WeightUnit.STONE);
    }

    // ==================== Conversion Methods ====================

    @Override
    public Weight convertTo(WeightUnit targetUnit) {
        if (this.unit == targetUnit) {
            return this;
        }
        BigDecimal converted = unit.convertTo(value, targetUnit);
        return new Weight(converted.setScale(SCALE, ROUNDING), targetUnit);
    }

    public Weight toGrams() {
        return convertTo(WeightUnit.GRAM);
    }

    public Weight toKilograms() {
        return convertTo(WeightUnit.KILOGRAM);
    }

    public Weight toPounds() {
        return convertTo(WeightUnit.POUND);
    }

    public Weight toOunces() {
        return convertTo(WeightUnit.OUNCE);
    }

    // ==================== Arithmetic Operations ====================

    @Override
    public Weight add(Weight other) {
        Weight otherConverted = other.convertTo(this.unit);
        return new Weight(this.value.add(otherConverted.value), this.unit);
    }

    @Override
    public Weight subtract(Weight other) {
        Weight otherConverted = other.convertTo(this.unit);
        BigDecimal result = this.value.subtract(otherConverted.value);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("Result would be negative");
        }
        return new Weight(result, this.unit);
    }

    @Override
    public Weight multiply(BigDecimal multiplier) {
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("Multiplier cannot be negative");
        }
        return new Weight(value.multiply(multiplier), unit);
    }

    public Weight multiply(double multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    @Override
    public Weight divide(BigDecimal divisor) {
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValueObjectException("Divisor must be positive");
        }
        return new Weight(value.divide(divisor, SCALE, ROUNDING), unit);
    }

    public Weight divide(double divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }

    /**
     * Returns how many times this weight contains another.
     *
     * @param other the other weight
     * @return the ratio
     */
    public BigDecimal ratio(Weight other) {
        Weight otherConverted = other.convertTo(this.unit);
        if (otherConverted.value.compareTo(BigDecimal.ZERO) == 0) {
            throw new ValueObjectException("Cannot divide by zero");
        }
        return this.value.divide(otherConverted.value, SCALE, ROUNDING);
    }

    // ==================== Comparison ====================

    @Override
    public int compareTo(Weight other) {
        Weight thisInKg = this.toKilograms();
        Weight otherInKg = other.toKilograms();
        return thisInKg.value.compareTo(otherInKg.value);
    }

    public boolean isGreaterThan(Weight other) {
        return compareTo(other) > 0;
    }

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

    @Override
    public String toString() {
        return formatted();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Weight weight)) return false;
        // Compare in base units for equality
        return this.toKilograms().value.compareTo(weight.toKilograms().value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(toKilograms().value.stripTrailingZeros());
    }
}
