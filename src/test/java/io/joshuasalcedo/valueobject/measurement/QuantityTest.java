package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class QuantityTest {

    @ParameterizedTest(name = "\"{0}\" -> {1} {2}")
    @CsvSource({"10 pcs, 10, PIECE", "2.5 kg, 2.5, KILOGRAM", "2.5kg, 2.5, KILOGRAM", "'1,200 m', 1200, METRE",
            "Nos 3, 3, PIECE", "12, 12, ONE", "3 x box, 3, BOX", ".5 L, 0.5, LITRE", "  4 hrs  , 4, HOUR",
            "2 fl oz, 2, US_FLUID_OUNCE", "1 st, 1, STONE", "7 d, 7, DAY", "512 B, 512, BYTE", "2.50 kg, 2.5, KILOGRAM"})
    void readsPrintedQuantities(String text, String amount, UnitOfMeasure unit) {
        Quantity q = Quantity.parse(text);
        assertEquals(new BigDecimal(amount), q.amount());
        assertEquals(unit, q.unit());
        assertEquals(q, Quantity.tryParse(text).orElseThrow());
        assertTrue(Quantity.isValid(text));
    }

    @ParameterizedTest(name = "\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "-1 kg", "kg", "2 zork", "1.2.3 kg", "abc", "2 kg extra words here and more"})
    void rejectsAnythingElse(String text) {
        assertThrows(ValueObjectException.class, () -> Quantity.parse(text));
        assertTrue(Quantity.tryParse(text).isEmpty());
        assertFalse(Quantity.isValid(text));
    }

    @Test
    void normalisesTheAmount() {
        assertEquals(new BigDecimal("2.5"), Quantity.of(new BigDecimal("2.50"), UnitOfMeasure.KILOGRAM).amount());
        assertEquals(new BigDecimal("1000"), Quantity.of(new BigDecimal("1E+3"), UnitOfMeasure.GRAM).amount());
        assertEquals("1000 g", Quantity.of(new BigDecimal("1E+3"), UnitOfMeasure.GRAM).toString());
        assertEquals(Quantity.of(new BigDecimal("2.50"), UnitOfMeasure.KILOGRAM), Quantity.of(new BigDecimal("2.5"), UnitOfMeasure.KILOGRAM));
        assertEquals(UnitOfMeasure.ONE, new Quantity(BigDecimal.ONE, null).unit());
        assertEquals(UnitOfMeasure.ONE, Quantity.of(BigDecimal.TEN).unit());
        assertEquals("10", Quantity.of(BigDecimal.TEN).toString());
        assertEquals("2.5 kg", Quantity.parse("2.5 kgs").toString());
    }

    @Test
    void badValuesAreRejected() {
        assertThrows(ValueObjectException.class, () -> Quantity.of(null, UnitOfMeasure.KILOGRAM));
        assertThrows(ValueObjectException.class, () -> Quantity.of(-1, UnitOfMeasure.KILOGRAM));
        assertThrows(ValueObjectException.class, () -> Quantity.of(BigDecimal.ONE, UnitOfMeasure.KILOGRAM).times(new BigDecimal("-1")));
        assertThrows(ValueObjectException.class, () -> Quantity.of(BigDecimal.ONE, UnitOfMeasure.KILOGRAM).times(null));
        assertThrows(ValueObjectException.class, () -> Quantity.of(BigDecimal.ONE, UnitOfMeasure.KILOGRAM).plus(null));
        assertThrows(ValueObjectException.class, () -> Quantity.of(BigDecimal.ONE, UnitOfMeasure.KILOGRAM).to(null));
        assertThrows(ValueObjectException.class, () -> Quantity.of(BigDecimal.ONE, UnitOfMeasure.KILOGRAM).compareTo(null));
    }

    @Test
    void arithmetic() {
        assertEquals(Quantity.parse("2000 g"), Quantity.parse("2 kg").to(UnitOfMeasure.GRAM));
        assertEquals(Quantity.parse("2.5 kg"), Quantity.parse("2 kg").plus(Quantity.parse("500 g")));
        assertEquals(Quantity.parse("36 pcs"), Quantity.parse("12 pcs").plus(Quantity.parse("2 doz")));
        assertEquals(Quantity.parse("7.5 kg"), Quantity.parse("2.5 kg").times(new BigDecimal("3")));
        assertThrows(ValueObjectException.class, () -> Quantity.parse("2 kg").plus(Quantity.parse("1 L")));
        assertThrows(ValueObjectException.class, () -> Quantity.parse("2 box").to(UnitOfMeasure.PIECE));
        assertTrue(Quantity.parse("0 kg").isZero());
        assertFalse(Quantity.parse("1 kg").isZero());
    }

    @Test
    void compareToConvertsButEqualsDoesNot() {
        Quantity kg = Quantity.parse("2 kg");
        Quantity grams = Quantity.parse("2000 g");
        assertEquals(0, kg.compareTo(grams), "the same weight");
        assertNotEquals(kg, grams, "but not the same value: equals compares amount and unit");
        TreeSet<Quantity> sorted = new TreeSet<>();
        sorted.add(kg);
        sorted.add(grams);
        assertEquals(1, sorted.size(), "as documented, the ordering is not consistent with equals");
        assertTrue(Quantity.parse("1 kg").compareTo(Quantity.parse("900 g")) > 0);
        assertTrue(Quantity.parse("1 doz").compareTo(Quantity.parse("13 pcs")) < 0);
    }

    @Test
    void unitsThatDoNotConvertCompareByAmountThenUnit() {
        assertTrue(Quantity.parse("2 box").compareTo(Quantity.parse("3 pcs")) < 0);
        assertTrue(Quantity.parse("3 kg").compareTo(Quantity.parse("3 L")) < 0, "KILOGRAM is declared before LITRE");
        assertTrue(Quantity.parse("1 month").compareTo(Quantity.parse("1 day")) > 0, "by declaration order, not length");
    }
}
