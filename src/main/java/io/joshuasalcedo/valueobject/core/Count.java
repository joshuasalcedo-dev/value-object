package io.joshuasalcedo.valueobject.core;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import io.joshuasalcedo.valueobject.financial.Percentage;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A count of things: a whole number, 0 or more, held as a {@code long}.
 *
 * <p>Every operation returns a new count and throws {@link ValueObjectException} when the result would be
 * negative or larger than {@link Long#MAX_VALUE}; arithmetic never wraps round. Null arguments also throw
 * {@link ValueObjectException}. A count is read from text with {@link #parse(String)}: a plain decimal number.
 *
 * <pre>{@code
 * Count n = Count.ofSize(items).increment();   // items.size() + 1
 * n.toStringWithLabel("item", "items");        // "4 items"
 * }</pre>
 *
 * @param value the count, 0 or more
 * @author JoshuaSalcedo
 */
public record Count(
        long value
) implements ValueObject, Comparable<Count> {

    /** Zero: {@code new Count(0L)}. */
    public static final Count ZERO = new Count(0L);
    /** One: {@code new Count(1L)}. */
    public static final Count ONE = new Count(1L);

    /**
     * Checks that the count is not negative.
     *
     * @throws ValueObjectException if {@code value} is negative
     */
    public Count {
        if (value < 0) {
            throw new ValueObjectException("Count cannot be negative: " + value);
        }
    }

    // ==================== Factory Methods ====================

    /**
     * Makes a count; 0 and 1 give {@link #ZERO} and {@link #ONE}.
     *
     * @param value the count, 0 or more
     * @return that count
     * @throws ValueObjectException if {@code value} is negative
     */
    public static Count of(long value) {
        if (value == 0) return ZERO;
        if (value == 1) return ONE;
        return new Count(value);
    }

    /**
     * Makes a count from an {@code int}.
     *
     * @param value the count, 0 or more
     * @return that count
     * @throws ValueObjectException if {@code value} is negative
     */
    public static Count of(int value) {
        return of((long) value);
    }

    /**
     * The count 0.
     *
     * @return {@link #ZERO}
     */
    public static Count zero() {
        return ZERO;
    }

    /**
     * The count 1.
     *
     * @return {@link #ONE}
     */
    public static Count one() {
        return ONE;
    }

    /**
     * Counts the elements of a collection.
     *
     * @param collection the collection
     * @return its size
     * @throws ValueObjectException if {@code collection} is null
     */
    public static Count ofSize(Collection<?> collection) {
        requireNonNull(collection);
        return of(collection.size());
    }

    /**
     * Counts the {@code char}s of a string (UTF-16 code units, not code points).
     *
     * @param str the string, or null
     * @return its length; 0 for null
     */
    public static Count ofLength(@Nullable String str) {
        return of(str == null ? 0 : str.length());
    }

    /**
     * Counts the elements of a collection that match a test.
     *
     * @param <T>        the element type
     * @param collection the collection
     * @param predicate  the test
     * @return how many elements pass it
     * @throws ValueObjectException if an argument is null
     */
    public static <T> Count countMatching(Collection<T> collection, Predicate<T> predicate) {
        requireNonNull(collection);
        requireNonNull(predicate);
        return of(collection.stream().filter(predicate).count());
    }

    /**
     * Counts the elements of a stream that match a test; the stream is consumed.
     *
     * @param <T>       the element type
     * @param stream    the stream
     * @param predicate the test
     * @return how many elements pass it
     * @throws ValueObjectException if an argument is null
     */
    public static <T> Count countMatching(Stream<T> stream, Predicate<T> predicate) {
        requireNonNull(stream);
        requireNonNull(predicate);
        return of(stream.filter(predicate).count());
    }

    // ==================== Arithmetic Operations ====================

    /**
     * Adds one.
     *
     * @return this count plus 1
     * @throws ValueObjectException if this count is already {@link Long#MAX_VALUE}
     */
    public Count increment() {
        return of(exact(() -> Math.addExact(value, 1)));
    }

    /**
     * Subtracts one.
     *
     * @return this count minus 1
     * @throws ValueObjectException if this count is 0
     */
    public Count decrement() {
        if (value == 0) {
            throw new ValueObjectException("Cannot decrement below zero");
        }
        return of(value - 1);
    }

    /**
     * Subtracts one, stopping at 0.
     *
     * @return this count minus 1, or {@link #ZERO} when it is already 0
     */
    public Count decrementOrZero() {
        return value == 0 ? ZERO : of(value - 1);
    }

    /**
     * Adds another count.
     *
     * @param other the count to add
     * @return the sum
     * @throws ValueObjectException if {@code other} is null or the sum is larger than {@link Long#MAX_VALUE}
     */
    public Count add(Count other) {
        requireNonNull(other);
        return of(exact(() -> Math.addExact(value, other.value)));
    }

    /**
     * Adds an amount, which may be negative.
     *
     * @param amount the amount to add
     * @return the sum
     * @throws ValueObjectException if the sum is negative or larger than {@link Long#MAX_VALUE}
     */
    public Count add(long amount) {
        return of(exact(() -> Math.addExact(value, amount)));
    }

    /**
     * Subtracts another count.
     *
     * @param other the count to subtract
     * @return the difference
     * @throws ValueObjectException if {@code other} is null or larger than this count
     */
    public Count subtract(Count other) {
        requireNonNull(other);
        return of(value - other.value);
    }

    /**
     * Subtracts an amount, which may be negative.
     *
     * @param amount the amount to subtract
     * @return the difference
     * @throws ValueObjectException if the difference is negative or larger than {@link Long#MAX_VALUE}
     */
    public Count subtract(long amount) {
        return of(exact(() -> Math.subtractExact(value, amount)));
    }

    /**
     * Multiplies by a number.
     *
     * @param multiplier the multiplier
     * @return the product
     * @throws ValueObjectException if the product is negative (a negative multiplier and a count above 0) or
     *                              larger than {@link Long#MAX_VALUE}
     */
    public Count multiply(long multiplier) {
        return of(exact(() -> Math.multiplyExact(value, multiplier)));
    }

    /**
     * Divides by a number, rounding toward zero.
     *
     * @param divisor the divisor, not 0
     * @return the whole quotient
     * @throws ValueObjectException if {@code divisor} is 0, or negative with a quotient below 0
     */
    public Count divide(long divisor) {
        if (divisor == 0) {
            throw new ValueObjectException("Cannot divide by zero");
        }
        return of(value / divisor);
    }

    /**
     * The remainder after dividing by a number ({@code value % divisor}).
     *
     * @param divisor the divisor, not 0; its sign does not matter
     * @return the remainder, from 0 to {@code |divisor| - 1}
     * @throws ValueObjectException if {@code divisor} is 0
     */
    public Count remainder(long divisor) {
        if (divisor == 0) {
            throw new ValueObjectException("Cannot divide by zero");
        }
        return of(value % divisor);
    }

    /**
     * The smaller of two counts.
     *
     * @param other the count to compare with
     * @return the smaller one (this one when they are equal)
     * @throws ValueObjectException if {@code other} is null
     */
    public Count min(Count other) {
        requireNonNull(other);
        return value <= other.value ? this : other;
    }

    /**
     * The larger of two counts.
     *
     * @param other the count to compare with
     * @return the larger one (this one when they are equal)
     * @throws ValueObjectException if {@code other} is null
     */
    public Count max(Count other) {
        requireNonNull(other);
        return value >= other.value ? this : other;
    }

    // ==================== Conditional Operations ====================

    /**
     * Adds one when a condition holds; the condition is asked once.
     *
     * @param condition supplies the condition; a null answer counts as {@code false}
     * @return this count plus 1, or this count
     * @throws ValueObjectException if {@code condition} is null, or the condition holds and this count is
     *                              already {@link Long#MAX_VALUE}
     */
    public Count incrementIf(Supplier<Boolean> condition) {
        requireNonNull(condition);
        return Boolean.TRUE.equals(condition.get()) ? increment() : this;
    }

    /**
     * Adds one when a condition holds.
     *
     * @param condition whether to add
     * @return this count plus 1, or this count
     * @throws ValueObjectException if {@code condition} is true and this count is already {@link Long#MAX_VALUE}
     */
    public Count incrementIf(boolean condition) {
        return condition ? increment() : this;
    }

    /**
     * Subtracts one when a condition holds; the condition is asked once.
     *
     * @param condition supplies the condition; a null answer counts as {@code false}
     * @return this count minus 1, or this count
     * @throws ValueObjectException if {@code condition} is null, or the condition holds and this count is 0
     */
    public Count decrementIf(Supplier<Boolean> condition) {
        requireNonNull(condition);
        return Boolean.TRUE.equals(condition.get()) ? decrement() : this;
    }

    /**
     * Subtracts one when a condition holds.
     *
     * @param condition whether to subtract
     * @return this count minus 1, or this count
     * @throws ValueObjectException if {@code condition} is true and this count is 0
     */
    public Count decrementIf(boolean condition) {
        return condition ? decrement() : this;
    }

    /**
     * Adds an amount when a condition holds.
     *
     * @param condition whether to add
     * @param amount    the amount to add, which may be negative
     * @return the sum, or this count
     * @throws ValueObjectException if the condition holds and the sum is negative or larger than
     *                              {@link Long#MAX_VALUE}
     */
    public Count addIf(boolean condition, long amount) {
        return condition ? add(amount) : this;
    }

    // ==================== Comparison Operations ====================

    /**
     * Tells whether the count is 0.
     *
     * @return whether it is 0
     */
    public boolean isZero() {
        return value == 0;
    }

    /**
     * Tells whether the count is 1.
     *
     * @return whether it is 1
     */
    public boolean isOne() {
        return value == 1;
    }

    /**
     * Tells whether the count is above 0.
     *
     * @return whether it is 1 or more
     */
    public boolean isPositive() {
        return value > 0;
    }

    /**
     * Tells whether this count is larger than another.
     *
     * @param other the count to compare with
     * @return whether this one is larger
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isGreaterThan(Count other) {
        requireNonNull(other);
        return value > other.value;
    }

    /**
     * Tells whether this count is larger than a number.
     *
     * @param other the number to compare with
     * @return whether this count is larger
     */
    public boolean isGreaterThan(long other) {
        return value > other;
    }

    /**
     * Tells whether this count is at least another.
     *
     * @param other the count to compare with
     * @return whether this one is larger or equal
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isGreaterThanOrEqual(Count other) {
        requireNonNull(other);
        return value >= other.value;
    }

    /**
     * Tells whether this count is at least a number.
     *
     * @param other the number to compare with
     * @return whether this count is larger or equal
     */
    public boolean isGreaterThanOrEqual(long other) {
        return value >= other;
    }

    /**
     * Tells whether this count is smaller than another.
     *
     * @param other the count to compare with
     * @return whether this one is smaller
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isLessThan(Count other) {
        requireNonNull(other);
        return value < other.value;
    }

    /**
     * Tells whether this count is smaller than a number.
     *
     * @param other the number to compare with
     * @return whether this count is smaller
     */
    public boolean isLessThan(long other) {
        return value < other;
    }

    /**
     * Tells whether this count is at most another.
     *
     * @param other the count to compare with
     * @return whether this one is smaller or equal
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isLessThanOrEqual(Count other) {
        requireNonNull(other);
        return value <= other.value;
    }

    /**
     * Tells whether this count is at most a number.
     *
     * @param other the number to compare with
     * @return whether this count is smaller or equal
     */
    public boolean isLessThanOrEqual(long other) {
        return value <= other;
    }

    /**
     * Tells whether this count lies between two others, both included.
     *
     * @param min the lowest allowed count
     * @param max the highest allowed count
     * @return whether {@code min <= value <= max}
     * @throws ValueObjectException if {@code min} or {@code max} is null
     */
    public boolean isBetween(Count min, Count max) {
        requireNonNull(min);
        requireNonNull(max);
        return value >= min.value && value <= max.value;
    }

    /**
     * Tells whether this count lies between two numbers, both included.
     *
     * @param min the lowest allowed number
     * @param max the highest allowed number
     * @return whether {@code min <= value <= max}
     */
    public boolean isBetween(long min, long max) {
        return value >= min && value <= max;
    }

    /**
     * Orders counts by value.
     *
     * @param other the count to compare to
     * @return negative, zero or positive as this count is smaller than, equal to or larger than {@code other}
     * @throws ValueObjectException if {@code other} is null
     */
    @Override
    public int compareTo(Count other) {
        requireNonNull(other);
        return Long.compare(value, other.value);
    }

    // ==================== Conversion ====================

    /**
     * The count as an {@code int}.
     *
     * @return the value
     * @throws ValueObjectException if it is larger than {@link Integer#MAX_VALUE}
     */
    public int toInt() {
        if (value > Integer.MAX_VALUE) {
            throw new ValueObjectException("Count exceeds Integer.MAX_VALUE: " + value);
        }
        return (int) value;
    }

    /**
     * The count as an {@code int}, capped at {@link Integer#MAX_VALUE}.
     *
     * @return the value, or {@link Integer#MAX_VALUE} when it is larger
     */
    public int toIntClamped() {
        return (int) Math.min(value, Integer.MAX_VALUE);
    }

    /**
     * The count as a {@code long}, the same as {@link #value()}.
     *
     * @return the value
     */
    public long toLong() {
        return value;
    }

    /**
     * What percent of a total this count is, worked out exactly in {@link java.math.BigDecimal} arithmetic and
     * rounded half-even to 4 decimal places, as {@link Percentage#fromRatio(Count, Count)} does; 3 of 4 gives
     * 75%, 1 of 3 gives 33.3333%. The result may exceed 100% when this count is larger than the total.
     *
     * @param total the whole, above 0
     * @return {@code value * 100 / total}, as a {@link Percentage}
     * @throws ValueObjectException if {@code total} is null or 0
     */
    public Percentage percentageOf(Count total) {
        requireNonNull(total);
        if (total.isZero()) {
            throw new ValueObjectException("Cannot calculate percentage of zero");
        }
        return Percentage.fromRatio(this, total);
    }

    // ==================== Utility ====================

    /**
     * Runs an action once for each number from 0 up to, but not including, this count.
     *
     * @param action receives 0, 1, ... {@code value - 1}, in order
     * @throws ValueObjectException if {@code action} is null
     */
    public void times(java.util.function.Consumer<Long> action) {
        requireNonNull(action);
        for (long i = 0; i < value; i++) {
            action.accept(i);
        }
    }

    /**
     * Runs an action this many times.
     *
     * @param action the action to run
     * @throws ValueObjectException if {@code action} is null
     */
    public void times(Runnable action) {
        requireNonNull(action);
        for (long i = 0; i < value; i++) {
            action.run();
        }
    }

    /**
     * The numbers from 0 up to, but not including, this count.
     *
     * @return a sequential stream of 0, 1, ... {@code value - 1}
     */
    public Stream<Long> stream() {
        return Stream.iterate(0L, i -> i < value, i -> i + 1);
    }

    /**
     * Writes the count as a plain number.
     *
     * @return e.g. {@code "42"}
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Writes the count with a word after it, singular for 1 and plural otherwise (including 0).
     *
     * @param singular the word for one, e.g. {@code "item"}
     * @param plural   the word for any other count, e.g. {@code "items"}
     * @return e.g. {@code "1 item"} or {@code "0 items"}
     * @throws ValueObjectException if {@code singular} or {@code plural} is null
     */
    public String toStringWithLabel(String singular, String plural) {
        requireNonNull(singular);
        requireNonNull(plural);
        return value + " " + (value == 1 ? singular : plural);
    }

    /**
     * Reads a count written as a plain decimal number, as {@link #toString()} writes it, ignoring blanks around
     * it: {@code "42"}, {@code " 7 "}. Signs, separators and decimals are not read.
     *
     * @param text the text
     * @return that count
     * @throws ValueObjectException if the text is null, not a plain number, or larger than {@link Long#MAX_VALUE}
     */
    public static Count parse(String text) {
        if (text == null) throw new ValueObjectException("A Count is required");
        String t = text.strip();
        if (t.isEmpty() || !t.chars().allMatch(c -> c >= '0' && c <= '9')) {
            throw new ValueObjectException("Not a count: '" + text + "'");
        }
        try {
            return of(Long.parseLong(t));
        } catch (NumberFormatException e) {
            throw new ValueObjectException("Count too large: '" + text + "'", e);
        }
    }

    /**
     * Reads a count like {@link #parse(String)}, without throwing.
     *
     * @param text any text, or null
     * @return it as a {@link Count}, or empty when it is not one; never throws
     */
    public static Optional<Count> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text is a count that {@link #parse(String)} accepts.
     *
     * @param text any text, or null
     * @return whether it is a {@link Count} ({@code false} for null)
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

    private static void requireNonNull(@Nullable Object o) {
        if (o == null) throw new ValueObjectException("A value is required");
    }

    private static long exact(java.util.function.LongSupplier f) {
        try {
            return f.getAsLong();
        } catch (ArithmeticException e) {
            throw new ValueObjectException("Count too large: the result is past Long.MAX_VALUE", e);
        }
    }
}