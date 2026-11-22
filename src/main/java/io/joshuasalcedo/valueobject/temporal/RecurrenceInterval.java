package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.ValueObject;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;

/**
 * Represents standardized recurrence intervals for scheduling and billing cycles.
 *
 * @author JoshuaSalcedo
 * @since 2/1/2026
 */
public enum RecurrenceInterval implements ValueObject {
    
    ONCE(Period.ZERO, "One-time", 1),
    DAILY(Period.ofDays(1), "Every day", 365),
    WEEKLY(Period.ofWeeks(1), "Every week", 52),
    BIWEEKLY(Period.ofWeeks(2), "Every two weeks", 26),
    SEMIMONTHLY(Period.ZERO, "Twice a month", 24),  // 1st & 15th typically
    MONTHLY(Period.ofMonths(1), "Every month", 12),
    BIMONTHLY(Period.ofMonths(2), "Every two months", 6),
    QUARTERLY(Period.ofMonths(3), "Every quarter", 4),
    SEMIANNUALLY(Period.ofMonths(6), "Every six months", 2),
    ANNUALLY(Period.ofYears(1), "Every year", 1),
    BIENNIALLY(Period.ofYears(2), "Every two years", 0);

    private final TemporalAmount duration;
    private final String displayName;
    private final int occurrencesPerYear;

    /**
     * Finds the next occurrence after now.
     *
     * @return the next scheduled date/time from current moment
     */
    public LocalDateTime nextOccurrenceFromNow() {
        return nextOccurrence(LocalDateTime.now());
    }
    /**
     * Calculates the next occurrence from the given starting point.
     *
     * @param from the starting date/time
     * @return the next scheduled date/time
     */
    public LocalDateTime nextOccurrence(LocalDateTime from) {
        return from.plus(duration);
    }

    /**
     * Calculates the next N occurrences from the given starting point.
     *
     * @param from  the starting date/time
     * @param count number of occurrences to generate
     * @return list of scheduled date/times
     */
    public java.util.List<LocalDateTime> nextOccurrences(LocalDateTime from, int count) {
        java.util.List<LocalDateTime> occurrences = new java.util.ArrayList<>(count);
        LocalDateTime current = from;
        for (int i = 0; i < count; i++) {
            current = current.plus(duration);
            occurrences.add(current);
        }
        return occurrences;
    }

    /**
     * Calculates when payment/event is due given a start date,
     * finding the next future occurrence.
     *
     * @param startDate the original start/anchor date
     * @return the next occurrence that is in the future
     */
    public LocalDateTime nextFutureOccurrence(LocalDateTime startDate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = startDate;

        while (!next.isAfter(now)) {
            next = next.plus(duration);
        }

        return next;
    }

    RecurrenceInterval(TemporalAmount duration, String displayName, int occurrencesPerYear) {
        this.duration = duration;
        this.displayName = displayName;
        this.occurrencesPerYear = occurrencesPerYear;
    }



    public TemporalAmount getDuration() {
        return duration;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getOccurrencesPerYear() {
        return occurrencesPerYear;
    }

    public boolean isRecurring() {
        return this != ONCE;
    }
}