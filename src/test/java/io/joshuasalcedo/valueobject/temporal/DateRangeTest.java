package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DateRangeTest {

    private static LocalDate d(int y, int m, int day) {
        return LocalDate.of(y, m, day);
    }

    private static final DateRange JAN = DateRange.of(d(2026, 1, 1), d(2026, 1, 31));

    @Test
    void parseReadsIsoIntervalsAndSingleDates() {
        assertEquals(JAN, DateRange.parse("2026-01-01/2026-01-31"));
        assertEquals(JAN, DateRange.parse(" 2026-01-01 / 2026-01-31 "));
        assertEquals(DateRange.ofDay(d(2026, 1, 1)), DateRange.parse("2026-01-01"));
        assertEquals(DateRange.ofDay(d(2026, 1, 1)), DateRange.parse("2026-01-01/2026-01-01"));
        assertEquals("2026-01-01/2026-01-31", JAN.toString());
        assertEquals("2026-01-01", DateRange.ofDay(d(2026, 1, 1)).toIsoString());
        assertEquals(JAN, DateRange.parse(JAN.toString()));
    }

    @Test
    void parseRejectsBadInput() {
        assertThrows(ValueObjectException.class, () -> DateRange.parse(null));
        assertThrows(ValueObjectException.class, () -> DateRange.parse(""));
        assertThrows(ValueObjectException.class, () -> DateRange.parse("2026-01-31/2026-01-01"));
        assertThrows(ValueObjectException.class, () -> DateRange.parse("2026-01-01/"));
        assertThrows(ValueObjectException.class, () -> DateRange.parse("2026-01-01/2026-01-02/2026-01-03"));
        assertThrows(ValueObjectException.class, () -> DateRange.parse("2026-02-30"));
        assertEquals(Optional.empty(), DateRange.tryParse(null));
        assertEquals(Optional.empty(), DateRange.tryParse("January"));
        assertEquals(Optional.of(JAN), DateRange.tryParse("2026-01-01/2026-01-31"));
        assertFalse(DateRange.isValid(null));
        assertTrue(DateRange.isValid("2026-01-01"));
    }

    @Test
    void constructionRejectsNullAndReversedDates() {
        assertThrows(ValueObjectException.class, () -> new DateRange(null, d(2026, 1, 1)));
        assertThrows(ValueObjectException.class, () -> new DateRange(d(2026, 1, 1), null));
        assertThrows(ValueObjectException.class, () -> DateRange.of(d(2026, 1, 2), d(2026, 1, 1)));
        assertThrows(ValueObjectException.class, () -> DateRange.ofDay(null));
        assertThrows(ValueObjectException.class, () -> DateRange.lastDays(0));
        assertThrows(ValueObjectException.class, () -> DateRange.nextDays(0));
        assertThrows(ValueObjectException.class, () -> DateRange.lastDays(Integer.MIN_VALUE));
        assertThrows(ValueObjectException.class, () -> DateRange.nextDays(-5));
    }

    @Test
    void relativeFactories() {
        LocalDate today = LocalDate.now();
        assertEquals(7, DateRange.lastDays(7).days());
        assertEquals(today, DateRange.lastDays(7).endDate());
        assertEquals(today, DateRange.nextDays(3).startDate());
        assertTrue(DateRange.today().isSingleDay());
        assertEquals(7, DateRange.thisWeek().days());
        assertEquals(today.lengthOfMonth(), DateRange.thisMonth().days());
        assertEquals(today.lengthOfYear(), DateRange.thisYear().days());
        assertTrue(DateRange.today().includesToday());
    }

    @Test
    void queries() {
        assertTrue(JAN.contains(d(2026, 1, 1)));
        assertTrue(JAN.contains(d(2026, 1, 31)));
        assertFalse(JAN.contains(d(2026, 2, 1)));
        assertTrue(JAN.contains(LocalDateTime.of(2026, 1, 31, 23, 59)));
        assertTrue(JAN.contains(DateRange.of(d(2026, 1, 5), d(2026, 1, 6))));
        assertFalse(JAN.contains(DateRange.of(d(2025, 12, 31), d(2026, 1, 6))));
        assertEquals(31, JAN.days());
        assertEquals(Period.ofMonths(1), JAN.period());
        assertEquals(Period.ofDays(1), DateRange.ofDay(d(2026, 1, 1)).period());
        assertTrue(DateRange.of(d(2000, 1, 1), d(2000, 1, 2)).isPast());
        assertTrue(DateRange.of(d(9000, 1, 1), d(9000, 1, 2)).isFuture());
        assertThrows(ValueObjectException.class, () -> JAN.contains((LocalDate) null));
        assertThrows(ValueObjectException.class, () -> JAN.contains((LocalDateTime) null));
        assertThrows(ValueObjectException.class, () -> JAN.contains((DateRange) null));
    }

    @Test
    void overlapAdjacencyIntersectionUnion() {
        DateRange feb = DateRange.of(d(2026, 2, 1), d(2026, 2, 28));
        DateRange mid = DateRange.of(d(2026, 1, 20), d(2026, 2, 10));
        DateRange mar = DateRange.of(d(2026, 3, 1), d(2026, 3, 31));
        assertTrue(JAN.overlaps(mid));
        assertFalse(JAN.overlaps(feb));
        assertTrue(JAN.isAdjacentTo(feb));
        assertTrue(feb.isAdjacentTo(JAN));
        assertFalse(JAN.isAdjacentTo(mar));
        assertEquals(java.util.Optional.of(DateRange.of(d(2026, 1, 20), d(2026, 1, 31))), JAN.intersection(mid));
        assertTrue(JAN.intersection(feb).isEmpty());
        assertEquals(DateRange.of(d(2026, 1, 1), d(2026, 2, 28)), JAN.union(feb));
        assertEquals(DateRange.of(d(2026, 1, 1), d(2026, 2, 10)), JAN.union(mid));
        assertThrows(ValueObjectException.class, () -> JAN.union(mar));
        assertThrows(ValueObjectException.class, () -> JAN.overlaps(null));
        assertThrows(ValueObjectException.class, () -> JAN.isAdjacentTo(null));
        assertThrows(ValueObjectException.class, () -> JAN.intersection(null));
        assertThrows(ValueObjectException.class, () -> JAN.union(null));
        // the end of time has no next day, but adjacency is still answered
        DateRange last = DateRange.ofDay(LocalDate.MAX);
        assertFalse(last.isAdjacentTo(JAN));
    }

    @Test
    void extendAndShift() {
        assertEquals(DateRange.of(d(2026, 1, 1), d(2026, 2, 5)), JAN.extendBy(5));
        assertEquals(DateRange.of(d(2026, 1, 1), d(2026, 1, 1)), JAN.extendBy(-30));
        assertThrows(ValueObjectException.class, () -> JAN.extendBy(-31));
        assertEquals(DateRange.of(d(2026, 1, 11), d(2026, 2, 10)), JAN.shift(10));
        assertEquals(DateRange.of(d(2025, 12, 22), d(2026, 1, 21)), JAN.shift(-10));
        assertThrows(ValueObjectException.class, () -> DateRange.ofDay(LocalDate.MAX).shift(1));
        assertThrows(ValueObjectException.class, () -> DateRange.ofDay(LocalDate.MAX).extendBy(1));
    }

    @Test
    void iteration() {
        DateRange three = DateRange.of(d(2026, 1, 30), d(2026, 2, 1));
        List<LocalDate> days = new ArrayList<>();
        for (LocalDate day : three) days.add(day);
        assertEquals(List.of(d(2026, 1, 30), d(2026, 1, 31), d(2026, 2, 1)), days);
        assertEquals(days, three.stream().toList());
    }

    @Test
    void ordering() {
        DateRange a = DateRange.of(d(2026, 1, 1), d(2026, 1, 5));
        DateRange b = DateRange.of(d(2026, 1, 1), d(2026, 1, 9));
        DateRange c = DateRange.of(d(2026, 1, 2), d(2026, 1, 3));
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(c) < 0);
        assertEquals(0, a.compareTo(DateRange.of(d(2026, 1, 1), d(2026, 1, 5))));
        assertThrows(ValueObjectException.class, () -> a.compareTo(null));
    }

    @Test
    void formatIsEnglishWhateverTheDefaultLocale() {
        Locale saved = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            assertEquals("Jan 1, 2026 - Jan 31, 2026", JAN.format("MMM d, yyyy"));
            assertEquals("Thursday 1 January", DateRange.ofDay(d(2026, 1, 1)).format("EEEE d MMMM"));
        } finally {
            Locale.setDefault(saved);
        }
        assertThrows(ValueObjectException.class, () -> JAN.format(null));
        assertThrows(ValueObjectException.class, () -> JAN.format("{{"));
        assertThrows(ValueObjectException.class, () -> JAN.format("HH:mm"));
    }
}
