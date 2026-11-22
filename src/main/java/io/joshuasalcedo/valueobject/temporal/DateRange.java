package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Value object representing a range between two dates (inclusive).
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record DateRange(
        LocalDate startDate,
        LocalDate endDate
) implements ValueObject, Comparable<DateRange>, Iterable<LocalDate> {

    public DateRange {
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");

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
     */
    public static DateRange of(LocalDate start, LocalDate end) {
        return new DateRange(start, end);
    }

    /**
     * Creates a DateRange for a single day.
     *
     * @param date the date
     * @return a new DateRange
     */
    public static DateRange ofDay(LocalDate date) {
        return new DateRange(date, date);
    }

    /**
     * Creates a DateRange for today.
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
     * Creates a DateRange from today going back a number of days.
     *
     * @param days the number of days back
     * @return a new DateRange
     */
    public static DateRange lastDays(int days) {
        LocalDate today = LocalDate.now();
        return new DateRange(today.minusDays(days - 1), today);
    }

    /**
     * Creates a DateRange from today going forward a number of days.
     *
     * @param days the number of days forward
     * @return a new DateRange
     */
    public static DateRange nextDays(int days) {
        LocalDate today = LocalDate.now();
        return new DateRange(today, today.plusDays(days - 1));
    }

    // ==================== Query Methods ====================

    /**
     * Checks if the range contains a specific date.
     *
     * @param date the date to check
     * @return true if contained
     */
    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Checks if the range contains a specific datetime.
     *
     * @param dateTime the datetime to check
     * @return true if the date part is contained
     */
    public boolean contains(LocalDateTime dateTime) {
        return contains(dateTime.toLocalDate());
    }

    /**
     * Checks if this range fully contains another range.
     *
     * @param other the other range
     * @return true if fully contained
     */
    public boolean contains(DateRange other) {
        return !other.startDate.isBefore(this.startDate) &&
               !other.endDate.isAfter(this.endDate);
    }

    /**
     * Checks if this range overlaps with another.
     *
     * @param other the other range
     * @return true if overlapping
     */
    public boolean overlaps(DateRange other) {
        return !this.endDate.isBefore(other.startDate) &&
               !this.startDate.isAfter(other.endDate);
    }

    /**
     * Checks if this range is adjacent to another (ends one day before other starts).
     *
     * @param other the other range
     * @return true if adjacent
     */
    public boolean isAdjacentTo(DateRange other) {
        return this.endDate.plusDays(1).equals(other.startDate) ||
               other.endDate.plusDays(1).equals(this.startDate);
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
     * Returns the period between start and end.
     *
     * @return the period
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
    public boolean includestoday() {
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
     * @return the intersection or null if no overlap
     */
    public @Nullable DateRange intersection(DateRange other) {
        if (!overlaps(other)) {
            return null;
        }
        LocalDate start = startDate.isAfter(other.startDate) ? startDate : other.startDate;
        LocalDate end = endDate.isBefore(other.endDate) ? endDate : other.endDate;
        return new DateRange(start, end);
    }

    /**
     * Returns the union with another range (merges if adjacent or overlapping).
     *
     * @param other the other range
     * @return the union
     * @throws ValueObjectException if ranges have a gap
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
     * Extends the range by a number of days at the end.
     *
     * @param days days to extend
     * @return a new extended range
     */
    public DateRange extendBy(int days) {
        return new DateRange(startDate, endDate.plusDays(days));
    }

    /**
     * Shifts the entire range by a number of days.
     *
     * @param days days to shift (positive = forward, negative = backward)
     * @return a new shifted range
     */
    public DateRange shift(int days) {
        return new DateRange(startDate.plusDays(days), endDate.plusDays(days));
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

    @Override
    public Iterator<LocalDate> iterator() {
        return stream().iterator();
    }

    // ==================== Formatting ====================

    /**
     * Formats the range using ISO dates.
     *
     * @return the formatted range
     */
    public String toIsoString() {
        if (isSingleDay()) {
            return startDate.toString();
        }
        return startDate + "/" + endDate;
    }

    /**
     * Formats using a custom pattern.
     *
     * @param pattern the date pattern
     * @return the formatted range
     */
    public String format(String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        if (isSingleDay()) {
            return startDate.format(formatter);
        }
        return startDate.format(formatter) + " - " + endDate.format(formatter);
    }

    @Override
    public int compareTo(DateRange other) {
        int startCompare = this.startDate.compareTo(other.startDate);
        return startCompare != 0 ? startCompare : this.endDate.compareTo(other.endDate);
    }

    @Override
    public String toString() {
        return toIsoString();
    }
}
