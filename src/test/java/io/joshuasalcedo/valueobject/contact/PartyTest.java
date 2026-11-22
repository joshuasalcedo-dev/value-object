package io.joshuasalcedo.valueobject.contact;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PartyTest {

    @Test
    void stripsNameAndDefaultsOptionals() {
        Party p = new Party("  Northwind Cloud ", null, null, null, null, null, null);
        assertEquals("Northwind Cloud", p.name());
        assertTrue(p.address().isEmpty());
        assertTrue(p.email().isEmpty());
        assertTrue(p.phone().isEmpty());
        assertTrue(p.website().isEmpty());
        assertTrue(p.taxId().isEmpty());
        assertEquals(List.of(), p.lines());
    }

    @Test
    void requiresAName() {
        assertThrows(ValueObjectException.class, () -> Party.of(null));
        assertThrows(ValueObjectException.class, () -> Party.of("  "));
    }

    @Test
    void personOnlyForNamesThatLookLikePeople() {
        assertEquals(new FullName("Andrea", "Lim", "Bautista"), Party.of("Ms. Andrea Lim Bautista").person().orElseThrow());
        assertTrue(Party.of("Skyline Telecom, Inc.").person().isEmpty());
        assertFalse(Party.of("Northwind Cloud").isPerson());
        assertFalse(Party.of("Cher").isPerson(), "a single word is not enough for a party to be a person");
        assertTrue(Party.of("Maria Santos Dizon").isPerson());
    }

    @Test
    void toStringShowsNameAddressAndEmail() {
        Party p = new Party("Northwind", Optional.empty(), Optional.of(Email.parse("bill@northwind.example")),
                Optional.empty(), Optional.empty(), Optional.empty(), List.of("Northwind"));
        assertEquals("Northwind <bill@northwind.example>", p.toString());
        assertEquals(List.of("Northwind"), Party.of("Northwind").lines());
    }
}
