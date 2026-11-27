package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import org.jspecify.annotations.Nullable;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;

/**
 * Represents standardized recurrence intervals for scheduling and billing cycles.
 *
 * <p>Most intervals step by a fixed {@link Period}. Two are special:
 * <ul>
 *   <li>{@code SEMIMONTHLY} falls on the 1st and the 15th of every month: from a date before the 15th the next
 *       occurrence is the 15th of the same month, and from a date on or after the 15th it is the 1st of the next
 *       month. The time of day is kept.</li>
 *   <li>{@code ONCE} happens a single time, at its start, and has no later occurrence:
 *       {@link #nextOccurrence(LocalDateTime)} returns the start itself, {@link #nextOccurrences(LocalDateTime, int)}
 *       returns an empty list, and {@link #nextFutureOccurrence(LocalDateTime, LocalDateTime)} returns the start
 *       only while it is still in the future.</li>
 * </ul>
 * No method loops without end.
 *
 * @author JoshuaSalcedo
 * @since 2/1/2026
 */
public enum RecurrenceInterval implements ValueObject {

    /** One-time: a single occurrence at the start, never repeated. */
    ONCE(Period.ZERO, "One-time", 1, "1"),
    /** Every day. */
    DAILY(Period.ofDays(1), "Every day", 365, "365"),
    /** Every week. */
    WEEKLY(Period.ofWeeks(1), "Every week", 52, "52"),
    /** Every two weeks. */
    BIWEEKLY(Period.ofWeeks(2), "Every two weeks", 26, "26"),
    /** Twice a month, on the 1st and the 15th. */
    SEMIMONTHLY(Period.ZERO, "Twice a month", 24, "24"),
    /** Every month. */
    MONTHLY(Period.ofMonths(1), "Every month", 12, "12"),
    /** Every two months. */
    BIMONTHLY(Period.ofMonths(2), "Every two months", 6, "6"),
    /** Every quarter. */
    QUARTERLY(Period.ofMonths(3), "Every quarter", 4, "4"),
    /** Every six months. */
    SEMIANNUALLY(Period.ofMonths(6), "Every six months", 2, "2"),
    /** Every year. */
    ANNUALLY(Period.ofYears(1), "Every year", 1, "1"),
    /** Every two years. */
    BIENNIALLY(Period.ofYears(2), "Every two years", 0, "0.5");

    private final TemporalAmount duration;
    private final String displayName;
    private final int occurrencesPerYear;
    private final BigDecimal exactOccurrencesPerYear;

    RecurrenceInterval(TemporalAmount duration, String displayName, int occurrencesPerYear, String exact) {
        this.duration = duration;
        this.displayName = displayName;
        this.occurrencesPerYear = occurrencesPerYear;
        this.exactOccurrencesPerYear = new BigDecimal(exact);
    }

    /**
     * Finds the next occurrence one step after now, on this machine's clock and zone.
     *
     * @return the next scheduled date/time from the current moment, as {@link #nextOccurrence(LocalDateTime)}
     */
    public LocalDateTime nextOccurrenceFromNow() {
        return nextOccurrence(LocalDateTime.now());
    }

    /**
     * Calculates the next occurrence one step after the given starting point. For {@code SEMIMONTHLY} that is the
     * next 1st or 15th after {@code from}'s date; for {@code ONCE}, which has no later occurrence, it is
     * {@code from} itself.
     *
     * @param from the starting date/time
     * @return the next scheduled date/time; {@code from} itself for {@code ONCE}
     * @throws ValueObjectException if {@code from} is null or the result is out of range
     */
    public LocalDateTime nextOccurrence(LocalDateTime from) {
        if (from == null) throw new ValueObjectException("A starting date/time is required");
        try {
            return switch (this) {
                case ONCE -> from;
                case SEMIMONTHLY -> from.getDayOfMonth() < 15
                        ? from.withDayOfMonth(15)
                        : from.withDayOfMonth(1).plusMonths(1);
                default -> from.plus(duration);
            };
        } catch (DateTimeException | ArithmeticException e) {
            throw new ValueObjectException("The next occurrence after " + from + " is out of range", e);
        }
    }

    /**
     * Calculates the next N occurrences after the given starting point, each one step after the last;
     * {@code from} itself is not included. {@code ONCE} has none, so its list is always empty.
     *
     * @param from  the starting date/time
     * @param count number of occurrences to generate; 0 or more
     * @return list of scheduled date/times, in order
     * @throws ValueObjectException if {@code from} is null, {@code count} is negative, or an occurrence is out of
     *                              range
     */
    public List<LocalDateTime> nextOccurrences(LocalDateTime from, int count) {
        if (from == null) throw new ValueObjectException("A starting date/time is required");
        if (count < 0) throw new ValueObjectException("The count must be 0 or more: " + count);
        if (this == ONCE) return new ArrayList<>();
        List<LocalDateTime> occurrences = new ArrayList<>(count);
        LocalDateTime current = from;
        for (int i = 0; i < count; i++) {
            current = nextOccurrence(current);
            occurrences.add(current);
        }
        return occurrences;
    }

    /**
     * Finds the next future occurrence, on this machine's clock and zone, as
     * {@link #nextFutureOccurrence(LocalDateTime, LocalDateTime)} does with now.
     *
     * @param startDate the original start/anchor date
     * @return the first occurrence after now, or empty for a {@code ONCE} start that is not in the future
     * @throws ValueObjectException if {@code startDate} is null or an occurrence is out of range
     */
    public Optional<LocalDateTime> nextFutureOccurrence(LocalDateTime startDate) {
        return nextFutureOccurrence(startDate, LocalDateTime.now());
    }

    /**
     * Finds the first occurrence after a given moment: steps from {@code startDate} until the result is after
     * {@code now}. A start that is already after {@code now} is returned as it is. A {@code ONCE} start that is
     * not after {@code now} has no future occurrence, so the result is empty; for every other interval it is
     * always present.
     *
     * @param startDate the original start/anchor date
     * @param now       the moment the result must be after
     * @return the first occurrence after {@code now}, or empty for a {@code ONCE} start that is not after it
     * @throws ValueObjectException if an argument is null or an occurrence is out of range
     */
    public Optional<LocalDateTime> nextFutureOccurrence(LocalDateTime startDate, LocalDateTime now) {
        if (startDate == null || now == null) throw new ValueObjectException("A start and a moment are required");
        if (startDate.isAfter(now)) return Optional.of(startDate);
        if (this == ONCE) return Optional.empty();
        LocalDateTime next = startDate;
        while (!next.isAfter(now)) {
            next = nextOccurrence(next);
        }
        return Optional.of(next);
    }

    /**
     * The step between occurrences.
     *
     * @return the step, a {@link Period}; {@link Period#ZERO} for {@code ONCE} and for {@code SEMIMONTHLY}, whose
     *         steps are not a fixed period (see the class description)
     */
    public TemporalAmount getDuration() {
        return duration;
    }

    /**
     * A label for people, e.g. {@code "Every two weeks"}.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * How many times the interval comes round in a year, rounded down to a whole number: 52 for {@code WEEKLY},
     * 1 for {@code ONCE} and {@code ANNUALLY}, 0 for {@code BIENNIALLY} (which comes round half a time a year; see
     * {@link #getExactOccurrencesPerYear()}).
     *
     * @return the whole occurrences per year
     */
    public int getOccurrencesPerYear() {
        return occurrencesPerYear;
    }

    /**
     * How many times the interval comes round in a year, exactly: {@code 0.5} for {@code BIENNIALLY}, otherwise
     * the same number as {@link #getOccurrencesPerYear()} (1 for {@code ONCE}, its single occurrence).
     *
     * @return the occurrences per year as a decimal
     */
    public BigDecimal getExactOccurrencesPerYear() {
        return exactOccurrencesPerYear;
    }

    /**
     * Tells whether the interval repeats.
     *
     * @return {@code false} for {@code ONCE}, {@code true} for every other interval
     */
    public boolean isRecurring() {
        return this != ONCE;
    }

    /**
     * Reads an interval by its constant name, ignoring case, blanks around it, and any dots, hyphens, underscores
     * and spaces: {@code "bi-weekly"}, {@code "Bi Weekly"} and {@code "BIWEEKLY"} all read as {@code BIWEEKLY}.
     * The display names are not read.
     *
     * @param text its name, e.g. {@code "monthly"}
     * @return it
     * @throws ValueObjectException if it is not one
     */
    public static RecurrenceInterval parse(String text) {
        if (text == null) throw new ValueObjectException("A RecurrenceInterval is required");
        String key = text.strip().replaceAll("[._\\-\\s]", "").toUpperCase(Locale.ROOT);
        for (RecurrenceInterval v : values()) {
            if (v.name().replace("_", "").equals(key)) return v;
        }
        throw new ValueObjectException("Not a RecurrenceInterval: '" + text + "'");
    }

    /**
     * Reads an interval like {@link #parse(String)}, without throwing.
     *
     * @param text any text, or null
     * @return it as a {@link RecurrenceInterval}, or empty when it is not one; never throws
     */
    public static Optional<RecurrenceInterval> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text names an interval that {@link #parse(String)} accepts.
     *
     * @param text any text, or null
     * @return whether it is a {@link RecurrenceInterval} ({@code false} for null)
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
