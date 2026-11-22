package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable value object representing a point in time with offset awareness.
 * <p>
 * Wraps {@link java.time.OffsetDateTime} and integrates with the {@link TimeZone}
 * value object enum for timezone-aware conversions and display.
 *
 * @author JoshuaSalcedo
 */
public record DateTimeWithZone(OffsetDateTime value) implements ValueObject, Comparable<DateTimeWithZone> {

    /** Default value representing the Unix epoch (1970-01-01T00:00:00Z). */
    public static final DateTimeWithZone DEFAULT = new DateTimeWithZone(
            OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC)
    );

    // ──────────────────────────────────────────────
    //  Compact Constructor
    // ──────────────────────────────────────────────

    public DateTimeWithZone {
        Objects.requireNonNull(value, "OffsetDateTime value cannot be null");
    }

    // ──────────────────────────────────────────────
    //  Factory Methods
    // ──────────────────────────────────────────────

    /**
     * Wraps an existing {@link OffsetDateTime}.
     */
    public static DateTimeWithZone of(OffsetDateTime value) {
        return new DateTimeWithZone(value);
    }

    /**
     * Creates from date-time components and a {@link TimeZone}.
     */
    public static DateTimeWithZone of(int year, int month, int day,
                                      int hour, int minute, int second,
                                      TimeZone timeZone) {
        Objects.requireNonNull(timeZone, "TimeZone cannot be null");
        ZonedDateTime zdt = ZonedDateTime.of(year, month, day, hour, minute, second, 0, timeZone.toZoneId());
        return new DateTimeWithZone(zdt.toOffsetDateTime());
    }

    /**
     * Creates from date-time components and a {@link ZoneOffset}.
     */
    public static DateTimeWithZone of(int year, int month, int day,
                                      int hour, int minute, int second,
                                      ZoneOffset offset) {
        Objects.requireNonNull(offset, "ZoneOffset cannot be null");
        return new DateTimeWithZone(OffsetDateTime.of(year, month, day, hour, minute, second, 0, offset));
    }

    /**
     * Creates from a {@link LocalDateTime} and a {@link TimeZone}.
     */
    public static DateTimeWithZone of(LocalDateTime localDateTime, TimeZone timeZone) {
        Objects.requireNonNull(localDateTime, "LocalDateTime cannot be null");
        Objects.requireNonNull(timeZone, "TimeZone cannot be null");
        ZonedDateTime zdt = localDateTime.atZone(timeZone.toZoneId());
        return new DateTimeWithZone(zdt.toOffsetDateTime());
    }

    /**
     * Creates from a {@link LocalDateTime} and a {@link ZoneOffset}.
     */
    public static DateTimeWithZone of(LocalDateTime localDateTime, ZoneOffset offset) {
        Objects.requireNonNull(localDateTime, "LocalDateTime cannot be null");
        Objects.requireNonNull(offset, "ZoneOffset cannot be null");
        return new DateTimeWithZone(OffsetDateTime.of(localDateTime, offset));
    }

    /**
     * Creates from a {@link LocalDate} at start of day in the given {@link TimeZone}.
     */
    public static DateTimeWithZone ofStartOfDay(LocalDate date, TimeZone timeZone) {
        Objects.requireNonNull(date, "LocalDate cannot be null");
        Objects.requireNonNull(timeZone, "TimeZone cannot be null");
        ZonedDateTime zdt = date.atStartOfDay(timeZone.toZoneId());
        return new DateTimeWithZone(zdt.toOffsetDateTime());
    }

    /**
     * Current date-time in UTC.
     */
    public static DateTimeWithZone now() {
        return new DateTimeWithZone(OffsetDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Current date-time in the given {@link TimeZone}.
     */
    public static DateTimeWithZone now(TimeZone timeZone) {
        Objects.requireNonNull(timeZone, "TimeZone cannot be null");
        ZonedDateTime zdt = ZonedDateTime.now(timeZone.toZoneId());
        return new DateTimeWithZone(zdt.toOffsetDateTime());
    }

    /**
     * Parses an ISO-8601 string (e.g. {@code "2026-02-08T14:30:00+08:00"}).
     */
    public static DateTimeWithZone parse(String text) {
        try {
            return new DateTimeWithZone(OffsetDateTime.parse(text));
        } catch (DateTimeParseException e) {
            throw new ValueObjectException("Invalid OffsetDateTime string: " + text, e);
        }
    }

    /**
     * Parses a date-time string with a custom formatter.
     */
    public static DateTimeWithZone parse(String text, DateTimeFormatter formatter) {
        try {
            return new DateTimeWithZone(OffsetDateTime.parse(text, formatter));
        } catch (DateTimeParseException e) {
            throw new ValueObjectException("Invalid OffsetDateTime string: " + text, e);
        }
    }

    /**
     * Creates from epoch seconds (result is in UTC).
     */
    public static DateTimeWithZone ofEpochSecond(long epochSecond) {
        Instant instant = Instant.ofEpochSecond(epochSecond);
        return new DateTimeWithZone(OffsetDateTime.ofInstant(instant, ZoneOffset.UTC));
    }

    /**
     * Creates from epoch milliseconds (result is in UTC).
     */
    public static DateTimeWithZone ofEpochMilli(long epochMilli) {
        Instant instant = Instant.ofEpochMilli(epochMilli);
        return new DateTimeWithZone(OffsetDateTime.ofInstant(instant, ZoneOffset.UTC));
    }

    /**
     * Creates from an {@link Instant} in the given {@link TimeZone}.
     */
    public static DateTimeWithZone ofInstant(Instant instant, TimeZone timeZone) {
        Objects.requireNonNull(instant, "Instant cannot be null");
        Objects.requireNonNull(timeZone, "TimeZone cannot be null");
        return new DateTimeWithZone(OffsetDateTime.ofInstant(instant, timeZone.toZoneId()));
    }

    /**
     * Creates from an {@link Instant} in UTC.
     */
    public static DateTimeWithZone ofInstant(Instant instant) {
        Objects.requireNonNull(instant, "Instant cannot be null");
        return new DateTimeWithZone(OffsetDateTime.ofInstant(instant, ZoneOffset.UTC));
    }

    /**
     * Returns the default value (Unix epoch).
     *
     * @return the DEFAULT constant
     */
    public static DateTimeWithZone defaultValue() {
        return DEFAULT;
    }

    // ──────────────────────────────────────────────
    //  Accessors
    // ──────────────────────────────────────────────

    public int year()          { return value.getYear(); }
    public Month month()       { return value.getMonth(); }
    public int monthValue()    { return value.getMonthValue(); }
    public int dayOfMonth()    { return value.getDayOfMonth(); }
    public DayOfWeek dayOfWeek() { return value.getDayOfWeek(); }
    public int dayOfYear()     { return value.getDayOfYear(); }
    public int hour()          { return value.getHour(); }
    public int minute()        { return value.getMinute(); }
    public int second()        { return value.getSecond(); }
    public int nano()          { return value.getNano(); }
    public ZoneOffset offset() { return value.getOffset(); }

    // ──────────────────────────────────────────────
    //  TimeZone Integration
    // ──────────────────────────────────────────────

    /**
     * Converts to a {@link ZonedDateTime} in the given {@link TimeZone}.
     * The instant is preserved — only the zone representation changes.
     */
    public ZonedDateTime toZonedDateTime(TimeZone timeZone) {
        Objects.requireNonNull(timeZone, "TimeZone cannot be null");
        return value.atZoneSameInstant(timeZone.toZoneId());
    }

    /**
     * Converts to a {@link ZonedDateTime} using the system default zone.
     */
    public ZonedDateTime toZonedDateTime() {
        return value.atZoneSameInstant(ZoneId.systemDefault());
    }

    /**
     * Returns a new {@code DateTimeWithZone} representing the same instant
     * but expressed in the given {@link TimeZone}'s current offset.
     */
    public DateTimeWithZone atTimeZone(TimeZone timeZone) {
        Objects.requireNonNull(timeZone, "TimeZone cannot be null");
        ZonedDateTime zdt = value.atZoneSameInstant(timeZone.toZoneId());
        return new DateTimeWithZone(zdt.toOffsetDateTime());
    }

    /**
     * Returns a new {@code DateTimeWithZone} converted to UTC.
     */
    public DateTimeWithZone toUtc() {
        return new DateTimeWithZone(value.withOffsetSameInstant(ZoneOffset.UTC));
    }

    /**
     * Attempts to resolve the current offset to a matching {@link TimeZone} enum value.
     * <p>
     * Since multiple timezones can share the same offset, this returns the first match
     * found by comparing the offset string. Returns {@link Optional#empty()} if no match.
     */
    public Optional<TimeZone> resolveTimeZone() {
        String currentOffset = formatOffset(value.getOffset());
        for (TimeZone tz : TimeZone.values()) {
            if (tz.getCurrentOffset().equals(currentOffset)) {
                return Optional.of(tz);
            }
        }
        return Optional.empty();
    }

    /**
     * Checks whether this date-time's offset matches the given {@link TimeZone}'s current offset.
     */
    public boolean isInTimeZone(TimeZone timeZone) {
        Objects.requireNonNull(timeZone, "TimeZone cannot be null");
        ZonedDateTime zdt = ZonedDateTime.now(timeZone.toZoneId());
        return value.getOffset().equals(zdt.getOffset());
    }

    // ──────────────────────────────────────────────
    //  Conversions
    // ──────────────────────────────────────────────

    public Instant toInstant()             { return value.toInstant(); }
    public LocalDateTime toLocalDateTime() { return value.toLocalDateTime(); }
    public LocalDate toLocalDate()         { return value.toLocalDate(); }
    public LocalTime toLocalTime()         { return value.toLocalTime(); }
    public long toEpochSecond()            { return value.toEpochSecond(); }
    public long toEpochMilli()             { return value.toInstant().toEpochMilli(); }

    // ──────────────────────────────────────────────
    //  Arithmetic (returns new instances — immutable)
    // ──────────────────────────────────────────────

    public DateTimeWithZone plusYears(long years)     { return new DateTimeWithZone(value.plusYears(years)); }
    public DateTimeWithZone plusMonths(long months)   { return new DateTimeWithZone(value.plusMonths(months)); }
    public DateTimeWithZone plusWeeks(long weeks)     { return new DateTimeWithZone(value.plusWeeks(weeks)); }
    public DateTimeWithZone plusDays(long days)       { return new DateTimeWithZone(value.plusDays(days)); }
    public DateTimeWithZone plusHours(long hours)     { return new DateTimeWithZone(value.plusHours(hours)); }
    public DateTimeWithZone plusMinutes(long minutes) { return new DateTimeWithZone(value.plusMinutes(minutes)); }
    public DateTimeWithZone plusSeconds(long seconds) { return new DateTimeWithZone(value.plusSeconds(seconds)); }
    public DateTimeWithZone plusNanos(long nanos)     { return new DateTimeWithZone(value.plusNanos(nanos)); }

    public DateTimeWithZone minusYears(long years)     { return new DateTimeWithZone(value.minusYears(years)); }
    public DateTimeWithZone minusMonths(long months)   { return new DateTimeWithZone(value.minusMonths(months)); }
    public DateTimeWithZone minusWeeks(long weeks)     { return new DateTimeWithZone(value.minusWeeks(weeks)); }
    public DateTimeWithZone minusDays(long days)       { return new DateTimeWithZone(value.minusDays(days)); }
    public DateTimeWithZone minusHours(long hours)     { return new DateTimeWithZone(value.minusHours(hours)); }
    public DateTimeWithZone minusMinutes(long minutes) { return new DateTimeWithZone(value.minusMinutes(minutes)); }
    public DateTimeWithZone minusSeconds(long seconds) { return new DateTimeWithZone(value.minusSeconds(seconds)); }
    public DateTimeWithZone minusNanos(long nanos)     { return new DateTimeWithZone(value.minusNanos(nanos)); }

    public DateTimeWithZone plus(long amount, ChronoUnit unit) {
        return new DateTimeWithZone(value.plus(amount, unit));
    }

    public DateTimeWithZone minus(long amount, ChronoUnit unit) {
        return new DateTimeWithZone(value.minus(amount, unit));
    }

    // ──────────────────────────────────────────────
    //  Duration / Period Between
    // ──────────────────────────────────────────────

    /**
     * Returns the {@link java.time.Duration} between this and another {@code DateTimeWithZone}.
     */
    public java.time.Duration durationUntil(DateTimeWithZone other) {
        Objects.requireNonNull(other, "other cannot be null");
        return java.time.Duration.between(this.value.toInstant(), other.value.toInstant());
    }

    /**
     * Returns the amount of the given unit between this and another {@code DateTimeWithZone}.
     */
    public long until(DateTimeWithZone other, ChronoUnit unit) {
        Objects.requireNonNull(other, "other cannot be null");
        return this.value.until(other.value, unit);
    }

    /**
     * Returns hours between this and another {@code DateTimeWithZone}.
     */
    public long hoursUntil(DateTimeWithZone other) {
        return until(other, ChronoUnit.HOURS);
    }

    /**
     * Returns days between this and another {@code DateTimeWithZone}.
     */
    public long daysUntil(DateTimeWithZone other) {
        return until(other, ChronoUnit.DAYS);
    }

    // ──────────────────────────────────────────────
    //  Comparison Helpers
    // ──────────────────────────────────────────────

    public boolean isBefore(DateTimeWithZone other) {
        return value.isBefore(other.value);
    }

    public boolean isAfter(DateTimeWithZone other) {
        return value.isAfter(other.value);
    }

    public boolean isEqual(DateTimeWithZone other) {
        return value.isEqual(other.value);
    }

    /**
     * Checks if this date-time is between two others (inclusive).
     */
    public boolean isBetween(DateTimeWithZone start, DateTimeWithZone end) {
        Objects.requireNonNull(start, "start cannot be null");
        Objects.requireNonNull(end, "end cannot be null");
        return !value.isBefore(start.value) && !value.isAfter(end.value);
    }

    public boolean isFuture() {
        return value.isAfter(OffsetDateTime.now(ZoneOffset.UTC));
    }

    public boolean isPast() {
        return value.isBefore(OffsetDateTime.now(ZoneOffset.UTC));
    }

    public boolean isToday(TimeZone timeZone) {
        LocalDate today = LocalDate.now(timeZone.toZoneId());
        LocalDate thisDate = toZonedDateTime(timeZone).toLocalDate();
        return thisDate.equals(today);
    }

    // ──────────────────────────────────────────────
    //  Truncation / Boundaries
    // ──────────────────────────────────────────────

    public DateTimeWithZone truncatedTo(ChronoUnit unit) {
        return new DateTimeWithZone(value.truncatedTo(unit));
    }

    public DateTimeWithZone startOfDay() {
        return new DateTimeWithZone(value.truncatedTo(ChronoUnit.DAYS));
    }

    public DateTimeWithZone startOfHour() {
        return new DateTimeWithZone(value.truncatedTo(ChronoUnit.HOURS));
    }

    // ──────────────────────────────────────────────
    //  Formatting
    // ──────────────────────────────────────────────

    /**
     * Formats using the given {@link DateTimeFormatter}.
     */
    public String format(DateTimeFormatter formatter) {
        return value.format(formatter);
    }

    /**
     * Returns ISO-8601 string (e.g. {@code "2026-02-08T14:30:00+08:00"}).
     */
    public String toIsoString() {
        return value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Returns a human-readable string like {@code "February 8, 2026 2:30 PM (+08:00)"}.
     */
    public String toHumanReadable() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a");
        return value.format(fmt) + " (" + formatOffset(value.getOffset()) + ")";
    }

    /**
     * Returns a formatted display string incorporating a {@link TimeZone},
     * e.g. {@code "Feb 8, 2026 2:30 PM PHT (Asia/Manila)"}.
     */
    public String toDisplayString(TimeZone timeZone) {
        ZonedDateTime zdt = toZonedDateTime(timeZone);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a z");
        return zdt.format(fmt) + " (" + timeZone.zoneId() + ")";
    }

    // ──────────────────────────────────────────────
    //  Comparable / toString
    // ──────────────────────────────────────────────

    @Override
    public int compareTo(DateTimeWithZone other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return toIsoString();
    }

    // ──────────────────────────────────────────────
    //  Internal Helpers
    // ──────────────────────────────────────────────

    private static String formatOffset(ZoneOffset offset) {
        int totalSeconds = offset.getTotalSeconds();
        int hours = totalSeconds / 3600;
        int minutes = Math.abs((totalSeconds % 3600) / 60);
        return String.format("GMT%+d:%02d", hours, minutes);
    }
}
