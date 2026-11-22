package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A day on the calendar, with no time or zone: an invoice date, a due date, a day paid.
 *
 * <p>Reads the forms dates are printed in: {@code 2026-03-10}, {@code January 15, 2026}, {@code Jan 15 2026},
 * {@code 15 January 2026}, {@code 15-Jan-2026}, {@code 03/10/2026} (month first), {@code 01/26/26} (two-digit years
 * are 20xx). A day-first slash date is read day first only when it cannot be month first ({@code 26/01/2026}).
 *
 * <pre>{@code
 * CalendarDate.parse("January 15, 2026");     // 2026-01-15
 * CalendarDate.find("Due on Feb 5, 2026.");   // Optional[2026-02-05], the first date in a text
 * }</pre>
 *
 * @param value the day
 * @author JoshuaSalcedo
 */
public record CalendarDate(LocalDate value) implements ValueObject, Comparable<CalendarDate> {

    private static final String MONTH = "(?:jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec)[a-z]*\\.?";

    /** A date as printed, anywhere in a text. */
    public static final Pattern TEXT = Pattern.compile("(?i)\\b(?:\\d{4}-\\d{1,2}-\\d{1,2}"
            + "|\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|\\d{1,2}\\.\\d{1,2}\\.\\d{4}"
            + "|\\d{1,2}[ -]" + MONTH + "[ ,-]*\\d{2,4}"
            + "|" + MONTH + " \\d{1,2}(?:st|nd|rd|th)?,? \\d{4})\\b");

    private static final List<DateTimeFormatter> FORMS = new ArrayList<>();
    private static final List<DateTimeFormatter> SHORT_YEAR = new ArrayList<>();

    static {
        for (String p : List.of("uuuu-M-d", "MMMM d, uuuu", "MMM d, uuuu", "MMMM d uuuu", "MMM d uuuu", "d MMMM uuuu",
                "d MMM uuuu", "d-MMM-uuuu", "d MMMM, uuuu", "M/d/uuuu", "d/M/uuuu", "M-d-uuuu", "d.M.uuuu", "MMM. d, uuuu")) {
            FORMS.add(form(p));
        }
        for (String p : List.of("M/d/uu", "d/M/uu", "d-MMM-uu", "M-d-uu", "d.M.uu")) SHORT_YEAR.add(form(p));
    }

    private static DateTimeFormatter form(String pattern) {
        return new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(pattern).toFormatter(Locale.ENGLISH);
    }

    /**
     * Checks that the date is present.
     *
     * @throws ValueObjectException if the date is missing
     */
    public CalendarDate {
        if (value == null) throw new ValueObjectException("A date is required");
    }

    /**
     * Wraps a {@link LocalDate}.
     *
     * @param date the day
     * @return it
     * @throws ValueObjectException if {@code date} is null
     */
    public static CalendarDate of(LocalDate date) {
        return new CalendarDate(date);
    }

    /**
     * Makes the day from its year, month and day of the month.
     *
     * @param year  e.g. 2026
     * @param month 1-12
     * @param day   1-31
     * @return that day
     * @throws ValueObjectException if there is no such day
     */
    public static CalendarDate of(int year, int month, int day) {
        try {
            return new CalendarDate(LocalDate.of(year, month, day));
        } catch (java.time.DateTimeException e) {
            throw new ValueObjectException("No such date: " + year + "-" + month + "-" + day);
        }
    }

    /**
     * Today's date in this machine's default zone.
     *
     * @return today, in this machine's zone
     */
    public static CalendarDate today() {
        return new CalendarDate(LocalDate.now());
    }

    /**
     * Reads a text that is a date and nothing else, in any of the forms listed on the class; surrounding blanks and
     * one trailing {@code .} or {@code ,} are ignored.
     *
     * @param text a date as printed, e.g. {@code "January 15, 2026"}
     * @return it
     * @throws ValueObjectException if the text is not (only) a date
     */
    public static CalendarDate parse(String text) {
        if (text == null) throw new ValueObjectException("A date is required");
        String t = text.strip().replaceAll("[.,]$", "");
        Matcher m = TEXT.matcher(t);
        if (!m.find() || m.start() != 0 || m.end() < t.length() - 1)
            throw new ValueObjectException("Not a date: '" + text + "'");
        return find(t).orElseThrow(() -> new ValueObjectException("Not a date: '" + text + "'"));
    }

    /**
     * Finds the first date in a text; a match that is not a real day (such as {@code 02/30/2026}) is skipped.
     *
     * @param text any text, or null
     * @return the first date in it, or empty (also when {@code text} is null)
     */
    public static Optional<CalendarDate> find(@Nullable String text) {
        if (text == null) return Optional.empty();
        Matcher m = TEXT.matcher(text);
        while (m.find()) {
            LocalDate d = read(m.group());
            if (d != null) return Optional.of(new CalendarDate(d));
        }
        return Optional.empty();
    }

    /**
     * Tells whether a text is a date that {@link #parse(String)} accepts.
     *
     * @param text any text, or null
     * @return whether it is a date ({@code false} for null)
     */
    public static boolean isValid(@Nullable String text) {
        if (text == null) return false;
        try {
            parse(text);
            return true;
        } catch (ValueObjectException e) {
            return false;
        }
    }

    private static @Nullable LocalDate read(String raw) {
        String d = raw.replaceAll("\\s+", " ").replaceAll("(?i)\\bsept\\b", "Sep")
                .replaceAll("(?i)(\\d)(st|nd|rd|th)\\b", "$1").replaceAll("(?i)([a-z])\\.? (\\d)", "$1 $2")
                .replaceAll(" ,", ",");
        // a two-digit year: "01/26/26", "15-Jan-26" (not "2026-03-10", whose last part is the day)
        List<DateTimeFormatter> forms = d.matches("^\\d{1,2}[/.-]\\d{1,2}[/.-]\\d{2}$|^\\d{1,2}-\\p{L}+-\\d{2}$")
                ? SHORT_YEAR : FORMS;
        for (DateTimeFormatter f : forms) {
            try {
                return LocalDate.parse(d, f);
            } catch (DateTimeParseException next) {
                // try the next form
            }
        }
        return null;
    }

    /**
     * The year of this day.
     *
     * @return its year
     */
    public int year() {
        return value.getYear();
    }

    /**
     * The day of the week this day falls on.
     *
     * @return its day of the week
     */
    public DayOfWeek dayOfWeek() {
        return value.getDayOfWeek();
    }

    /**
     * Tells whether this day falls on a weekend.
     *
     * @return whether it is a Saturday or Sunday
     */
    public boolean isWeekend() {
        return value.getDayOfWeek() == DayOfWeek.SATURDAY || value.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /**
     * Counts the days from this day to another.
     *
     * @param other another day
     * @return the days from this one to it (negative when it is earlier)
     */
    public long daysUntil(CalendarDate other) {
        return ChronoUnit.DAYS.between(value, other.value);
    }

    /**
     * Moves this day by a number of days.
     *
     * @param days days to add (negative to go back)
     * @return that day
     */
    public CalendarDate plusDays(long days) {
        return new CalendarDate(value.plusDays(days));
    }

    /**
     * Tells whether this day is before today, in this machine's default zone.
     *
     * @return whether it is before today
     */
    public boolean isPast() {
        return value.isBefore(LocalDate.now());
    }

    /**
     * Writes this day in a {@link DateTimeFormatter} pattern, with English month and day names.
     *
     * @param pattern a {@link DateTimeFormatter} pattern, e.g. {@code "MMM d, uuuu"}
     * @return it written in that pattern, in English
     */
    public String format(String pattern) {
        return value.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }

    @Override
    public int compareTo(CalendarDate other) {
        return value.compareTo(other.value);
    }

    /**
     * Writes this day in ISO-8601 form.
     *
     * @return ISO-8601, e.g. {@code 2026-01-15}
     */
    @Override
    public String toString() {
        return value.toString();
    }

    /**
     * Reads a text like {@link #parse(String)}, without throwing.
     *
     * @param text any text, or null
     * @return it as a {@link CalendarDate}, or empty when it is not one; never throws
     */
    public static Optional<CalendarDate> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

}
