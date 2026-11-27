package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.jspecify.annotations.Nullable;
import java.util.Locale;
import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * The day a person was born: a calendar date with no time or zone, never later than today.
 *
 * <p>Ages are counted up to today in this machine's default zone; every age and birthday method also has a form
 * that takes the day to count to, for a fixed or a different "today". Bad input (a missing date, a future date,
 * a day that does not exist, an unreadable text) throws {@link ValueObjectException}.
 *
 * <p>A person born on 29 February has their birthday on 28 February in a year that has no 29 February.
 * Text meant for people is written in English, whatever this machine's default locale.
 *
 * @param value the date of birth
 * @author JoshuaSalcedo
 * @since 2/25/2026 2:48 AM
 */
public record DateOfBirth(LocalDate value) implements ValueObject {

    /**
     * Checks that the date is present and not in the future.
     *
     * @throws ValueObjectException if {@code value} is null or after today
     */
    public DateOfBirth {
        if (value == null) throw new ValueObjectException("Date of birth must not be null");
        if (value.isAfter(LocalDate.now())) {
            throw new ValueObjectException("Date of birth cannot be in the future: " + value);
        }
    }

    // --- Factory Methods ---

    /**
     * Wraps a {@link LocalDate}.
     *
     * @param date the date of birth
     * @return it
     * @throws ValueObjectException if {@code date} is null or after today
     */
    public static DateOfBirth of(LocalDate date) {
        return new DateOfBirth(date);
    }

    /**
     * Makes the date of birth from its year, month and day of the month.
     *
     * @param year  e.g. 2000
     * @param month 1-12
     * @param day   1-31
     * @return that day
     * @throws ValueObjectException if there is no such day or it is after today
     */
    public static DateOfBirth of(int year, int month, int day) {
        try {
            return new DateOfBirth(LocalDate.of(year, month, day));
        } catch (DateTimeException e) {
            throw new ValueObjectException("Not a date: " + year + "-" + month + "-" + day, e);
        }
    }

    /**
     * Reads an ISO-8601 date, e.g. {@code "2000-01-15"}, after stripping blanks around it.
     *
     * @param date the text
     * @return that day
     * @throws ValueObjectException if the text is null, not an ISO-8601 date, or a day after today
     */
    public static DateOfBirth parse(String date) {
        if (date == null) throw new ValueObjectException("A DateOfBirth is required");
        try {
            return new DateOfBirth(LocalDate.parse(date.strip()));
        } catch (DateTimeException e) {
            throw new ValueObjectException("Not an ISO-8601 date: '" + date + "'", e);
        }
    }

    /**
     * Reads a date with the given formatter.
     *
     * @param date      the text
     * @param formatter how the text is written
     * @return that day
     * @throws ValueObjectException if an argument is null, the text does not match the formatter, or the day is
     *                              after today
     */
    public static DateOfBirth parse(String date, DateTimeFormatter formatter) {
        if (date == null) throw new ValueObjectException("A DateOfBirth is required");
        if (formatter == null) throw new ValueObjectException("A DateTimeFormatter is required");
        try {
            return new DateOfBirth(LocalDate.parse(date, formatter));
        } catch (DateTimeException e) {
            throw new ValueObjectException("Not a date in the expected format: '" + date + "'", e);
        }
    }

    // --- Age & Time Helpers ---

    /**
     * The age today in complete years.
     *
     * @return the age in whole years
     */
    public int ageInYears() {
        return ageInYears(LocalDate.now());
    }

    /**
     * The age on a given day in complete years.
     *
     * @param asOf the day to count to
     * @return the age in whole years; negative when {@code asOf} is before the date of birth
     * @throws ValueObjectException if {@code asOf} is null
     */
    public int ageInYears(LocalDate asOf) {
        return agePrecise(asOf).getYears();
    }

    /**
     * The age today in complete months (e.g. 25 years 3 months = 303 months).
     *
     * @return the age in whole months
     */
    public long ageInMonths() {
        return ChronoUnit.MONTHS.between(value, LocalDate.now());
    }

    /**
     * The age today in complete weeks.
     *
     * @return the age in whole weeks
     */
    public long ageInWeeks() {
        return ChronoUnit.WEEKS.between(value, LocalDate.now());
    }

    /**
     * The number of days from the date of birth to today.
     *
     * @return the days alive (0 when born today)
     */
    public long ageInDays() {
        return ChronoUnit.DAYS.between(value, LocalDate.now());
    }

    /**
     * The age today as years, months and days.
     *
     * @return the age as a {@link Period}
     */
    public Period agePrecise() {
        return agePrecise(LocalDate.now());
    }

    /**
     * The age on a given day as years, months and days.
     *
     * @param asOf the day to count to
     * @return the age as a {@link Period}
     * @throws ValueObjectException if {@code asOf} is null
     */
    public Period agePrecise(LocalDate asOf) {
        if (asOf == null) throw new ValueObjectException("A day to count to is required");
        return Period.between(value, asOf);
    }

    /**
     * The birthday in a given year: the same month and day as the date of birth, except that a 29 February
     * birthday is 28 February in a year without 29 February.
     *
     * @param year the year
     * @return the birthday in that year
     * @throws ValueObjectException if the year is out of range
     */
    public LocalDate birthdayIn(int year) {
        try {
            return value.withYear(year);
        } catch (DateTimeException e) {
            throw new ValueObjectException("Year out of range: " + year, e);
        }
    }

    /**
     * Counts the days from today to the next birthday. A 29 February birthday falls on 28 February in a
     * year that has no 29 February.
     *
     * @return the days until the next birthday; 0 when the birthday is today
     */
    public long daysUntilNextBirthday() {
        return daysUntilNextBirthday(LocalDate.now());
    }

    /**
     * Counts the days from a given day to the next birthday on or after it, as {@link #birthdayIn(int)} places
     * the birthday.
     *
     * @param today the day to count from
     * @return the days until the next birthday; 0 when the birthday is {@code today}
     * @throws ValueObjectException if {@code today} is null
     */
    public long daysUntilNextBirthday(LocalDate today) {
        if (today == null) throw new ValueObjectException("A day to count from is required");
        LocalDate next = birthdayIn(today.getYear());
        if (next.isBefore(today)) {
            next = birthdayIn(today.getYear() + 1);
        }
        return ChronoUnit.DAYS.between(today, next);
    }

    /**
     * Tells whether today is the birthday, as {@link #isBirthdayOn(LocalDate)} decides.
     *
     * @return whether today is the birthday
     */
    public boolean isBirthdayToday() {
        return isBirthdayOn(LocalDate.now());
    }

    /**
     * Tells whether a day is the birthday: it has the same month and day of the month as the date of birth, or,
     * for a 29 February birthday, it is 28 February in a year without 29 February.
     *
     * @param date the day
     * @return whether it is the birthday
     * @throws ValueObjectException if {@code date} is null
     */
    public boolean isBirthdayOn(LocalDate date) {
        if (date == null) throw new ValueObjectException("A day is required");
        return birthdayIn(date.getYear()).equals(date);
    }

    // --- Comparison ---

    /**
     * Tells whether this date of birth is earlier than another, that is, whether this person is older.
     *
     * @param other another date of birth
     * @return whether this date is before {@code other}
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isBefore(DateOfBirth other) {
        if (other == null) throw new ValueObjectException("A DateOfBirth to compare with is required");
        return this.value.isBefore(other.value());
    }

    /**
     * Tells whether this date of birth is later than another, that is, whether this person is younger.
     *
     * @param other another date of birth
     * @return whether this date is after {@code other}
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isAfter(DateOfBirth other) {
        if (other == null) throw new ValueObjectException("A DateOfBirth to compare with is required");
        return this.value.isAfter(other.value());
    }

    // --- Formatting ---

    /**
     * Writes the date in ISO-8601 form.
     *
     * @return e.g. {@code "2000-01-15"}
     */
    public String toIsoDate() {
        return value.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Writes the date month first, as is usual in the US.
     *
     * @return e.g. {@code "01/15/2000"}
     */
    public String toUsDate() {
        return english("MM/dd/yyyy");
    }

    /**
     * Writes the date day first, as is usual in Europe.
     *
     * @return e.g. {@code "15/01/2000"}
     */
    public String toEuDate() {
        return english("dd/MM/yyyy");
    }

    /**
     * Writes the date with the full month name, in English.
     *
     * @return e.g. {@code "January 15, 2000"}
     */
    public String toLongDate() {
        return english("MMMM d, yyyy");
    }

    /**
     * Writes the date with the short month name, in English.
     *
     * @return e.g. {@code "Jan 15, 2000"}
     */
    public String toMediumDate() {
        return english("MMM d, yyyy");
    }

    /**
     * Writes the date day first with the short month name, in English.
     *
     * @return e.g. {@code "15-Jan-2000"}
     */
    public String toShortDate() {
        return english("dd-MMM-yyyy");
    }

    /**
     * Writes the date day first with the full month name, in English.
     *
     * @return e.g. {@code "15 January 2000"}
     */
    public String toDayFirstLongDate() {
        return english("d MMMM yyyy");
    }

    /**
     * Writes the date with the day of the week and full month name, in English.
     *
     * @return e.g. {@code "Saturday, January 15, 2000"}
     */
    public String toFullDate() {
        return english("EEEE, MMMM d, yyyy");
    }

    /**
     * Writes the age today in years, months and days (always plural).
     *
     * @return e.g. {@code "25 years, 3 months, 10 days"}
     */
    public String toAgeString() {
        Period period = agePrecise();
        return String.format(Locale.ENGLISH, "%d years, %d months, %d days",
                period.getYears(), period.getMonths(), period.getDays());
    }

    /**
     * Writes the date in a {@link DateTimeFormatter} pattern, in English.
     *
     * @param pattern a {@link DateTimeFormatter} pattern, e.g. {@code "MMM d, yyyy"}
     * @return the date in that pattern
     * @throws ValueObjectException if the pattern is null or invalid, or asks for a field a date does not have
     */
    public String formatted(String pattern) {
        if (pattern == null) throw new ValueObjectException("A pattern is required");
        try {
            return english(pattern);
        } catch (IllegalArgumentException e) {
            throw new ValueObjectException("Not a date pattern: '" + pattern + "'", e);
        }
    }

    /**
     * Writes the date with the given formatter, in the formatter's own locale.
     *
     * @param formatter how to write it
     * @return the date as the formatter writes it
     * @throws ValueObjectException if the formatter is null or asks for a field a date does not have
     */
    public String formatted(DateTimeFormatter formatter) {
        if (formatter == null) throw new ValueObjectException("A DateTimeFormatter is required");
        try {
            return value.format(formatter);
        } catch (DateTimeException e) {
            throw new ValueObjectException("Cannot format " + value + ": " + e.getMessage(), e);
        }
    }

    private String english(String pattern) {
        try {
            return value.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
        } catch (DateTimeException e) {
            throw new ValueObjectException("Cannot format " + value + " as '" + pattern + "'", e);
        }
    }

    /**
     * Writes the date in ISO-8601 form, as {@link #toIsoDate()}.
     *
     * @return e.g. {@code "2000-01-15"}
     */
    @Override
    public String toString() {
        return toIsoDate();
    }

    /**
     * Reads an ISO-8601 date like {@link #parse(String)}, without throwing; a future date gives empty.
     *
     * @param text any text, or null
     * @return it as a {@link DateOfBirth}, or empty when it is not one; never throws
     */
    public static Optional<DateOfBirth> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text is an ISO-8601 date that is not in the future.
     *
     * @param text any text, or null
     * @return whether it is a {@link DateOfBirth} ({@code false} for null)
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
