package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.ValueObject;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Immutable value object representing a point in time.
 *
 * <p>Wraps {@link Instant} with a richer API for comparison, arithmetic,
 * and formatting. All operations return new instances.</p>
 *
 * <h3>Usage examples:</h3>
 * <pre>{@code
 * Timestamp now     = Timestamp.now();
 * Timestamp created = Timestamp.ofEpochSecond(1706140800);   // from Unix timestamp
 * Timestamp parsed  = Timestamp.parse("2025-01-25T10:30:00Z");
 *
 * boolean overdue = now.isAfter(deadline);
 * Duration elapsed = created.durationUntil(now);
 * Timestamp tomorrow = now.plusDays(1);
 * }</pre>
 *
 * @author JoshuaSalcedo
 * @since 12/24/2025 12:27 AM
 */
public record Timestamp(Instant value) implements ValueObject, Comparable<Timestamp> {

    private static final DateTimeFormatter HUMAN_READABLE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm:ss a");

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_INSTANT;

    private static final DateTimeFormatter DATE_ONLY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter COMPACT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== Compact Constructor ====================

    public Timestamp {
        Objects.requireNonNull(value, "Timestamp value cannot be null");
    }

    // ==================== Factory Methods ====================

    /**
     * Returns the current timestamp.
     */
    public static Timestamp now() {
        return new Timestamp(Instant.now());
    }

    /**
     * Creates a Timestamp from an Instant.
     */
    public static Timestamp of(Instant instant) {
        return new Timestamp(instant);
    }

    /**
     * Creates a Timestamp from Unix epoch seconds.
     * This is the most common format in APIs (Telegram, Unix, etc.).
     *
     * @param epochSecond seconds since 1970-01-01T00:00:00Z
     */
    public static Timestamp ofEpochSecond(long epochSecond) {
        return new Timestamp(Instant.ofEpochSecond(epochSecond));
    }

    /**
     * Creates a Timestamp from Unix epoch seconds (int overload).
     * Convenience for APIs that return epoch seconds as int (e.g., Telegram TDLib).
     */
    public static Timestamp ofEpochSecond(int epochSecond) {
        return ofEpochSecond((long) epochSecond);
    }

    /**
     * Creates a Timestamp from Unix epoch milliseconds.
     * Common in Java APIs and JavaScript.
     *
     * @param epochMilli milliseconds since 1970-01-01T00:00:00Z
     */
    public static Timestamp ofEpochMilli(long epochMilli) {
        return new Timestamp(Instant.ofEpochMilli(epochMilli));
    }

    /**
     * Creates a Timestamp from a LocalDateTime in a specific timezone.
     */
    public static Timestamp of(LocalDateTime dateTime, ZoneId zone) {
        Objects.requireNonNull(dateTime, "LocalDateTime cannot be null");
        Objects.requireNonNull(zone, "ZoneId cannot be null");
        return new Timestamp(dateTime.atZone(zone).toInstant());
    }

    /**
     * Creates a Timestamp from a LocalDate at midnight in a specific timezone.
     */
    public static Timestamp of(LocalDate date, ZoneId zone) {
        Objects.requireNonNull(date, "LocalDate cannot be null");
        Objects.requireNonNull(zone, "ZoneId cannot be null");
        return of(date.atStartOfDay(), zone);
    }

    /**
     * Creates a Timestamp from a ZonedDateTime.
     */
    public static Timestamp of(ZonedDateTime zonedDateTime) {
        Objects.requireNonNull(zonedDateTime, "ZonedDateTime cannot be null");
        return new Timestamp(zonedDateTime.toInstant());
    }

    /**
     * Parses an ISO-8601 string.
     * Accepts formats like "2025-01-25T10:30:00Z" or "2025-01-25T10:30:00+08:00".
     *
     * @throws java.time.format.DateTimeParseException if the string cannot be parsed
     */
    public static Timestamp parse(String isoString) {
        Objects.requireNonNull(isoString, "ISO string cannot be null");
        return new Timestamp(Instant.parse(isoString.trim()));
    }

    /**
     * Returns the earliest possible timestamp (epoch zero: 1970-01-01T00:00:00Z).
     */
    public static Timestamp epoch() {
        return new Timestamp(Instant.EPOCH);
    }

    // ==================== Comparison ====================

    /**
     * Checks if this timestamp is before another.
     */
    public boolean isBefore(Timestamp other) {
        Objects.requireNonNull(other, "Cannot compare with null");
        return value.isBefore(other.value);
    }

    /**
     * Checks if this timestamp is after another.
     */
    public boolean isAfter(Timestamp other) {
        Objects.requireNonNull(other, "Cannot compare with null");
        return value.isAfter(other.value);
    }

    /**
     * Checks if this timestamp is between two others (inclusive).
     */
    public boolean isBetween(Timestamp from, Timestamp to) {
        Objects.requireNonNull(from, "From timestamp cannot be null");
        Objects.requireNonNull(to, "To timestamp cannot be null");
        return !isBefore(from) && !isAfter(to);
    }

    /**
     * Checks if this timestamp is in the past relative to now.
     */
    public boolean isPast() {
        return value.isBefore(Instant.now());
    }

    /**
     * Checks if this timestamp is in the future relative to now.
     */
    public boolean isFuture() {
        return value.isAfter(Instant.now());
    }

    /**
     * Checks if this timestamp is on the same calendar day as another
     * in the given timezone.
     */
    public boolean isSameDay(Timestamp other, ZoneId zone) {
        Objects.requireNonNull(other, "Cannot compare with null");
        Objects.requireNonNull(zone, "ZoneId cannot be null");
        LocalDate thisDate = value.atZone(zone).toLocalDate();
        LocalDate otherDate = other.value.atZone(zone).toLocalDate();
        return thisDate.equals(otherDate);
    }

    /**
     * Checks if this timestamp is on the same calendar day as another
     * using the system default timezone.
     */
    public boolean isSameDay(Timestamp other) {
        return isSameDay(other, ZoneId.systemDefault());
    }

    @Override
    public int compareTo(Timestamp other) {
        return this.value.compareTo(other.value);
    }

    // ==================== Duration & Arithmetic ====================

    /**
     * Returns the duration between this timestamp and another.
     *
     * @param other the other timestamp
     * @return the duration (positive if other is after this, negative if before)
     */
    public Duration durationUntil(Timestamp other) {
        Objects.requireNonNull(other, "Cannot compute duration to null");
        return Duration.between(this.value, other.value);
    }

    /**
     * Returns the duration between this timestamp and now.
     * Positive if this is in the past, negative if in the future.
     */
    public Duration elapsed() {
        return Duration.between(this.value, Instant.now());
    }

    /**
     * Returns the number of seconds between this and another timestamp.
     */
    public long secondsUntil(Timestamp other) {
        Objects.requireNonNull(other, "Cannot compute seconds to null");
        return ChronoUnit.SECONDS.between(this.value, other.value);
    }

    /**
     * Returns the number of days between this and another timestamp.
     */
    public long daysUntil(Timestamp other) {
        Objects.requireNonNull(other, "Cannot compute days to null");
        return ChronoUnit.DAYS.between(this.value, other.value);
    }

    // --- Plus operations ---

    public Timestamp plusSeconds(long seconds) {
        return new Timestamp(value.plusSeconds(seconds));
    }

    public Timestamp plusMinutes(long minutes) {
        return new Timestamp(value.plus(minutes, ChronoUnit.MINUTES));
    }

    public Timestamp plusHours(long hours) {
        return new Timestamp(value.plus(hours, ChronoUnit.HOURS));
    }

    public Timestamp plusDays(long days) {
        return new Timestamp(value.plus(days, ChronoUnit.DAYS));
    }

    public Timestamp plus(Duration duration) {
        Objects.requireNonNull(duration, "Duration cannot be null");
        return new Timestamp(value.plus(duration));
    }

    // --- Minus operations ---

    public Timestamp minusSeconds(long seconds) {
        return new Timestamp(value.minusSeconds(seconds));
    }

    public Timestamp minusMinutes(long minutes) {
        return new Timestamp(value.minus(minutes, ChronoUnit.MINUTES));
    }

    public Timestamp minusHours(long hours) {
        return new Timestamp(value.minus(hours, ChronoUnit.HOURS));
    }

    public Timestamp minusDays(long days) {
        return new Timestamp(value.minus(days, ChronoUnit.DAYS));
    }

    public Timestamp minus(Duration duration) {
        Objects.requireNonNull(duration, "Duration cannot be null");
        return new Timestamp(value.minus(duration));
    }

    // --- Min / Max ---

    /**
     * Returns the earlier of this and another timestamp.
     */
    public Timestamp min(Timestamp other) {
        Objects.requireNonNull(other, "Cannot compare with null");
        return isBefore(other) ? this : other;
    }

    /**
     * Returns the later of this and another timestamp.
     */
    public Timestamp max(Timestamp other) {
        Objects.requireNonNull(other, "Cannot compare with null");
        return isAfter(other) ? this : other;
    }

    // ==================== Conversion ====================

    /**
     * Returns the Unix epoch second.
     */
    public long toEpochSecond() {
        return value.getEpochSecond();
    }

    /**
     * Returns the Unix epoch millisecond.
     */
    public long toEpochMilli() {
        return value.toEpochMilli();
    }

    /**
     * Converts to a ZonedDateTime in the given timezone.
     */
    public ZonedDateTime toZonedDateTime(ZoneId zone) {
        Objects.requireNonNull(zone, "ZoneId cannot be null");
        return value.atZone(zone);
    }

    /**
     * Converts to a ZonedDateTime in the system default timezone.
     */
    public ZonedDateTime toZonedDateTime() {
        return toZonedDateTime(ZoneId.systemDefault());
    }

    /**
     * Converts to a LocalDateTime in the given timezone.
     */
    public LocalDateTime toLocalDateTime(ZoneId zone) {
        return toZonedDateTime(zone).toLocalDateTime();
    }

    /**
     * Converts to a LocalDateTime in the system default timezone.
     */
    public LocalDateTime toLocalDateTime() {
        return toLocalDateTime(ZoneId.systemDefault());
    }

    /**
     * Converts to a LocalDate in the given timezone.
     */
    public LocalDate toLocalDate(ZoneId zone) {
        return toZonedDateTime(zone).toLocalDate();
    }

    /**
     * Converts to a LocalDate in the system default timezone.
     */
    public LocalDate toLocalDate() {
        return toLocalDate(ZoneId.systemDefault());
    }

    /**
     * Returns the underlying Instant.
     */
    public Instant toInstant() {
        return value;
    }

    // ==================== Formatting ====================

    /**
     * Returns a human-readable string with AM/PM using the system default timezone.
     * Example: "December 24, 2025 12:27:00 AM"
     */
    public String toHumanReadable() {
        return toHumanReadable(ZoneId.systemDefault());
    }

    /**
     * Returns a human-readable string with AM/PM for a specific timezone.
     */
    public String toHumanReadable(ZoneId zoneId) {
        return toHumanReadable(zoneId, HUMAN_READABLE_FORMATTER);
    }

    /**
     * Returns a formatted string using a custom formatter and timezone.
     */
    public String toHumanReadable(ZoneId zoneId, DateTimeFormatter formatter) {
        Objects.requireNonNull(zoneId, "ZoneId cannot be null");
        Objects.requireNonNull(formatter, "Formatter cannot be null");
        return value.atZone(zoneId).format(formatter);
    }

    /**
     * Returns an ISO-8601 string.
     * Example: "2025-01-25T10:30:00Z"
     */
    public String toIsoString() {
        return ISO_FORMATTER.format(value);
    }

    /**
     * Returns a date-only string in the given timezone.
     * Example: "2025-01-25"
     */
    public String toDateString(ZoneId zone) {
        return value.atZone(zone).format(DATE_ONLY_FORMATTER);
    }

    /**
     * Returns a date-only string using the system default timezone.
     */
    public String toDateString() {
        return toDateString(ZoneId.systemDefault());
    }

    /**
     * Returns a compact datetime string in the given timezone.
     * Example: "2025-01-25 10:30:00"
     */
    public String toCompactString(ZoneId zone) {
        return value.atZone(zone).format(COMPACT_FORMATTER);
    }

    /**
     * Returns a compact datetime string using the system default timezone.
     */
    public String toCompactString() {
        return toCompactString(ZoneId.systemDefault());
    }

    /**
     * Returns a human-friendly relative time description.
     * Examples: "just now", "5 minutes ago", "2 hours ago", "3 days ago", "in 2 hours"
     */
    public String toRelativeString() {
        Duration elapsed = Duration.between(value, Instant.now());
        boolean past = !elapsed.isNegative();
        long seconds = Math.abs(elapsed.getSeconds());

        String description;
        if (seconds < 60) {
            return "just now";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            description = minutes + (minutes == 1 ? " minute" : " minutes");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            description = hours + (hours == 1 ? " hour" : " hours");
        } else if (seconds < 2592000) {
            long days = seconds / 86400;
            description = days + (days == 1 ? " day" : " days");
        } else if (seconds < 31536000) {
            long months = seconds / 2592000;
            description = months + (months == 1 ? " month" : " months");
        } else {
            long years = seconds / 31536000;
            description = years + (years == 1 ? " year" : " years");
        }

        return past ? description + " ago" : "in " + description;
    }

    @Override
    public String toString() {
        return toHumanReadable();
    }
}