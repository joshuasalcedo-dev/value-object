package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.Count;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Percentage(
        BigDecimal value
) implements ValueObject, Comparable<Percentage> {

    public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;
    private static final int DEFAULT_SCALE = 4;

    public static final Percentage ZERO = new Percentage(BigDecimal.ZERO);
    public static final Percentage ONE = new Percentage(BigDecimal.ONE);
    public static final Percentage TEN = new Percentage(BigDecimal.TEN);
    public static final Percentage TWENTY_FIVE = new Percentage(BigDecimal.valueOf(25));
    public static final Percentage FIFTY = new Percentage(BigDecimal.valueOf(50));
    public static final Percentage SEVENTY_FIVE = new Percentage(BigDecimal.valueOf(75));
    public static final Percentage HUNDRED = new Percentage(BigDecimal.valueOf(100));

    public Percentage {
        Objects.requireNonNull(value, "Percentage value cannot be null");
        value = value.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    // ==================== Factory Methods ====================

    public static Percentage of(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) == 0) return ZERO;
        if (value.compareTo(BigDecimal.valueOf(100)) == 0) return HUNDRED;
        return new Percentage(value);
    }

    public static Percentage of(double value) {
        return of(BigDecimal.valueOf(value));
    }

    public static Percentage of(long value) {
        return of(BigDecimal.valueOf(value));
    }

    /**
     * @param text a number with or without a trailing {@code %}, e.g. {@code "12.5%"} or {@code "12.5"}
     * @return the percentage
     * @throws ValueObjectException if it is not a number
     */
    public static Percentage parse(String text) {
        Objects.requireNonNull(text, "Percentage text cannot be null");
        String t = text.trim();
        if (t.endsWith("%")) t = t.substring(0, t.length() - 1).trim();
        return of(t);
    }

    public static Percentage of(String value) {
        try {
            return of(new BigDecimal(value));
        } catch (NumberFormatException e) {
            throw new ValueObjectException("Invalid percentage value: " + value);
        }
    }

    public static Percentage zero() {
        return ZERO;
    }

    public static Percentage hundred() {
        return HUNDRED;
    }

    /**
     * Creates percentage from a decimal (0.25 -> 25%)
     */
    public static Percentage fromDecimal(BigDecimal decimal) {
        return of(decimal.multiply(BigDecimal.valueOf(100)));
    }

    public static Percentage fromDecimal(double decimal) {
        return fromDecimal(BigDecimal.valueOf(decimal));
    }

    /**
     * Creates percentage from a fraction (3/4 -> 75%)
     */
    public static Percentage fromFraction(long numerator, long denominator) {
        if (denominator == 0) {
            throw new ValueObjectException("Denominator cannot be zero");
        }
        return of(BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), DEFAULT_SCALE, DEFAULT_ROUNDING));
    }

    /**
     * Creates percentage from two counts (7 of 10 -> 70%)
     */
    public static Percentage fromRatio(Count part, Count total) {
        if (total.isZero()) {
            throw new ValueObjectException("Total cannot be zero");
        }
        return fromFraction(part.toLong(), total.toLong());
    }

    // ==================== Arithmetic Operations ====================

    public Percentage add(Percentage other) {
        return of(value.add(other.value));
    }

    public Percentage add(double amount) {
        return of(value.add(BigDecimal.valueOf(amount)));
    }

    public Percentage subtract(Percentage other) {
        return of(value.subtract(other.value));
    }

    public Percentage subtract(double amount) {
        return of(value.subtract(BigDecimal.valueOf(amount)));
    }

    public Percentage multiply(BigDecimal multiplier) {
        return of(value.multiply(multiplier));
    }

    public Percentage multiply(double multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    public Percentage divide(BigDecimal divisor) {
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ValueObjectException("Cannot divide by zero");
        }
        return of(value.divide(divisor, DEFAULT_SCALE, DEFAULT_ROUNDING));
    }

    public Percentage divide(double divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }

    public Percentage negate() {
        return of(value.negate());
    }

    public Percentage abs() {
        return of(value.abs());
    }

    public Percentage complement() {
        return of(BigDecimal.valueOf(100).subtract(value));
    }

    public Percentage min(Percentage other) {
        return value.compareTo(other.value) <= 0 ? this : other;
    }

    public Percentage max(Percentage other) {
        return value.compareTo(other.value) >= 0 ? this : other;
    }

    // ==================== Application Methods ====================

    /**
     * Applies this percentage to an amount (e.g., 25% of 200 = 50)
     */
    public BigDecimal applyTo(BigDecimal amount) {
        return amount.multiply(toDecimal());
    }

    public double applyTo(double amount) {
        return amount * toDecimalDouble();
    }

    public long applyTo(long amount) {
        return Math.round(amount * toDecimalDouble());
    }

    /**
     * Applies this percentage to Money
     */
    public Money applyTo(Money money) {
        return money.percentage(value);
    }

    /**
     * Calculates what value this percentage represents of a total
     * (e.g., if this is 25% and total is 200, returns 50)
     */
    public BigDecimal valueOfTotal(BigDecimal total) {
        return applyTo(total);
    }

    /**
     * Calculates what total this percentage's value represents
     * (e.g., if this is 25% and value is 50, total is 200)
     */
    public BigDecimal totalFromValue(BigDecimal partValue) {
        if (isZero()) {
            throw new ValueObjectException("Cannot calculate total from zero percentage");
        }
        return partValue.divide(toDecimal(), DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    // ==================== Comparison Operations ====================

    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isHundred() {
        return value.compareTo(BigDecimal.valueOf(100)) == 0;
    }

    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isNegative() {
        return value.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isGreaterThan(Percentage other) {
        return value.compareTo(other.value) > 0;
    }

    public boolean isGreaterThan(double other) {
        return value.compareTo(BigDecimal.valueOf(other)) > 0;
    }

    public boolean isGreaterThanOrEqual(Percentage other) {
        return value.compareTo(other.value) >= 0;
    }

    public boolean isLessThan(Percentage other) {
        return value.compareTo(other.value) < 0;
    }

    public boolean isLessThan(double other) {
        return value.compareTo(BigDecimal.valueOf(other)) < 0;
    }

    public boolean isLessThanOrEqual(Percentage other) {
        return value.compareTo(other.value) <= 0;
    }

    public boolean isBetween(Percentage min, Percentage max) {
        return value.compareTo(min.value) >= 0 && value.compareTo(max.value) <= 0;
    }

    public boolean isBetween(double min, double max) {
        return value.compareTo(BigDecimal.valueOf(min)) >= 0
                && value.compareTo(BigDecimal.valueOf(max)) <= 0;
    }

    public boolean isWithinRange() {
        return isBetween(0, 100);
    }

    @Override
    public int compareTo(Percentage other) {
        return value.compareTo(other.value);
    }

    // ==================== Conversion ====================

    public BigDecimal toDecimal() {
        return value.divide(BigDecimal.valueOf(100), DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    public double toDecimalDouble() {
        return value.doubleValue() / 100.0;
    }

    public double toDouble() {
        return value.doubleValue();
    }

    public int toIntRounded() {
        return value.setScale(0, DEFAULT_ROUNDING).intValue();
    }

    // ==================== Formatting ====================

    @Override
    public String toString() {
        return format(2);
    }

    public String format() {
        return format(2);
    }

    public String format(int decimalPlaces) {
        return value.setScale(decimalPlaces, DEFAULT_ROUNDING).toPlainString() + "%";
    }

    public String formatWithSign() {
        String prefix = isPositive() ? "+" : "";
        return prefix + format();
    }

    public String toProgressBar(int length) {
        int filled = (int) Math.round(length * toDecimalDouble());
        filled = Math.max(0, Math.min(length, filled));
        return "█".repeat(filled) + "░".repeat(length - filled);
    }

    public String toProgressBarWithLabel(int length) {
        return toProgressBar(length) + " " + format(1);
    }

    // ==================== Equality ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Percentage that)) return false;
        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.stripTrailingZeros());
    }
}
