package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.core.Count;
import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PercentageTest {

    private static void assertNumber(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "expected " + expected + " but was " + actual);
    }

    private static void assertNumber(String expected, BigDecimal actual, String message) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> message + ": expected " + expected + " but was " + actual);
    }

    @Nested
    class Parsing {

        @ParameterizedTest(name = "\"{0}\" -> {1}")
        @CsvSource({"12.5%, 12.5", "12.5, 12.5", "' 12.5 % ', 12.5", "-3%, -3", "150%, 150", "0%, 0", "0.00006%, 0.0001", "0.00005%, 0",
                "0.00004, 0", "12.34565, 12.3456", "1e2%, 100"})
        void readsANumberWithOrWithoutPercent(String text, String value) {
            Percentage p = Percentage.parse(text);
            assertNumber(value, p.value());
            assertEquals(4, p.value().scale(), "held at 4 decimal places");
            assertEquals(p, Percentage.tryParse(text).orElseThrow());
            assertTrue(Percentage.isValid(text));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "%", "abc", "12.5%%", "12,5%", "% 12", "twelve"})
        void rejectsAnythingElse(String text) {
            assertThrows(ValueObjectException.class, () -> Percentage.parse(text));
            assertTrue(Percentage.tryParse(text).isEmpty());
            assertFalse(Percentage.isValid(text));
        }

        @Test
        void badFactoryInputIsAValueObjectException() {
            assertThrows(ValueObjectException.class, () -> new Percentage(null));
            assertThrows(ValueObjectException.class, () -> Percentage.of((BigDecimal) null));
            assertThrows(ValueObjectException.class, () -> Percentage.of((String) null));
            assertThrows(ValueObjectException.class, () -> Percentage.of(Double.NaN));
            assertThrows(ValueObjectException.class, () -> Percentage.of(Double.NEGATIVE_INFINITY));
            assertThrows(ValueObjectException.class, () -> Percentage.fromDecimal((BigDecimal) null));
            assertThrows(ValueObjectException.class, () -> Percentage.fromDecimal(Double.NaN));
            assertThrows(ValueObjectException.class, () -> Percentage.fromFraction(1, 0));
            assertThrows(ValueObjectException.class, () -> Percentage.fromRatio(Count.of(1), Count.of(0)));
            assertThrows(ValueObjectException.class, () -> Percentage.fromRatio(null, Count.of(1)));
        }

        @Test
        void factories() {
            assertSame(Percentage.ZERO, Percentage.of(0));
            assertSame(Percentage.HUNDRED, Percentage.of(new BigDecimal("100.000")));
            assertEquals(Percentage.ZERO, Percentage.zero());
            assertEquals(Percentage.HUNDRED, Percentage.hundred());
            assertNumber("12.5", Percentage.of(12.5).value());
            assertNumber("25", Percentage.fromDecimal(0.25).value());
            assertNumber("12.3456", Percentage.fromDecimal(new BigDecimal("0.123456")).value());
            assertNumber("75", Percentage.fromFraction(3, 4).value());
            assertNumber("33.3333", Percentage.fromFraction(1, 3).value());
            assertNumber("66.6667", Percentage.fromFraction(2, 3).value());
            assertNumber("70", Percentage.fromRatio(Count.of(7), Count.of(10)).value());
        }
    }

    @Nested
    class Precision {

        @Test
        void toDecimalKeepsEveryDigit() {
            assertNumber("0.123456", Percentage.of("12.3456").toDecimal());
            assertNumber("0.25", Percentage.of(25).toDecimal());
            assertNumber("0.000001", Percentage.of("0.0001").toDecimal());
            assertNumber("0.00004", Percentage.of("0.004").toDecimal());
            assertTrue(Percentage.of("0.004").toDecimal().signum() > 0, "was rounded to 0 before");
        }

        @Test
        void applyToUsesTheFullPercentage() {
            assertNumber("12.3456", Percentage.of("12.3456").applyTo(new BigDecimal("100")),
                    "acted as 12.35% before");
            assertNumber("0.04", Percentage.of("0.004").applyTo(new BigDecimal("1000")), "was 0 before");
            assertNumber("24", Percentage.parse("12%").applyTo(new BigDecimal("200")));
            assertNumber("50", Percentage.of(25).valueOfTotal(new BigDecimal("200")));
            assertNumber("12.3456", Percentage.of("12.3456").valueOfTotal(new BigDecimal("100")));
            assertEquals(50.0, Percentage.of(25).applyTo(200.0), 1e-12);
        }

        @Test
        void totalFromValue() {
            assertNumber("200", Percentage.of(25).totalFromValue(new BigDecimal("50")));
            assertEquals(4, Percentage.of(25).totalFromValue(new BigDecimal("50")).scale());
            assertNumber("25000", Percentage.of("0.004").totalFromValue(BigDecimal.ONE),
                    "threw ArithmeticException (divide by zero) before");
            assertNumber("405.0836", Percentage.of("12.3456").totalFromValue(new BigDecimal("50.01")));
        }

        @Test
        void totalFromAZeroPercentageIsAValueObjectException() {
            assertThrows(ValueObjectException.class, () -> Percentage.ZERO.totalFromValue(BigDecimal.ONE));
            assertThrows(ValueObjectException.class, () -> Percentage.of("0.00004").totalFromValue(BigDecimal.ONE),
                    "rounds to 0.0000% when made");
            assertThrows(ValueObjectException.class, () -> Percentage.TEN.totalFromValue(null));
        }

        @Test
        void applyToWholeAmountsRoundsHalvesAwayFromZero() {
            assertEquals(3, Percentage.of(25).applyTo(10L));
            assertEquals(-3, Percentage.of(25).applyTo(-10L));
            assertEquals(2, Percentage.of(25).applyTo(9L));
            assertEquals(Long.MAX_VALUE / 2 + 0, Percentage.FIFTY.applyTo(Long.MAX_VALUE - 1));
            assertThrows(ValueObjectException.class, () -> Percentage.of(200).applyTo(Long.MAX_VALUE));
        }

        @Test
        void applyToMoney() {
            assertEquals(Money.usd("24.00"), Percentage.of(12).applyTo(Money.usd(200)));
            assertThrows(ValueObjectException.class, () -> Percentage.TEN.applyTo((Money) null));
            assertThrows(ValueObjectException.class, () -> Percentage.TEN.applyTo((BigDecimal) null));
        }
    }

    @Nested
    class Arithmetic {

        @Test
        void pointsArithmetic() {
            assertEquals(Percentage.of(15), Percentage.TEN.add(Percentage.of(5)));
            assertEquals(Percentage.of(15), Percentage.TEN.add(5.0));
            assertEquals(Percentage.of(-5), Percentage.of(5).subtract(Percentage.TEN));
            assertEquals(Percentage.of(10), Percentage.of(15).subtract(5.0));
            assertEquals(Percentage.of(15), Percentage.TEN.multiply(1.5));
            assertEquals(Percentage.of(15), Percentage.TEN.multiply(new BigDecimal("1.5")));
            assertEquals(Percentage.of(5), Percentage.of(15).divide(3.0));
            assertEquals(Percentage.of("3.3333"), Percentage.TEN.divide(new BigDecimal("3")));
            assertEquals(Percentage.of(-10), Percentage.TEN.negate());
            assertEquals(Percentage.TEN, Percentage.of(-10).abs());
            assertEquals(Percentage.of(70), Percentage.of(30).complement());
            assertEquals(Percentage.of(-20), Percentage.of(120).complement());
            assertEquals(Percentage.TEN, Percentage.TEN.min(Percentage.FIFTY));
            assertEquals(Percentage.FIFTY, Percentage.TEN.max(Percentage.FIFTY));
        }

        @Test
        void badOperandsAreValueObjectExceptions() {
            assertThrows(ValueObjectException.class, () -> Percentage.TEN.divide(0.0));
            assertThrows(ValueObjectException.class, () -> Percentage.TEN.divide(BigDecimal.ZERO));
            assertThrows(ValueObjectException.class, () -> Percentage.TEN.divide((BigDecimal) null));
            assertThrows(ValueObjectException.class, () -> Percentage.TEN.add((Percentage) null));
            assertThrows(ValueObjectException.class, () -> Percentage.TEN.add(Double.NaN));
            assertThrows(ValueObjectException.class, () -> Percentage.TEN.subtract((Percentage) null));
            assertThrows(ValueObjectException.class, () -> Percentage.TEN.multiply((BigDecimal) null));
            assertThrows(ValueObjectException.class, () -> Percentage.TEN.compareTo(null));
            assertThrows(ValueObjectException.class, () -> Percentage.TEN.isGreaterThan((Percentage) null));
            assertThrows(ValueObjectException.class, () -> Percentage.TEN.isGreaterThan(Double.NaN));
            assertThrows(ValueObjectException.class, () -> Percentage.TEN.isBetween(null, Percentage.HUNDRED));
            assertThrows(ValueObjectException.class, () -> Percentage.TEN.min(null));
        }
    }

    @Nested
    class Comparing {

        @Test
        void comparisons() {
            assertTrue(Percentage.TEN.isGreaterThan(Percentage.ONE));
            assertTrue(Percentage.TEN.isGreaterThan(9.99));
            assertTrue(Percentage.TEN.isGreaterThanOrEqual(Percentage.TEN));
            assertTrue(Percentage.ONE.isLessThan(Percentage.TEN));
            assertTrue(Percentage.ONE.isLessThan(1.01));
            assertTrue(Percentage.ONE.isLessThanOrEqual(Percentage.ONE));
            assertTrue(Percentage.FIFTY.isBetween(Percentage.FIFTY, Percentage.HUNDRED));
            assertTrue(Percentage.FIFTY.isBetween(0, 50));
            assertTrue(Percentage.HUNDRED.isWithinRange());
            assertFalse(Percentage.of(-1).isWithinRange());
            assertFalse(Percentage.of(101).isWithinRange());
            assertTrue(Percentage.ZERO.isZero());
            assertTrue(Percentage.HUNDRED.isHundred());
            assertTrue(Percentage.of(-1).isNegative());
            assertTrue(Percentage.ONE.isPositive());
            assertTrue(Percentage.ONE.compareTo(Percentage.TEN) < 0);
        }

        @Test
        void equalityIgnoresScale() {
            assertEquals(Percentage.of(12.5), Percentage.parse("12.50"));
            assertEquals(Percentage.of(12.5).hashCode(), Percentage.parse("12.50").hashCode());
            assertEquals(Percentage.of("12.34561"), Percentage.of("12.3456"), "both held at 12.3456");
            assertNotEquals(Percentage.of(12.5), "12.5%");
        }
    }

    @Nested
    class Formatting {

        @Test
        void text() {
            assertEquals("12.50%", Percentage.of(12.5).toString());
            assertEquals("12.50%", Percentage.of(12.5).format());
            assertEquals("12.5%", Percentage.of(12.5).format(1));
            assertEquals("12%", Percentage.of(12.5).format(0), "half-even");
            assertEquals("+12.50%", Percentage.of(12.5).formatWithSign());
            assertEquals("-3.00%", Percentage.of(-3).formatWithSign());
            assertEquals("0.00%", Percentage.ZERO.formatWithSign());
            assertThrows(ValueObjectException.class, () -> Percentage.TEN.format(-1));
        }

        @Test
        void conversions() {
            assertEquals(0.25, Percentage.of(25).toDecimalDouble(), 1e-12);
            assertEquals(25.0, Percentage.of(25).toDouble(), 1e-12);
            assertEquals(12, Percentage.of(12.5).toIntRounded());
            assertEquals(14, Percentage.of(13.5).toIntRounded());
            assertThrows(ValueObjectException.class, () -> Percentage.of(new BigDecimal("1E12")).toIntRounded());
        }

        @Test
        void progressBars() {
            assertEquals("█████░░░░░", Percentage.FIFTY.toProgressBar(10));
            assertEquals("█".repeat(10), Percentage.of(150).toProgressBar(10));
            assertEquals("░".repeat(4), Percentage.of(-20).toProgressBar(4));
            assertEquals("", Percentage.FIFTY.toProgressBar(0));
            assertEquals("█████░░░░░ 50.0%", Percentage.FIFTY.toProgressBarWithLabel(10));
            assertThrows(ValueObjectException.class, () -> Percentage.FIFTY.toProgressBar(-1));
        }
    }
}
