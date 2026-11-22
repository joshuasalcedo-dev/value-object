package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.ValueObject;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * DateOfBirth value object.
 *
 * @author JoshuaSalcedo
 * @since 2/25/2026 2:48 AM
 */
public record DateOfBirth(LocalDate value) implements ValueObject {

    public DateOfBirth {
        Objects.requireNonNull(value, "Date of birth must not be null");
        if (value.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
    }

    // --- Factory Methods ---

    public static DateOfBirth of(LocalDate date) {
        return new DateOfBirth(date);
    }

    public static DateOfBirth of(int year, int month, int day) {
        return new DateOfBirth(LocalDate.of(year, month, day));
    }

    public static DateOfBirth parse(String date) {
        return new DateOfBirth(LocalDate.parse(date));
    }

    public static DateOfBirth parse(String date, DateTimeFormatter formatter) {
        return new DateOfBirth(LocalDate.parse(date, formatter));
    }

    // --- Age & Time Helpers ---

    /** Returns age in complete years. */
    public int ageInYears() {
        return Period.between(value, LocalDate.now()).getYears();
    }

    /** Returns age in complete months (e.g. 25 years 3 months = 303 months). */
    public long ageInMonths() {
        return ChronoUnit.MONTHS.between(value, LocalDate.now());
    }

    /** Returns age in complete weeks. */
    public long ageInWeeks() {
        return ChronoUnit.WEEKS.between(value, LocalDate.now());
    }

    /** Returns total days alive. */
    public long ageInDays() {
        return ChronoUnit.DAYS.between(value, LocalDate.now());
    }

    /** Returns precise age as a Period (years, months, days). */
    public Period agePrecise() {
        return Period.between(value, LocalDate.now());
    }

    /** Returns days until next birthday. Returns 0 if birthday is today. */
    public long daysUntilNextBirthday() {
        LocalDate today = LocalDate.now();
        LocalDate birthdayThisYear = value.withYear(today.getYear());
        if (birthdayThisYear.isBefore(today)) {
            birthdayThisYear = birthdayThisYear.plusYears(1);
        }
        return ChronoUnit.DAYS.between(today, birthdayThisYear);
    }

    /** Returns true if today is the birthday. */
    public boolean isBirthdayToday() {
        LocalDate today = LocalDate.now();
        return value.getMonth() == today.getMonth()
                && value.getDayOfMonth() == today.getDayOfMonth();
    }

    // --- Comparison ---

    public boolean isBefore(DateOfBirth other) {
        return this.value.isBefore(other.value());
    }

    public boolean isAfter(DateOfBirth other) {
        return this.value.isAfter(other.value());
    }

    // --- Formatting ---

    /** Returns ISO format: "2000-01-15" */
    public String toIsoDate() {
        return value.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /** Returns US format: "01/15/2000" */
    public String toUsDate() {
        return value.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }

    /** Returns EU format: "15/01/2000" */
    public String toEuDate() {
        return value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /** Returns long format: "January 15, 2000" */
    public String toLongDate() {
        return value.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
    }

    /** Returns medium format: "Jan 15, 2000" */
    public String toMediumDate() {
        return value.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    /** Returns short format: "15-Jan-2000" */
    public String toShortDate() {
        return value.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
    }

    /** Returns day-first long format: "15 January 2000" */
    public String toDayFirstLongDate() {
        return value.format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
    }

    /** Returns with day of week: "Saturday, January 15, 2000" */
    public String toFullDate() {
        return value.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
    }

    /** Returns human-readable age string: "25 years, 3 months, 10 days" */
    public String toAgeString() {
        Period period = agePrecise();
        return String.format("%d years, %d months, %d days",
                period.getYears(), period.getMonths(), period.getDays());
    }

    /** Returns custom formatted date. */
    public String formatted(String pattern) {
        return value.format(DateTimeFormatter.ofPattern(pattern));
    }

    /** Returns custom formatted date. */
    public String formatted(DateTimeFormatter formatter) {
        return value.format(formatter);
    }

    @Override
    public String toString() {
        return toIsoDate();
    }
}