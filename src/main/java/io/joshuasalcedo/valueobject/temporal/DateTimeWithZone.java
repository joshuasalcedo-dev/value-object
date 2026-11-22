package io.joshuasalcedo.valueobject.temporal;

import org.jspecify.annotations.Nullable;
import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;

/**
 * Immutable value object representing a point in time with offset awareness.
 * <p>
 * Wraps {@link java.time.OffsetDateTime} and integrates with the {@link TimeZone}
 * value object enum for timezone-aware conversions and display. A {@link TimeZone} is turned into the
 * offset it has at the date-time concerned (so daylight saving time is applied); only the offset is kept,
 * not the zone. Equality is that of {@link OffsetDateTime}: the same instant at different offsets is not
 * equal (use {@link #isEqual(DateTimeWithZone)}), while {@link #compareTo(DateTimeWithZone)} orders by instant first.
 *
 * @param value the date-time with its offset
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

    /**
     * Checks that the date-time is present.
     *
     * @throws ValueObjectException if {@code value} is null
     */
    public DateTimeWithZone {
        if (value == null) throw new ValueObjectException("OffsetDateTime value cannot be null");
    }

    // ──────────────────────────────────────────────
    //  Factory Methods
    // ──────────────────────────────────────────────

    /**
     * Wraps an existing {@link OffsetDateTime}.
     *
     * @param value the date-time with its offset
     * @return it
     * @throws ValueObjectException if {@code value} is null
     */
    public static DateTimeWithZone of(OffsetDateTime value) {
        return new DateTimeWithZone(value);
    }

    /**
     * Creates from date-time components and a {@link TimeZone}, taking the zone's offset at that date-time.
     * A local time that falls in a daylight-saving gap is moved later by the length of the gap; in an overlap
     * the earlier offset is used.
     *
     * @param year     the year
     * @param month    the month, 1-12
     * @param day      the day of the month, 1-31
     * @param hour     the hour, 0-23
     * @param minute   the minute, 0-59
     * @param second   the second, 0-59
     * @param timeZone the zone the date-time is in
     * @return that date-time, with no fraction of a second
     * @throws ValueObjectException if {@code timeZone} is null
     * @throws ValueObjectException      if a component is out of range or the day does not exist
     */
    public static DateTimeWithZone of(int year, int month, int day,
                                      int hour, int minute, int second,
                                      TimeZone timeZone) {
        requireNonNull(timeZone, "TimeZone cannot be null");
        return arith(() -> ZonedDateTime.of(year, month, day, hour, minute, second, 0, timeZone.toZoneId())
                .toOffsetDateTime());
    }

    /**
     * Creates from date-time components and a {@link ZoneOffset}.
     *
     * @param year   the year
     * @param month  the month, 1-12
     * @param day    the day of the month, 1-31
     * @param hour   the hour, 0-23
     * @param minute the minute, 0-59
     * @param second the second, 0-59
     * @param offset the offset from UTC
     * @return that date-time, with no fraction of a second
     * @throws ValueObjectException if {@code offset} is null
     * @throws ValueObjectException      if a component is out of range or the day does not exist
     */
    public static DateTimeWithZone of(int year, int month, int day,
                                      int hour, int minute, int second,
                                      ZoneOffset offset) {
        requireNonNull(offset, "ZoneOffset cannot be null");
        return arith(() -> OffsetDateTime.of(year, month, day, hour, minute, second, 0, offset));
    }

    /**
     * Creates from a {@link LocalDateTime} and a {@link TimeZone}, taking the zone's offset at that date-time
     * (a time in a daylight-saving gap is moved later by the length of the gap).
     *
     * @param localDateTime the date and time on the clock
     * @param timeZone      the zone the clock is in
     * @return that date-time
     * @throws ValueObjectException if either argument is null
     */
    public static DateTimeWithZone of(LocalDateTime localDateTime, TimeZone timeZone) {
        requireNonNull(localDateTime, "LocalDateTime cannot be null");
        requireNonNull(timeZone, "TimeZone cannot be null");
        ZonedDateTime zdt = localDateTime.atZone(timeZone.toZoneId());
        return new DateTimeWithZone(zdt.toOffsetDateTime());
    }

    /**
     * Creates from a {@link LocalDateTime} and a {@link ZoneOffset}.
     *
     * @param localDateTime the date and time on the clock
     * @param offset        the offset from UTC
     * @return that date-time
     * @throws ValueObjectException if either argument is null
     */
    public static DateTimeWithZone of(LocalDateTime localDateTime, ZoneOffset offset) {
        requireNonNull(localDateTime, "LocalDateTime cannot be null");
        requireNonNull(offset, "ZoneOffset cannot be null");
        return new DateTimeWithZone(OffsetDateTime.of(localDateTime, offset));
    }

    /**
     * Creates from a {@link LocalDate} at start of day in the given {@link TimeZone}: usually midnight, or the
     * first valid time when the day starts in a daylight-saving gap.
     *
     * @param date     the day
     * @param timeZone the zone whose start of day is meant
     * @return the start of that day in that zone
     * @throws ValueObjectException if either argument is null
     */
    public static DateTimeWithZone ofStartOfDay(LocalDate date, TimeZone timeZone) {
        requireNonNull(date, "LocalDate cannot be null");
        requireNonNull(timeZone, "TimeZone cannot be null");
        ZonedDateTime zdt = date.atStartOfDay(timeZone.toZoneId());
        return new DateTimeWithZone(zdt.toOffsetDateTime());
    }

    /**
     * Current date-time in UTC.
     *
     * @return now, at offset {@code Z}
     */
    public static DateTimeWithZone now() {
        return new DateTimeWithZone(OffsetDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Current date-time in the given {@link TimeZone}.
     *
     * @param timeZone the zone to read the clock in
     * @return now, at that zone's current offset
     * @throws ValueObjectException if {@code timeZone} is null
     */
    public static DateTimeWithZone now(TimeZone timeZone) {
        requireNonNull(timeZone, "TimeZone cannot be null");
        ZonedDateTime zdt = ZonedDateTime.now(timeZone.toZoneId());
        return new DateTimeWithZone(zdt.toOffsetDateTime());
    }

    /**
     * Parses an ISO-8601 string with an offset (e.g. {@code "2026-02-08T14:30:00+08:00"}).
     *
     * @param text the text
     * @return that date-time
     * @throws ValueObjectException if the text is null or not an ISO-8601 date-time with an offset
     */
    public static DateTimeWithZone parse(String text) {
        if (text == null) throw new ValueObjectException("A DateTimeWithZone is required");
        try {
            return new DateTimeWithZone(OffsetDateTime.parse(text));
        } catch (DateTimeParseException e) {
            throw new ValueObjectException("Invalid OffsetDateTime string: " + text, e);
        }
    }

    /**
     * Parses a date-time string with a custom formatter; the formatter must yield an offset.
     *
     * @param text      the text
     * @param formatter how the text is written
     * @return that date-time
     * @throws ValueObjectException if the text or formatter is null, or the text does not match the formatter
     */
    public static DateTimeWithZone parse(String text, DateTimeFormatter formatter) {
        if (text == null) throw new ValueObjectException("A DateTimeWithZone is required");
        requireNonNull(formatter, "DateTimeFormatter cannot be null");
        try {
            return new DateTimeWithZone(OffsetDateTime.parse(text, formatter));
        } catch (DateTimeException e) {
            throw new ValueObjectException("Invalid OffsetDateTime string: " + text, e);
        }
    }

    /**
     * Creates from epoch seconds (result is in UTC).
     *
     * @param epochSecond seconds since 1970-01-01T00:00:00Z
     * @return that instant, at offset {@code Z}
     * @throws ValueObjectException if the instant is out of range
     */
    public static DateTimeWithZone ofEpochSecond(long epochSecond) {
        return arith(() -> OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneOffset.UTC));
    }

    /**
     * Creates from epoch milliseconds (result is in UTC).
     *
     * @param epochMilli milliseconds since 1970-01-01T00:00:00Z
     * @return that instant, at offset {@code Z}
     */
    public static DateTimeWithZone ofEpochMilli(long epochMilli) {
        Instant instant = Instant.ofEpochMilli(epochMilli);
        return new DateTimeWithZone(OffsetDateTime.ofInstant(instant, ZoneOffset.UTC));
    }

    /**
     * Creates from an {@link Instant} in the given {@link TimeZone}, at the zone's offset at that instant.
     *
     * @param instant  the point in time
     * @param timeZone the zone to express it in
     * @return that instant in that zone
     * @throws ValueObjectException if either argument is null
     */
    public static DateTimeWithZone ofInstant(Instant instant, TimeZone timeZone) {
        requireNonNull(instant, "Instant cannot be null");
        requireNonNull(timeZone, "TimeZone cannot be null");
        return arith(() -> OffsetDateTime.ofInstant(instant, timeZone.toZoneId()));
    }

    /**
     * Creates from an {@link Instant} in UTC.
     *
     * @param instant the point in time
     * @return that instant, at offset {@code Z}
     * @throws ValueObjectException if {@code instant} is null
     */
    public static DateTimeWithZone ofInstant(Instant instant) {
        requireNonNull(instant, "Instant cannot be null");
        return arith(() -> OffsetDateTime.ofInstant(instant, ZoneOffset.UTC));
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

    /**
     * The year, at this date-time's own offset.
     *
     * @return the year
     */
    public int year()          { return value.getYear(); }
    /**
     * The month, at this date-time's own offset.
     *
     * @return the month
     */
    public Month month()       { return value.getMonth(); }
    /**
     * The month as a number, at this date-time's own offset.
     *
     * @return the month, 1-12
     */
    public int monthValue()    { return value.getMonthValue(); }
    /**
     * The day of the month, at this date-time's own offset.
     *
     * @return the day of the month, 1-31
     */
    public int dayOfMonth()    { return value.getDayOfMonth(); }
    /**
     * The day of the week, at this date-time's own offset.
     *
     * @return the day of the week
     */
    public DayOfWeek dayOfWeek() { return value.getDayOfWeek(); }
    /**
     * The day of the year, at this date-time's own offset.
     *
     * @return the day of the year, 1-366
     */
    public int dayOfYear()     { return value.getDayOfYear(); }
    /**
     * The hour of the day, at this date-time's own offset.
     *
     * @return the hour, 0-23
     */
    public int hour()          { return value.getHour(); }
    /**
     * The minute of the hour.
     *
     * @return the minute, 0-59
     */
    public int minute()        { return value.getMinute(); }
    /**
     * The second of the minute.
     *
     * @return the second, 0-59
     */
    public int second()        { return value.getSecond(); }
    /**
     * The fraction of the second, in nanoseconds.
     *
     * @return the nanosecond, 0-999,999,999
     */
    public int nano()          { return value.getNano(); }
    /**
     * The offset from UTC.
     *
     * @return the offset
     */
    public ZoneOffset offset() { return value.getOffset(); }

    // ──────────────────────────────────────────────
    //  TimeZone Integration
    // ──────────────────────────────────────────────

    /**
     * Converts to a {@link ZonedDateTime} in the given {@link TimeZone}.
     * The instant is preserved — only the zone representation changes.
     *
     * @param timeZone the zone to express it in
     * @return the same instant in that zone
     * @throws ValueObjectException if {@code timeZone} is null or the result is out of range
     */
    public ZonedDateTime toZonedDateTime(TimeZone timeZone) {
        requireNonNull(timeZone, "TimeZone cannot be null");
        try {
            return value.atZoneSameInstant(timeZone.toZoneId());
        } catch (DateTimeException e) {
            throw new ValueObjectException("Out of range in " + timeZone.zoneId() + ": " + value, e);
        }
    }

    /**
     * Converts to a {@link ZonedDateTime} using the system default zone.
     *
     * @return the same instant in this machine's default zone
     */
    public ZonedDateTime toZonedDateTime() {
        return value.atZoneSameInstant(ZoneId.systemDefault());
    }

    /**
     * Returns a new {@code DateTimeWithZone} representing the same instant
     * but expressed in the offset the given {@link TimeZone} has at that instant.
     *
     * @param timeZone the zone to express it in
     * @return the same instant at that zone's offset
     * @throws ValueObjectException if {@code timeZone} is null or the result is out of range
     */
    public DateTimeWithZone atTimeZone(TimeZone timeZone) {
        return new DateTimeWithZone(toZonedDateTime(timeZone).toOffsetDateTime());
    }

    /**
     * Returns a new {@code DateTimeWithZone} converted to UTC.
     *
     * @return the same instant at offset {@code Z}
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone toUtc() {
        return arith(() -> value.withOffsetSameInstant(ZoneOffset.UTC));
    }

    /**
     * Finds a {@link TimeZone} whose offset at this date-time's instant equals this date-time's offset.
     * <p>
     * Since multiple timezones can share the same offset, this returns the first match in
     * declaration order (e.g. {@code AFRICA_ABIDJAN} for {@code +00:00}). Each zone's offset is taken at this
     * instant, so a July date-time at {@code -07:00} matches {@code AMERICA_LOS_ANGELES} and a January one
     * {@code AMERICA_DENVER}, whatever today's date is.
     *
     * @return the first zone with the same offset at this instant, or {@link Optional#empty()} if no match
     */
    public Optional<TimeZone> resolveTimeZone() {
        Instant instant = value.toInstant();
        for (TimeZone tz : TimeZone.values()) {
            if (tz.offsetAt(instant).equals(value.getOffset())) {
                return Optional.of(tz);
            }
        }
        return Optional.empty();
    }

    /**
     * Checks whether this date-time's offset matches the offset the given {@link TimeZone} has at this
     * date-time's instant (so daylight saving time at that date is taken into account).
     *
     * @param timeZone the zone to compare with
     * @return whether the offsets are the same
     * @throws ValueObjectException if {@code timeZone} is null
     */
    public boolean isInTimeZone(TimeZone timeZone) {
        requireNonNull(timeZone, "TimeZone cannot be null");
        return value.getOffset().equals(timeZone.offsetAt(value.toInstant()));
    }

    // ──────────────────────────────────────────────
    //  Conversions
    // ──────────────────────────────────────────────

    /**
     * The point in time, without an offset.
     *
     * @return the instant
     */
    public Instant toInstant()             { return value.toInstant(); }
    /**
     * The date and time on the clock at this date-time's own offset, with the offset dropped.
     *
     * @return the local date-time
     */
    public LocalDateTime toLocalDateTime() { return value.toLocalDateTime(); }
    /**
     * The date at this date-time's own offset.
     *
     * @return the local date
     */
    public LocalDate toLocalDate()         { return value.toLocalDate(); }
    /**
     * The time of day at this date-time's own offset.
     *
     * @return the local time
     */
    public LocalTime toLocalTime()         { return value.toLocalTime(); }
    /**
     * The seconds since 1970-01-01T00:00:00Z; any fraction of a second is dropped.
     *
     * @return the epoch second
     */
    public long toEpochSecond()            { return value.toEpochSecond(); }
    /**
     * The milliseconds since 1970-01-01T00:00:00Z; any finer fraction is dropped.
     *
     * @return the epoch millisecond
     * @throws ValueObjectException if the value does not fit in a {@code long}
     */
    public long toEpochMilli() {
        try {
            return value.toInstant().toEpochMilli();
        } catch (ArithmeticException e) {
            throw new ValueObjectException("Too far from the epoch for milliseconds: " + value, e);
        }
    }

    // ──────────────────────────────────────────────
    //  Arithmetic (returns new instances — immutable)
    // ──────────────────────────────────────────────

    /**
     * Adds years, keeping the offset; a 29 February that does not exist in the result becomes 28 February.
     *
     * @param years years to add (negative to go back)
     * @return the new date-time
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone plusYears(long years)     { return arith(() -> value.plusYears(years)); }
    /**
     * Adds months, keeping the offset; a day past the end of the resulting month becomes its last day.
     *
     * @param months months to add (negative to go back)
     * @return the new date-time
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone plusMonths(long months)   { return arith(() -> value.plusMonths(months)); }
    /**
     * Adds weeks, keeping the offset.
     *
     * @param weeks weeks to add (negative to go back)
     * @return the new date-time
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone plusWeeks(long weeks)     { return arith(() -> value.plusWeeks(weeks)); }
    /**
     * Adds days, keeping the offset.
     *
     * @param days days to add (negative to go back)
     * @return the new date-time
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone plusDays(long days)       { return arith(() -> value.plusDays(days)); }
    /**
     * Adds hours, keeping the offset.
     *
     * @param hours hours to add (negative to go back)
     * @return the new date-time
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone plusHours(long hours)     { return arith(() -> value.plusHours(hours)); }
    /**
     * Adds minutes, keeping the offset.
     *
     * @param minutes minutes to add (negative to go back)
     * @return the new date-time
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone plusMinutes(long minutes) { return arith(() -> value.plusMinutes(minutes)); }
    /**
     * Adds seconds, keeping the offset.
     *
     * @param seconds seconds to add (negative to go back)
     * @return the new date-time
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone plusSeconds(long seconds) { return arith(() -> value.plusSeconds(seconds)); }
    /**
     * Adds nanoseconds, keeping the offset.
     *
     * @param nanos nanoseconds to add (negative to go back)
     * @return the new date-time
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone plusNanos(long nanos)     { return arith(() -> value.plusNanos(nanos)); }

    /**
     * Subtracts years, keeping the offset; a 29 February that does not exist in the result becomes 28 February.
     *
     * @param years years to subtract (negative to go forward)
     * @return the new date-time
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone minusYears(long years)     { return arith(() -> value.minusYears(years)); }
    /**
     * Subtracts months, keeping the offset; a day past the end of the resulting month becomes its last day.
     *
     * @param months months to subtract (negative to go forward)
     * @return the new date-time
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone minusMonths(long months)   { return arith(() -> value.minusMonths(months)); }
    /**
     * Subtracts weeks, keeping the offset.
     *
     * @param weeks weeks to subtract (negative to go forward)
     * @return the new date-time
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone minusWeeks(long weeks)     { return arith(() -> value.minusWeeks(weeks)); }
    /**
     * Subtracts days, keeping the offset.
     *
     * @param days days to subtract (negative to go forward)
     * @return the new date-time
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone minusDays(long days)       { return arith(() -> value.minusDays(days)); }
    /**
     * Subtracts hours, keeping the offset.
     *
     * @param hours hours to subtract (negative to go forward)
     * @return the new date-time
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone minusHours(long hours)     { return arith(() -> value.minusHours(hours)); }
    /**
     * Subtracts minutes, keeping the offset.
     *
     * @param minutes minutes to subtract (negative to go forward)
     * @return the new date-time
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone minusMinutes(long minutes) { return arith(() -> value.minusMinutes(minutes)); }
    /**
     * Subtracts seconds, keeping the offset.
     *
     * @param seconds seconds to subtract (negative to go forward)
     * @return the new date-time
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone minusSeconds(long seconds) { return arith(() -> value.minusSeconds(seconds)); }
    /**
     * Subtracts nanoseconds, keeping the offset.
     *
     * @param nanos nanoseconds to subtract (negative to go forward)
     * @return the new date-time
     * @throws ValueObjectException if the result is out of range
     */
    public DateTimeWithZone minusNanos(long nanos)     { return arith(() -> value.minusNanos(nanos)); }

    /**
     * Adds an amount of a unit, keeping the offset.
     *
     * @param amount how many units to add (negative to go back)
     * @param unit   the unit
     * @return the new date-time
     * @throws ValueObjectException if the unit is null or not supported (such as {@code FOREVER}), or the result
     *                              is out of range
     */
    public DateTimeWithZone plus(long amount, ChronoUnit unit) {
        requireNonNull(unit, "unit cannot be null");
        return arith(() -> value.plus(amount, unit));
    }

    /**
     * Subtracts an amount of a unit, keeping the offset.
     *
     * @param amount how many units to subtract (negative to go forward)
     * @param unit   the unit
     * @return the new date-time
     * @throws ValueObjectException if the unit is null or not supported (such as {@code FOREVER}), or the result
     *                              is out of range
     */
    public DateTimeWithZone minus(long amount, ChronoUnit unit) {
        requireNonNull(unit, "unit cannot be null");
        return arith(() -> value.minus(amount, unit));
    }

    // ──────────────────────────────────────────────
    //  Duration / Period Between
    // ──────────────────────────────────────────────

    /**
     * Returns the {@link java.time.Duration} between this and another {@code DateTimeWithZone}, by instant.
     *
     * @param other the later (or earlier) date-time
     * @return the time from this one to {@code other}; negative when {@code other} is earlier
     * @throws ValueObjectException if {@code other} is null
     */
    public java.time.Duration durationUntil(DateTimeWithZone other) {
        requireNonNull(other, "other cannot be null");
        return java.time.Duration.between(this.value.toInstant(), other.value.toInstant());
    }

    /**
     * Returns the number of whole units between this and another {@code DateTimeWithZone}
     * ({@code other} is first moved to this one's offset).
     *
     * @param other the date-time to count to
     * @param unit  the unit to count in
     * @return the whole units from this one to {@code other}; negative when {@code other} is earlier
     * @throws ValueObjectException if an argument is null, the unit is not supported, or the count overflows
     */
    public long until(DateTimeWithZone other, ChronoUnit unit) {
        requireNonNull(other, "other cannot be null");
        requireNonNull(unit, "unit cannot be null");
        try {
            return this.value.until(other.value, unit);
        } catch (DateTimeException | ArithmeticException e) {
            throw new ValueObjectException("Cannot count " + unit + " from " + value + " to " + other.value, e);
        }
    }

    /**
     * Returns whole hours between this and another {@code DateTimeWithZone}.
     *
     * @param other the date-time to count to
     * @return the whole hours from this one to {@code other}; negative when {@code other} is earlier
     * @throws ValueObjectException if {@code other} is null
     */
    public long hoursUntil(DateTimeWithZone other) {
        return until(other, ChronoUnit.HOURS);
    }

    /**
     * Returns whole days between this and another {@code DateTimeWithZone}.
     *
     * @param other the date-time to count to
     * @return the whole days from this one to {@code other}; negative when {@code other} is earlier
     * @throws ValueObjectException if {@code other} is null
     */
    public long daysUntil(DateTimeWithZone other) {
        return until(other, ChronoUnit.DAYS);
    }

    // ──────────────────────────────────────────────
    //  Comparison Helpers
    // ──────────────────────────────────────────────

    /**
     * Tells whether this instant is before another, whatever their offsets.
     *
     * @param other the date-time to compare with
     * @return whether this one is earlier
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isBefore(DateTimeWithZone other) {
        requireNonNull(other, "other cannot be null");
        return value.isBefore(other.value);
    }

    /**
     * Tells whether this instant is after another, whatever their offsets.
     *
     * @param other the date-time to compare with
     * @return whether this one is later
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isAfter(DateTimeWithZone other) {
        requireNonNull(other, "other cannot be null");
        return value.isAfter(other.value);
    }

    /**
     * Tells whether this is the same instant as another, whatever their offsets (unlike {@link #equals(Object)}).
     *
     * @param other the date-time to compare with
     * @return whether both are the same instant
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isEqual(DateTimeWithZone other) {
        requireNonNull(other, "other cannot be null");
        return value.isEqual(other.value);
    }

    /**
     * Checks if this date-time is between two others (inclusive), by instant.
     *
     * @param start the earliest allowed instant
     * @param end   the latest allowed instant
     * @return whether this one is neither before {@code start} nor after {@code end}
     * @throws ValueObjectException if {@code start} or {@code end} is null
     */
    public boolean isBetween(DateTimeWithZone start, DateTimeWithZone end) {
        requireNonNull(start, "start cannot be null");
        requireNonNull(end, "end cannot be null");
        return !value.isBefore(start.value) && !value.isAfter(end.value);
    }

    /**
     * Tells whether this instant is after now.
     *
     * @return whether it is in the future
     */
    public boolean isFuture() {
        return value.isAfter(OffsetDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Tells whether this instant is before now.
     *
     * @return whether it is in the past
     */
    public boolean isPast() {
        return value.isBefore(OffsetDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Tells whether this instant falls on today's date in the given zone.
     *
     * @param timeZone the zone whose calendar day is meant
     * @return whether it is today there
     * @throws ValueObjectException if {@code timeZone} is null
     */
    public boolean isToday(TimeZone timeZone) {
        requireNonNull(timeZone, "TimeZone cannot be null");
        LocalDate today = LocalDate.now(timeZone.toZoneId());
        LocalDate thisDate = toZonedDateTime(timeZone).toLocalDate();
        return thisDate.equals(today);
    }

    // ──────────────────────────────────────────────
    //  Truncation / Boundaries
    // ──────────────────────────────────────────────

    /**
     * Drops every field smaller than the unit, keeping the offset; {@code truncatedTo(MINUTES)} zeroes the
     * seconds and nanoseconds.
     *
     * @param unit the unit to truncate to, at most {@code DAYS}
     * @return the truncated date-time
     * @throws ValueObjectException if the unit is null, larger than {@code DAYS}, or does not divide a day evenly
     */
    public DateTimeWithZone truncatedTo(ChronoUnit unit) {
        requireNonNull(unit, "unit cannot be null");
        return arith(() -> value.truncatedTo(unit));
    }

    /**
     * Midnight at the start of this date-time's day, at its own offset.
     *
     * @return the same day at {@code 00:00}
     */
    public DateTimeWithZone startOfDay() {
        return new DateTimeWithZone(value.truncatedTo(ChronoUnit.DAYS));
    }

    /**
     * The start of this date-time's hour, at its own offset.
     *
     * @return the same hour with minutes, seconds and nanoseconds zeroed
     */
    public DateTimeWithZone startOfHour() {
        return new DateTimeWithZone(value.truncatedTo(ChronoUnit.HOURS));
    }

    // ──────────────────────────────────────────────
    //  Formatting
    // ──────────────────────────────────────────────

    /**
     * Formats using the given {@link DateTimeFormatter}.
     *
     * @param formatter how to write it
     * @return the formatted text
     * @throws ValueObjectException if the formatter is null or cannot write this date-time
     */
    public String format(DateTimeFormatter formatter) {
        requireNonNull(formatter, "DateTimeFormatter cannot be null");
        try {
            return value.format(formatter);
        } catch (DateTimeException e) {
            throw new ValueObjectException("Cannot format " + value + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns ISO-8601 string (e.g. {@code "2026-02-08T14:30:00+08:00"}).
     *
     * @return the date-time with its offset in ISO-8601 form ({@code Z} for UTC)
     */
    public String toIsoString() {
        return value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Returns a human-readable string like {@code "February 8, 2026 2:30 PM (GMT+8:00)"}, in English, at this
     * date-time's own offset.
     *
     * @return the date, 12-hour time and offset
     */
    public String toHumanReadable() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a", Locale.ENGLISH);
        return value.format(fmt) + " (" + formatOffset(value.getOffset()) + ")";
    }

    /**
     * Returns a formatted display string incorporating a {@link TimeZone},
     * e.g. {@code "Feb 8, 2026 2:30 PM PHT (Asia/Manila)"}, in English.
     *
     * @param timeZone the zone to show the instant in
     * @return the date, 12-hour time and zone abbreviation, then the zone id
     * @throws ValueObjectException if {@code timeZone} is null
     */
    public String toDisplayString(TimeZone timeZone) {
        ZonedDateTime zdt = toZonedDateTime(timeZone);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a z", Locale.ENGLISH);
        return zdt.format(fmt) + " (" + timeZone.zoneId() + ")";
    }

    // ──────────────────────────────────────────────
    //  Comparable / toString
    // ──────────────────────────────────────────────

    @Override
    public int compareTo(DateTimeWithZone other) {
        requireNonNull(other, "other cannot be null");
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
        return TimeZone.formatOffset(offset);
    }

    private static void requireNonNull(@Nullable Object o, String message) {
        if (o == null) throw new ValueObjectException(message);
    }

    private static DateTimeWithZone arith(java.util.function.Supplier<OffsetDateTime> f) {
        try {
            return new DateTimeWithZone(f.get());
        } catch (DateTimeException | ArithmeticException e) {
            throw new ValueObjectException("Date-time out of range: " + e.getMessage(), e);
        }
    }

    /**
     * Parses an ISO-8601 string like {@link #parse(String)}, without throwing.
     *
     * @param text any text, or null
     * @return it as a {@link DateTimeWithZone}, or empty when it is not one; never throws
     */
    public static Optional<DateTimeWithZone> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text is an ISO-8601 date-time with an offset.
     *
     * @param text any text, or null
     * @return whether it is a {@link DateTimeWithZone} ({@code false} for null)
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
