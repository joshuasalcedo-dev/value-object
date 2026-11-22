package io.joshuasalcedo.valueobject.temporal;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TimeZoneTest {

    private static final Instant JANUARY = Instant.parse("2026-01-15T12:00:00Z");
    private static final Instant JULY = Instant.parse("2026-07-15T12:00:00Z");

    @Test
    void parseReadsZoneIdsAndConstantNames() {
        assertEquals(TimeZone.ASIA_MANILA, TimeZone.parse("Asia/Manila"));
        assertEquals(TimeZone.ASIA_MANILA, TimeZone.parse("  Asia/Manila "));
        assertEquals(TimeZone.AUSTRALIA_SYDNEY, TimeZone.parse("AUSTRALIA_SYDNEY"));
        assertEquals(TimeZone.UTC, TimeZone.fromZoneId("UTC"));
        for (TimeZone tz : TimeZone.values()) {
            assertEquals(tz, TimeZone.parse(tz.zoneId()));
        }
    }

    @Test
    void parseRejectsBadInput() {
        assertThrows(ValueObjectException.class, () -> TimeZone.parse(null));
        assertThrows(ValueObjectException.class, () -> TimeZone.parse("Mars/Olympus"));
        assertThrows(ValueObjectException.class, () -> TimeZone.parse("asia/manila"));
        assertThrows(ValueObjectException.class, () -> TimeZone.fromZoneId(null));
        assertEquals(Optional.empty(), TimeZone.tryParse(null));
        assertEquals(Optional.empty(), TimeZone.tryParse(""));
        assertEquals(Optional.of(TimeZone.EUROPE_LONDON), TimeZone.tryParse("Europe/London"));
        assertFalse(TimeZone.isValid(null));
        assertTrue(TimeZone.isValid("America/New_York"));
    }

    @Test
    void everyZoneIdIsAJavaZone() {
        for (TimeZone tz : TimeZone.values()) {
            assertEquals(tz.zoneId(), tz.toZoneId().getId());
        }
    }

    @Test
    void storedOffsetsAreTheStandardOffsets() {
        assertEquals("GMT+10:00", TimeZone.AUSTRALIA_SYDNEY.offset());
        assertEquals("GMT+12:00", TimeZone.PACIFIC_AUCKLAND.offset());
        // the stored offset is the one in force outside summer time: the smaller of the January and July
        // offsets (the rules' "standard" offset differs for zones modelled with negative DST, e.g. Casablanca)
        List<String> mismatches = new ArrayList<>();
        for (TimeZone tz : TimeZone.values()) {
            ZoneOffset jan = tz.offsetAt(JANUARY);
            ZoneOffset jul = tz.offsetAt(JULY);
            ZoneOffset winter = jan.getTotalSeconds() <= jul.getTotalSeconds() ? jan : jul;
            if (!winter.equals(tz.standardOffset())) {
                mismatches.add(tz.name() + " stored " + tz.offset() + " but outside summer time it is " + winter);
            }
            assertEquals(TimeZone.formatOffset(tz.standardOffset()), tz.offset(), tz.name());
        }
        assertEquals(List.of(), mismatches);
    }

    @Test
    void offsetAtFollowsDaylightSavingTime() {
        assertEquals(ZoneOffset.ofHours(-5), TimeZone.AMERICA_NEW_YORK.offsetAt(JANUARY));
        assertEquals(ZoneOffset.ofHours(-4), TimeZone.AMERICA_NEW_YORK.offsetAt(JULY));
        assertEquals(ZoneOffset.ofHours(11), TimeZone.AUSTRALIA_SYDNEY.offsetAt(JANUARY));
        assertEquals(ZoneOffset.ofHours(10), TimeZone.AUSTRALIA_SYDNEY.offsetAt(JULY));
        assertEquals(ZoneOffset.ofHours(8), TimeZone.ASIA_MANILA.offsetAt(JULY));
        assertThrows(ValueObjectException.class, () -> TimeZone.UTC.offsetAt(null));
    }

    @Test
    void formatOffsetKeepsTheSignForOffsetsUnderAnHour() {
        assertEquals("GMT-0:30", TimeZone.formatOffset(ZoneOffset.ofHoursMinutes(0, -30)));
        assertEquals("GMT+0:30", TimeZone.formatOffset(ZoneOffset.ofHoursMinutes(0, 30)));
        assertEquals("GMT-9:30", TimeZone.formatOffset(ZoneOffset.ofHoursMinutes(-9, -30)));
        assertEquals("GMT+5:45", TimeZone.formatOffset(ZoneOffset.ofHoursMinutes(5, 45)));
        assertEquals("GMT+0:00", TimeZone.formatOffset(ZoneOffset.UTC));
        assertEquals("GMT+14:00", TimeZone.formatOffset(ZoneOffset.ofHours(14)));
        assertEquals("GMT-12:00", TimeZone.formatOffset(ZoneOffset.ofHours(-12)));
        assertThrows(ValueObjectException.class, () -> TimeZone.formatOffset(null));
    }

    @Test
    void currentOffsetAndDisplayUseTheZoneRules() {
        String expected = TimeZone.formatOffset(ZoneId.of("Asia/Manila").getRules().getOffset(Instant.now()));
        assertEquals(expected, TimeZone.ASIA_MANILA.getCurrentOffset());
        assertEquals("(GMT+8:00) Asia/Manila - Philippine Time", TimeZone.ASIA_MANILA.getFormattedDisplay());
        assertEquals(TimeZone.ASIA_MANILA.getFormattedDisplay(), TimeZone.ASIA_MANILA.toString());
        assertEquals("Philippine Time", TimeZone.ASIA_MANILA.displayName());
    }

    @Test
    void compareByOffsetIsNumericWestToEast() {
        // as text "GMT+10:00" < "GMT+9:00" and every "+" before every "-"
        assertTrue(TimeZone.ASIA_TOKYO.compareByOffset(TimeZone.AUSTRALIA_SYDNEY) < 0);
        assertTrue(TimeZone.AMERICA_NEW_YORK.compareByOffset(TimeZone.UTC) < 0);
        assertTrue(TimeZone.AMERICA_LOS_ANGELES.compareByOffset(TimeZone.AMERICA_NEW_YORK) < 0);
        assertEquals(0, TimeZone.ASIA_MANILA.compareByOffset(TimeZone.ASIA_SINGAPORE));
        assertTrue(TimeZone.PACIFIC_AUCKLAND.compareByOffset(TimeZone.AUSTRALIA_SYDNEY) > 0);
        assertThrows(ValueObjectException.class, () -> TimeZone.UTC.compareByOffset(null));
    }

    @Test
    void compareByOffsetAtAnInstant() {
        // London is UTC+1 in July but level with Abidjan in January
        assertEquals(0, TimeZone.EUROPE_LONDON.compareByOffset(TimeZone.AFRICA_ABIDJAN, JANUARY));
        assertTrue(TimeZone.EUROPE_LONDON.compareByOffset(TimeZone.AFRICA_ABIDJAN, JULY) > 0);
        // Sydney (+11 in January) ahead of Tokyo (+9), level with... nothing; in July +10
        assertTrue(TimeZone.AUSTRALIA_SYDNEY.compareByOffset(TimeZone.ASIA_TOKYO, JULY) > 0);
        List<TimeZone> zones = new ArrayList<>(List.of(TimeZone.ASIA_TOKYO, TimeZone.UTC, TimeZone.AMERICA_NEW_YORK,
                TimeZone.AUSTRALIA_SYDNEY));
        zones.sort(TimeZone.byOffsetAt(JANUARY));
        assertEquals(List.of(TimeZone.AMERICA_NEW_YORK, TimeZone.UTC, TimeZone.ASIA_TOKYO, TimeZone.AUSTRALIA_SYDNEY), zones);
        assertThrows(ValueObjectException.class, () -> TimeZone.UTC.compareByOffset(TimeZone.UTC, null));
        assertThrows(ValueObjectException.class, () -> TimeZone.byOffsetAt(null));
    }

    @Test
    void compareByZoneId() {
        assertTrue(TimeZone.AFRICA_ABIDJAN.compareByZoneId(TimeZone.ASIA_MANILA) < 0);
        assertThrows(ValueObjectException.class, () -> TimeZone.UTC.compareByZoneId(null));
    }
}
