package io.joshuasalcedo.valueobject.contact;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "+63 917 123 4567       | 63  | 9171234567",
            "+1 (555) 123-4567      | 1   | 5551234567",
            "+44 20 7946 0958       | 44  | 2079460958",
            "+7 495 123-45-67       | 7   | 4951234567",
            "+852 2123 4567         | 852 | 21234567",
            "+971 4 123 4567        | 971 | 41234567",
            "+20 2 1234 5678        | 20  | 212345678",
            "+27 21 123 4567        | 27  | 211234567",
            "+49 30 123456          | 49  | 30123456",
            "+86 10 1234 5678       | 86  | 1012345678",
            "+98 21 1234 5678       | 98  | 2112345678",
            "+65 6123 4567          | 65  | 61234567",
            "+353 1 234 5678        | 353 | 12345678",
            "+421 2 1234 5678       | 421 | 212345678",
            "+880 2 1234 5678       | 880 | 212345678",
            "'  +63 917 123 4567  ' | 63  | 9171234567",
    })
    void readsTheCountryCallingCodeByItuPrefix(String text, String cc, String nn) {
        PhoneNumber p = PhoneNumber.parse(text);
        assertEquals(cc, p.countryCode());
        assertEquals(nn, p.nationalNumber());
        assertEquals("+" + cc + nn, p.toE164());
    }

    @Test
    void philippineAndUkNumbersAreNoLongerGuessedFromTheLength() {
        // before the fix these came back as +639 and +442
        assertEquals("63", PhoneNumber.parse("+63 917 123 4567").countryCode());
        assertEquals("44", PhoneNumber.parse("+44 20 7946 0958").countryCode());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "917 123 4567", "+12", "abc", "+0 123 4567", "+1 2345 6789 0123 4567"})
    void rejectsBadText(String text) {
        assertThrows(ValueObjectException.class, () -> PhoneNumber.parse(text));
        assertTrue(PhoneNumber.tryParse(text).isEmpty());
        assertFalse(PhoneNumber.isValid(text));
    }

    @Test
    void validAndTryParseAgreeWithParse() {
        assertTrue(PhoneNumber.isValid("+63 917 123 4567"));
        assertEquals(PhoneNumber.parse("+63 917 123 4567"), PhoneNumber.tryParse("+63 917 123 4567").orElseThrow());
    }

    @Test
    void constructorRejectsNullWithValueObjectException() {
        assertThrows(ValueObjectException.class, () -> new PhoneNumber(null, "123"));
        assertThrows(ValueObjectException.class, () -> new PhoneNumber("1", null));
        assertThrows(ValueObjectException.class, () -> new PhoneNumber("", "123"));
        assertThrows(ValueObjectException.class, () -> new PhoneNumber("1234", "123"));
    }

    @Test
    void constructorDropsNonDigits() {
        PhoneNumber p = PhoneNumber.of("+44", "(20) 7946-0958");
        assertEquals("44", p.countryCode());
        assertEquals("2079460958", p.nationalNumber());
    }

    @Test
    void formatsAndMasks() {
        PhoneNumber us = PhoneNumber.us("555-123-4567");
        assertEquals("+1 (555) 123-4567", us.formatted());
        assertEquals("+1 ******4567", us.masked());
        assertEquals("15551234567", us.toDigits());
        assertTrue(us.isNorthAmerican());
        assertEquals("+44 2079460958", PhoneNumber.uk("20 7946 0958").formatted());
        assertEquals("+44 ****", PhoneNumber.uk("1234").masked());
        assertEquals("+15551234567", us.toString());
    }

    @Test
    void ordersByCountryCodeThenNumber() {
        PhoneNumber a = PhoneNumber.parse("+1 555 123 4567");
        PhoneNumber b = PhoneNumber.parse("+44 20 7946 0958");
        assertTrue(a.compareTo(b) < 0);
        assertEquals(0, a.compareTo(PhoneNumber.us("5551234567")));
    }
}
