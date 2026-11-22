package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.Count;
import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.contact.Link;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.network.DomainName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MeasurementParseTest {

    @Test
    void distances() {
        assertEquals(Distance.of(new BigDecimal("5.2"), DistanceUnit.KILOMETER), Distance.parse("5.2 km"));
        assertEquals(Distance.of(1200, DistanceUnit.METER), Distance.parse("1,200m"));
        assertTrue(Distance.isValid("  3 mi "));
        assertFalse(Distance.isValid("km 5"));
        assertFalse(Distance.isValid(null));
        assertThrows(ValueObjectException.class, () -> Distance.parse("5 parsecs"));
    }

    @Test
    void weights() {
        assertEquals(Weight.of(new BigDecimal("2.5"), WeightUnit.KILOGRAM), Weight.parse("2.5 kg"));
        assertEquals(Weight.parse("2500 g"), Weight.parse("2.5 kg"), "same weight in another unit");
        assertTrue(Weight.tryParse("heavy").isEmpty());
    }

    @Test
    void fileSizes() {
        assertEquals(FileSize.of(new BigDecimal("1.5"), DataUnit.GIGABYTE), FileSize.parse("1.5 GB"));
        assertThrows(ValueObjectException.class, () -> FileSize.parse(null));
    }

    @Test
    void parseThroughValueObject() {
        assertEquals(Weight.parse("2.5 kg"), ValueObject.Parse.Weight("2.5 kg"));
        assertEquals(Count.of(5_000_000_000L), ValueObject.Parse.Count(java.math.BigInteger.valueOf(5_000_000_000L)),
                "a long count is not cut down to an int");
    }

    @Test
    void leftovers() {
        assertEquals("mailto:", Link.parse("mailto:jane@example.com").baseUrl());
        assertEquals("https://northwind.example", Link.parse("https://northwind.example/a?b=1").baseUrl());
        assertThrows(ValueObjectException.class, () -> DomainName.parse("a.example").isSubdomainOf(null));
    }
}
