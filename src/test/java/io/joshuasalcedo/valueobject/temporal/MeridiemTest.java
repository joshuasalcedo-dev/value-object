package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MeridiemTest {

    @Test
    void parseReadsPlainAndDottedForms() {
        assertEquals(Meridiem.AM, Meridiem.parse("AM"));
        assertEquals(Meridiem.AM, Meridiem.parse("am"));
        assertEquals(Meridiem.AM, Meridiem.parse("a.m."));
        assertEquals(Meridiem.PM, Meridiem.parse(" P.M. "));
        assertEquals(Meridiem.PM, Meridiem.parse("p. m."));
        assertThrows(ValueObjectException.class, () -> Meridiem.parse(null));
        assertThrows(ValueObjectException.class, () -> Meridiem.parse(""));
        assertThrows(ValueObjectException.class, () -> Meridiem.parse("noon"));
        assertEquals(Optional.empty(), Meridiem.tryParse(null));
        assertEquals(Optional.of(Meridiem.PM), Meridiem.tryParse("pm"));
        assertFalse(Meridiem.isValid("XM"));
        assertTrue(Meridiem.isValid("a.m."));
    }

    @Test
    void fromHour() {
        assertEquals(Meridiem.AM, Meridiem.fromHour(0));
        assertEquals(Meridiem.AM, Meridiem.fromHour(11));
        assertEquals(Meridiem.PM, Meridiem.fromHour(12));
        assertEquals(Meridiem.PM, Meridiem.fromHour(23));
        assertThrows(ValueObjectException.class, () -> Meridiem.fromHour(24));
        assertThrows(ValueObjectException.class, () -> Meridiem.fromHour(-1));
        assertEquals(Meridiem.PM, Meridiem.fromLocalTime(LocalTime.NOON));
        assertThrows(ValueObjectException.class, () -> Meridiem.fromLocalTime(null));
    }

    @Test
    void properties() {
        assertEquals("AM", Meridiem.AM.toString());
        assertEquals("Post Meridiem", Meridiem.PM.getFullName());
        assertEquals("PM", Meridiem.PM.getAbbreviation());
        assertEquals(0, Meridiem.AM.getStartHour());
        assertEquals(11, Meridiem.AM.getEndHour());
        assertEquals(12, Meridiem.PM.getStartHour());
        assertEquals(23, Meridiem.PM.getEndHour());
        assertTrue(Meridiem.AM.isAm());
        assertTrue(Meridiem.PM.isPm());
        assertFalse(Meridiem.PM.isAm());
    }
}
