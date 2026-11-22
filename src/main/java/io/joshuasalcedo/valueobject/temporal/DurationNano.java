package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Value object representing a duration of time.
 * Provides convenient provider methods and formatting.
 *
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

    public static final DurationNano ZERO = new DurationNano(0);

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
     * @return a new DurationNano
     */
    public static DurationNano ofNanos(long nanos) {
        return nanos == 0 ? ZERO : new DurationNano(nanos);
    }

    /**
     * Creates a DurationNano from microseconds.
     *
     * @param micros the microseconds
     * @return a new DurationNano
     */
    public static DurationNano ofMicros(long micros) {
        return ofNanos(micros * NANOS_PER_MICRO);
    }

    /**
     * Creates a DurationNano from milliseconds.
     *
     * @param millis the milliseconds
     * @return a new DurationNano
     */
    public static DurationNano ofMillis(long millis) {
        return ofNanos(millis * NANOS_PER_MILLI);
    }

    /**
     * Creates a DurationNano from seconds.
     *
     * @param seconds the seconds
     * @return a new DurationNano
     */
    public static DurationNano ofSeconds(long seconds) {
        return ofNanos(seconds * NANOS_PER_SECOND);
    }

    /**
     * Creates a DurationNano from minutes.
     *
     * @param minutes the minutes
     * @return a new DurationNano
     */
    public static DurationNano ofMinutes(long minutes) {
        return ofNanos(minutes * NANOS_PER_MINUTE);
    }

    /**
     * Creates a DurationNano from hours.
     *
     * @param hours the hours
     * @return a new DurationNano
     */
    public static DurationNano ofHours(long hours) {
        return ofNanos(hours * NANOS_PER_HOUR);
    }

    /**
     * Creates a DurationNano from days.
     *
     * @param days the days
     * @return a new DurationNano
     */
    public static DurationNano ofDays(long days) {
        return ofNanos(days * NANOS_PER_DAY);
    }

    /**
     * Creates a DurationNano from java.time.DurationNano.
     *
     * @param duration the Java duration
     * @return a new DurationNano
     */
    public static DurationNano from(java.time.Duration duration) {
        Objects.requireNonNull(duration, "DurationNano cannot be null");
        if (duration.isNegative()) {
            throw new ValueObjectException("DurationNano cannot be negative");
        }
        return ofNanos(duration.toNanos());
    }

    /**
     * Creates a DurationNano by parsing an ISO-8601 duration string.
     *
     * @param text the duration string (e.g., "PT1H30M")
     * @return a new DurationNano
     */
    public static DurationNano parse(String text) {
        Objects.requireNonNull(text, "DurationNano text cannot be null");
        try {
            return from(java.time.Duration.parse(text));
        } catch (Exception e) {
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
     * Returns the duration in microseconds.
     *
     * @return microseconds
     */
    public long toMicros() {
        return nanos / NANOS_PER_MICRO;
    }

    /**
     * Returns the duration in milliseconds.
     *
     * @return milliseconds
     */
    public long toMillis() {
        return nanos / NANOS_PER_MILLI;
    }

    /**
     * Returns the duration in seconds.
     *
     * @return seconds
     */
    public long toSeconds() {
        return nanos / NANOS_PER_SECOND;
    }

    /**
     * Returns the duration in minutes.
     *
     * @return minutes
     */
    public long toMinutes() {
        return nanos / NANOS_PER_MINUTE;
    }

    /**
     * Returns the duration in hours.
     *
     * @return hours
     */
    public long toHours() {
        return nanos / NANOS_PER_HOUR;
    }

    /**
     * Returns the duration in days.
     *
     * @return days
     */
    public long toDays() {
        return nanos / NANOS_PER_DAY;
    }

    /**
     * Converts to java.time.DurationNano.
     *
     * @return the Java DurationNano
     */
    public java.time.Duration toJavaDuration() {
        return java.time.Duration.ofNanos(nanos);
    }

    /**
     * Converts to TimeUnit value.
     *
     * @param unit the target unit
     * @return the value in the target unit
     */
    public long to(TimeUnit unit) {
        return unit.convert(nanos, TimeUnit.NANOSECONDS);
    }

    // ==================== Arithmetic Operations ====================

    /**
     * Adds another duration to this one.
     *
     * @param other the duration to add
     * @return a new DurationNano
     */
    public DurationNano plus(DurationNano other) {
        return ofNanos(this.nanos + other.nanos);
    }

    /**
     * Subtracts another duration from this one.
     *
     * @param other the duration to subtract
     * @return a new DurationNano
     */
    public DurationNano minus(DurationNano other) {
        long result = this.nanos - other.nanos;
        if (result < 0) {
            throw new ValueObjectException("Result would be negative");
        }
        return ofNanos(result);
    }

    /**
     * Multiplies this duration by a factor.
     *
     * @param factor the multiplier
     * @return a new DurationNano
     */
    public DurationNano multipliedBy(long factor) {
        if (factor < 0) {
            throw new ValueObjectException("Factor cannot be negative");
        }
        return ofNanos(this.nanos * factor);
    }

    /**
     * Divides this duration by a divisor.
     *
     * @param divisor the divisor
     * @return a new DurationNano
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
     */
    public boolean isGreaterThan(DurationNano other) {
        return this.nanos > other.nanos;
    }

    /**
     * Checks if this duration is less than another.
     *
     * @param other the other duration
     * @return true if less
     */
    public boolean isLessThan(DurationNano other) {
        return this.nanos < other.nanos;
    }

    // ==================== Formatting ====================

    /**
     * Returns an ISO-8601 formatted string.
     *
     * @return the ISO-8601 string
     */
    public String toIsoString() {
        return toJavaDuration().toString();
    }

    /**
     * Returns a human-readable format like "2h 30m 15s".
     *
     * @return the formatted string
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

        return sb.toString().trim();
    }

    @Override
    public int compareTo(DurationNano other) {
        return Long.compare(this.nanos, other.nanos);
    }

    @Override
    public String toString() {
        return formatted();
    }
}
