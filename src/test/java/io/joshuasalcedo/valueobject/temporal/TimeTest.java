package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TimeTest {

    private static Time t(String s) {
        return Time.parse(s);
    }

    @Test
    void parseTwelveHourForms() {
        assertEquals(Time.of(11, 30, Meridiem.AM), t("11:30 AM"));
        assertEquals(Time.of(11, 30, Meridiem.AM), t("11:30AM"));
        assertEquals(Time.of(9, 5, Meridiem.PM), t(" 9:05 pm "));
        assertEquals(Time.of(11, 30, Meridiem.AM), t("11:30 a.m."));
        assertEquals(Time.of(11, 30, Meridiem.PM), t("11:30 P.M."));
        assertEquals(Time.of(11, 30, Meridiem.PM), t("11:30p.m."));
        assertEquals(Time.of(12, 0, Meridiem.AM), t("12:00 AM"));
        assertEquals(Time.of(9, 5, Meridiem.PM), t("9:05:30 PM"));
    }

    @Test
    void parseTwentyFourHourForms() {
        assertEquals(Time.of24(23, 30), t("23:30"));
        assertEquals(Time.of24(23, 30), t("23:30:00"));
        assertEquals(Time.of24(23, 30), t("23:30:59"));
        assertEquals(Time.of24(0, 0), t("00:00"));
        assertEquals(Time.of24(9, 5), t("9:05"));
        assertTrue(t("00:00").isMidnight());
        assertTrue(t("12:00").isNoon());
    }

    @Test
    void toStringRoundTrips() {
        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m += 7) {
                Time time = Time.of24(h, m);
                assertEquals(time, Time.parse(time.toString()));
                assertEquals(time, Time.parse(time.to24HourFormat()));
                assertEquals(time, Time.parse(time.toIsoFormat()));
                assertEquals(time, Time.parse(time.to12HourFormatPadded()));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "11", "11:3", "11:30:5", "24:00", "13:00 PM", "0:30 AM", "11:60",
            "23:30:60", "11:30 XM", "11:30 AM PM", "-1:00", "11.30", "1:2:3:4", "11:30 AMX"})
    void parseRejectsBadInput(String bad) {
        assertThrows(ValueObjectException.class, () -> Time.parse(bad));
        assertEquals(Optional.empty(), Time.tryParse(bad));
        assertFalse(Time.isValid(bad));
    }

    @Test
    void nullInputThrowsValueObjectException() {
        assertThrows(ValueObjectException.class, () -> Time.parse(null));
        assertEquals(Optional.empty(), Time.tryParse(null));
        assertFalse(Time.isValid(null));
        assertThrows(ValueObjectException.class, () -> Time.from(null));
        assertThrows(ValueObjectException.class, () -> new Time(1, 0, null));
        assertThrows(ValueObjectException.class, () -> Time.of(1, 0, Meridiem.AM).withMeridiem(null));
    }

    @Test
    void rangeChecksThrowValueObjectException() {
        assertThrows(ValueObjectException.class, () -> Time.of(0, 0, Meridiem.AM));
        assertThrows(ValueObjectException.class, () -> Time.of(13, 0, Meridiem.PM));
        assertThrows(ValueObjectException.class, () -> Time.of(1, 60, Meridiem.PM));
        assertThrows(ValueObjectException.class, () -> Time.of24(24, 0));
        assertThrows(ValueObjectException.class, () -> Time.of24(-1, 0));
        assertThrows(ValueObjectException.class, () -> Time.of24(1, -1));
        assertThrows(ValueObjectException.class, () -> Time.of(1, 0, Meridiem.AM).withHour(13));
        assertThrows(ValueObjectException.class, () -> Time.of(1, 0, Meridiem.AM).withMinute(60));
    }

    @Test
    void conversions() {
        Time t = Time.of(11, 30, Meridiem.PM);
        assertEquals(23, t.hour24());
        assertEquals(LocalTime.of(23, 30), t.toLocalTime());
        assertEquals("23:30", t.to24HourFormat());
        assertEquals("11:30 PM", t.to12HourFormat());
        assertEquals("11:30 PM", t.toString());
        assertEquals("09:05 AM", Time.of(9, 5, Meridiem.AM).to12HourFormatPadded());
        assertEquals("23:30:00", t.toIsoFormat());
        assertEquals(Time.of24(14, 15), Time.from(LocalTime.of(14, 15, 59)));
        assertEquals(0, Time.of(12, 0, Meridiem.AM).hour24());
        assertEquals(12, Time.of(12, 0, Meridiem.PM).hour24());
    }

    @Test
    void arithmeticWrapsAroundMidnight() {
        Time late = Time.of24(23, 30);
        assertEquals(Time.of24(0, 15), late.plusMinutes(45));
        assertEquals(Time.of24(22, 30), late.plusHours(-1));
        assertEquals(Time.of24(1, 30), late.plusHours(2));
        assertEquals(Time.of(11, 30, Meridiem.AM), late.withMeridiem(Meridiem.AM));
        assertEquals(Time.of(5, 30, Meridiem.PM), late.withHour(5));
        assertEquals(Time.of(11, 0, Meridiem.PM), late.withMinute(0));
    }

    @Test
    void comparisons() {
        Time nine = Time.of24(9, 0);
        Time five = Time.of24(17, 0);
        assertTrue(nine.isBefore(five));
        assertTrue(five.isAfter(nine));
        assertEquals(480, nine.minutesUntil(five));
        assertEquals(-480, five.minutesUntil(nine));
        assertTrue(Time.of(12, 0, Meridiem.AM).isBefore(Time.of(1, 0, Meridiem.AM)));
        assertTrue(Time.of(11, 59, Meridiem.AM).isBefore(Time.of(12, 0, Meridiem.PM)));
        assertThrows(ValueObjectException.class, () -> nine.compareTo(null));
        assertThrows(ValueObjectException.class, () -> nine.isBefore(null));
        assertThrows(ValueObjectException.class, () -> nine.minutesUntil(null));
        assertTrue(nine.isMorning());
        assertTrue(five.isAfternoon());
    }

    @Test
    void isBetweenSameDay() {
        Time nine = Time.of24(9, 0);
        Time five = Time.of24(17, 0);
        assertTrue(Time.of24(12, 0).isBetween(nine, five));
        assertTrue(nine.isBetween(nine, five));
        assertTrue(five.isBetween(nine, five));
        assertFalse(Time.of24(8, 59).isBetween(nine, five));
        assertFalse(Time.of24(17, 1).isBetween(nine, five));
        assertTrue(nine.isBetween(nine, nine));
    }

    @Test
    void isBetweenWrapsAcrossMidnight() {
        Time ten = Time.of(10, 0, Meridiem.PM);
        Time two = Time.of(2, 0, Meridiem.AM);
        assertTrue(Time.of(11, 0, Meridiem.PM).isBetween(ten, two));
        assertTrue(Time.of(1, 0, Meridiem.AM).isBetween(ten, two));
        assertTrue(Time.of(12, 0, Meridiem.AM).isBetween(ten, two));
        assertTrue(ten.isBetween(ten, two));
        assertTrue(two.isBetween(ten, two));
        assertFalse(Time.of(12, 0, Meridiem.PM).isBetween(ten, two));
        assertFalse(Time.of(2, 1, Meridiem.AM).isBetween(ten, two));
        assertFalse(Time.of(9, 59, Meridiem.PM).isBetween(ten, two));
        assertThrows(ValueObjectException.class, () -> ten.isBetween(null, two));
        assertThrows(ValueObjectException.class, () -> ten.isBetween(ten, null));
    }
}
