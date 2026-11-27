package io.joshuasalcedo.valueobject.temporal;

import org.jspecify.annotations.Nullable;
import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import io.joshuasalcedo.valueobject.core.ValueObjectException;

import java.util.concurrent.TimeUnit;

/**
 * Value object representing a duration of time, held as a whole number of nanoseconds, never negative.
 * Provides convenient provider methods and formatting. The largest duration is {@link Long#MAX_VALUE}
 * nanoseconds (about 292 years); the {@code of...} factories and arithmetic throw
 * {@link ValueObjectException} when a result would be longer.
 * Conversions to larger units truncate toward zero.
 *
 * @param nanos the length in nanoseconds; 0 or more
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record DurationNano(long nanos) implements ValueObject, Comparable<DurationNano> {

    // Constants
    private static final long NANOS_PER_MICRO = 1_000L;
    private static final long NANOS_PER_MILLI = 1_000_000L;
    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private static final long NANOS_PER_MINUTE = NANOS_PER_SECOND * 60;
    private static final long NANOS_PER_HOUR = NANOS_PER_MINUTE * 60;
    private static final long NANOS_PER_DAY = NANOS_PER_HOUR * 24;

    /** Zero: {@code new DurationNano(0)}. */
    public static final DurationNano ZERO = new DurationNano(0);

    /**
     * Checks that the duration is not negative.
     *
     * @throws ValueObjectException if {@code nanos} is negative
     */
    public DurationNano {
        if (nanos < 0) {
            throw new ValueObjectException("DurationNano cannot be negative: " + nanos);
        }
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a DurationNano of zero.
     *
     * @return zero duration
     */
    public static DurationNano zero() {
        return ZERO;
    }

    /**
     * Creates a DurationNano from nanoseconds.
     *
     * @param nanos the nanoseconds
     * @return a new DurationNano ({@link #ZERO} for 0)
     * @throws ValueObjectException if {@code nanos} is negative
     */
    public static DurationNano ofNanos(long nanos) {
        return nanos == 0 ? ZERO : new DurationNano(nanos);
    }

    /**
     * Creates a DurationNano from microseconds.
     *
     * @param micros the microseconds
     * @return a new DurationNano
     * @throws ValueObjectException if {@code micros} is negative or the duration is too long to hold in nanoseconds
     */
    public static DurationNano ofMicros(long micros) {
        return ofNanos(exactProduct(micros, NANOS_PER_MICRO));
    }

    /**
     * Creates a DurationNano from milliseconds.
     *
     * @param millis the milliseconds
     * @return a new DurationNano
     * @throws ValueObjectException if {@code millis} is negative or the duration is too long to hold in nanoseconds
     */
    public static DurationNano ofMillis(long millis) {
        return ofNanos(exactProduct(millis, NANOS_PER_MILLI));
    }

    /**
     * Creates a DurationNano from seconds.
     *
     * @param seconds the seconds
     * @return a new DurationNano
     * @throws ValueObjectException if {@code seconds} is negative or the duration is too long to hold in nanoseconds
     */
    public static DurationNano ofSeconds(long seconds) {
        return ofNanos(exactProduct(seconds, NANOS_PER_SECOND));
    }

    /**
     * Creates a DurationNano from minutes.
     *
     * @param minutes the minutes
     * @return a new DurationNano
     * @throws ValueObjectException if {@code minutes} is negative or the duration is too long to hold in nanoseconds
     */
    public static DurationNano ofMinutes(long minutes) {
        return ofNanos(exactProduct(minutes, NANOS_PER_MINUTE));
    }

    /**
     * Creates a DurationNano from hours.
     *
     * @param hours the hours
     * @return a new DurationNano
     * @throws ValueObjectException if {@code hours} is negative or the duration is too long to hold in nanoseconds
     */
    public static DurationNano ofHours(long hours) {
        return ofNanos(exactProduct(hours, NANOS_PER_HOUR));
    }

    /**
     * Creates a DurationNano from days of exactly 24 hours.
     *
     * @param days the days
     * @return a new DurationNano
     * @throws ValueObjectException if {@code days} is negative or the duration is too long to hold in nanoseconds
     */
    public static DurationNano ofDays(long days) {
        return ofNanos(exactProduct(days, NANOS_PER_DAY));
    }

    /**
     * Creates a DurationNano from a {@link java.time.Duration}.
     *
     * @param duration the Java duration
     * @return a new DurationNano
     * @throws ValueObjectException if {@code duration} is null, negative, or too long to hold in nanoseconds
     */
    public static DurationNano from(java.time.Duration duration) {
        if (duration == null) throw new ValueObjectException("A duration is required");
        if (duration.isNegative()) {
            throw new ValueObjectException("DurationNano cannot be negative: " + duration);
        }
        try {
            return ofNanos(duration.toNanos());
        } catch (ArithmeticException e) {
            throw new ValueObjectException("Duration too long to hold in nanoseconds: " + duration, e);
        }
    }

    /**
     * Creates a DurationNano by parsing an ISO-8601 duration string.
     *
     * @param text the duration string (e.g., {@code "PT1H30M"})
     * @return a new DurationNano
     * @throws ValueObjectException if the text is null, not an ISO-8601 duration, or is negative or too long
     */
    public static DurationNano parse(String text) {
        if (text == null) throw new ValueObjectException("A DurationNano is required");
        try {
            return from(java.time.Duration.parse(text.strip()));
        } catch (RuntimeException e) {
            throw new ValueObjectException("Invalid duration format: " + text, e);
        }
    }

    // ==================== Conversion Methods ====================

    /**
     * Returns the duration in nanoseconds.
     *
     * @return nanoseconds
     */
    public long toNanos() {
        return nanos;
    }

    /**
     * Returns the duration in whole microseconds.
     *
     * @return microseconds
     */
    public long toMicros() {
        return nanos / NANOS_PER_MICRO;
    }

    /**
     * Returns the duration in whole milliseconds.
     *
     * @return milliseconds
     */
    public long toMillis() {
        return nanos / NANOS_PER_MILLI;
    }

    /**
     * Returns the duration in whole seconds.
     *
     * @return seconds
     */
    public long toSeconds() {
        return nanos / NANOS_PER_SECOND;
    }

    /**
     * Returns the duration in whole minutes.
     *
     * @return minutes
     */
    public long toMinutes() {
        return nanos / NANOS_PER_MINUTE;
    }

    /**
     * Returns the duration in whole hours.
     *
     * @return hours
     */
    public long toHours() {
        return nanos / NANOS_PER_HOUR;
    }

    /**
     * Returns the duration in whole 24-hour days.
     *
     * @return days
     */
    public long toDays() {
        return nanos / NANOS_PER_DAY;
    }

    /**
     * Converts to a {@link java.time.Duration}.
     *
     * @return the Java duration
     */
    public java.time.Duration toJavaDuration() {
        return java.time.Duration.ofNanos(nanos);
    }

    /**
     * Converts to a whole number of the given {@link TimeUnit}, truncating toward zero.
     *
     * @param unit the target unit
     * @return the value in the target unit
     * @throws ValueObjectException if {@code unit} is null
     */
    public long to(TimeUnit unit) {
        if (unit == null) throw new ValueObjectException("A TimeUnit is required");
        return unit.convert(nanos, TimeUnit.NANOSECONDS);
    }

    // ==================== Arithmetic Operations ====================

    /**
     * Adds another duration to this one.
     *
     * @param other the duration to add
     * @return a new DurationNano
     * @throws ValueObjectException if {@code other} is null or the sum is too long to hold in nanoseconds
     */
    public DurationNano plus(DurationNano other) {
        requireNonNull(other);
        try {
            return ofNanos(Math.addExact(this.nanos, other.nanos));
        } catch (ArithmeticException e) {
            throw new ValueObjectException("Duration too long to hold in nanoseconds: " + this.nanos + " + " + other.nanos, e);
        }
    }

    /**
     * Subtracts another duration from this one.
     *
     * @param other the duration to subtract
     * @return a new DurationNano
     * @throws ValueObjectException if {@code other} is null or longer than this duration
     */
    public DurationNano minus(DurationNano other) {
        requireNonNull(other);
        long result = this.nanos - other.nanos;
        if (result < 0) {
            throw new ValueObjectException("Result would be negative");
        }
        return ofNanos(result);
    }

    /**
     * Multiplies this duration by a factor.
     *
     * @param factor the multiplier; 0 or more
     * @return a new DurationNano
     * @throws ValueObjectException if {@code factor} is negative or the product is too long to hold in nanoseconds
     */
    public DurationNano multipliedBy(long factor) {
        if (factor < 0) {
            throw new ValueObjectException("Factor cannot be negative");
        }
        return ofNanos(exactProduct(this.nanos, factor));
    }

    /**
     * Divides this duration by a divisor, truncating to a whole nanosecond.
     *
     * @param divisor the divisor; greater than 0
     * @return a new DurationNano
     * @throws ValueObjectException if {@code divisor} is 0 or negative
     */
    public DurationNano dividedBy(long divisor) {
        if (divisor <= 0) {
            throw new ValueObjectException("Divisor must be positive");
        }
        return ofNanos(this.nanos / divisor);
    }

    // ==================== Query Methods ====================

    /**
     * Checks if this duration is zero.
     *
     * @return true if zero
     */
    public boolean isZero() {
        return nanos == 0;
    }

    /**
     * Checks if this duration is greater than another.
     *
     * @param other the other duration
     * @return true if greater
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isGreaterThan(DurationNano other) {
        requireNonNull(other);
        return this.nanos > other.nanos;
    }

    /**
     * Checks if this duration is less than another.
     *
     * @param other the other duration
     * @return true if less
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isLessThan(DurationNano other) {
        requireNonNull(other);
        return this.nanos < other.nanos;
    }

    // ==================== Formatting ====================

    /**
     * Returns an ISO-8601 formatted string, in hours, minutes and seconds (e.g. {@code "PT26H0.5S"}).
     *
     * @return the ISO-8601 string
     */
    public String toIsoString() {
        return toJavaDuration().toString();
    }

    /**
     * Returns a human-readable format like {@code "2h 30m 15s"}: days, hours, minutes and seconds, each left out
     * when 0, then milliseconds when there are no days or hours. Anything under a millisecond is dropped, except
     * for a duration under a millisecond, which is written in microseconds ({@code "250µs"}, rounded down) from
     * 1,000 nanoseconds and in nanoseconds ({@code "999ns"}) below that.
     *
     * @return the formatted string; {@code "0s"} for zero
     */
    public String formatted() {
        if (nanos == 0) {
            return "0s";
        }

        StringBuilder sb = new StringBuilder();
        long remaining = nanos;

        long days = remaining / NANOS_PER_DAY;
        remaining %= NANOS_PER_DAY;

        long hours = remaining / NANOS_PER_HOUR;
        remaining %= NANOS_PER_HOUR;

        long minutes = remaining / NANOS_PER_MINUTE;
        remaining %= NANOS_PER_MINUTE;

        long seconds = remaining / NANOS_PER_SECOND;
        remaining %= NANOS_PER_SECOND;

        long millis = remaining / NANOS_PER_MILLI;

        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s ");
        if (millis > 0 && days == 0 && hours == 0) sb.append(millis).append("ms ");

        if (sb.isEmpty()) {
            return nanos >= NANOS_PER_MICRO ? (nanos / NANOS_PER_MICRO) + "\u00b5s" : nanos + "ns";
        }
        return sb.toString().trim();
    }

    @Override
    public int compareTo(DurationNano other) {
        requireNonNull(other);
        return Long.compare(this.nanos, other.nanos);
    }

    /**
     * Writes the duration as {@link #formatted()} does.
     *
     * @return e.g. {@code "2h 30m 15s"}
     */
    @Override
    public String toString() {
        return formatted();
    }

    private static long exactProduct(long a, long b) {
        try {
            return Math.multiplyExact(a, b);
        } catch (ArithmeticException e) {
            throw new ValueObjectException("Duration too long to hold in nanoseconds: " + a + " x " + b, e);
        }
    }

    private static void requireNonNull(@Nullable DurationNano other) {
        if (other == null) throw new ValueObjectException("A DurationNano to compare or combine with is required");
    }

    /**
     * Parses an ISO-8601 duration like {@link #parse(String)}, without throwing.
     *
     * @param text any text, or null
     * @return it as a {@link DurationNano}, or empty when it is not one; never throws
     */
    public static Optional<DurationNano> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text is an ISO-8601 duration that is not negative and fits in nanoseconds.
     *
     * @param text any text, or null
     * @return whether it is a {@link DurationNano} ({@code false} for null)
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
