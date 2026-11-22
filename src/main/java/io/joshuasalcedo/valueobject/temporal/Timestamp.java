package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;
import java.util.Optional;
import io.joshuasalcedo.valueobject.ValueObject;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Immutable value object representing a point in time.
 *
 * <p>Wraps {@link Instant} with a richer API for comparison, arithmetic,
 * and formatting. All operations return new instances.</p>
 *
 * <h2>Usage examples</h2>
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
 * <p>Methods that need a calendar date or clock time take a {@link ZoneId}; their no-zone overloads use this
 * machine's default zone. {@link #toString()} is the ISO-8601 instant in UTC, the same on every machine. Text
 * meant for people is written in English. Day arithmetic counts days of exactly 24 hours. Bad input (null, an
 * unreadable text, a result out of {@link Instant}'s range) throws {@link ValueObjectException}.</p>
 *
 * @param value the instant
 * @author JoshuaSalcedo
 * @since 12/24/2025 12:27 AM
 */
public record Timestamp(Instant value) implements ValueObject, Comparable<Timestamp> {

    private static final DateTimeFormatter HUMAN_READABLE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm:ss a", Locale.ENGLISH);

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_INSTANT;

    private static final DateTimeFormatter DATE_ONLY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

    private static final DateTimeFormatter COMPACT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    // ==================== Compact Constructor ====================

    /**
     * Checks that the instant is present.
     *
     * @throws ValueObjectException if {@code value} is null
     */
    public Timestamp {
        if (value == null) throw new ValueObjectException("Timestamp value cannot be null");
    }

    // ==================== Factory Methods ====================

    /**
     * Returns the current timestamp, from the system clock.
     *
     * @return now
     */
    public static Timestamp now() {
        return new Timestamp(Instant.now());
    }

    /**
     * Creates a Timestamp from an Instant.
     *
     * @param instant the point in time
     * @return it
     * @throws ValueObjectException if {@code instant} is null
     */
    public static Timestamp of(Instant instant) {
        return new Timestamp(instant);
    }

    /**
     * Creates a Timestamp from Unix epoch seconds.
     * This is the most common format in APIs (Telegram, Unix, etc.).
     *
     * @param epochSecond seconds since 1970-01-01T00:00:00Z
     * @return that instant
     * @throws ValueObjectException if the value is beyond the range {@link Instant} supports
     */
    public static Timestamp ofEpochSecond(long epochSecond) {
        return arith(() -> Instant.ofEpochSecond(epochSecond));
    }

    /**
     * Creates a Timestamp from Unix epoch seconds (int overload).
     * Convenience for APIs that return epoch seconds as int (e.g., Telegram TDLib).
     *
     * @param epochSecond seconds since 1970-01-01T00:00:00Z
     * @return that instant
     */
    public static Timestamp ofEpochSecond(int epochSecond) {
        return ofEpochSecond((long) epochSecond);
    }

    /**
     * Creates a Timestamp from Unix epoch milliseconds.
     * Common in Java APIs and JavaScript.
     *
     * @param epochMilli milliseconds since 1970-01-01T00:00:00Z
     * @return that instant
     */
    public static Timestamp ofEpochMilli(long epochMilli) {
        return new Timestamp(Instant.ofEpochMilli(epochMilli));
    }

    /**
     * Creates a Timestamp from a LocalDateTime in a specific timezone. A time in a daylight-saving gap is moved
     * later by the length of the gap; in an overlap the earlier offset is used.
     *
     * @param dateTime the date and time on the clock
     * @param zone     the zone the clock is in
     * @return that instant
     * @throws ValueObjectException if either argument is null
     */
    public static Timestamp of(LocalDateTime dateTime, ZoneId zone) {
        requireNonNull(dateTime, "LocalDateTime cannot be null");
        requireNonNull(zone, "ZoneId cannot be null");
        return arith(() -> dateTime.atZone(zone).toInstant());
    }

    /**
     * Creates a Timestamp from a LocalDate at midnight in a specific timezone (moved later if midnight falls in
     * a daylight-saving gap).
     *
     * @param date the day
     * @param zone the zone whose midnight is meant
     * @return that instant
     * @throws ValueObjectException if either argument is null
     */
    public static Timestamp of(LocalDate date, ZoneId zone) {
        requireNonNull(date, "LocalDate cannot be null");
        requireNonNull(zone, "ZoneId cannot be null");
        return of(date.atStartOfDay(), zone);
    }

    /**
     * Creates a Timestamp from a ZonedDateTime.
     *
     * @param zonedDateTime the date-time and zone
     * @return its instant
     * @throws ValueObjectException if {@code zonedDateTime} is null
     */
    public static Timestamp of(ZonedDateTime zonedDateTime) {
        requireNonNull(zonedDateTime, "ZonedDateTime cannot be null");
        return new Timestamp(zonedDateTime.toInstant());
    }

    /**
     * Parses an ISO-8601 string, ignoring blanks around it.
     * Accepts formats like {@code "2025-01-25T10:30:00Z"} or {@code "2025-01-25T10:30:00+08:00"}; the offset is
     * required.
     *
     * @param isoString the text
     * @return that instant
     * @throws ValueObjectException if {@code isoString} is null or cannot be parsed
     */
    public static Timestamp parse(String isoString) {
        requireNonNull(isoString, "ISO string cannot be null");
        try {
            return new Timestamp(Instant.parse(isoString.strip()));
        } catch (DateTimeException e) {
            throw new ValueObjectException("Not an ISO-8601 instant: '" + isoString + "'", e);
        }
    }

    /**
     * Returns the Unix epoch (epoch zero: 1970-01-01T00:00:00Z). Earlier timestamps are allowed.
     *
     * @return the epoch
     */
    public static Timestamp epoch() {
        return new Timestamp(Instant.EPOCH);
    }

    // ==================== Comparison ====================

    /**
     * Checks if this timestamp is before another.
     *
     * @param other the timestamp to compare with
     * @return whether this one is earlier
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isBefore(Timestamp other) {
        requireNonNull(other, "Cannot compare with null");
        return value.isBefore(other.value);
    }

    /**
     * Checks if this timestamp is after another.
     *
     * @param other the timestamp to compare with
     * @return whether this one is later
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isAfter(Timestamp other) {
        requireNonNull(other, "Cannot compare with null");
        return value.isAfter(other.value);
    }

    /**
     * Checks if this timestamp is between two others (inclusive).
     *
     * @param from the earliest allowed timestamp
     * @param to   the latest allowed timestamp
     * @return whether this one is neither before {@code from} nor after {@code to}
     * @throws ValueObjectException if either argument is null
     */
    public boolean isBetween(Timestamp from, Timestamp to) {
        requireNonNull(from, "From timestamp cannot be null");
        requireNonNull(to, "To timestamp cannot be null");
        return !isBefore(from) && !isAfter(to);
    }

    /**
     * Checks if this timestamp is in the past relative to now.
     *
     * @return whether it is before now
     */
    public boolean isPast() {
        return value.isBefore(Instant.now());
    }

    /**
     * Checks if this timestamp is in the future relative to now.
     *
     * @return whether it is after now
     */
    public boolean isFuture() {
        return value.isAfter(Instant.now());
    }

    /**
     * Checks if this timestamp is on the same calendar day as another
     * in the given timezone.
     *
     * @param other the timestamp to compare with
     * @param zone  the zone whose calendar is meant
     * @return whether both fall on the same date there
     * @throws ValueObjectException if either argument is null
     */
    public boolean isSameDay(Timestamp other, ZoneId zone) {
        requireNonNull(other, "Cannot compare with null");
        requireNonNull(zone, "ZoneId cannot be null");
        LocalDate thisDate = toLocalDate(zone);
        LocalDate otherDate = other.toLocalDate(zone);
        return thisDate.equals(otherDate);
    }

    /**
     * Checks if this timestamp is on the same calendar day as another
     * using the system default timezone.
     *
     * @param other the timestamp to compare with
     * @return whether both fall on the same date in this machine's zone
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isSameDay(Timestamp other) {
        return isSameDay(other, ZoneId.systemDefault());
    }

    /**
     * Orders timestamps from earliest to latest.
     *
     * @param other the timestamp to compare to
     * @return negative, zero or positive as this one is earlier than, the same as or later than {@code other}
     * @throws ValueObjectException if {@code other} is null
     */
    @Override
    public int compareTo(Timestamp other) {
        requireNonNull(other, "Cannot compare with null");
        return this.value.compareTo(other.value);
    }

    // ==================== Duration & Arithmetic ====================

    /**
     * Returns the duration between this timestamp and another.
     *
     * @param other the other timestamp
     * @return the duration (positive if other is after this, negative if before)
     * @throws ValueObjectException if {@code other} is null
     */
    public Duration durationUntil(Timestamp other) {
        requireNonNull(other, "Cannot compute duration to null");
        return Duration.between(this.value, other.value);
    }

    /**
     * Returns the duration between this timestamp and now.
     * Positive if this is in the past, negative if in the future.
     *
     * @return the time from this timestamp to now
     */
    public Duration elapsed() {
        return Duration.between(this.value, Instant.now());
    }

    /**
     * Returns the number of whole seconds between this and another timestamp.
     *
     * @param other the timestamp to count to
     * @return the whole seconds from this one to {@code other}; negative when {@code other} is earlier
     * @throws ValueObjectException if {@code other} is null
     */
    public long secondsUntil(Timestamp other) {
        requireNonNull(other, "Cannot compute seconds to null");
        return ChronoUnit.SECONDS.between(this.value, other.value);
    }

    /**
     * Returns the number of whole 24-hour days between this and another timestamp.
     *
     * @param other the timestamp to count to
     * @return the whole days from this one to {@code other}; negative when {@code other} is earlier
     * @throws ValueObjectException if {@code other} is null
     */
    public long daysUntil(Timestamp other) {
        requireNonNull(other, "Cannot compute days to null");
        return ChronoUnit.DAYS.between(this.value, other.value);
    }

    // --- Plus operations ---

    /**
     * Adds seconds.
     *
     * @param seconds seconds to add (negative to go back)
     * @return the new timestamp
     * @throws ValueObjectException if the result is out of range
     */
    public Timestamp plusSeconds(long seconds) {
        return arith(() -> value.plusSeconds(seconds));
    }

    /**
     * Adds minutes.
     *
     * @param minutes minutes to add (negative to go back)
     * @return the new timestamp
     * @throws ValueObjectException if the result is out of range
     */
    public Timestamp plusMinutes(long minutes) {
        return arith(() -> value.plus(minutes, ChronoUnit.MINUTES));
    }

    /**
     * Adds hours.
     *
     * @param hours hours to add (negative to go back)
     * @return the new timestamp
     * @throws ValueObjectException if the result is out of range
     */
    public Timestamp plusHours(long hours) {
        return arith(() -> value.plus(hours, ChronoUnit.HOURS));
    }

    /**
     * Adds days of exactly 24 hours (not calendar days, which daylight saving time can lengthen or shorten).
     *
     * @param days days to add (negative to go back)
     * @return the new timestamp
     * @throws ValueObjectException if the result is out of range
     */
    public Timestamp plusDays(long days) {
        return arith(() -> value.plus(days, ChronoUnit.DAYS));
    }

    /**
     * Adds a duration.
     *
     * @param duration the time to add (negative to go back)
     * @return the new timestamp
     * @throws ValueObjectException if {@code duration} is null or the result is out of range
     */
    public Timestamp plus(Duration duration) {
        requireNonNull(duration, "Duration cannot be null");
        return arith(() -> value.plus(duration));
    }

    // --- Minus operations ---

    /**
     * Subtracts seconds.
     *
     * @param seconds seconds to subtract (negative to go forward)
     * @return the new timestamp
     * @throws ValueObjectException if the result is out of range
     */
    public Timestamp minusSeconds(long seconds) {
        return arith(() -> value.minusSeconds(seconds));
    }

    /**
     * Subtracts minutes.
     *
     * @param minutes minutes to subtract (negative to go forward)
     * @return the new timestamp
     * @throws ValueObjectException if the result is out of range
     */
    public Timestamp minusMinutes(long minutes) {
        return arith(() -> value.minus(minutes, ChronoUnit.MINUTES));
    }

    /**
     * Subtracts hours.
     *
     * @param hours hours to subtract (negative to go forward)
     * @return the new timestamp
     * @throws ValueObjectException if the result is out of range
     */
    public Timestamp minusHours(long hours) {
        return arith(() -> value.minus(hours, ChronoUnit.HOURS));
    }

    /**
     * Subtracts days of exactly 24 hours.
     *
     * @param days days to subtract (negative to go forward)
     * @return the new timestamp
     * @throws ValueObjectException if the result is out of range
     */
    public Timestamp minusDays(long days) {
        return arith(() -> value.minus(days, ChronoUnit.DAYS));
    }

    /**
     * Subtracts a duration.
     *
     * @param duration the time to subtract (negative to go forward)
     * @return the new timestamp
     * @throws ValueObjectException if {@code duration} is null or the result is out of range
     */
    public Timestamp minus(Duration duration) {
        requireNonNull(duration, "Duration cannot be null");
        return arith(() -> value.minus(duration));
    }

    // --- Min / Max ---

    /**
     * Returns the earlier of this and another timestamp.
     *
     * @param other the timestamp to compare with
     * @return the earlier one ({@code other} when they are equal)
     * @throws ValueObjectException if {@code other} is null
     */
    public Timestamp min(Timestamp other) {
        requireNonNull(other, "Cannot compare with null");
        return isBefore(other) ? this : other;
    }

    /**
     * Returns the later of this and another timestamp.
     *
     * @param other the timestamp to compare with
     * @return the later one ({@code other} when they are equal)
     * @throws ValueObjectException if {@code other} is null
     */
    public Timestamp max(Timestamp other) {
        requireNonNull(other, "Cannot compare with null");
        return isAfter(other) ? this : other;
    }

    // ==================== Conversion ====================

    /**
     * Returns the Unix epoch second; any fraction of a second is dropped (rounding toward the past).
     *
     * @return seconds since 1970-01-01T00:00:00Z
     */
    public long toEpochSecond() {
        return value.getEpochSecond();
    }

    /**
     * Returns the Unix epoch millisecond; any finer fraction is dropped.
     *
     * @return milliseconds since 1970-01-01T00:00:00Z
     * @throws ValueObjectException if the value does not fit in a {@code long}
     */
    public long toEpochMilli() {
        try {
            return value.toEpochMilli();
        } catch (ArithmeticException e) {
            throw new ValueObjectException("Too far from the epoch for milliseconds: " + value, e);
        }
    }

    /**
     * Converts to a ZonedDateTime in the given timezone.
     *
     * @param zone the zone to express it in
     * @return this instant in that zone
     * @throws ValueObjectException if {@code zone} is null or the date-time there is out of range
     */
    public ZonedDateTime toZonedDateTime(ZoneId zone) {
        requireNonNull(zone, "ZoneId cannot be null");
        try {
            return value.atZone(zone);
        } catch (DateTimeException e) {
            throw new ValueObjectException("Out of range in " + zone + ": " + value, e);
        }
    }

    /**
     * Converts to a ZonedDateTime in the system default timezone.
     *
     * @return this instant in this machine's zone
     */
    public ZonedDateTime toZonedDateTime() {
        return toZonedDateTime(ZoneId.systemDefault());
    }

    /**
     * Converts to a LocalDateTime in the given timezone.
     *
     * @param zone the zone whose clock is meant
     * @return the date and time on that zone's clock
     * @throws ValueObjectException if {@code zone} is null
     */
    public LocalDateTime toLocalDateTime(ZoneId zone) {
        return toZonedDateTime(zone).toLocalDateTime();
    }

    /**
     * Converts to a LocalDateTime in the system default timezone.
     *
     * @return the date and time on this machine's clock
     */
    public LocalDateTime toLocalDateTime() {
        return toLocalDateTime(ZoneId.systemDefault());
    }

    /**
     * Converts to a LocalDate in the given timezone.
     *
     * @param zone the zone whose calendar is meant
     * @return the date there
     * @throws ValueObjectException if {@code zone} is null
     */
    public LocalDate toLocalDate(ZoneId zone) {
        return toZonedDateTime(zone).toLocalDate();
    }

    /**
     * Converts to a LocalDate in the system default timezone.
     *
     * @return the date in this machine's zone
     */
    public LocalDate toLocalDate() {
        return toLocalDate(ZoneId.systemDefault());
    }

    /**
     * Returns the underlying Instant, the same as {@link #value()}.
     *
     * @return the instant
     */
    public Instant toInstant() {
        return value;
    }

    // ==================== Formatting ====================

    /**
     * Returns a human-readable string with AM/PM, in English, on this machine's default zone's clock.
     * Example: {@code "December 24, 2025 12:27:00 AM"}
     *
     * @return the date and 12-hour time on this machine's clock
     */
    public String toHumanReadable() {
        return toHumanReadable(ZoneId.systemDefault());
    }

    /**
     * Returns a human-readable string with AM/PM for a specific timezone, in English.
     *
     * @param zoneId the zone whose clock is meant
     * @return e.g. {@code "December 24, 2025 12:27:00 AM"}
     * @throws ValueObjectException if {@code zoneId} is null
     */
    public String toHumanReadable(ZoneId zoneId) {
        return toHumanReadable(zoneId, HUMAN_READABLE_FORMATTER);
    }

    /**
     * Returns a formatted string using a custom formatter and timezone.
     *
     * @param zoneId    the zone whose clock is meant
     * @param formatter how to write it
     * @return the formatted text
     * @throws ValueObjectException if either argument is null, or the formatter cannot write this date-time
     */
    public String toHumanReadable(ZoneId zoneId, DateTimeFormatter formatter) {
        requireNonNull(zoneId, "ZoneId cannot be null");
        requireNonNull(formatter, "Formatter cannot be null");
        try {
            return toZonedDateTime(zoneId).format(formatter);
        } catch (DateTimeException e) {
            throw new ValueObjectException("Cannot format " + value + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns an ISO-8601 string, always in UTC.
     * Example: {@code "2025-01-25T10:30:00Z"}
     *
     * @return the instant in ISO-8601 form; fractions of a second are written only when present
     */
    public String toIsoString() {
        return ISO_FORMATTER.format(value);
    }

    /**
     * Returns a date-only string in the given timezone.
     * Example: {@code "2025-01-25"}
     *
     * @param zone the zone whose calendar is meant
     * @return the date there, as {@code yyyy-MM-dd}
     * @throws ValueObjectException if {@code zone} is null
     */
    public String toDateString(ZoneId zone) {
        return toZonedDateTime(zone).format(DATE_ONLY_FORMATTER);
    }

    /**
     * Returns a date-only string using the system default timezone.
     *
     * @return the date in this machine's zone, as {@code yyyy-MM-dd}
     */
    public String toDateString() {
        return toDateString(ZoneId.systemDefault());
    }

    /**
     * Returns a compact datetime string in the given timezone.
     * Example: {@code "2025-01-25 10:30:00"}
     *
     * @param zone the zone whose clock is meant
     * @return the date and 24-hour time there, as {@code yyyy-MM-dd HH:mm:ss}
     * @throws ValueObjectException if {@code zone} is null
     */
    public String toCompactString(ZoneId zone) {
        return toZonedDateTime(zone).format(COMPACT_FORMATTER);
    }

    /**
     * Returns a compact datetime string using the system default timezone.
     *
     * @return the date and 24-hour time on this machine's clock, as {@code yyyy-MM-dd HH:mm:ss}
     */
    public String toCompactString() {
        return toCompactString(ZoneId.systemDefault());
    }

    /**
     * Returns a human-friendly relative time description, measured from now and rounded down to one unit.
     * Examples: {@code "just now"}, {@code "5 minutes ago"}, {@code "2 hours ago"}, {@code "3 days ago"},
     * {@code "in 2 hours"}. Under a minute either way is {@code "just now"}; a month counts as 30 days and a
     * year as 365.
     *
     * @return the description
     */
    public String toRelativeString() {
        return toRelativeString(Instant.now());
    }

    /**
     * Returns a human-friendly relative time description, as {@link #toRelativeString()} does, measured from a
     * given moment instead of now.
     *
     * @param now the moment to measure from
     * @return the description, e.g. {@code "5 minutes ago"} or {@code "in 2 hours"}
     * @throws ValueObjectException if {@code now} is null
     */
    public String toRelativeString(Instant now) {
        requireNonNull(now, "The moment to measure from cannot be null");
        Duration elapsed = Duration.between(value, now);
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

    /**
     * Writes the timestamp as {@link #toIsoString()} does: the ISO-8601 instant in UTC, the same on every
     * machine, which {@link #parse(String)} reads back. Use {@link #toHumanReadable(ZoneId)} for people.
     *
     * @return e.g. {@code "2025-12-24T00:27:00Z"}
     */
    @Override
    public String toString() {
        return toIsoString();
    }

    private static void requireNonNull(@Nullable Object o, String message) {
        if (o == null) throw new ValueObjectException(message);
    }

    private static Timestamp arith(java.util.function.Supplier<Instant> f) {
        try {
            return new Timestamp(f.get());
        } catch (DateTimeException | ArithmeticException e) {
            throw new ValueObjectException("Timestamp out of range: " + e.getMessage(), e);
        }
    }

    /**
     * Parses an ISO-8601 string like {@link #parse(String)}, without throwing.
     *
     * @param text any text, or null
     * @return it as a {@link Timestamp}, or empty when it is not one; never throws
     */
    public static Optional<Timestamp> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text is an ISO-8601 instant that {@link #parse(String)} accepts.
     *
     * @param text any text, or null
     * @return whether it is a {@link Timestamp} ({@code false} for null)
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
