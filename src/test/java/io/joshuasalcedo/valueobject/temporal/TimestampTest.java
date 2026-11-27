package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

class TimestampTest {

    private static final Instant I = Instant.parse("2025-12-24T00:27:00Z");
    private static final Timestamp T = Timestamp.of(I);
    private static final ZoneId MANILA = ZoneId.of("Asia/Manila");

    @Test
    void parseReadsIsoInstants() {
        assertEquals(T, Timestamp.parse("2025-12-24T00:27:00Z"));
        assertEquals(T, Timestamp.parse("  2025-12-24T08:27:00+08:00 "));
        assertEquals(Instant.parse("2025-01-25T10:30:00.123Z"), Timestamp.parse("2025-01-25T10:30:00.123Z").value());
    }

    @Test
    void parseRejectsBadInputWithValueObjectException() {
        assertThrows(ValueObjectException.class, () -> Timestamp.parse(null));
        assertThrows(ValueObjectException.class, () -> Timestamp.parse(""));
        assertThrows(ValueObjectException.class, () -> Timestamp.parse("2025-12-24"));
        assertThrows(ValueObjectException.class, () -> Timestamp.parse("2025-12-24T00:27:00"));
        assertThrows(ValueObjectException.class, () -> Timestamp.parse("December 24, 2025"));
        assertEquals(Optional.empty(), Timestamp.tryParse(null));
        assertEquals(Optional.empty(), Timestamp.tryParse("nope"));
        assertEquals(Optional.of(T), Timestamp.tryParse("2025-12-24T00:27:00Z"));
        assertFalse(Timestamp.isValid(null));
        assertFalse(Timestamp.isValid("2025-13-01T00:00:00Z"));
        assertTrue(Timestamp.isValid("2025-12-01T00:00:00Z"));
    }

    @Test
    void toStringIsTheUtcIsoInstantWhateverTheMachine() {
        Locale savedLocale = Locale.getDefault();
        TimeZone savedZone = TimeZone.getDefault();
        try {
            Locale.setDefault(Locale.JAPAN);
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
            assertEquals("2025-12-24T00:27:00Z", T.toString());
            assertEquals(T.toIsoString(), T.toString());
            assertEquals(T, Timestamp.parse(T.toString()));
            // human form is English even under another default locale
            assertEquals("December 24, 2025 8:27:00 AM", T.toHumanReadable(MANILA));
        } finally {
            Locale.setDefault(savedLocale);
            TimeZone.setDefault(savedZone);
        }
    }

    @Test
    void factoriesRejectBadInputWithValueObjectException() {
        assertThrows(ValueObjectException.class, () -> new Timestamp(null));
        assertThrows(ValueObjectException.class, () -> Timestamp.of((Instant) null));
        assertThrows(ValueObjectException.class, () -> Timestamp.of((ZonedDateTime) null));
        assertThrows(ValueObjectException.class, () -> Timestamp.of((LocalDateTime) null, MANILA));
        assertThrows(ValueObjectException.class, () -> Timestamp.of(LocalDateTime.of(2025, 1, 1, 0, 0), null));
        assertThrows(ValueObjectException.class, () -> Timestamp.of((LocalDate) null, MANILA));
        assertThrows(ValueObjectException.class, () -> Timestamp.ofEpochSecond(Long.MAX_VALUE));
    }

    @Test
    void factories() {
        assertEquals(Timestamp.epoch(), Timestamp.ofEpochSecond(0));
        assertEquals(Timestamp.ofEpochSecond(1706140800L), Timestamp.ofEpochSecond(1706140800));
        assertEquals(Timestamp.ofEpochMilli(1500), Timestamp.of(Instant.ofEpochMilli(1500)));
        assertEquals(T, Timestamp.of(LocalDateTime.of(2025, 12, 24, 8, 27), MANILA));
        assertEquals(Timestamp.parse("2025-12-23T16:00:00Z"), Timestamp.of(LocalDate.of(2025, 12, 24), MANILA));
        assertEquals(T, Timestamp.of(I.atZone(MANILA)));
    }

    @Test
    void comparisons() {
        Timestamp later = T.plusHours(1);
        assertTrue(T.isBefore(later));
        assertTrue(later.isAfter(T));
        assertTrue(T.isBetween(T, later));
        assertFalse(later.plusSeconds(1).isBetween(T, later));
        assertTrue(T.compareTo(later) < 0);
        assertEquals(T, T.min(later));
        assertEquals(later, T.max(later));
        assertTrue(Timestamp.epoch().isPast());
        assertFalse(Timestamp.epoch().isFuture());
        assertThrows(ValueObjectException.class, () -> T.isBefore(null));
        assertThrows(ValueObjectException.class, () -> T.isAfter(null));
        assertThrows(ValueObjectException.class, () -> T.isBetween(null, later));
        assertThrows(ValueObjectException.class, () -> T.compareTo(null));
        assertThrows(ValueObjectException.class, () -> T.min(null));
        assertThrows(ValueObjectException.class, () -> T.max(null));
    }

    @Test
    void sameDayDependsOnTheZone() {
        Timestamp a = Timestamp.parse("2025-12-24T15:00:00Z");
        Timestamp b = Timestamp.parse("2025-12-24T17:00:00Z");
        assertTrue(a.isSameDay(b, ZoneOffset.UTC));
        assertFalse(a.isSameDay(b, MANILA)); // 23:00 and 01:00 next day
        assertThrows(ValueObjectException.class, () -> a.isSameDay(null, MANILA));
        assertThrows(ValueObjectException.class, () -> a.isSameDay(b, null));
    }

    @Test
    void arithmetic() {
        assertEquals(Timestamp.parse("2025-12-25T00:27:00Z"), T.plusDays(1));
        assertEquals(Timestamp.parse("2025-12-24T01:27:00Z"), T.plusMinutes(60));
        assertEquals(Timestamp.parse("2025-12-24T00:27:30Z"), T.plusSeconds(30));
        assertEquals(Timestamp.parse("2025-12-23T23:27:00Z"), T.minusHours(1));
        assertEquals(Timestamp.parse("2025-12-24T00:26:00Z"), T.minusMinutes(1));
        assertEquals(Timestamp.parse("2025-12-24T00:26:59Z"), T.minusSeconds(61).plusSeconds(60));
        assertEquals(Timestamp.parse("2025-12-17T00:27:00Z"), T.minusDays(7));
        assertEquals(T.plusHours(2), T.plus(Duration.ofHours(2)));
        assertEquals(T.minusHours(2), T.minus(Duration.ofHours(2)));
        assertThrows(ValueObjectException.class, () -> T.plusDays(Long.MAX_VALUE));
        assertThrows(ValueObjectException.class, () -> T.minusSeconds(Long.MAX_VALUE));
        assertThrows(ValueObjectException.class, () -> T.plus(null));
        assertThrows(ValueObjectException.class, () -> T.minus(null));
    }

    @Test
    void durations() {
        Timestamp later = T.plusDays(2).plusHours(3);
        assertEquals(Duration.ofHours(51), T.durationUntil(later));
        assertEquals(51 * 3600, T.secondsUntil(later));
        assertEquals(2, T.daysUntil(later));
        assertEquals(-2, later.daysUntil(T));
        assertThrows(ValueObjectException.class, () -> T.durationUntil(null));
        assertThrows(ValueObjectException.class, () -> T.secondsUntil(null));
        assertThrows(ValueObjectException.class, () -> T.daysUntil(null));
        assertFalse(Timestamp.epoch().elapsed().isNegative());
    }

    @Test
    void conversions() {
        assertEquals(I.getEpochSecond(), T.toEpochSecond());
        assertEquals(I.toEpochMilli(), T.toEpochMilli());
        assertEquals(I, T.toInstant());
        assertEquals(LocalDateTime.of(2025, 12, 24, 8, 27), T.toLocalDateTime(MANILA));
        assertEquals(LocalDate.of(2025, 12, 23), T.toLocalDate(ZoneId.of("America/New_York")));
        assertEquals(MANILA, T.toZonedDateTime(MANILA).getZone());
        assertThrows(ValueObjectException.class, () -> T.toZonedDateTime(null));
        assertThrows(ValueObjectException.class, () -> Timestamp.of(Instant.MAX).toEpochMilli());
        assertThrows(ValueObjectException.class, () -> Timestamp.of(Instant.MAX).toZonedDateTime(MANILA));
    }

    @Test
    void formatting() {
        assertEquals("2025-12-24", T.toDateString(MANILA));
        assertEquals("2025-12-23", T.toDateString(ZoneId.of("America/New_York")));
        assertEquals("2025-12-24 08:27:00", T.toCompactString(MANILA));
        assertEquals("24/12/2025", T.toHumanReadable(MANILA, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        assertThrows(ValueObjectException.class, () -> T.toHumanReadable(null));
        assertThrows(ValueObjectException.class, () -> T.toHumanReadable(MANILA, null));
        assertThrows(ValueObjectException.class, () -> T.toDateString(null));
        assertThrows(ValueObjectException.class, () -> T.toCompactString(null));
    }

    @Test
    void relativeStringFromAFixedMoment() {
        assertEquals("just now", T.toRelativeString(I.plusSeconds(59)));
        assertEquals("just now", T.toRelativeString(I.minusSeconds(30)));
        assertEquals("1 minute ago", T.toRelativeString(I.plusSeconds(60)));
        assertEquals("5 minutes ago", T.toRelativeString(I.plusSeconds(5 * 60 + 10)));
        assertEquals("2 hours ago", T.toRelativeString(I.plusSeconds(2 * 3600)));
        assertEquals("1 day ago", T.toRelativeString(I.plusSeconds(86400)));
        assertEquals("3 days ago", T.toRelativeString(I.plusSeconds(3 * 86400)));
        assertEquals("2 months ago", T.toRelativeString(I.plusSeconds(61L * 86400)));
        assertEquals("1 year ago", T.toRelativeString(I.plusSeconds(400L * 86400)));
        assertEquals("in 2 hours", T.toRelativeString(I.minusSeconds(2 * 3600)));
        assertThrows(ValueObjectException.class, () -> T.toRelativeString(null));
    }
}
