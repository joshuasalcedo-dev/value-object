package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UnitOfMeasureTest {

    @ParameterizedTest(name = "{0}")
    @EnumSource(UnitOfMeasure.class)
    void everySymbolReadsBackAsItsUnit(UnitOfMeasure unit) {
        assertEquals(unit, UnitOfMeasure.parse(unit.symbol()), unit.symbol());
        assertEquals(unit, UnitOfMeasure.find(unit.symbol()).orElseThrow());
        assertEquals(unit, UnitOfMeasure.parse(unit.code()), "its Rec 20 code");
        assertEquals(unit, UnitOfMeasure.fromCode(unit.code()).orElseThrow());
        assertEquals(unit.symbol(), unit.toString());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(UnitOfMeasure.class)
    void everyQuantityRoundTripsThroughItsText(UnitOfMeasure unit) {
        for (String amount : new String[]{"0", "1", "2.5", "1200", "0.001"}) {
            Quantity q = Quantity.of(new BigDecimal(amount), unit);
            assertEquals(q, Quantity.parse(q.toString()), () -> "\"" + q + "\" did not read back as " + unit);
        }
    }

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource({"st, STONE", "ac, ACRE", "d, DAY", "y, YEAR", "B, BYTE", "gross, GROSS", "gr, GRAM", "g, GRAM",
            "GRO, GROSS", "gro, GROSS", "Gross, GROSS", "kgs., KILOGRAM", "hrs., HOUR", "sq. m, SQUARE_METRE",
            "m³, CUBIC_METRE", "cm², SQUARE_CENTIMETRE", "fl oz, US_FLUID_OUNCE", "Pieces, PIECE",
            "kgm, KILOGRAM", "KWH, KILOWATT_HOUR", "kWh, KILOWATT_HOUR", "units, ONE", "boxes, BOX"})
    void readsPrintedSpellings(String text, UnitOfMeasure unit) {
        assertEquals(unit, UnitOfMeasure.parse(text));
        assertEquals(unit, UnitOfMeasure.tryParse(text).orElseThrow());
        assertTrue(UnitOfMeasure.isValid(text));
    }

    @Test
    void grossNoLongerPrintsAsGrams() {
        assertEquals("gross", UnitOfMeasure.GROSS.symbol());
        assertEquals("2 gross", Quantity.of(2, UnitOfMeasure.GROSS).toString());
        assertEquals(UnitOfMeasure.GROSS, Quantity.parse("2 gross").unit());
        assertEquals(UnitOfMeasure.GRAM, Quantity.parse("2 gr").unit());
    }

    @ParameterizedTest(name = "\"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "zork", "kgz", "XYZ", "sq"})
    void rejectsUnknownUnits(String text) {
        assertThrows(ValueObjectException.class, () -> UnitOfMeasure.parse(text));
        assertTrue(UnitOfMeasure.tryParse(text).isEmpty());
        assertTrue(UnitOfMeasure.find(text).isEmpty());
        assertFalse(UnitOfMeasure.isValid(text));
    }

    @Test
    void fromCode() {
        assertEquals(UnitOfMeasure.KILOGRAM, UnitOfMeasure.fromCode(" kgm ").orElseThrow());
        assertTrue(UnitOfMeasure.fromCode(null).isEmpty());
        assertTrue(UnitOfMeasure.fromCode("kg").isEmpty(), "a symbol is not a code");
    }

    @ParameterizedTest(name = "{1} {0} = {3} {2}")
    @CsvSource({"KILOGRAM, 2.5, GRAM, 2500", "DOZEN, 1, PIECE, 12", "GROSS, 1, PIECE, 144", "GROSS, 1, DOZEN, 12",
            "HOUR, 1.5, MINUTE, 90", "POUND, 1, OUNCE, 16", "MILE, 1, FOOT, 5280", "HECTARE, 1, SQUARE_METRE, 10000",
            "CUBIC_METRE, 1, LITRE, 1000", "TERABYTE, 1, MEGABYTE, 1000000", "WEEK, 1, DAY, 7", "GRAM, 1, KILOGRAM, 0.001",
            "MINUTE, 1, HOUR, 0.016666666667"})
    void converts(UnitOfMeasure from, String amount, UnitOfMeasure to, String expected) {
        BigDecimal out = from.convert(new BigDecimal(amount), to);
        assertEquals(0, new BigDecimal(expected).compareTo(out), () -> "was " + out);
        assertEquals(out, from.convertTo(new BigDecimal(amount), to));
    }

    @Test
    void conversionsThatDoNotWork() {
        assertThrows(ValueObjectException.class, () -> UnitOfMeasure.KILOGRAM.convert(BigDecimal.ONE, UnitOfMeasure.LITRE));
        assertThrows(ValueObjectException.class, () -> UnitOfMeasure.MONTH.convert(BigDecimal.ONE, UnitOfMeasure.DAY));
        assertThrows(ValueObjectException.class, () -> UnitOfMeasure.BOX.convert(BigDecimal.ONE, UnitOfMeasure.PIECE));
        assertThrows(ValueObjectException.class, () -> UnitOfMeasure.PAIR.convert(BigDecimal.ONE, UnitOfMeasure.PIECE));
        assertThrows(ValueObjectException.class, () -> UnitOfMeasure.KILOGRAM.convert(null, UnitOfMeasure.GRAM));
        assertThrows(ValueObjectException.class, () -> UnitOfMeasure.KILOGRAM.convert(BigDecimal.ONE, null));
        assertEquals(BigDecimal.TEN, UnitOfMeasure.MONTH.convert(BigDecimal.TEN, UnitOfMeasure.MONTH), "same unit");
    }

    @Test
    void properties() {
        assertEquals(UnitOfMeasure.Category.WEIGHT, UnitOfMeasure.STONE.category());
        assertEquals(UnitOfMeasure.KILOGRAM, UnitOfMeasure.STONE.baseUnit());
        assertEquals(UnitOfMeasure.BOX, UnitOfMeasure.BOX.baseUnit());
        assertEquals(UnitOfMeasure.PIECE, UnitOfMeasure.DOZEN.baseUnit());
        assertEquals(UnitOfMeasure.SECOND, UnitOfMeasure.YEAR.baseUnit());
        assertTrue(UnitOfMeasure.DAY.convertible());
        assertFalse(UnitOfMeasure.YEAR.convertible());
        assertEquals(0, BigDecimal.ZERO.compareTo(UnitOfMeasure.BOX.toBaseUnitFactor()));
        assertEquals(0, new BigDecimal("144").compareTo(UnitOfMeasure.GROSS.toBaseUnitFactor()));
        assertEquals("stone", UnitOfMeasure.STONE.unitName());
        assertEquals("STI", UnitOfMeasure.STONE.code());
    }

    @Test
    void bridgesToTheOtherUnitTypes() {
        assertEquals(WeightUnit.STONE, UnitOfMeasure.STONE.weightUnit().orElseThrow());
        assertEquals(WeightUnit.METRIC_TON, UnitOfMeasure.TONNE.weightUnit().orElseThrow());
        assertTrue(UnitOfMeasure.METRE.weightUnit().isEmpty());
        assertEquals(DistanceUnit.MILE, UnitOfMeasure.MILE.distanceUnit().orElseThrow());
        assertTrue(UnitOfMeasure.LITRE.distanceUnit().isEmpty());
        assertEquals(DataUnit.MEGABYTE, UnitOfMeasure.MEGABYTE.dataUnit().orElseThrow());
        assertTrue(UnitOfMeasure.HOUR.dataUnit().isEmpty());
        for (UnitOfMeasure u : UnitOfMeasure.values()) {
            u.weightUnit().ifPresent(w -> assertEquals(0, w.toBaseUnitFactor().compareTo(u.toBaseUnitFactor()), u.name()));
            u.distanceUnit().ifPresent(d -> assertEquals(0, d.toBaseUnitFactor().compareTo(u.toBaseUnitFactor()), u.name()));
            u.dataUnit().ifPresent(d -> assertEquals(0, d.toBaseUnitFactor().compareTo(u.toBaseUnitFactor()), u.name()));
        }
    }
}
