package io.joshuasalcedo.valueobject.contact;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import io.joshuasalcedo.valueobject.geo.Country;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    void trimsPartsAndTurnsBlanksIntoNull() {
        Address a = Address.full("  12 Mabini St ", " ", " Makati ", "", " 1200 ", Country.PHILIPPINES);
        assertEquals("12 Mabini St", a.street());
        assertNull(a.addressLine2());
        assertEquals("Makati", a.city());
        assertNull(a.stateOrProvince());
        assertEquals("1200", a.postalCode());
        assertTrue(a.state().isEmpty());
        assertEquals("1200", a.zipCode().orElseThrow());
        assertTrue(a.line2().isEmpty());
    }

    @Test
    void requiresCountryWithValueObjectException() {
        assertThrows(ValueObjectException.class, () -> Address.of(null));
        assertThrows(ValueObjectException.class, () -> Address.builder(null));
        assertThrows(ValueObjectException.class, () -> Address.of(Country.PHILIPPINES).withCountry(null));
    }

    @Test
    void formatsWithCityStateAndPostalCode() {
        Address a = Address.builder(Country.UNITED_STATES)
                .street("1 Market St").addressLine2("Suite 300").city("San Francisco")
                .stateOrProvince("CA").postalCode("94105").build();
        assertEquals("1 Market St, Suite 300, San Francisco, CA 94105, United States", a.singleLine());
        assertEquals("1 Market St\nSuite 300\nSan Francisco, CA 94105\nUnited States", a.format());
        assertTrue(a.isComplete());
    }

    @Test
    void postalCodeWithoutCityOrStateHasNoStrayPunctuation() {
        Address a = Address.of("12 Mabini St", null, null, "1200", Country.PHILIPPINES);
        assertEquals("12 Mabini St, 1200, Philippines", a.singleLine());
        assertEquals("1200 | Philippines", Address.builder(Country.PHILIPPINES).postalCode("1200").build()
                .formatWith(" | "));
        assertEquals("Metro Manila 1200, Philippines",
                Address.builder(Country.PHILIPPINES).stateOrProvince("Metro Manila").postalCode("1200").build()
                        .singleLine());
    }

    @Test
    void hasDetailsCountsTheSecondAddressLine() {
        assertFalse(Address.of(Country.PHILIPPINES).hasDetails());
        Address onlyLine2 = Address.of(Country.PHILIPPINES).withAddressLine2("Unit 5B");
        assertTrue(onlyLine2.hasDetails());
        assertEquals("Unit 5B, Philippines", onlyLine2.toString());
        assertEquals("Philippines", Address.of(Country.PHILIPPINES).toString());
    }

    @Test
    void withMethodsReplaceOnePart() {
        Address a = Address.of("1 St", "Old", Country.PHILIPPINES);
        assertEquals("New", a.withCity("New").city());
        assertEquals("2 St", a.withStreet("2 St").street());
        assertEquals("NCR", a.withStateOrProvince("NCR").stateOrProvince());
        assertEquals("1000", a.withPostalCode("1000").postalCode());
        assertEquals(Country.JAPAN, a.withCountry(Country.JAPAN).country());
        assertNull(a.withCity(null).city());
        assertFalse(a.withCity(null).isComplete());
        assertEquals(a, Address.of("1 St", "Old", null, Country.PHILIPPINES));
    }
}
