package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DurationNanoTest {

    @Test
    void parseReadsIsoDurations() {
        assertEquals(DurationNano.ofMinutes(90), DurationNano.parse("PT1H30M"));
        assertEquals(DurationNano.ofMillis(500), DurationNano.parse(" PT0.5S "));
        assertEquals(DurationNano.ofDays(2), DurationNano.parse("P2D"));
        assertEquals(DurationNano.ZERO, DurationNano.parse("PT0S"));
        DurationNano d = DurationNano.ofHours(26).plus(DurationNano.ofMillis(500));
        assertEquals("PT26H0.5S", d.toIsoString());
        assertEquals(d, DurationNano.parse(d.toIsoString()));
    }

    @Test
    void parseRejectsBadInput() {
        assertThrows(ValueObjectException.class, () -> DurationNano.parse(null));
        assertThrows(ValueObjectException.class, () -> DurationNano.parse(""));
        assertThrows(ValueObjectException.class, () -> DurationNano.parse("1h"));
        assertThrows(ValueObjectException.class, () -> DurationNano.parse("-PT1S"));
        assertThrows(ValueObjectException.class, () -> DurationNano.parse("PT9999999999H"));
        assertEquals(Optional.empty(), DurationNano.tryParse(null));
        assertEquals(Optional.empty(), DurationNano.tryParse("PT-1S"));
        assertEquals(Optional.of(DurationNano.ofSeconds(5)), DurationNano.tryParse("PT5S"));
        assertFalse(DurationNano.isValid(null));
        assertFalse(DurationNano.isValid("PT9999999999H"));
        assertTrue(DurationNano.isValid("PT1M"));
    }

    @Test
    void constructorAndFactoriesRejectNegatives() {
        assertThrows(ValueObjectException.class, () -> new DurationNano(-1));
        assertThrows(ValueObjectException.class, () -> DurationNano.ofNanos(-1));
        assertThrows(ValueObjectException.class, () -> DurationNano.ofSeconds(-1));
        assertThrows(ValueObjectException.class, () -> DurationNano.ofDays(-1));
        assertSame(DurationNano.ZERO, DurationNano.ofNanos(0));
        assertSame(DurationNano.ZERO, DurationNano.zero());
    }

    @Test
    void factoriesRejectOverflowInsteadOfWrapping() {
        // 1e11 days * 8.64e13 ns used to wrap round silently
        assertThrows(ValueObjectException.class, () -> DurationNano.ofDays(200_000));
        assertThrows(ValueObjectException.class, () -> DurationNano.ofHours(Long.MAX_VALUE / 1000));
        assertThrows(ValueObjectException.class, () -> DurationNano.ofMinutes(Long.MAX_VALUE));
        assertThrows(ValueObjectException.class, () -> DurationNano.ofSeconds(10_000_000_000L));
        assertThrows(ValueObjectException.class, () -> DurationNano.ofMillis(Long.MAX_VALUE));
        assertThrows(ValueObjectException.class, () -> DurationNano.ofMicros(Long.MAX_VALUE));
        assertEquals(106_751, DurationNano.ofDays(106_751).toDays());
    }

    @Test
    void fromJavaDuration() {
        assertEquals(DurationNano.ofSeconds(3), DurationNano.from(Duration.ofSeconds(3)));
        assertThrows(ValueObjectException.class, () -> DurationNano.from(null));
        assertThrows(ValueObjectException.class, () -> DurationNano.from(Duration.ofSeconds(-1)));
        // too long for nanoseconds: used to let ArithmeticException through
        assertThrows(ValueObjectException.class, () -> DurationNano.from(Duration.ofDays(1_000_000)));
        assertEquals(Duration.ofMillis(1500), DurationNano.ofMillis(1500).toJavaDuration());
    }

    @Test
    void arithmetic() {
        DurationNano a = DurationNano.ofSeconds(10);
        DurationNano b = DurationNano.ofSeconds(4);
        assertEquals(DurationNano.ofSeconds(14), a.plus(b));
        assertEquals(DurationNano.ofSeconds(6), a.minus(b));
        assertThrows(ValueObjectException.class, () -> b.minus(a));
        assertEquals(DurationNano.ofSeconds(30), a.multipliedBy(3));
        assertEquals(DurationNano.ZERO, a.multipliedBy(0));
        assertThrows(ValueObjectException.class, () -> a.multipliedBy(-1));
        assertEquals(DurationNano.ofMillis(3333), a.dividedBy(3).dividedBy(1_000_000).multipliedBy(1_000_000));
        assertThrows(ValueObjectException.class, () -> a.dividedBy(0));
        assertThrows(ValueObjectException.class, () -> a.dividedBy(-2));
    }

    @Test
    void arithmeticRejectsOverflowAndNull() {
        DurationNano big = DurationNano.ofNanos(Long.MAX_VALUE);
        assertThrows(ValueObjectException.class, () -> big.plus(DurationNano.ofNanos(1)));
        assertThrows(ValueObjectException.class, () -> big.multipliedBy(2));
        assertThrows(ValueObjectException.class, () -> DurationNano.ofDays(100_000).multipliedBy(2));
        DurationNano one = DurationNano.ofSeconds(1);
        assertThrows(ValueObjectException.class, () -> one.plus(null));
        assertThrows(ValueObjectException.class, () -> one.minus(null));
        assertThrows(ValueObjectException.class, () -> one.isGreaterThan(null));
        assertThrows(ValueObjectException.class, () -> one.isLessThan(null));
        assertThrows(ValueObjectException.class, () -> one.compareTo(null));
        assertThrows(ValueObjectException.class, () -> one.to(null));
    }

    @Test
    void conversionsTruncate() {
        DurationNano d = DurationNano.ofNanos(90_061_001_001_001L); // 1d 1h 1m 1s 1ms 1us 1ns
        assertEquals(90_061_001_001_001L, d.toNanos());
        assertEquals(90_061_001_001L, d.toMicros());
        assertEquals(90_061_001L, d.toMillis());
        assertEquals(90_061L, d.toSeconds());
        assertEquals(1_501L, d.toMinutes());
        assertEquals(25L, d.toHours());
        assertEquals(1L, d.toDays());
        assertEquals(25L, d.to(TimeUnit.HOURS));
    }

    @Test
    void formattedShowsSubMillisecondDurations() {
        assertEquals("0s", DurationNano.ZERO.formatted());
        assertEquals("1ns", DurationNano.ofNanos(1).formatted());
        assertEquals("999ns", DurationNano.ofNanos(999).formatted());
        assertEquals("1µs", DurationNano.ofNanos(1_000).formatted());
        assertEquals("250µs", DurationNano.ofNanos(250_400).formatted());
        assertEquals("999µs", DurationNano.ofNanos(999_999).toString());
        assertEquals("1ms", DurationNano.ofNanos(1_000_000).formatted());
    }

    @Test
    void formattedLargerDurations() {
        assertEquals("2h 30m 15s", DurationNano.ofSeconds(2 * 3600 + 30 * 60 + 15).formatted());
        assertEquals("1s 500ms", DurationNano.ofMillis(1500).formatted());
        assertEquals("1d 1h 1m 1s", DurationNano.ofNanos(90_061_001_001_001L).formatted());
        assertEquals("1h", DurationNano.ofHours(1).plus(DurationNano.ofNanos(5)).formatted());
        assertEquals("2d", DurationNano.ofDays(2).formatted());
    }

    @Test
    void comparisons() {
        DurationNano a = DurationNano.ofSeconds(1);
        DurationNano b = DurationNano.ofMillis(1000);
        assertEquals(a, b);
        assertEquals(0, a.compareTo(b));
        assertTrue(DurationNano.ofSeconds(2).isGreaterThan(a));
        assertTrue(DurationNano.ofMillis(999).isLessThan(a));
        assertTrue(DurationNano.ZERO.isZero());
        assertFalse(a.isZero());
    }
}
