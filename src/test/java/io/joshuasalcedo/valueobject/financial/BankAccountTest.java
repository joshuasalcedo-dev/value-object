package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.geo.Country;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BankAccountTest {

    @Nested
    class Ibans {

        @ParameterizedTest(name = "\"{0}\"")
        @CsvSource({
                "'GB82 WEST 1234 5698 7654 32', GB82WEST12345698765432",
                "GB82WEST12345698765432, GB82WEST12345698765432",
                "gb82 west 1234 5698 7654 32, GB82WEST12345698765432",
                "GB82-WEST-1234-5698-7654-32, GB82WEST12345698765432",
                "DE89 3704 0044 0532 0130 00, DE89370400440532013000",
                "FR14 2004 1010 0505 0001 3M02 606, FR1420041010050500013M02606",
                "NL91 ABNA 0417 1643 00, NL91ABNA0417164300",
                "BE68 5390 0754 7034, BE68539007547034"
        })
        void readsValidIbans(String text, String value) {
            Iban iban = Iban.parse(text);
            assertEquals(value, iban.value());
            assertEquals(iban, Iban.tryParse(text).orElseThrow());
            assertTrue(Iban.isValid(text));
        }

        @Test
        void parts() {
            Iban iban = Iban.parse("GB82 WEST 1234 5698 7654 32");
            assertEquals("GB", iban.countryCode());
            assertEquals(Country.UNITED_KINGDOM, iban.country());
            assertEquals("82", iban.checkDigits());
            assertEquals("WEST12345698765432", iban.bban());
            assertEquals("GB82 WEST 1234 5698 7654 32", iban.formatted());
            assertEquals("GB82 WEST 1234 5698 7654 32", iban.toString());
            assertTrue(iban.compareTo(Iban.parse("DE89370400440532013000")) > 0);
        }

        @ParameterizedTest(name = "\"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "GB83 WEST 1234 5698 7654 32", "GB82 WEST 1234 5698 7654 3", "GB82 WEST 1234 5698 7654 321",
                "DE89 3704 0044 0532 0130 01", "GBXX WEST 1234 5698 7654 32", "1234567890123456", "GB82 WEST 1234 5698 7654 3!",
                "GB82"})
        void rejectsInvalidIbans(String text) {
            assertThrows(ValueObjectException.class, () -> Iban.parse(text));
            assertTrue(Iban.tryParse(text).isEmpty());
            assertFalse(Iban.isValid(text));
        }
    }

    @Nested
    class Bics {

        @ParameterizedTest(name = "\"{0}\"")
        @CsvSource({"DEUTDEFF, DEUT, GERMANY, FF, XXX, true", "DEUTDEFF500, DEUT, GERMANY, FF, 500, false",
                "BOPIPHMM, BOPI, PHILIPPINES, MM, XXX, true", "'  NWBK GB 2L  ', NWBK, UNITED_KINGDOM, 2L, XXX, true",
                "DEUTDEFFXXX, DEUT, GERMANY, FF, XXX, true"})
        void readsBics(String text, String bank, Country country, String location, String branch, boolean head) {
            Bic bic = Bic.parse(text);
            assertEquals(bank, bic.bankCode());
            assertEquals(country, bic.country());
            assertEquals(location, bic.locationCode());
            assertEquals(branch, bic.branchCode());
            assertEquals(head, bic.isHeadOffice());
            assertEquals(bic.value(), bic.toString());
            assertEquals(bic, Bic.tryParse(text).orElseThrow());
            assertTrue(Bic.isValid(text));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "deutdeff", "DEUTDEF", "DEUTDEFF50", "DEUTDEFF5000", "DEU1DEFF", "DEUTZZFF", "DEUT-DE-FF"})
        void rejectsOthers(String text) {
            assertThrows(ValueObjectException.class, () -> Bic.parse(text));
            assertTrue(Bic.tryParse(text).isEmpty());
            assertFalse(Bic.isValid(text));
        }

        @Test
        void ordering() {
            assertTrue(Bic.parse("BOPIPHMM").compareTo(Bic.parse("DEUTDEFF")) < 0);
        }
    }
}
