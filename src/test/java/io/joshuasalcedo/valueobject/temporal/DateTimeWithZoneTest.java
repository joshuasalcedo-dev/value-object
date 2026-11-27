package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeWithZoneTest {

    private static final DateTimeWithZone MANILA =
            DateTimeWithZone.of(2026, 2, 8, 14, 30, 0, ZoneOffset.ofHours(8));

    @Test
    void parseReadsIsoWithOffset() {
        DateTimeWithZone d = DateTimeWithZone.parse("2026-02-08T14:30:00+08:00");
        assertEquals(MANILA, d);
        assertEquals("2026-02-08T14:30:00+08:00", d.toIsoString());
        assertEquals(d.toIsoString(), d.toString());
        assertEquals(d, DateTimeWithZone.parse(d.toString()));
        assertEquals("1970-01-01T00:00:00Z", DateTimeWithZone.DEFAULT.toString());
    }

    @Test
    void parseRejectsBadInputWithValueObjectException() {
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.parse(null));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.parse(""));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.parse("2026-02-08T14:30:00"));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.parse("2026-02-30T14:30:00Z"));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.parse(null, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.parse("2026-02-08T14:30:00Z", null));
        assertEquals(Optional.empty(), DateTimeWithZone.tryParse(null));
        assertEquals(Optional.empty(), DateTimeWithZone.tryParse("garbage"));
        assertEquals(Optional.of(MANILA), DateTimeWithZone.tryParse("2026-02-08T14:30+08:00"));
        assertFalse(DateTimeWithZone.isValid(null));
        assertFalse(DateTimeWithZone.isValid("2026-02-08"));
        assertTrue(DateTimeWithZone.isValid("2026-02-08T06:30:00Z"));
    }

    @Test
    void factoriesRejectBadInputWithValueObjectException() {
        assertThrows(ValueObjectException.class, () -> new DateTimeWithZone(null));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.of(null));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.of(2026, 2, 30, 0, 0, 0, ZoneOffset.UTC));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.of(2026, 13, 1, 0, 0, 0, TimeZone.UTC));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.of(2026, 1, 1, 0, 0, 0, (TimeZone) null));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.of(2026, 1, 1, 0, 0, 0, (ZoneOffset) null));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.of(null, ZoneOffset.UTC));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.of(LocalDateTime.of(2026, 1, 1, 0, 0), (TimeZone) null));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.ofStartOfDay(null, TimeZone.UTC));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.ofInstant(null));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.ofInstant(Instant.EPOCH, null));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.ofEpochSecond(Long.MAX_VALUE));
        assertThrows(ValueObjectException.class, () -> DateTimeWithZone.now(null));
    }

    @Test
    void timeZoneFactoriesApplyTheOffsetAtThatDate() {
        DateTimeWithZone winter = DateTimeWithZone.of(2026, 1, 15, 12, 0, 0, TimeZone.AMERICA_NEW_YORK);
        DateTimeWithZone summer = DateTimeWithZone.of(2026, 7, 15, 12, 0, 0, TimeZone.AMERICA_NEW_YORK);
        assertEquals(ZoneOffset.ofHours(-5), winter.offset());
        assertEquals(ZoneOffset.ofHours(-4), summer.offset());
        assertEquals(LocalDateTime.of(2026, 3, 8, 0, 0),
                DateTimeWithZone.ofStartOfDay(LocalDate.of(2026, 3, 8), TimeZone.AMERICA_NEW_YORK).toLocalDateTime());
    }

    @Test
    void resolveTimeZoneUsesTheOffsetAtTheStoredInstant() {
        // New York summer time; resolving used to compare with every zone's offset today
        // -07:00 is Los Angeles in summer and Denver in winter, whatever today's date is
        assertEquals(Optional.of(TimeZone.AMERICA_LOS_ANGELES),
                DateTimeWithZone.parse("2026-07-15T12:00:00-07:00").resolveTimeZone());
        assertEquals(Optional.of(TimeZone.AMERICA_DENVER),
                DateTimeWithZone.parse("2026-01-15T12:00:00-07:00").resolveTimeZone());
        assertEquals(Optional.of(TimeZone.AMERICA_NEW_YORK),
                DateTimeWithZone.parse("2026-01-15T12:00:00-05:00").resolveTimeZone());
        DateTimeWithZone sydneySummer = DateTimeWithZone.parse("2026-01-15T12:00:00+11:00");
        assertEquals(Optional.of(TimeZone.AUSTRALIA_SYDNEY), sydneySummer.resolveTimeZone());
        assertEquals(Optional.of(TimeZone.AFRICA_ABIDJAN), DateTimeWithZone.DEFAULT.resolveTimeZone());
        assertEquals(Optional.empty(), DateTimeWithZone.parse("2026-01-15T12:00:00+05:45").resolveTimeZone());
    }

    @Test
    void isInTimeZoneUsesTheOffsetAtTheStoredInstant() {
        DateTimeWithZone summerNy = DateTimeWithZone.parse("2026-07-15T12:00:00-04:00");
        DateTimeWithZone winterNy = DateTimeWithZone.parse("2026-01-15T12:00:00-05:00");
        assertTrue(summerNy.isInTimeZone(TimeZone.AMERICA_NEW_YORK));
        assertTrue(winterNy.isInTimeZone(TimeZone.AMERICA_NEW_YORK));
        assertFalse(DateTimeWithZone.parse("2026-07-15T12:00:00-05:00").isInTimeZone(TimeZone.AMERICA_NEW_YORK));
        assertTrue(MANILA.isInTimeZone(TimeZone.ASIA_MANILA));
        assertThrows(ValueObjectException.class, () -> MANILA.isInTimeZone(null));
    }

    @Test
    void humanFormsAreEnglishAndSignCorrect() {
        Locale saved = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            assertEquals("February 8, 2026 2:30 PM (GMT+8:00)", MANILA.toHumanReadable());
            String display = MANILA.toDisplayString(TimeZone.ASIA_MANILA);
            assertTrue(display.startsWith("Feb 8, 2026 2:30 PM "), display);
            assertTrue(display.endsWith(" (Asia/Manila)"), display);
        } finally {
            Locale.setDefault(saved);
        }
        DateTimeWithZone minusHalf = DateTimeWithZone.of(2026, 1, 1, 9, 0, 0, ZoneOffset.ofHoursMinutes(0, -30));
        assertEquals("January 1, 2026 9:00 AM (GMT-0:30)", minusHalf.toHumanReadable());
        DateTimeWithZone nepal = DateTimeWithZone.of(2026, 1, 1, 9, 0, 0, ZoneOffset.ofHoursMinutes(5, 45));
        assertEquals("January 1, 2026 9:00 AM (GMT+5:45)", nepal.toHumanReadable());
        assertThrows(ValueObjectException.class, () -> MANILA.toDisplayString(null));
    }

    @Test
    void conversionsKeepTheInstant() {
        DateTimeWithZone utc = MANILA.toUtc();
        assertEquals(ZoneOffset.UTC, utc.offset());
        assertEquals(6, utc.hour());
        assertTrue(utc.isEqual(MANILA));
        assertNotEquals(utc, MANILA);
        assertTrue(utc.compareTo(MANILA) != 0); // same instant, ordered then by local date-time
        DateTimeWithZone tokyo = MANILA.atTimeZone(TimeZone.ASIA_TOKYO);
        assertEquals(15, tokyo.hour());
        assertEquals(ZoneId.of("Asia/Tokyo"), MANILA.toZonedDateTime(TimeZone.ASIA_TOKYO).getZone());
        assertEquals(Instant.parse("2026-02-08T06:30:00Z"), MANILA.toInstant());
        assertEquals(MANILA.toInstant().getEpochSecond(), MANILA.toEpochSecond());
        assertEquals(MANILA.toInstant().toEpochMilli(), MANILA.toEpochMilli());
        assertEquals(MANILA, DateTimeWithZone.ofEpochMilli(MANILA.toEpochMilli()).atTimeZone(TimeZone.ASIA_MANILA));
        assertEquals(DateTimeWithZone.ofEpochSecond(0), DateTimeWithZone.DEFAULT);
        assertThrows(ValueObjectException.class, () -> MANILA.atTimeZone(null));
        assertThrows(ValueObjectException.class,
                () -> DateTimeWithZone.of(OffsetDateTime.MAX).toEpochMilli());
    }

    @Test
    void accessors() {
        assertEquals(2026, MANILA.year());
        assertEquals(Month.FEBRUARY, MANILA.month());
        assertEquals(2, MANILA.monthValue());
        assertEquals(8, MANILA.dayOfMonth());
        assertEquals(DayOfWeek.SUNDAY, MANILA.dayOfWeek());
        assertEquals(39, MANILA.dayOfYear());
        assertEquals(14, MANILA.hour());
        assertEquals(30, MANILA.minute());
        assertEquals(0, MANILA.second());
        assertEquals(0, MANILA.nano());
    }

    @Test
    void arithmeticAndOverflow() {
        assertEquals(DateTimeWithZone.parse("2026-02-09T14:30:00+08:00"), MANILA.plusDays(1));
        assertEquals(DateTimeWithZone.parse("2026-02-08T12:30:00+08:00"), MANILA.minusHours(2));
        assertEquals(MANILA.plusMonths(1), MANILA.plus(1, ChronoUnit.MONTHS));
        assertEquals(MANILA.minusWeeks(1), MANILA.minus(1, ChronoUnit.WEEKS));
        assertEquals(DateTimeWithZone.parse("2024-02-29T00:00:00Z").plusYears(1),
                DateTimeWithZone.parse("2025-02-28T00:00:00Z"));
        assertThrows(ValueObjectException.class, () -> MANILA.plusYears(Long.MAX_VALUE));
        assertThrows(ValueObjectException.class, () -> MANILA.minusDays(Long.MAX_VALUE));
        assertThrows(ValueObjectException.class, () -> MANILA.plus(1, ChronoUnit.FOREVER));
        assertThrows(ValueObjectException.class, () -> MANILA.plus(1, null));
        assertThrows(ValueObjectException.class, () -> MANILA.truncatedTo(ChronoUnit.MONTHS));
        assertEquals(DateTimeWithZone.parse("2026-02-08T14:00:00+08:00"), MANILA.startOfHour());
        assertEquals(DateTimeWithZone.parse("2026-02-08T00:00:00+08:00"), MANILA.startOfDay());
    }

    @Test
    void betweenAndComparisons() {
        DateTimeWithZone later = MANILA.plusHours(30);
        assertEquals(Duration.ofHours(30), MANILA.durationUntil(later));
        assertEquals(30, MANILA.hoursUntil(later));
        assertEquals(1, MANILA.daysUntil(later));
        assertEquals(-30, later.hoursUntil(MANILA));
        assertTrue(MANILA.isBefore(later));
        assertTrue(later.isAfter(MANILA));
        assertTrue(MANILA.plusHours(1).isBetween(MANILA, later));
        assertTrue(MANILA.isBetween(MANILA, later));
        assertFalse(later.plusSeconds(1).isBetween(MANILA, later));
        assertThrows(ValueObjectException.class, () -> MANILA.isBefore(null));
        assertThrows(ValueObjectException.class, () -> MANILA.isAfter(null));
        assertThrows(ValueObjectException.class, () -> MANILA.isEqual(null));
        assertThrows(ValueObjectException.class, () -> MANILA.compareTo(null));
        assertThrows(ValueObjectException.class, () -> MANILA.durationUntil(null));
        assertThrows(ValueObjectException.class, () -> MANILA.until(later, ChronoUnit.FOREVER));
        assertThrows(ValueObjectException.class, () -> MANILA.isBetween(null, later));
        assertThrows(ValueObjectException.class, () -> MANILA.isToday(null));
        assertTrue(DateTimeWithZone.DEFAULT.isPast());
        assertFalse(DateTimeWithZone.DEFAULT.isFuture());
    }

    @Test
    void formatWithFormatter() {
        assertEquals("2026/02/08", MANILA.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        assertThrows(ValueObjectException.class, () -> MANILA.format(null));
    }
}
