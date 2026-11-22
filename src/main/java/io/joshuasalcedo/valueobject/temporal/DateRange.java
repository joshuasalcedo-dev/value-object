package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A run of calendar days from a start date to an end date, both included. A range of one day has the same start
 * and end. Ranges order by start date, then end date, and iterate over every day in them. A range is written
 * and read in ISO-8601 interval form, {@code "2026-01-01/2026-01-31"}, or as a single date for a one-day range.
 * Bad input (null, an end before the start, a date out of range, an unreadable text) throws
 * {@link ValueObjectException}.
 *
 * @param startDate the first day (inclusive)
 * @param endDate   the last day (inclusive); never before {@code startDate}
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record DateRange(
        LocalDate startDate,
        LocalDate endDate
) implements ValueObject, Comparable<DateRange>, Iterable<LocalDate> {

    /**
     * Checks that both dates are present and the end is not before the start.
     *
     * @throws ValueObjectException if either date is null or {@code endDate} is before {@code startDate}
     */
    public DateRange {
        if (startDate == null) throw new ValueObjectException("Start date cannot be null");
        if (endDate == null) throw new ValueObjectException("End date cannot be null");

        if (endDate.isBefore(startDate)) {
            throw new ValueObjectException(
                String.format("End date (%s) cannot be before start date (%s)", endDate, startDate)
            );
        }
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a DateRange from start and end dates.
     *
     * @param start the start date (inclusive)
     * @param end the end date (inclusive)
     * @return a new DateRange
     * @throws ValueObjectException if either date is null or {@code end} is before {@code start}
     */
    public static DateRange of(LocalDate start, LocalDate end) {
        return new DateRange(start, end);
    }

    /**
     * Creates a DateRange for a single day.
     *
     * @param date the date
     * @return a new DateRange
     * @throws ValueObjectException if {@code date} is null
     */
    public static DateRange ofDay(LocalDate date) {
        return new DateRange(date, date);
    }

    /**
     * Creates a DateRange for today, in this machine's default zone.
     *
     * @return a new DateRange for today
     */
    public static DateRange today() {
        LocalDate now = LocalDate.now();
        return new DateRange(now, now);
    }

    /**
     * Creates a DateRange for the current week (Monday to Sunday).
     *
     * @return a new DateRange
     */
    public static DateRange thisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate sunday = monday.plusDays(6);
        return new DateRange(monday, sunday);
    }

    /**
     * Creates a DateRange for the current month.
     *
     * @return a new DateRange
     */
    public static DateRange thisMonth() {
        LocalDate today = LocalDate.now();
        LocalDate first = today.withDayOfMonth(1);
        LocalDate last = today.withDayOfMonth(today.lengthOfMonth());
        return new DateRange(first, last);
    }

    /**
     * Creates a DateRange for the current year.
     *
     * @return a new DateRange
     */
    public static DateRange thisYear() {
        LocalDate today = LocalDate.now();
        LocalDate first = today.withDayOfYear(1);
        LocalDate last = today.withDayOfYear(today.lengthOfYear());
        return new DateRange(first, last);
    }

    /**
     * Creates a DateRange of {@code days} days that ends today; {@code lastDays(7)} is today and the six days before.
     *
     * @param days the number of days, today included; at least 1
     * @return a new DateRange
     * @throws ValueObjectException if {@code days} is less than 1
     */
    public static DateRange lastDays(int days) {
        if (days < 1) throw new ValueObjectException("The number of days must be at least 1: " + days);
        LocalDate today = LocalDate.now();
        return new DateRange(today.minusDays(days - 1L), today);
    }

    /**
     * Creates a DateRange of {@code days} days that starts today; {@code nextDays(7)} is today and the six days after.
     *
     * @param days the number of days, today included; at least 1
     * @return a new DateRange
     * @throws ValueObjectException if {@code days} is less than 1
     */
    public static DateRange nextDays(int days) {
        if (days < 1) throw new ValueObjectException("The number of days must be at least 1: " + days);
        LocalDate today = LocalDate.now();
        return new DateRange(today, today.plusDays(days - 1L));
    }

    // ==================== Query Methods ====================

    /**
     * Checks if the range contains a specific date.
     *
     * @param date the date to check
     * @return true if contained
     * @throws ValueObjectException if {@code date} is null
     */
    public boolean contains(LocalDate date) {
        requireNonNull(date);
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Checks if the range contains a specific datetime.
     *
     * @param dateTime the datetime to check
     * @return true if the date part is contained
     * @throws ValueObjectException if {@code dateTime} is null
     */
    public boolean contains(LocalDateTime dateTime) {
        requireNonNull(dateTime);
        return contains(dateTime.toLocalDate());
    }

    /**
     * Checks if this range fully contains another range.
     *
     * @param other the other range
     * @return true if fully contained
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean contains(DateRange other) {
        requireNonNull(other);
        return !other.startDate.isBefore(this.startDate) &&
               !other.endDate.isAfter(this.endDate);
    }

    /**
     * Checks if this range overlaps with another.
     *
     * @param other the other range
     * @return true if overlapping
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean overlaps(DateRange other) {
        requireNonNull(other);
        return !this.endDate.isBefore(other.startDate) &&
               !this.startDate.isAfter(other.endDate);
    }

    /**
     * Checks if this range is adjacent to another (ends one day before other starts).
     *
     * @param other the other range
     * @return true if adjacent
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isAdjacentTo(DateRange other) {
        requireNonNull(other);
        return ChronoUnit.DAYS.between(this.endDate, other.startDate) == 1 ||
               ChronoUnit.DAYS.between(other.endDate, this.startDate) == 1;
    }

    /**
     * Returns the number of days in the range (inclusive).
     *
     * @return the number of days
     */
    public long days() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Returns the length of the range as a period, counting the end day (a single day is one day).
     *
     * @return the period from the start date to the day after the end date
     */
    public Period period() {
        return Period.between(startDate, endDate.plusDays(1));
    }

    /**
     * Checks if this is a single day.
     *
     * @return true if single day
     */
    public boolean isSingleDay() {
        return startDate.equals(endDate);
    }

    /**
     * Checks if today falls within this range.
     *
     * @return true if contains today
     */
    public boolean includesToday() {
        return contains(LocalDate.now());
    }

    /**
     * Checks if this range is in the past.
     *
     * @return true if entirely in the past
     */
    public boolean isPast() {
        return endDate.isBefore(LocalDate.now());
    }

    /**
     * Checks if this range is in the future.
     *
     * @return true if entirely in the future
     */
    public boolean isFuture() {
        return startDate.isAfter(LocalDate.now());
    }

    // ==================== Operations ====================

    /**
     * Returns the intersection with another range.
     *
     * @param other the other range
     * @return the days both cover, or empty when they do not overlap
     * @throws ValueObjectException if {@code other} is null
     */
    public java.util.Optional<DateRange> intersection(DateRange other) {
        if (!overlaps(other)) {
            return java.util.Optional.empty();
        }
        LocalDate start = startDate.isAfter(other.startDate) ? startDate : other.startDate;
        LocalDate end = endDate.isBefore(other.endDate) ? endDate : other.endDate;
        return java.util.Optional.of(new DateRange(start, end));
    }

    /**
     * Returns the smallest range that covers both ranges; they must overlap or be adjacent.
     *
     * @param other the other range
     * @return the union
     * @throws ValueObjectException if {@code other} is null or the ranges have a gap
     */
    public DateRange union(DateRange other) {
        if (!overlaps(other) && !isAdjacentTo(other)) {
            throw new ValueObjectException("Cannot union non-adjacent ranges with gap");
        }
        LocalDate start = startDate.isBefore(other.startDate) ? startDate : other.startDate;
        LocalDate end = endDate.isAfter(other.endDate) ? endDate : other.endDate;
        return new DateRange(start, end);
    }

    /**
     * Moves the end date by a number of days; the start date stays.
     *
     * @param days days to add to the end (negative to shorten)
     * @return a new extended range
     * @throws ValueObjectException if the new end date would be before the start date or out of range
     */
    public DateRange extendBy(int days) {
        return new DateRange(startDate, plusDays(endDate, days));
    }

    /**
     * Shifts the entire range by a number of days.
     *
     * @param days days to shift (positive = forward, negative = backward)
     * @return a new shifted range
     * @throws ValueObjectException if a shifted date would be out of range
     */
    public DateRange shift(int days) {
        return new DateRange(plusDays(startDate, days), plusDays(endDate, days));
    }

    // ==================== Iteration ====================

    /**
     * Returns a stream of all dates in the range.
     *
     * @return stream of dates
     */
    public Stream<LocalDate> stream() {
        return startDate.datesUntil(endDate.plusDays(1));
    }

    /**
     * Iterates over every day in the range, in order, from the start date to the end date.
     *
     * @return an iterator over the days
     */
    @Override
    public Iterator<LocalDate> iterator() {
        return stream().iterator();
    }

    // ==================== Formatting ====================

    /**
     * Formats the range as an ISO-8601 interval of dates.
     *
     * @return {@code "2026-01-01/2026-01-31"}, or a single date such as {@code "2026-01-01"} for a one-day range
     */
    public String toIsoString() {
        if (isSingleDay()) {
            return startDate.toString();
        }
        return startDate + "/" + endDate;
    }

    /**
     * Formats both dates with a {@link DateTimeFormatter} pattern, in English.
     *
     * @param pattern the date pattern, e.g. {@code "MMM d, yyyy"}
     * @return {@code "start - end"}, or a single date for a one-day range
     * @throws ValueObjectException if the pattern is null or invalid, or asks for a field a date does not have
     */
    public String format(String pattern) {
        if (pattern == null) throw new ValueObjectException("A pattern is required");
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
            if (isSingleDay()) {
                return startDate.format(formatter);
            }
            return startDate.format(formatter) + " - " + endDate.format(formatter);
        } catch (IllegalArgumentException | DateTimeException e) {
            throw new ValueObjectException("Cannot format a date range as '" + pattern + "'", e);
        }
    }

    /**
     * Orders ranges by start date, then by end date.
     *
     * @param other the range to compare to
     * @return negative, zero or positive as this range comes before, with or after {@code other}
     * @throws ValueObjectException if {@code other} is null
     */
    @Override
    public int compareTo(DateRange other) {
        requireNonNull(other);
        int startCompare = this.startDate.compareTo(other.startDate);
        return startCompare != 0 ? startCompare : this.endDate.compareTo(other.endDate);
    }

    /**
     * Writes the range as {@link #toIsoString()} does.
     *
     * @return the ISO-8601 form of the range
     */
    @Override
    public String toString() {
        return toIsoString();
    }

    /**
     * Reads a range in the form {@link #toIsoString()} writes: two ISO-8601 dates joined by {@code '/'}
     * ({@code "2026-01-01/2026-01-31"}), or a single date for a one-day range, ignoring blanks around each date.
     *
     * @param text the text
     * @return that range
     * @throws ValueObjectException if the text is null, a date cannot be read, or the end is before the start
     */
    public static DateRange parse(String text) {
        if (text == null) throw new ValueObjectException("A DateRange is required");
        String[] parts = text.split("/", -1);
        if (parts.length > 2) throw new ValueObjectException("Not a date range: '" + text + "'");
        try {
            LocalDate start = LocalDate.parse(parts[0].strip());
            LocalDate end = parts.length == 2 ? LocalDate.parse(parts[1].strip()) : start;
            return new DateRange(start, end);
        } catch (DateTimeException e) {
            throw new ValueObjectException("Not a date range: '" + text + "'", e);
        }
    }

    /**
     * Reads a range like {@link #parse(String)}, without throwing.
     *
     * @param text any text, or null
     * @return it as a {@link DateRange}, or empty when it is not one; never throws
     */
    public static Optional<DateRange> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text is a date range that {@link #parse(String)} accepts.
     *
     * @param text any text, or null
     * @return whether it is a {@link DateRange} ({@code false} for null)
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

    private static void requireNonNull(@Nullable Object o) {
        if (o == null) throw new ValueObjectException("A date or date range is required");
    }

    private static LocalDate plusDays(LocalDate date, long days) {
        try {
            return date.plusDays(days);
        } catch (DateTimeException e) {
            throw new ValueObjectException("Date out of range: " + date + " plus " + days + " days", e);
        }
    }
}
