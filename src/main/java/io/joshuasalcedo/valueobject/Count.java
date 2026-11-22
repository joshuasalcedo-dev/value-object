package io.joshuasalcedo.valueobject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.financial.Percentage;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record Count(
        long value
) implements ValueObject, Comparable<Count> {

    public static final Count ZERO = new Count(0L);
    public static final Count ONE = new Count(1L);

    public Count {
        if (value < 0) {
            throw new ValueObjectException("Count cannot be negative: " + value);
        }
    }

    // ==================== Factory Methods ====================

    public static Count of(long value) {
        if (value == 0) return ZERO;
        if (value == 1) return ONE;
        return new Count(value);
    }

    public static Count of(int value) {
        return of((long) value);
    }

    public static Count zero() {
        return ZERO;
    }

    public static Count one() {
        return ONE;
    }

    public static Count ofSize(Collection<?> collection) {
        return of(collection.size());
    }

    public static Count ofLength(String str) {
        return of(str == null ? 0 : str.length());
    }

    public static <T> Count countMatching(Collection<T> collection, Predicate<T> predicate) {
        return of(collection.stream().filter(predicate).count());
    }

    public static <T> Count countMatching(Stream<T> stream, Predicate<T> predicate) {
        return of(stream.filter(predicate).count());
    }

    // ==================== Arithmetic Operations ====================

    public Count increment() {
        return of(value + 1);
    }

    public Count decrement() {
        if (value == 0) {
            throw new ValueObjectException("Cannot decrement below zero");
        }
        return of(value - 1);
    }

    public Count decrementOrZero() {
        return value == 0 ? ZERO : of(value - 1);
    }

    public Count add(Count other) {
        return of(value + other.value);
    }

    public Count add(long amount) {
        return of(value + amount);
    }

    public Count subtract(Count other) {
        return of(value - other.value);
    }

    public Count subtract(long amount) {
        return of(value - amount);
    }

    public Count multiply(long multiplier) {
        return of(value * multiplier);
    }

    public Count divide(long divisor) {
        if (divisor == 0) {
            throw new ValueObjectException("Cannot divide by zero");
        }
        return of(value / divisor);
    }

    public Count remainder(long divisor) {
        if (divisor == 0) {
            throw new ValueObjectException("Cannot divide by zero");
        }
        return of(value % divisor);
    }

    public Count min(Count other) {
        return value <= other.value ? this : other;
    }

    public Count max(Count other) {
        return value >= other.value ? this : other;
    }

    // ==================== Conditional Operations ====================

    public Count incrementIf(Supplier<Boolean> condition) {
        return Boolean.TRUE.equals(condition.get()) ? increment() : this;
    }

    public Count incrementIf(boolean condition) {
        return condition ? increment() : this;
    }

    public Count decrementIf(Supplier<Boolean> condition) {
        return Boolean.TRUE.equals(condition.get()) ? decrement() : this;
    }

    public Count decrementIf(boolean condition) {
        return condition ? decrement() : this;
    }

    public Count addIf(boolean condition, long amount) {
        return condition ? add(amount) : this;
    }

    // ==================== Comparison Operations ====================

    public boolean isZero() {
        return value == 0;
    }

    public boolean isOne() {
        return value == 1;
    }

    public boolean isPositive() {
        return value > 0;
    }

    public boolean isGreaterThan(Count other) {
        return value > other.value;
    }

    public boolean isGreaterThan(long other) {
        return value > other;
    }

    public boolean isGreaterThanOrEqual(Count other) {
        return value >= other.value;
    }

    public boolean isGreaterThanOrEqual(long other) {
        return value >= other;
    }

    public boolean isLessThan(Count other) {
        return value < other.value;
    }

    public boolean isLessThan(long other) {
        return value < other;
    }

    public boolean isLessThanOrEqual(Count other) {
        return value <= other.value;
    }

    public boolean isLessThanOrEqual(long other) {
        return value <= other;
    }

    public boolean isBetween(Count min, Count max) {
        return value >= min.value && value <= max.value;
    }

    public boolean isBetween(long min, long max) {
        return value >= min && value <= max;
    }

    @Override
    public int compareTo(Count other) {
        return Long.compare(value, other.value);
    }

    // ==================== Conversion ====================

    public int toInt() {
        if (value > Integer.MAX_VALUE) {
            throw new ValueObjectException("Count exceeds Integer.MAX_VALUE: " + value);
        }
        return (int) value;
    }

    public int toIntClamped() {
        return (int) Math.min(value, Integer.MAX_VALUE);
    }

    public long toLong() {
        return value;
    }

    public Percentage percentageOf(Count total) {
        if (total.isZero()) {
            throw new ValueObjectException("Cannot calculate percentage of zero");
        }
        return Percentage.of(java.math.BigDecimal.valueOf((value * 100.0) / total.value));
    }

    // ==================== Utility ====================

    public <T> void times(java.util.function.Consumer<Long> action) {
        for (long i = 0; i < value; i++) {
            action.accept(i);
        }
    }

    public void times(Runnable action) {
        for (long i = 0; i < value; i++) {
            action.run();
        }
    }

    public Stream<Long> stream() {
        return Stream.iterate(0L, i -> i < value, i -> i + 1);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public String toStringWithLabel(String singular, String plural) {
        return value + " " + (value == 1 ? singular : plural);
    }
}