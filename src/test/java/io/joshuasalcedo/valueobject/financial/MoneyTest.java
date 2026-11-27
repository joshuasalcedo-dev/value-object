package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    private static Money usd(String amount) {
        return Money.of(amount, FiatCurrency.USD);
    }

    @Nested
    class Creating {

        @ParameterizedTest(name = "{0} {1} -> {2}")
        @CsvSource({
                "149.99, USD, 149.99", "1.005, USD, 1.00", "1.015, USD, 1.02", "1.025, USD, 1.02", "10, USD, 10.00",
                "100.5, JPY, 100", "101.5, JPY, 102", "1.2345, KWD, 1.234", "1.2355, KWD, 1.236",
                "' 7.5 ', EUR, 7.50"
        })
        void roundsHalfEvenToTheCurrencysDecimalPlaces(String amount, String code, String expected) {
            Money m = Money.of(amount, FiatCurrency.parse(code));
            assertEquals(new BigDecimal(expected), m.amount());
            assertEquals(new BigDecimal(expected).scale(), m.amount().scale());
        }

        @Test
        void cryptoKeepsTheCoinsDecimalPlaces() {
            assertEquals(new BigDecimal("0.00542100"), Money.of("0.005421", CryptoCoin.Known.BTC).amount());
            assertEquals(18, Money.of("1", CryptoCoin.Known.ETH).amount().scale());
        }

        @Test
        void fromNumbers() {
            assertEquals(usd("10.00"), Money.of(10, FiatCurrency.USD));
            assertEquals(usd("10.00"), Money.of(10L, FiatCurrency.USD));
            assertEquals(usd("0.10"), Money.of(0.10));
            assertEquals(usd("19.99"), Money.of((Number) 19.99));
            assertEquals(usd("10.00"), Money.of(BigInteger.TEN, FiatCurrency.USD));
            assertEquals(usd("100.00"), Money.of(100L));
            assertEquals(Money.php("1250.00"), Money.php(1250));
            assertEquals(Money.eur("99.50"), Money.of("99.5", FiatCurrency.EUR));
            assertEquals(Money.usd("5"), Money.usd(5));
            assertEquals(Money.eur(3), Money.of(3, FiatCurrency.EUR));
        }

        @Test
        void minorUnits() {
            assertEquals(usd("14.99"), Money.ofMinorUnits(1499, FiatCurrency.USD));
            assertEquals(1499, usd("14.99").toMinorUnits());
            assertEquals(500, Money.of("500", FiatCurrency.JPY).toMinorUnits());
            assertEquals(100_000_000L, Money.of("1", CryptoCoin.Known.BTC).toMinorUnits());
        }

        @Test
        void toMinorUnitsOverflowIsAValueObjectException() {
            Money huge = Money.of(new BigDecimal("1E30"), FiatCurrency.USD);
            assertThrows(ValueObjectException.class, huge::toMinorUnits);
            assertEquals(1_000_000_000_000_000_000L, Money.of("1", CryptoCoin.Known.ETH).toMinorUnits(), "1 ETH is 10^18 wei");
            assertThrows(ValueObjectException.class, () -> Money.of("100", CryptoCoin.Known.ETH).toMinorUnits(),
                    "10^20 wei does not fit in a long");
        }

        @Test
        void zero() {
            assertTrue(Money.zero(FiatCurrency.PHP).isZero());
            assertEquals(Money.zero(FiatCurrency.USD), Money.ZERO);
            assertEquals(usd("-1"), Money.MINUS_ONE);
        }

        @Test
        void badInputIsAValueObjectException() {
            assertThrows(ValueObjectException.class, () -> new Money(null, FiatCurrency.USD));
            assertThrows(ValueObjectException.class, () -> new Money(BigDecimal.ONE, null));
            assertThrows(ValueObjectException.class, () -> Money.of((String) null, FiatCurrency.USD));
            assertThrows(ValueObjectException.class, () -> Money.of("1", null));
            assertThrows(ValueObjectException.class, () -> Money.of("abc", FiatCurrency.USD));
            assertThrows(ValueObjectException.class, () -> Money.of("1,000", FiatCurrency.USD));
            assertThrows(ValueObjectException.class, () -> Money.of((Number) null, FiatCurrency.USD));
            assertThrows(ValueObjectException.class, () -> Money.of(Double.NaN));
            assertThrows(ValueObjectException.class, () -> Money.of(Double.POSITIVE_INFINITY));
            assertThrows(ValueObjectException.class, () -> Money.of(5, null));
            assertThrows(ValueObjectException.class, () -> Money.ofMinorUnits(5, null));
            assertThrows(ValueObjectException.class, () -> Money.zero(null));
            assertThrows(ValueObjectException.class, () -> usd("1").inCurrency(null));
        }
    }

    @Nested
    class Parsing {

        @ParameterizedTest(name = "{0}")
        @CsvSource({
                "USD 149.99, 149.99, USD", "149.99 usd, 149.99, USD", "'PHP 1,250.00', 1250.00, PHP",
                "0.5 BTC, 0.50000000, BTC", "-5 EUR, -5.00, EUR", "EUR-5, -5.00, EUR", "USD.5, 0.50, USD",
                "'  JPY 1,000  ', 1000, JPY", "10 eth, 10, ETH"
        })
        void readsAnAmountWithItsCode(String text, String amount, String code) {
            Money m = Money.parse(text);
            assertEquals(Currency.parse(code), m.currency());
            assertEquals(0, new BigDecimal(amount).compareTo(m.amount()));
            assertEquals(m, Money.tryParse(text).orElseThrow());
            assertTrue(Money.isValid(text));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "abc", "USD", "149.99", "XYZ 10", "XXX 10", "10 USD USD", "$10", "USD 1.2.3",
                "USD --5"})
        void rejectsAnythingElse(String text) {
            assertThrows(ValueObjectException.class, () -> Money.parse(text));
            assertTrue(Money.tryParse(text).isEmpty());
            assertFalse(Money.isValid(text));
        }
    }

    @Nested
    class Arithmetic {

        @Test
        void addAndSubtract() {
            assertEquals(usd("167.99"), usd("149.99").add(usd("18")));
            assertEquals(usd("-0.01"), usd("0.10").subtract(usd("0.11")));
            assertEquals(usd("30"), Money.sum(usd("10"), usd("15"), usd("5")));
            assertEquals(usd("10"), Money.sum(usd("10")));
            assertEquals(usd("6"), Money.sum(List.of(usd("1"), usd("2"), usd("3")), FiatCurrency.USD));
            assertEquals(Money.zero(FiatCurrency.EUR), Money.sum(List.of(), FiatCurrency.EUR));
        }

        @Test
        void mixedCurrenciesAreRejected() {
            Money dollars = usd("1");
            Money pesos = Money.php("1");
            assertThrows(ValueObjectException.class, () -> dollars.add(pesos));
            assertThrows(ValueObjectException.class, () -> dollars.subtract(pesos));
            assertThrows(ValueObjectException.class, () -> dollars.compareTo(pesos));
            assertThrows(ValueObjectException.class, () -> dollars.isGreaterThan(pesos));
            assertThrows(ValueObjectException.class, () -> dollars.min(pesos));
            assertThrows(ValueObjectException.class, () -> dollars.isBetween(pesos, pesos));
            assertThrows(ValueObjectException.class, () -> Money.sum(dollars, pesos));
            assertThrows(ValueObjectException.class, () -> Money.sum(List.of(pesos), FiatCurrency.USD));
            assertThrows(ValueObjectException.class, () -> dollars.add(Money.of("1", CryptoCoin.Known.BTC)));
            assertFalse(dollars.isSameCurrency(pesos));
            assertTrue(dollars.isSameCurrency(usd("7")));
        }

        @Test
        void nullOperandsAreValueObjectExceptions() {
            Money m = usd("1");
            assertThrows(ValueObjectException.class, () -> m.add(null));
            assertThrows(ValueObjectException.class, () -> m.subtract(null));
            assertThrows(ValueObjectException.class, () -> m.multiply((BigDecimal) null));
            assertThrows(ValueObjectException.class, () -> m.divide((BigDecimal) null));
            assertThrows(ValueObjectException.class, () -> m.percentage((BigDecimal) null));
            assertThrows(ValueObjectException.class, () -> m.percentage((Percentage) null));
            assertThrows(ValueObjectException.class, () -> m.compareTo(null));
            assertThrows(ValueObjectException.class, () -> m.isSameCurrency(null));
            assertThrows(ValueObjectException.class, () -> Money.sum(null));
            assertThrows(ValueObjectException.class, () -> Money.sum(null, FiatCurrency.USD));
        }

        @Test
        void multiplyRoundsHalfEven() {
            assertEquals(usd("15.00"), usd("10").multiply(new BigDecimal("1.5")));
            assertEquals(usd("3.33"), usd("10").multiply(new BigDecimal("0.333")));
            assertEquals(usd("0.02"), usd("0.05").multiply(new BigDecimal("0.5")), "0.025 rounds to even");
            assertEquals(usd("30"), usd("10").multiply(3));
            assertEquals(usd("-30"), usd("10").multiply(-3L));
        }

        @Test
        void divideRoundsHalfEven() {
            assertEquals(usd("3.33"), usd("10").divide(3));
            assertEquals(usd("6.67"), usd("20").divide(3L));
            assertEquals(usd("0.12"), usd("0.25").divide(new BigDecimal("2")), "0.125 rounds to even");
            assertThrows(ValueObjectException.class, () -> usd("10").divide(0));
            assertThrows(ValueObjectException.class, () -> usd("10").divide(BigDecimal.ZERO));
        }

        @Test
        void signs() {
            assertEquals(usd("-100"), usd("100").negate());
            assertEquals(usd("50"), usd("-50").abs());
            assertTrue(usd("1").isPositive());
            assertTrue(usd("-1").isNegative());
            assertTrue(usd("0").isZero());
        }

        @Test
        void percentages() {
            assertEquals(usd("20.00"), usd("200").percentage(BigDecimal.TEN));
            assertEquals(usd("18.00"), usd("149.99").percentage(Percentage.of(12)));
            assertEquals(usd("25.00"), usd("200").percentage(new BigDecimal("12.5")));
            assertEquals(usd("110.00"), usd("100").addPercentage(BigDecimal.TEN));
            assertEquals(usd("110.00"), usd("100").addPercentage(Percentage.TEN));
            assertEquals(usd("90.00"), usd("100").subtractPercentage(BigDecimal.TEN));
            assertEquals(usd("90.00"), usd("100").subtractPercentage(Percentage.TEN));
        }
    }

    @Nested
    class Allocation {

        @Test
        void splitGivesTheRemainderToTheFirstPart() {
            assertArrayEquals(new Money[]{usd("33.34"), usd("33.33"), usd("33.33")}, usd("100").split(3));
            assertArrayEquals(new Money[]{usd("66.66"), usd("66.67"), usd("66.67")}, usd("200").split(3),
                    "66.67 x 3 overshoots, so the first part gives a cent back");
            assertArrayEquals(new Money[]{usd("5")}, usd("5").split(1));
        }

        @ParameterizedTest(name = "{0} split {1}")
        @CsvSource({"100, 3", "200, 3", "0.05, 3", "0.01, 7", "1000000, 9", "-10, 3", "0, 4"})
        void splitPartsAlwaysAddUp(String amount, int parts) {
            Money total = usd(amount);
            Money[] shares = total.split(parts);
            assertEquals(parts, shares.length);
            assertEquals(total, Money.sum(Arrays.asList(shares), FiatCurrency.USD));
        }

        @Test
        void splitIntoNoPartsIsRejected() {
            assertThrows(ValueObjectException.class, () -> usd("1").split(0));
            assertThrows(ValueObjectException.class, () -> usd("1").split(-2));
        }

        @Test
        void allocateByRatios() {
            assertArrayEquals(new Money[]{usd("16.67"), usd("33.33"), usd("50.00")}, usd("100").allocate(1, 2, 3));
            assertArrayEquals(new Money[]{usd("0.02"), usd("0.02"), usd("0.01")}, usd("0.05").allocate(1, 1, 1));
            assertArrayEquals(new Money[]{usd("0"), usd("10")}, usd("10").allocate(0, 1));
            assertArrayEquals(new Money[]{Money.of("33", FiatCurrency.JPY), Money.of("67", FiatCurrency.JPY)},
                    Money.of("100", FiatCurrency.JPY).allocate(1, 2));
        }

        @ParameterizedTest(name = "{0}")
        @CsvSource({"100", "0.07", "99999.99", "1"})
        void allocatedSharesAlwaysAddUp(String amount) {
            Money total = usd(amount);
            assertEquals(total, Money.sum(Arrays.asList(total.allocate(3, 7, 11, 13)), FiatCurrency.USD));
        }

        @Test
        void badRatiosAreRejected() {
            assertThrows(ValueObjectException.class, () -> usd("1").allocate());
            assertThrows(ValueObjectException.class, () -> usd("1").allocate((int[]) null));
            assertThrows(ValueObjectException.class, () -> usd("1").allocate(1, -1));
            assertThrows(ValueObjectException.class, () -> usd("1").allocate(0, 0));
        }
    }

    @Nested
    class Comparing {

        @Test
        void ordering() {
            assertTrue(usd("2").isGreaterThan(usd("1")));
            assertTrue(usd("2").isGreaterThanOrEqual(usd("2")));
            assertTrue(usd("1").isLessThan(usd("2")));
            assertTrue(usd("2").isLessThanOrEqual(usd("2")));
            assertEquals(usd("1"), usd("1").min(usd("2")));
            assertEquals(usd("2"), usd("1").max(usd("2")));
            assertTrue(usd("5").isBetween(usd("5"), usd("10")));
            assertFalse(usd("11").isBetween(usd("5"), usd("10")));
            assertTrue(usd("1").compareTo(usd("2")) < 0);
        }

        @Test
        void equalityIgnoresScaleButNotCurrency() {
            assertEquals(Money.of(new BigDecimal("10.0"), FiatCurrency.USD), Money.of(new BigDecimal("10.000"), FiatCurrency.USD));
            assertEquals(Money.of(new BigDecimal("10.0"), FiatCurrency.USD).hashCode(),
                    Money.of(new BigDecimal("10.000"), FiatCurrency.USD).hashCode());
            assertNotEquals(usd("10"), Money.of("10", FiatCurrency.EUR));
            assertNotEquals(usd("10"), "10 USD");
        }

        @Test
        void redenominate() {
            assertEquals(Money.of("10", FiatCurrency.JPY), usd("10.50").inCurrency(FiatCurrency.JPY), "10.5 rounds to even");
            assertEquals(new BigDecimal("10.50"), usd("10.50").toBigDecimal());
        }
    }

    @Nested
    class Formatting {

        @Test
        void plainForms() {
            assertEquals("$ 149.99", usd("149.99").toString());
            assertEquals("149.99 USD", usd("149.99").toCodeString());
            assertEquals("0.50000000 BTC", Money.of("0.5", CryptoCoin.Known.BTC).toCodeString());
        }

        @Test
        void fiatInALocalesStyle() {
            assertEquals("$1,234,567.89", usd("1234567.89").format(Locale.US));
            assertEquals("-$5.00", usd("-5").format(Locale.US));
            assertTrue(Money.eur("1234.56").format(Locale.GERMANY).contains("1.234,56"));
            assertTrue(Money.of("1000", FiatCurrency.JPY).format(Locale.US).endsWith("1,000"));
            assertTrue(Money.of("1.5", FiatCurrency.KWD).format(Locale.US).endsWith("1.500"));
        }

        @Test
        void cryptoFormatsWithItsSymbolAndDecimalPlacesInsteadOfThrowing() {
            assertEquals("₿0.00542100", Money.of("0.005421", CryptoCoin.Known.BTC).format(Locale.US));
            assertEquals("-₿1.00000000", Money.of("-1", CryptoCoin.Known.BTC).format(Locale.US));
            assertEquals("₿1,234.50000000", Money.of("1234.5", CryptoCoin.Known.BTC).format(Locale.US));
            assertEquals("₿1.234,50000000", Money.of("1234.5", CryptoCoin.Known.BTC).format(Locale.GERMANY));
            assertEquals("1.500000000 SOL", Money.of("1.5", CryptoCoin.Known.SOL).format(Locale.US));
            assertEquals("Ξ12,345.500000000000000000", Money.of("12345.5", CryptoCoin.Known.ETH).format(Locale.US));
            CryptoCoin pepe = CryptoCoin.custom("PEPE", "Pepe", "PEPE", 2);
            assertEquals("3.00 PEPE", Money.of("3", pepe).format(Locale.US));
            assertThrows(ValueObjectException.class, () -> usd("1").format(null));
        }
    }
}
