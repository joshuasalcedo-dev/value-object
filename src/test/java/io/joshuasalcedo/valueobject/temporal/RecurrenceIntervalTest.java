package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RecurrenceIntervalTest {

    private static final LocalDateTime JAN_10 = LocalDateTime.of(2026, 1, 10, 9, 30);

    @Test
    void parseReadsNamesIgnoringCaseSeparatorsAndBlanks() {
        assertEquals(RecurrenceInterval.MONTHLY, RecurrenceInterval.parse("monthly"));
        assertEquals(RecurrenceInterval.MONTHLY, RecurrenceInterval.parse("  MONTHLY "));
        assertEquals(RecurrenceInterval.BIWEEKLY, RecurrenceInterval.parse("bi-weekly"));
        assertEquals(RecurrenceInterval.BIWEEKLY, RecurrenceInterval.parse("Bi Weekly"));
        assertEquals(RecurrenceInterval.BIWEEKLY, RecurrenceInterval.parse("bi_weekly"));
        assertEquals(RecurrenceInterval.SEMIMONTHLY, RecurrenceInterval.parse("semi-monthly"));
        assertEquals(RecurrenceInterval.SEMIANNUALLY, RecurrenceInterval.parse("Semi.Annually"));
        assertEquals(RecurrenceInterval.ONCE, RecurrenceInterval.parse("once"));
        for (RecurrenceInterval v : RecurrenceInterval.values()) {
            assertEquals(v, RecurrenceInterval.parse(v.name()));
            assertEquals(v, RecurrenceInterval.parse(v.name().toLowerCase()));
        }
    }

    @Test
    void parseRejectsBadInput() {
        assertThrows(ValueObjectException.class, () -> RecurrenceInterval.parse(null));
        assertThrows(ValueObjectException.class, () -> RecurrenceInterval.parse(""));
        assertThrows(ValueObjectException.class, () -> RecurrenceInterval.parse("fortnightly"));
        assertThrows(ValueObjectException.class, () -> RecurrenceInterval.parse("Every month"));
    }

    @Test
    void tryParseAndIsValidNeverThrow() {
        assertEquals(Optional.empty(), RecurrenceInterval.tryParse(null));
        assertEquals(Optional.empty(), RecurrenceInterval.tryParse("nope"));
        assertEquals(Optional.of(RecurrenceInterval.DAILY), RecurrenceInterval.tryParse("daily"));
        assertFalse(RecurrenceInterval.isValid(null));
        assertFalse(RecurrenceInterval.isValid(" "));
        assertTrue(RecurrenceInterval.isValid("bi-weekly"));
    }

    @Test
    void fixedStepsAdvanceByTheirPeriod() {
        assertEquals(JAN_10.plusDays(1), RecurrenceInterval.DAILY.nextOccurrence(JAN_10));
        assertEquals(JAN_10.plusWeeks(1), RecurrenceInterval.WEEKLY.nextOccurrence(JAN_10));
        assertEquals(JAN_10.plusWeeks(2), RecurrenceInterval.BIWEEKLY.nextOccurrence(JAN_10));
        assertEquals(JAN_10.plusMonths(1), RecurrenceInterval.MONTHLY.nextOccurrence(JAN_10));
        assertEquals(JAN_10.plusMonths(3), RecurrenceInterval.QUARTERLY.nextOccurrence(JAN_10));
        assertEquals(JAN_10.plusYears(2), RecurrenceInterval.BIENNIALLY.nextOccurrence(JAN_10));
        assertEquals(Period.ofMonths(6), RecurrenceInterval.SEMIANNUALLY.getDuration());
    }

    @Test
    void semimonthlyFallsOnThe1stAnd15th() {
        RecurrenceInterval s = RecurrenceInterval.SEMIMONTHLY;
        assertEquals(LocalDateTime.of(2026, 1, 15, 9, 30), s.nextOccurrence(JAN_10));
        assertEquals(LocalDateTime.of(2026, 2, 1, 9, 30), s.nextOccurrence(LocalDateTime.of(2026, 1, 15, 9, 30)));
        assertEquals(LocalDateTime.of(2026, 2, 1, 9, 30), s.nextOccurrence(LocalDateTime.of(2026, 1, 31, 9, 30)));
        assertEquals(LocalDateTime.of(2027, 1, 1, 0, 0), s.nextOccurrence(LocalDateTime.of(2026, 12, 20, 0, 0)));
        assertNotEquals(JAN_10, s.nextOccurrence(JAN_10));

        List<LocalDateTime> next = s.nextOccurrences(LocalDateTime.of(2026, 1, 1, 0, 0), 4);
        assertEquals(List.of(
                LocalDateTime.of(2026, 1, 15, 0, 0),
                LocalDateTime.of(2026, 2, 1, 0, 0),
                LocalDateTime.of(2026, 2, 15, 0, 0),
                LocalDateTime.of(2026, 3, 1, 0, 0)), next);
    }

    @Test
    void onceHasNoLaterOccurrence() {
        assertEquals(JAN_10, RecurrenceInterval.ONCE.nextOccurrence(JAN_10));
        assertTrue(RecurrenceInterval.ONCE.nextOccurrences(JAN_10, 5).isEmpty());
        assertFalse(RecurrenceInterval.ONCE.isRecurring());
        assertTrue(RecurrenceInterval.DAILY.isRecurring());
    }

    @Test
    @Timeout(5)
    void nextFutureOccurrenceNeverLoopsForever() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 20, 12, 0);
        assertEquals(Optional.empty(), RecurrenceInterval.ONCE.nextFutureOccurrence(JAN_10, now));
        assertEquals(Optional.of(LocalDateTime.of(2026, 4, 1, 9, 30)),
                RecurrenceInterval.SEMIMONTHLY.nextFutureOccurrence(JAN_10, now));
        assertEquals(Optional.of(LocalDateTime.of(2026, 4, 10, 9, 30)),
                RecurrenceInterval.MONTHLY.nextFutureOccurrence(JAN_10, now));
        // a start already in the future is returned as it is, also for ONCE
        LocalDateTime later = now.plusDays(1);
        assertEquals(Optional.of(later), RecurrenceInterval.ONCE.nextFutureOccurrence(later, now));
        assertEquals(Optional.of(later), RecurrenceInterval.WEEKLY.nextFutureOccurrence(later, now));
        // the no-clock overload also returns for ONCE in the past
        assertEquals(Optional.empty(), RecurrenceInterval.ONCE.nextFutureOccurrence(JAN_10.minusYears(10)));
        assertTrue(RecurrenceInterval.SEMIMONTHLY.nextFutureOccurrence(JAN_10.minusYears(1)).isPresent());
    }

    @Test
    void occurrencesPerYear() {
        assertEquals(0, RecurrenceInterval.BIENNIALLY.getOccurrencesPerYear());
        assertEquals(0, new BigDecimal("0.5").compareTo(RecurrenceInterval.BIENNIALLY.getExactOccurrencesPerYear()));
        assertEquals(24, RecurrenceInterval.SEMIMONTHLY.getOccurrencesPerYear());
        for (RecurrenceInterval v : RecurrenceInterval.values()) {
            if (v != RecurrenceInterval.BIENNIALLY) {
                assertEquals(0, BigDecimal.valueOf(v.getOccurrencesPerYear()).compareTo(v.getExactOccurrencesPerYear()), v.name());
            }
        }
    }

    @Test
    void badArgumentsThrowValueObjectException() {
        assertThrows(ValueObjectException.class, () -> RecurrenceInterval.DAILY.nextOccurrence(null));
        assertThrows(ValueObjectException.class, () -> RecurrenceInterval.DAILY.nextOccurrences(JAN_10, -1));
        assertThrows(ValueObjectException.class, () -> RecurrenceInterval.DAILY.nextOccurrences(null, 1));
        assertThrows(ValueObjectException.class, () -> RecurrenceInterval.DAILY.nextFutureOccurrence(null, JAN_10));
        assertThrows(ValueObjectException.class, () -> RecurrenceInterval.DAILY.nextFutureOccurrence(JAN_10, null));
        assertThrows(ValueObjectException.class, () -> RecurrenceInterval.ANNUALLY.nextOccurrence(LocalDateTime.MAX));
        assertEquals(List.of(), RecurrenceInterval.DAILY.nextOccurrences(JAN_10, 0));
    }

    @Test
    void displayNames() {
        assertEquals("Every two weeks", RecurrenceInterval.BIWEEKLY.getDisplayName());
        assertEquals("Twice a month", RecurrenceInterval.SEMIMONTHLY.getDisplayName());
    }
}
