package io.joshuasalcedo.valueobject.financial;

import org.jspecify.annotations.Nullable;
import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import io.joshuasalcedo.valueobject.core.Count;
import io.joshuasalcedo.valueobject.core.ValueObjectException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * A percentage, held as the number of percent ({@code 12.5} for 12.5%) at 4 decimal places, rounded half-even.
 * It is not limited to 0-100: negative values and values over 100 are allowed (see {@link #isWithinRange()}).
 *
 * <pre>{@code
 * Percentage vat = Percentage.parse("12%");
 * vat.applyTo(new BigDecimal("200"));     // 24.000000 (exact: 200 x 0.120000)
 * vat.applyTo(Money.usd(200));            // $24.00
 * Percentage.fromFraction(3, 4);          // 75.00%
 * }</pre>
 *
 * @param value the number of percent, at 4 decimal places
 */
public record Percentage(
        BigDecimal value
) implements ValueObject, Comparable<Percentage> {

    /** Default rounding: {@code RoundingMode.HALF_EVEN}. */
    public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;
    private static final int DEFAULT_SCALE = 4;

    /** Zero: {@code new Percentage(BigDecimal.ZERO)}. */
    public static final Percentage ZERO = new Percentage(BigDecimal.ZERO);
    /** One: {@code new Percentage(BigDecimal.ONE)}. */
    public static final Percentage ONE = new Percentage(BigDecimal.ONE);
    /** Ten: {@code new Percentage(BigDecimal.TEN)}. */
    public static final Percentage TEN = new Percentage(BigDecimal.TEN);
    /** Twenty five: {@code new Percentage(BigDecimal.valueOf(25))}. */
    public static final Percentage TWENTY_FIVE = new Percentage(BigDecimal.valueOf(25));
    /** Fifty: {@code new Percentage(BigDecimal.valueOf(50))}. */
    public static final Percentage FIFTY = new Percentage(BigDecimal.valueOf(50));
    /** Seventy five: {@code new Percentage(BigDecimal.valueOf(75))}. */
    public static final Percentage SEVENTY_FIVE = new Percentage(BigDecimal.valueOf(75));
    /** Hundred: {@code new Percentage(BigDecimal.valueOf(100))}. */
    public static final Percentage HUNDRED = new Percentage(BigDecimal.valueOf(100));

    /**
     * Creates a percentage, rounding the value half-even to 4 decimal places.
     *
     * @param value the number of percent, e.g. {@code 12.5} for 12.5%
     * @throws ValueObjectException if the value is {@code null}
     */
    public Percentage {
        if (value == null) throw new ValueObjectException("Percentage value cannot be null");
        value = value.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a percentage from a number of percent; 0 and 100 return {@link #ZERO} and {@link #HUNDRED}.
     *
     * @param value the number of percent, e.g. {@code 12.5} for 12.5%
     * @return the percentage
     * @throws ValueObjectException if the value is {@code null}
     */
    public static Percentage of(BigDecimal value) {
        if (value == null) throw new ValueObjectException("Percentage value cannot be null");
        if (value.compareTo(BigDecimal.ZERO) == 0) return ZERO;
        if (value.compareTo(BigDecimal.valueOf(100)) == 0) return HUNDRED;
        return new Percentage(value);
    }

    /**
     * Creates a percentage from a number of percent, converted through its decimal string form.
     *
     * @param value the number of percent, e.g. {@code 12.5} for 12.5%
     * @return the percentage
     * @throws ValueObjectException if the value is NaN or infinite
     */
    public static Percentage of(double value) {
        return of(decimal(value));
    }

    /**
     * Creates a percentage from a whole number of percent.
     *
     * @param value the number of percent, e.g. {@code 25} for 25%
     * @return the percentage
     */
    public static Percentage of(long value) {
        return of(BigDecimal.valueOf(value));
    }

    /**
     * Parses a percentage written as a number, with or without a trailing {@code %}.
     *
     * @param text a number with or without a trailing {@code %}, e.g. {@code "12.5%"} or {@code "12.5"}
     * @return the percentage
     * @throws ValueObjectException if it is {@code null} or not a number
     */
    public static Percentage parse(String text) {
        if (text == null) throw new ValueObjectException("Percentage text cannot be null");
        String t = text.trim();
        if (t.endsWith("%")) t = t.substring(0, t.length() - 1).trim();
        return of(t);
    }

    /**
     * Creates a percentage from a plain decimal string, without a {@code %} sign (see {@link #parse(String)}).
     *
     * @param value the number of percent, e.g. {@code "12.5"}
     * @return the percentage
     * @throws ValueObjectException if it is {@code null} or not a number
     */
    public static Percentage of(String value) {
        if (value == null) throw new ValueObjectException("Percentage value cannot be null");
        try {
            return of(new BigDecimal(value.trim()));
        } catch (NumberFormatException e) {
            throw new ValueObjectException("Invalid percentage value: " + value);
        }
    }

    /**
     * Returns 0%.
     *
     * @return {@link #ZERO}
     */
    public static Percentage zero() {
        return ZERO;
    }

    /**
     * Returns 100%.
     *
     * @return {@link #HUNDRED}
     */
    public static Percentage hundred() {
        return HUNDRED;
    }

    /**
     * Creates a percentage from a decimal fraction: 0.25 is 25%.
     *
     * @param decimal the fraction of one
     * @return the percentage
     * @throws ValueObjectException if the fraction is {@code null}
     */
    public static Percentage fromDecimal(BigDecimal decimal) {
        if (decimal == null) throw new ValueObjectException("Decimal fraction cannot be null");
        return of(decimal.movePointRight(2));
    }

    /**
     * Creates a percentage from a decimal fraction: 0.25 is 25%.
     *
     * @param decimal the fraction of one
     * @return the percentage
     * @throws ValueObjectException if the fraction is NaN or infinite
     */
    public static Percentage fromDecimal(double decimal) {
        return fromDecimal(decimal(decimal));
    }

    /**
     * Creates a percentage from a fraction: 3/4 is 75%. The result is rounded half-even to 4 decimal places.
     *
     * @param numerator   the top of the fraction
     * @param denominator the bottom of the fraction
     * @return the percentage
     * @throws ValueObjectException if the denominator is zero
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
     * Creates a percentage from a part of a total: 7 of 10 is 70%.
     *
     * @param part  the part
     * @param total the total
     * @return the part as a percentage of the total
     * @throws ValueObjectException if either is {@code null} or the total is zero
     */
    public static Percentage fromRatio(Count part, Count total) {
        if (part == null || total == null) throw new ValueObjectException("Part and total cannot be null");
        if (total.isZero()) {
            throw new ValueObjectException("Total cannot be zero");
        }
        return fromFraction(part.toLong(), total.toLong());
    }

    // ==================== Arithmetic Operations ====================

    /**
     * Adds percentage points: 10% plus 5% is 15%.
     *
     * @param other the percentage to add
     * @return the sum
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    public Percentage add(Percentage other) {
        return of(value.add(require(other).value));
    }

    /**
     * Adds percentage points: 10% plus 5 is 15%.
     *
     * @param amount the number of points to add
     * @return the sum
     * @throws ValueObjectException if the amount is NaN or infinite
     */
    public Percentage add(double amount) {
        return of(value.add(decimal(amount)));
    }

    /**
     * Subtracts percentage points: 15% less 5% is 10%. The result may be negative.
     *
     * @param other the percentage to subtract
     * @return the difference
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    public Percentage subtract(Percentage other) {
        return of(value.subtract(require(other).value));
    }

    /**
     * Subtracts percentage points: 15% less 5 is 10%. The result may be negative.
     *
     * @param amount the number of points to subtract
     * @return the difference
     * @throws ValueObjectException if the amount is NaN or infinite
     */
    public Percentage subtract(double amount) {
        return of(value.subtract(decimal(amount)));
    }

    /**
     * Multiplies the number of percent: 10% times 1.5 is 15%. Rounded half-even to 4 decimal places.
     *
     * @param multiplier the factor
     * @return the product
     * @throws ValueObjectException if the factor is {@code null}
     */
    public Percentage multiply(BigDecimal multiplier) {
        if (multiplier == null) throw new ValueObjectException("Multiplier cannot be null");
        return of(value.multiply(multiplier));
    }

    /**
     * Multiplies the number of percent: 10% times 1.5 is 15%. Rounded half-even to 4 decimal places.
     *
     * @param multiplier the factor
     * @return the product
     * @throws ValueObjectException if the factor is NaN or infinite
     */
    public Percentage multiply(double multiplier) {
        return multiply(decimal(multiplier));
    }

    /**
     * Divides the number of percent: 15% divided by 3 is 5%. Rounded half-even to 4 decimal places.
     *
     * @param divisor the divisor
     * @return the quotient
     * @throws ValueObjectException if the divisor is {@code null} or zero
     */
    public Percentage divide(BigDecimal divisor) {
        if (divisor == null) throw new ValueObjectException("Divisor cannot be null");
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ValueObjectException("Cannot divide by zero");
        }
        return of(value.divide(divisor, DEFAULT_SCALE, DEFAULT_ROUNDING));
    }

    /**
     * Divides the number of percent: 15% divided by 3 is 5%. Rounded half-even to 4 decimal places.
     *
     * @param divisor the divisor
     * @return the quotient
     * @throws ValueObjectException if the divisor is zero, NaN or infinite
     */
    public Percentage divide(double divisor) {
        return divide(decimal(divisor));
    }

    /**
     * Flips the sign: 10% becomes -10%.
     *
     * @return the negated percentage
     */
    public Percentage negate() {
        return of(value.negate());
    }

    /**
     * Drops the sign: -10% becomes 10%.
     *
     * @return the absolute percentage
     */
    public Percentage abs() {
        return of(value.abs());
    }

    /**
     * Returns what is left of 100%: the complement of 30% is 70% (and of 120% is -20%).
     *
     * @return {@code 100 - value} percent
     */
    public Percentage complement() {
        return of(BigDecimal.valueOf(100).subtract(value));
    }

    /**
     * Returns the smaller of this and another percentage; this one when they are equal.
     *
     * @param other the percentage to compare to
     * @return the smaller percentage
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    public Percentage min(Percentage other) {
        return value.compareTo(require(other).value) <= 0 ? this : other;
    }

    /**
     * Returns the larger of this and another percentage; this one when they are equal.
     *
     * @param other the percentage to compare to
     * @return the larger percentage
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    public Percentage max(Percentage other) {
        return value.compareTo(require(other).value) >= 0 ? this : other;
    }

    // ==================== Application Methods ====================

    /**
     * Works out this percentage of an amount exactly: 25% of 200 is 50. The amount is multiplied by
     * {@link #toDecimal()}, the exact fraction, and the product is not rounded (12.3456% of 100 is
     * {@code 12.345600}).
     *
     * @param amount the amount
     * @return this percentage of the amount
     * @throws ValueObjectException if the amount is {@code null}
     */
    public BigDecimal applyTo(BigDecimal amount) {
        if (amount == null) throw new ValueObjectException("Amount cannot be null");
        return amount.multiply(toDecimal());
    }

    /**
     * Works out this percentage of an amount in floating point: 25% of 200 is 50.0.
     *
     * @param amount the amount
     * @return this percentage of the amount
     */
    public double applyTo(double amount) {
        return amount * toDecimalDouble();
    }

    /**
     * Works out this percentage of a whole amount exactly, then rounds to the nearest whole number, halves away
     * from zero: 25% of 10 is 3, 25% of -10 is -3.
     *
     * @param amount the amount
     * @return this percentage of the amount, rounded
     * @throws ValueObjectException if the result does not fit in a {@code long}
     */
    public long applyTo(long amount) {
        try {
            return BigDecimal.valueOf(amount).multiply(toDecimal()).setScale(0, RoundingMode.HALF_UP).longValueExact();
        } catch (ArithmeticException overflow) {
            throw new ValueObjectException("Result does not fit in a long: " + this + " of " + amount);
        }
    }

    /**
     * Works out this percentage of money, rounded half-even to the currency's decimal places,
     * as {@link Money#percentage(BigDecimal)}.
     *
     * @param money the money
     * @return this percentage of it, in the same currency
     * @throws ValueObjectException if the money is {@code null}
     */
    public Money applyTo(Money money) {
        if (money == null) throw new ValueObjectException("Money cannot be null");
        return money.percentage(value);
    }

    /**
     * Works out the part of a total this percentage stands for, the same as {@link #applyTo(BigDecimal)}:
     * 25% of 200 is 50.
     *
     * @param total the total
     * @return this percentage of the total
     * @throws ValueObjectException if the total is {@code null}
     */
    public BigDecimal valueOfTotal(BigDecimal total) {
        return applyTo(total);
    }

    /**
     * Works out the total that a part stands for, when the part is this percentage of it: if 50 is 25%, the
     * total is 200. Worked out from the full percentage (no rounding before the division) and rounded half-even
     * to 4 decimal places: if 1 is 0.004%, the total is 25000.0000.
     *
     * @param partValue the part
     * @return the total
     * @throws ValueObjectException if the part is {@code null} or this percentage is zero
     */
    public BigDecimal totalFromValue(BigDecimal partValue) {
        if (partValue == null) throw new ValueObjectException("Part value cannot be null");
        if (isZero()) {
            throw new ValueObjectException("Cannot calculate total from zero percentage");
        }
        return partValue.movePointRight(2).divide(value, DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

    // ==================== Comparison Operations ====================

    /**
     * Tells whether this is 0%.
     *
     * @return whether the value is zero
     */
    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Tells whether this is 100%.
     *
     * @return whether the value is 100
     */
    public boolean isHundred() {
        return value.compareTo(BigDecimal.valueOf(100)) == 0;
    }

    /**
     * Tells whether this is above 0%.
     *
     * @return whether the value is greater than zero
     */
    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Tells whether this is below 0%.
     *
     * @return whether the value is less than zero
     */
    public boolean isNegative() {
        return value.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Tells whether this is more than another percentage.
     *
     * @param other the percentage to compare to
     * @return whether this is greater than {@code other}
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    public boolean isGreaterThan(Percentage other) {
        return value.compareTo(require(other).value) > 0;
    }

    /**
     * Tells whether this is more than a number of percent.
     *
     * @param other the number of percent to compare to, e.g. {@code 50} for 50%
     * @return whether this is greater than {@code other}
     * @throws ValueObjectException if {@code other} is NaN or infinite
     */
    public boolean isGreaterThan(double other) {
        return value.compareTo(decimal(other)) > 0;
    }

    /**
     * Tells whether this is at least another percentage.
     *
     * @param other the percentage to compare to
     * @return whether this is greater than or equal to {@code other}
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    public boolean isGreaterThanOrEqual(Percentage other) {
        return value.compareTo(require(other).value) >= 0;
    }

    /**
     * Tells whether this is less than another percentage.
     *
     * @param other the percentage to compare to
     * @return whether this is less than {@code other}
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    public boolean isLessThan(Percentage other) {
        return value.compareTo(require(other).value) < 0;
    }

    /**
     * Tells whether this is less than a number of percent.
     *
     * @param other the number of percent to compare to, e.g. {@code 50} for 50%
     * @return whether this is less than {@code other}
     * @throws ValueObjectException if {@code other} is NaN or infinite
     */
    public boolean isLessThan(double other) {
        return value.compareTo(decimal(other)) < 0;
    }

    /**
     * Tells whether this is at most another percentage.
     *
     * @param other the percentage to compare to
     * @return whether this is less than or equal to {@code other}
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    public boolean isLessThanOrEqual(Percentage other) {
        return value.compareTo(require(other).value) <= 0;
    }

    /**
     * Tells whether this lies between two percentages, both inclusive.
     *
     * @param min the lower bound
     * @param max the upper bound
     * @return whether {@code min <= this <= max}
     * @throws ValueObjectException if either bound is {@code null}
     */
    public boolean isBetween(Percentage min, Percentage max) {
        return value.compareTo(require(min).value) >= 0 && value.compareTo(require(max).value) <= 0;
    }

    /**
     * Tells whether this lies between two numbers of percent, both inclusive.
     *
     * @param min the lower bound, in percent
     * @param max the upper bound, in percent
     * @return whether {@code min <= this <= max}
     * @throws ValueObjectException if either bound is NaN or infinite
     */
    public boolean isBetween(double min, double max) {
        return value.compareTo(decimal(min)) >= 0
                && value.compareTo(decimal(max)) <= 0;
    }

    /**
     * Tells whether this is a proportion of a whole: from 0% to 100%, both inclusive.
     *
     * @return whether {@code 0 <= value <= 100}
     */
    public boolean isWithinRange() {
        return isBetween(0, 100);
    }

    /**
     * Compares by the number of percent.
     *
     * @param other the percentage to compare to
     * @return negative, zero or positive as this is less than, equal to or more than {@code other}
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    @Override
    public int compareTo(Percentage other) {
        return value.compareTo(require(other).value);
    }

    // ==================== Conversion ====================

    /**
     * Returns this as a decimal fraction of one, exactly the value divided by 100 (so at 6 decimal places):
     * 25% is {@code 0.250000}, 12.3456% is {@code 0.123456}, 0.0001% is {@code 0.000001}.
     *
     * @return the value divided by 100, not rounded
     */
    public BigDecimal toDecimal() {
        return value.movePointLeft(2);
    }

    /**
     * Returns this as a decimal fraction of one in floating point: 25% is {@code 0.25}.
     *
     * @return the value divided by 100
     */
    public double toDecimalDouble() {
        return value.doubleValue() / 100.0;
    }

    /**
     * Returns the number of percent in floating point: 25% is {@code 25.0}.
     *
     * @return the value as a {@code double}
     */
    public double toDouble() {
        return value.doubleValue();
    }

    /**
     * Returns the number of percent rounded half-even to a whole number: 12.5% is 12, 13.5% is 14.
     *
     * @return the rounded value as an {@code int}
     * @throws ValueObjectException if it does not fit in an {@code int}
     */
    public int toIntRounded() {
        try {
            return value.setScale(0, DEFAULT_ROUNDING).intValueExact();
        } catch (ArithmeticException overflow) {
            throw new ValueObjectException("Percentage does not fit in an int: " + value.toPlainString());
        }
    }

    // ==================== Formatting ====================

    /**
     * Formats with 2 decimal places, as {@link #format()}: {@code "12.50%"}.
     *
     * @return the percentage as text
     */
    @Override
    public String toString() {
        return format(2);
    }

    /**
     * Formats with 2 decimal places, rounded half-even: {@code "12.50%"}.
     *
     * @return the percentage as text
     */
    public String format() {
        return format(2);
    }

    /**
     * Formats with the given number of decimal places, rounded half-even, followed by {@code %}.
     *
     * @param decimalPlaces the number of decimal places to show, zero or more
     * @return the percentage as text, e.g. {@code "12.5%"} for 1 place
     * @throws ValueObjectException if the number of decimal places is negative
     */
    public String format(int decimalPlaces) {
        if (decimalPlaces < 0) throw new ValueObjectException("Decimal places cannot be negative: " + decimalPlaces);
        return value.setScale(decimalPlaces, DEFAULT_ROUNDING).toPlainString() + "%";
    }

    /**
     * Formats with 2 decimal places and a {@code +} in front of positive values: {@code "+12.50%"},
     * {@code "-3.00%"}, {@code "0.00%"}.
     *
     * @return the signed percentage as text
     */
    public String formatWithSign() {
        String prefix = isPositive() ? "+" : "";
        return prefix + format();
    }

    /**
     * Draws a text progress bar of {@code length} characters: filled blocks ({@code █}) for this share of the
     * length, rounded, and light blocks ({@code ░}) for the rest. Values below 0% or above 100% are clamped.
     *
     * @param length the bar's length in characters, zero or more
     * @return the bar, e.g. {@code "█████░░░░░"} for 50% of 10
     * @throws ValueObjectException if the length is negative
     */
    public String toProgressBar(int length) {
        if (length < 0) throw new ValueObjectException("Progress bar length cannot be negative: " + length);
        int filled = (int) Math.round(length * toDecimalDouble());
        filled = Math.max(0, Math.min(length, filled));
        return "█".repeat(filled) + "░".repeat(length - filled);
    }

    /**
     * Draws a text progress bar, as {@link #toProgressBar(int)}, followed by a space and the percentage with
     * 1 decimal place.
     *
     * @param length the bar's length in characters, zero or more
     * @return the bar and label, e.g. {@code "█████░░░░░ 50.0%"}
     * @throws ValueObjectException if the length is negative
     */
    public String toProgressBarWithLabel(int length) {
        return toProgressBar(length) + " " + format(1);
    }

    // ==================== Equality ====================

    private static Percentage require(Percentage other) {
        if (other == null) throw new ValueObjectException("Percentage cannot be null");
        return other;
    }

    private static BigDecimal decimal(double d) {
        if (Double.isNaN(d) || Double.isInfinite(d)) throw new ValueObjectException("Not a finite number: " + d);
        return BigDecimal.valueOf(d);
    }

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

    /**
     * Parses a percentage like {@link #parse(String)}, without throwing.
     *
     * @param text any text
     * @return it as a {@link Percentage}, or empty when it is not one; never throws
     */
    public static Optional<Percentage> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether text is a number, with or without a trailing {@code %}.
     *
     * @param text any text
     * @return whether it is a {@link Percentage}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
