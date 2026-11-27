package io.joshuasalcedo.valueobject.core;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import io.joshuasalcedo.valueobject.financial.Percentage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CountTest {

    private static final Count MAX = Count.of(Long.MAX_VALUE);

    @Test
    void parseReadsPlainNumbers() {
        assertEquals(Count.of(42), Count.parse("42"));
        assertEquals(Count.of(7), Count.parse(" 7 "));
        assertSame(Count.ZERO, Count.parse("0"));
        assertEquals(MAX, Count.parse(String.valueOf(Long.MAX_VALUE)));
        assertEquals(Count.of(42), Count.parse(Count.of(42).toString()));
    }

    @Test
    void parseRejectsBadInput() {
        for (String bad : new String[]{"", " ", "-1", "+1", "1.0", "1,000", "abc", "9223372036854775808", "١"}) {
            assertThrows(ValueObjectException.class, () -> Count.parse(bad), bad);
            assertEquals(Optional.empty(), Count.tryParse(bad), bad);
            assertFalse(Count.isValid(bad), bad);
        }
        assertThrows(ValueObjectException.class, () -> Count.parse(null));
        assertEquals(Optional.empty(), Count.tryParse(null));
        assertFalse(Count.isValid(null));
        assertTrue(Count.isValid("12"));
    }

    @Test
    void factories() {
        assertSame(Count.ZERO, Count.of(0));
        assertSame(Count.ONE, Count.of(1L));
        assertSame(Count.ZERO, Count.zero());
        assertSame(Count.ONE, Count.one());
        assertThrows(ValueObjectException.class, () -> Count.of(-1));
        assertThrows(ValueObjectException.class, () -> new Count(-1));
        assertEquals(Count.of(3), Count.ofSize(List.of(1, 2, 3)));
        assertThrows(ValueObjectException.class, () -> Count.ofSize(null));
        assertEquals(Count.of(5), Count.ofLength("hello"));
        assertEquals(Count.ZERO, Count.ofLength(null));
        assertEquals(Count.of(2), Count.countMatching(List.of(1, 2, 3, 4), i -> i % 2 == 0));
        assertEquals(Count.of(1), Count.countMatching(Stream.of("a", "bb"), s -> s.length() == 2));
        assertThrows(ValueObjectException.class, () -> Count.countMatching((List<Integer>) null, i -> true));
        assertThrows(ValueObjectException.class, () -> Count.countMatching(List.of(1), null));
    }

    @Test
    void arithmetic() {
        Count five = Count.of(5);
        assertEquals(Count.of(6), five.increment());
        assertEquals(Count.of(4), five.decrement());
        assertThrows(ValueObjectException.class, () -> Count.ZERO.decrement());
        assertEquals(Count.ZERO, Count.ZERO.decrementOrZero());
        assertEquals(Count.of(8), five.add(Count.of(3)));
        assertEquals(Count.of(2), five.add(-3));
        assertThrows(ValueObjectException.class, () -> five.add(-6));
        assertEquals(Count.of(2), five.subtract(Count.of(3)));
        assertThrows(ValueObjectException.class, () -> five.subtract(Count.of(6)));
        assertEquals(Count.of(7), five.subtract(-2));
        assertEquals(Count.of(15), five.multiply(3));
        assertEquals(Count.ZERO, five.multiply(0));
        assertThrows(ValueObjectException.class, () -> five.multiply(-1));
        assertEquals(Count.ZERO, Count.ZERO.multiply(-1));
        assertEquals(Count.of(2), five.divide(2));
        assertThrows(ValueObjectException.class, () -> five.divide(0));
        assertThrows(ValueObjectException.class, () -> five.divide(-1));
        assertEquals(Count.of(1), five.remainder(2));
        assertEquals(Count.of(1), five.remainder(-2));
        assertThrows(ValueObjectException.class, () -> five.remainder(0));
        assertEquals(five, five.min(Count.of(9)));
        assertEquals(Count.of(9), five.max(Count.of(9)));
    }

    @Test
    void arithmeticRejectsOverflowInsteadOfWrapping() {
        assertThrows(ValueObjectException.class, MAX::increment);
        assertThrows(ValueObjectException.class, () -> MAX.add(Count.ONE));
        assertThrows(ValueObjectException.class, () -> MAX.add(1));
        assertThrows(ValueObjectException.class, () -> MAX.subtract(-1));
        assertThrows(ValueObjectException.class, () -> Count.ONE.subtract(Long.MIN_VALUE));
        // 2^62 * 4 wraps to 0 with plain multiplication
        assertThrows(ValueObjectException.class, () -> Count.of(1L << 62).multiply(4));
        assertThrows(ValueObjectException.class, () -> Count.of(3).multiply(Long.MAX_VALUE));
        assertThrows(ValueObjectException.class, () -> MAX.incrementIf(true));
        assertThrows(ValueObjectException.class, () -> MAX.addIf(true, 1));
        assertEquals(MAX, MAX.addIf(false, 1));
    }

    @Test
    void nullArgumentsThrowValueObjectException() {
        Count c = Count.of(3);
        assertThrows(ValueObjectException.class, () -> c.add(null));
        assertThrows(ValueObjectException.class, () -> c.subtract(null));
        assertThrows(ValueObjectException.class, () -> c.min(null));
        assertThrows(ValueObjectException.class, () -> c.max(null));
        assertThrows(ValueObjectException.class, () -> c.compareTo(null));
        assertThrows(ValueObjectException.class, () -> c.isGreaterThan(null));
        assertThrows(ValueObjectException.class, () -> c.isGreaterThanOrEqual(null));
        assertThrows(ValueObjectException.class, () -> c.isLessThan(null));
        assertThrows(ValueObjectException.class, () -> c.isLessThanOrEqual(null));
        assertThrows(ValueObjectException.class, () -> c.isBetween(null, c));
        assertThrows(ValueObjectException.class, () -> c.percentageOf(null));
        assertThrows(ValueObjectException.class, () -> c.incrementIf((java.util.function.Supplier<Boolean>) null));
        assertThrows(ValueObjectException.class, () -> c.decrementIf((java.util.function.Supplier<Boolean>) null));
        assertThrows(ValueObjectException.class, () -> c.times((Runnable) null));
        assertThrows(ValueObjectException.class, () -> c.times((java.util.function.Consumer<Long>) null));
        assertThrows(ValueObjectException.class, () -> c.toStringWithLabel(null, "x"));
    }

    @Test
    void conditionals() {
        Count c = Count.of(3);
        assertEquals(Count.of(4), c.incrementIf(true));
        assertEquals(c, c.incrementIf(false));
        assertEquals(Count.of(4), c.incrementIf(() -> true));
        assertEquals(c, c.incrementIf(() -> null));
        assertEquals(Count.of(2), c.decrementIf(true));
        assertEquals(Count.of(2), c.decrementIf(() -> Boolean.TRUE));
        assertEquals(c, c.decrementIf(() -> null));
        assertThrows(ValueObjectException.class, () -> Count.ZERO.decrementIf(true));
        assertEquals(Count.of(13), c.addIf(true, 10));
    }

    @Test
    void comparisons() {
        Count c = Count.of(3);
        assertTrue(Count.ZERO.isZero());
        assertTrue(Count.ONE.isOne());
        assertTrue(c.isPositive());
        assertFalse(Count.ZERO.isPositive());
        assertTrue(c.isGreaterThan(Count.of(2)));
        assertTrue(c.isGreaterThan(2));
        assertTrue(c.isGreaterThanOrEqual(Count.of(3)));
        assertTrue(c.isGreaterThanOrEqual(3));
        assertTrue(c.isLessThan(Count.of(4)));
        assertTrue(c.isLessThan(4));
        assertTrue(c.isLessThanOrEqual(Count.of(3)));
        assertTrue(c.isLessThanOrEqual(3));
        assertTrue(c.isBetween(Count.of(3), Count.of(5)));
        assertTrue(c.isBetween(1, 3));
        assertFalse(c.isBetween(4, 5));
        assertTrue(c.compareTo(Count.of(4)) < 0);
    }

    @Test
    void conversions() {
        assertEquals(3, Count.of(3).toInt());
        assertThrows(ValueObjectException.class, () -> Count.of(Integer.MAX_VALUE + 1L).toInt());
        assertEquals(Integer.MAX_VALUE, MAX.toIntClamped());
        assertEquals(Long.MAX_VALUE, MAX.toLong());
    }

    @Test
    void percentageOfIsExactDecimal() {
        assertEquals(0, new BigDecimal("75").compareTo(Count.of(3).percentageOf(Count.of(4)).value()));
        assertEquals(0, new BigDecimal("33.3333").compareTo(Count.of(1).percentageOf(Count.of(3)).value()));
        assertEquals(0, new BigDecimal("150").compareTo(Count.of(3).percentageOf(Count.of(2)).value()));
        assertEquals(Percentage.ZERO, Count.ZERO.percentageOf(Count.of(9)));
        // a double loses digits here: 9007199254740993 / 9007199254740993 * 100 is exact in BigDecimal
        Count big = Count.of(9_007_199_254_740_993L);
        assertEquals(0, new BigDecimal("100").compareTo(big.percentageOf(big).value()));
        assertEquals(0, new BigDecimal("50").compareTo(Count.of(Long.MAX_VALUE / 2 + 1).percentageOf(Count.of(Long.MAX_VALUE)).value().setScale(0, java.math.RoundingMode.HALF_UP)));
        assertThrows(ValueObjectException.class, () -> Count.of(3).percentageOf(Count.ZERO));
    }

    @Test
    void utilities() {
        List<Long> seen = new ArrayList<>();
        Count.of(3).times(seen::add);
        assertEquals(List.of(0L, 1L, 2L), seen);
        AtomicInteger runs = new AtomicInteger();
        Count.of(4).times(runs::incrementAndGet);
        assertEquals(4, runs.get());
        Count.ZERO.times(() -> fail("must not run"));
        assertEquals(List.of(0L, 1L, 2L, 3L), Count.of(4).stream().toList());
        assertEquals("42", Count.of(42).toString());
        assertEquals("1 item", Count.ONE.toStringWithLabel("item", "items"));
        assertEquals("0 items", Count.ZERO.toStringWithLabel("item", "items"));
        assertEquals("5 items", Count.of(5).toStringWithLabel("item", "items"));
    }
}
