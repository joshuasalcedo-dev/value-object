package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DateOfBirthTest {

    private static final DateOfBirth JAN_15_2000 = DateOfBirth.of(2000, 1, 15);
    private static final DateOfBirth LEAP_DAY = DateOfBirth.of(2000, 2, 29);

    @Test
    void parseReadsIsoDates() {
        assertEquals(JAN_15_2000, DateOfBirth.parse("2000-01-15"));
        assertEquals(JAN_15_2000, DateOfBirth.parse(" 2000-01-15 "));
        assertEquals("2000-01-15", JAN_15_2000.toString());
        assertEquals(JAN_15_2000, DateOfBirth.parse(JAN_15_2000.toString()));
        assertEquals(JAN_15_2000, DateOfBirth.parse("15/01/2000", DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    @Test
    void badInputThrowsValueObjectException() {
        assertThrows(ValueObjectException.class, () -> DateOfBirth.parse(null));
        assertThrows(ValueObjectException.class, () -> DateOfBirth.parse(""));
        assertThrows(ValueObjectException.class, () -> DateOfBirth.parse("2000-02-30"));
        assertThrows(ValueObjectException.class, () -> DateOfBirth.parse("15/01/2000"));
        assertThrows(ValueObjectException.class, () -> DateOfBirth.parse("9999-01-01"));
        assertThrows(ValueObjectException.class, () -> DateOfBirth.parse("x", DateTimeFormatter.ISO_LOCAL_DATE));
        assertThrows(ValueObjectException.class, () -> DateOfBirth.parse(null, DateTimeFormatter.ISO_LOCAL_DATE));
        assertThrows(ValueObjectException.class, () -> DateOfBirth.parse("2000-01-15", null));
        assertThrows(ValueObjectException.class, () -> new DateOfBirth(null));
        assertThrows(ValueObjectException.class, () -> DateOfBirth.of(null));
        assertThrows(ValueObjectException.class, () -> DateOfBirth.of(LocalDate.now().plusDays(1)));
        assertThrows(ValueObjectException.class, () -> DateOfBirth.of(2001, 2, 29));
        assertThrows(ValueObjectException.class, () -> DateOfBirth.of(2000, 13, 1));
        assertDoesNotThrow(() -> DateOfBirth.of(LocalDate.now()));
    }

    @Test
    void tryParseAndIsValidNeverThrow() {
        assertEquals(Optional.empty(), DateOfBirth.tryParse(null));
        assertEquals(Optional.empty(), DateOfBirth.tryParse("tomorrow"));
        assertEquals(Optional.empty(), DateOfBirth.tryParse(LocalDate.now().plusDays(1).toString()));
        assertEquals(Optional.of(JAN_15_2000), DateOfBirth.tryParse("2000-01-15"));
        assertFalse(DateOfBirth.isValid(null));
        assertFalse(DateOfBirth.isValid("2000-1-15"));
        assertTrue(DateOfBirth.isValid("1990-12-31"));
    }

    @Test
    void agesOnAGivenDay() {
        LocalDate asOf = LocalDate.of(2025, 4, 25);
        assertEquals(25, JAN_15_2000.ageInYears(asOf));
        assertEquals(Period.of(25, 3, 10), JAN_15_2000.agePrecise(asOf));
        assertEquals(24, JAN_15_2000.ageInYears(LocalDate.of(2025, 1, 14)));
        assertEquals(25, JAN_15_2000.ageInYears(LocalDate.of(2025, 1, 15)));
        assertThrows(ValueObjectException.class, () -> JAN_15_2000.ageInYears(null));
        assertThrows(ValueObjectException.class, () -> JAN_15_2000.agePrecise(null));
        // today-based forms agree with the explicit ones
        assertEquals(JAN_15_2000.ageInYears(LocalDate.now()), JAN_15_2000.ageInYears());
        assertTrue(JAN_15_2000.ageInMonths() >= 300);
        assertTrue(JAN_15_2000.ageInWeeks() > 1300);
        assertTrue(JAN_15_2000.ageInDays() > 9000);
    }

    @Test
    void leapDayBirthdayIs28FebruaryInCommonYears() {
        assertTrue(LEAP_DAY.isBirthdayOn(LocalDate.of(2025, 2, 28)));
        assertFalse(LEAP_DAY.isBirthdayOn(LocalDate.of(2025, 3, 1)));
        assertTrue(LEAP_DAY.isBirthdayOn(LocalDate.of(2024, 2, 29)));
        assertFalse(LEAP_DAY.isBirthdayOn(LocalDate.of(2024, 2, 28)));
        assertEquals(LocalDate.of(2025, 2, 28), LEAP_DAY.birthdayIn(2025));
        assertEquals(LocalDate.of(2028, 2, 29), LEAP_DAY.birthdayIn(2028));
        assertEquals(0, LEAP_DAY.daysUntilNextBirthday(LocalDate.of(2025, 2, 28)));
        assertEquals(1, LEAP_DAY.daysUntilNextBirthday(LocalDate.of(2024, 2, 28)));
        // from 1 March 2027 the next birthday is 29 February 2028, not 28 February
        assertEquals(365, LEAP_DAY.daysUntilNextBirthday(LocalDate.of(2027, 3, 1)));
        assertThrows(ValueObjectException.class, () -> LEAP_DAY.isBirthdayOn(null));
        assertThrows(ValueObjectException.class, () -> LEAP_DAY.daysUntilNextBirthday(null));
    }

    @Test
    void daysUntilNextBirthday() {
        assertEquals(0, JAN_15_2000.daysUntilNextBirthday(LocalDate.of(2026, 1, 15)));
        assertEquals(1, JAN_15_2000.daysUntilNextBirthday(LocalDate.of(2026, 1, 14)));
        assertEquals(364, JAN_15_2000.daysUntilNextBirthday(LocalDate.of(2026, 1, 16)));
        assertTrue(JAN_15_2000.isBirthdayOn(LocalDate.of(2030, 1, 15)));
        long today = JAN_15_2000.daysUntilNextBirthday();
        assertTrue(today >= 0 && today <= 365);
        assertEquals(JAN_15_2000.isBirthdayOn(LocalDate.now()), JAN_15_2000.isBirthdayToday());
    }

    @Test
    void comparisons() {
        DateOfBirth later = DateOfBirth.of(2005, 6, 1);
        assertTrue(JAN_15_2000.isBefore(later));
        assertTrue(later.isAfter(JAN_15_2000));
        assertFalse(JAN_15_2000.isAfter(JAN_15_2000));
        assertThrows(ValueObjectException.class, () -> JAN_15_2000.isBefore(null));
        assertThrows(ValueObjectException.class, () -> JAN_15_2000.isAfter(null));
    }

    @Test
    void formattingIsEnglishWhateverTheDefaultLocale() {
        Locale saved = Locale.getDefault();
        try {
            Locale.setDefault(Locale.FRANCE);
            assertEquals("2000-01-15", JAN_15_2000.toIsoDate());
            assertEquals("01/15/2000", JAN_15_2000.toUsDate());
            assertEquals("15/01/2000", JAN_15_2000.toEuDate());
            assertEquals("January 15, 2000", JAN_15_2000.toLongDate());
            assertEquals("Jan 15, 2000", JAN_15_2000.toMediumDate());
            assertEquals("15-Jan-2000", JAN_15_2000.toShortDate());
            assertEquals("15 January 2000", JAN_15_2000.toDayFirstLongDate());
            assertEquals("Saturday, January 15, 2000", JAN_15_2000.toFullDate());
            assertEquals("Sat 15 Jan", JAN_15_2000.formatted("EEE d MMM"));
            assertTrue(JAN_15_2000.toAgeString().matches("\\d+ years, \\d+ months, \\d+ days"));
        } finally {
            Locale.setDefault(saved);
        }
        assertEquals("15.01.2000", JAN_15_2000.formatted(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    }

    @Test
    void formattingRejectsBadPatterns() {
        assertThrows(ValueObjectException.class, () -> JAN_15_2000.formatted((String) null));
        assertThrows(ValueObjectException.class, () -> JAN_15_2000.formatted("{{"));
        assertThrows(ValueObjectException.class, () -> JAN_15_2000.formatted("HH:mm"));
        assertThrows(ValueObjectException.class, () -> JAN_15_2000.formatted((DateTimeFormatter) null));
        assertThrows(ValueObjectException.class, () -> JAN_15_2000.formatted(DateTimeFormatter.ISO_LOCAL_TIME));
    }
}
