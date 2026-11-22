package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.geo.Country;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyTest {

    @Nested
    class Fiat {

        @ParameterizedTest(name = "\"{0}\" -> {1}")
        @CsvSource({"PHP, PHP", "php, PHP", "' usd ', USD", "Eur, EUR", "jpy, JPY", "kwd, KWD"})
        void parsesIso4217Codes(String text, FiatCurrency expected) {
            assertEquals(expected, FiatCurrency.parse(text));
            assertEquals(expected, FiatCurrency.tryParse(text).orElseThrow());
            assertTrue(FiatCurrency.isValid(text));
            assertTrue(FiatCurrency.isValidCode(text));
            assertEquals(expected, FiatCurrency.fromCode(text));
            assertEquals(expected, FiatCurrency.findByCode(text).orElseThrow());
        }

        @ParameterizedTest(name = "\"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "XXX", "ZZZ", "US", "USDD", "BTC", "dollar"})
        void rejectsOtherCodes(String text) {
            assertThrows(ValueObjectException.class, () -> FiatCurrency.parse(text));
            assertTrue(FiatCurrency.tryParse(text).isEmpty());
            assertFalse(FiatCurrency.isValid(text));
            assertFalse(FiatCurrency.isValidCode(text));
            assertTrue(FiatCurrency.findByCode(text).isEmpty());
        }

        @Test
        void unknownPlaceholder() {
            assertEquals(FiatCurrency.UNKNOWN, FiatCurrency.fromCode(null));
            assertEquals(FiatCurrency.UNKNOWN, FiatCurrency.fromCode("nope"));
            assertEquals(FiatCurrency.UNKNOWN, FiatCurrency.defaultCurrency());
            assertTrue(FiatCurrency.UNKNOWN.isUnknown());
            assertEquals(FiatCurrency.UNKNOWN, FiatCurrency.fromNumericCode(-1));
        }

        @Test
        void codesIgnoreTheDefaultLocale() {
            Locale before = Locale.getDefault();
            try {
                Locale.setDefault(Locale.forLanguageTag("tr-TR"));
                assertEquals(FiatCurrency.IDR, FiatCurrency.parse("idr"), "Turkish upper-cases i to a dotted capital I");
                assertEquals(FiatCurrency.CHF, FiatCurrency.fromCode("chf"));
            } finally {
                Locale.setDefault(before);
            }
        }

        @Test
        void properties() {
            assertEquals("USD", FiatCurrency.USD.code());
            assertEquals(840, FiatCurrency.USD.numericCode());
            assertEquals(FiatCurrency.USD, FiatCurrency.fromNumericCode(840));
            assertEquals("US Dollar", FiatCurrency.USD.displayName());
            assertEquals("$", FiatCurrency.USD.symbol());
            assertEquals(2, FiatCurrency.USD.decimalPlaces());
            assertTrue(FiatCurrency.USD.hasMinorUnit());
            assertTrue(FiatCurrency.JPY.isZeroDecimal());
            assertFalse(FiatCurrency.UNKNOWN.isZeroDecimal());
            assertTrue(FiatCurrency.KWD.isThreeDecimal());
            assertEquals("USD (US Dollar)", FiatCurrency.USD.toString());
            assertEquals(FiatCurrency.PHP, FiatCurrency.forCountry(Country.PHILIPPINES).orElseThrow());
            assertEquals(FiatCurrency.EUR, FiatCurrency.forCountry(Country.GERMANY).orElseThrow());
            assertTrue(FiatCurrency.forCountry(null).isEmpty());
            assertTrue(FiatCurrency.forCountry(Country.UNKNOWN).isEmpty());
        }
    }

    @Nested
    class AnyCurrency {

        @ParameterizedTest(name = "\"{0}\" -> {1}")
        @CsvSource({"usd, USD", "PHP, PHP", "btc, BTC", "' Eth ', ETH", "usdt, USDT"})
        void fiatCodesThenCryptoTickers(String text, String code) {
            assertEquals(code, Currency.parse(text).code());
            assertEquals(code, Currency.tryParse(text).orElseThrow().code());
            assertTrue(Currency.isValid(text));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "XXX", "NOPE", "PEPE"})
        void rejectsOthers(String text) {
            assertThrows(ValueObjectException.class, () -> Currency.parse(text));
            assertTrue(Currency.tryParse(text).isEmpty());
            assertFalse(Currency.isValid(text));
        }

        @Test
        void typeIsTheKindOfCurrency() {
            assertEquals("FiatCurrency", FiatCurrency.USD.type());
            assertEquals("CryptoCoin", CryptoCoin.Known.BTC.type(), "was \"Known\" before");
            assertEquals("CryptoCoin", CryptoCoin.custom("PEPE", "Pepe", "PEPE", 18).type());
        }

        @Test
        void comparisons() {
            assertTrue(FiatCurrency.EUR.compareByCode(FiatCurrency.USD) < 0);
            assertTrue(CryptoCoin.Known.BTC.compareByCode(FiatCurrency.USD) < 0);
            assertTrue(CryptoCoin.Known.BTC.compareByName(FiatCurrency.USD) < 0, "Bitcoin before US Dollar");
            assertThrows(ValueObjectException.class, () -> FiatCurrency.USD.compareByCode(null));
            assertThrows(ValueObjectException.class, () -> FiatCurrency.USD.compareByName(null));
        }
    }

    @Nested
    class Crypto {

        @ParameterizedTest(name = "\"{0}\" -> {1}")
        @CsvSource({"BTC, BTC", "btc, BTC", "' eth ', ETH", "Doge, DOGE", "dai, DAI"})
        void knownTickers(String text, CryptoCoin.Known expected) {
            assertSame(expected, CryptoCoin.of(text));
            assertSame(expected, CryptoCoin.parse(text));
            assertSame(expected, CryptoCoin.tryParse(text).orElseThrow());
            assertSame(expected, CryptoCoin.Known.fromCode(text).orElseThrow());
            assertTrue(CryptoCoin.isValid(text));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "NOPE", "PEPE", "USD", "BT C"})
        void unknownTickers(String text) {
            assertThrows(ValueObjectException.class, () -> CryptoCoin.of(text));
            assertThrows(ValueObjectException.class, () -> CryptoCoin.parse(text));
            assertTrue(CryptoCoin.tryParse(text).isEmpty());
            assertFalse(CryptoCoin.isValid(text));
        }

        @Test
        void knownProperties() {
            assertEquals("Bitcoin", CryptoCoin.Known.BTC.displayName());
            assertEquals("₿", CryptoCoin.Known.BTC.symbol());
            assertEquals(8, CryptoCoin.Known.BTC.decimalPlaces());
            assertEquals(18, CryptoCoin.Known.ETH.decimalPlaces());
        }

        @Test
        void customCoinsAreNormalised() {
            CryptoCoin pepe = CryptoCoin.custom("  pepe ", " Pepe ", " PEPE ", 18);
            assertEquals("PEPE", pepe.code());
            assertEquals("Pepe", pepe.displayName());
            assertEquals("PEPE", pepe.symbol());
            assertEquals(18, pepe.decimalPlaces());
            assertEquals("PEPE", pepe.toString());
            assertEquals(pepe, CryptoCoin.custom("PEPE", "Pepe", "PEPE", 18));
            assertTrue(CryptoCoin.tryParse("PEPE").isEmpty(), "custom coins are not looked up");
        }

        @Test
        void badCustomCoinsAreRejected() {
            assertThrows(ValueObjectException.class, () -> CryptoCoin.custom(null, "x", "x", 2));
            assertThrows(ValueObjectException.class, () -> CryptoCoin.custom("AB", null, "x", 2));
            assertThrows(ValueObjectException.class, () -> CryptoCoin.custom("AB", "x", null, 2));
            assertThrows(ValueObjectException.class, () -> CryptoCoin.custom("A", "x", "x", 2));
            assertThrows(ValueObjectException.class, () -> CryptoCoin.custom("ABCDEFGHIJK", "x", "x", 2));
            assertThrows(ValueObjectException.class, () -> CryptoCoin.custom("A-B", "x", "x", 2));
            assertThrows(ValueObjectException.class, () -> CryptoCoin.custom("btc", "Bitcoin", "B", 8));
            assertThrows(ValueObjectException.class, () -> CryptoCoin.custom("AB", " ", "x", 2));
            assertThrows(ValueObjectException.class, () -> CryptoCoin.custom("AB", "x", "", 2));
            assertThrows(ValueObjectException.class, () -> CryptoCoin.custom("AB", "x", "x", -1));
            assertThrows(ValueObjectException.class, () -> CryptoCoin.custom("AB", "x", "x", 37));
            assertDoesNotThrow(() -> CryptoCoin.custom("AB", "x", "x", 36));
        }
    }

    @Nested
    class Addresses {

        private static final String GENESIS = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa";
        private static final String ETH = "0x52908400098527886E0F7030069857D2E4169EE7";

        @Test
        void acceptsRealAddresses() {
            CryptoCurrencyAddress btc = new CryptoCurrencyAddress(GENESIS, CryptoCoin.Known.BTC);
            assertEquals(GENESIS, btc.address());
            assertEquals(CryptoCoin.Known.BTC, btc.coin());
            assertEquals(ETH, new CryptoCurrencyAddress(ETH, CryptoCoin.Known.ETH).address());
            assertEquals("bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq",
                    new CryptoCurrencyAddress("bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq", CryptoCoin.Known.BTC).address());
            assertTrue(CryptoCurrencyAddress.isValidAddress(GENESIS));
        }

        @Test
        void trimsSurroundingSpaces() {
            assertEquals(GENESIS, new CryptoCurrencyAddress("  " + GENESIS + "\n", CryptoCoin.Known.BTC).address());
            assertEquals(new CryptoCurrencyAddress(GENESIS, CryptoCoin.Known.BTC),
                    new CryptoCurrencyAddress(" " + GENESIS, CryptoCoin.Known.BTC));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "abc", "1A1zP1eP5QGefi2D MPTfTL5SLmv7DivfNa", "1A1zP1eP5QGefi2DMPTfTL5SLmv7Divf€Na",
                "1A1zP1eP5QGefi2DMPT\tfTL5SLmv7DivfNa", "1234567890123456789"})
        void rejectsBadAddresses(String address) {
            assertThrows(ValueObjectException.class, () -> new CryptoCurrencyAddress(address, CryptoCoin.Known.BTC));
            assertFalse(CryptoCurrencyAddress.isValidAddress(address));
        }

        @Test
        void lengthLimits() {
            String min = "a".repeat(CryptoCurrencyAddress.MIN_LENGTH);
            String max = "a".repeat(CryptoCurrencyAddress.MAX_LENGTH);
            assertDoesNotThrow(() -> new CryptoCurrencyAddress(min, CryptoCoin.Known.BTC));
            assertDoesNotThrow(() -> new CryptoCurrencyAddress(max, CryptoCoin.Known.BTC));
            assertThrows(ValueObjectException.class, () -> new CryptoCurrencyAddress(max + "a", CryptoCoin.Known.BTC));
        }

        @Test
        void needsItsCoin() {
            assertThrows(ValueObjectException.class, () -> new CryptoCurrencyAddress(GENESIS, null));
        }
    }
}
