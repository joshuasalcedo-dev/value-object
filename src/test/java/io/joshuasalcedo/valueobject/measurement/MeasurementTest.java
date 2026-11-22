package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MeasurementTest {

    private static void assertNumber(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "expected " + expected + " but was " + actual);
    }

    private static void assertNumber(String expected, BigDecimal actual, String message) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> message + ": expected " + expected + " but was " + actual);
    }

    @Nested
    class Distances {

        @Test
        void conversionsAreExactWhereTheyCanBe() {
            assertNumber("1000", Distance.kilometers(1).toMeters().value());
            assertNumber("0.3048", Distance.feet(1).toMeters().value());
            assertNumber("5280", Distance.miles(1).toFeet().value());
            assertNumber("1", Distance.inches(12).convertTo(DistanceUnit.FOOT).value());
            assertNumber("1E-10", Distance.of(new BigDecimal("0.0000001"), DistanceUnit.METER).toKilometers().value(),
                    "was rounded to 0.000000 km before");
            assertEquals("3.106856 mi", Distance.kilometers(5).toMiles().formatted(6));
            assertNumber("3.280839895013123359580052493438320", Distance.meters(1).toFeet().value());
            assertEquals(33, Distance.meters(1).toFeet().value().precision(), "34 significant digits, the last a dropped zero");
            assertSame(Distance.ZERO, Distance.ZERO.toMeters(), "the same unit returns itself");
        }

        @Test
        void equalityIsExactAndScaleIndependent() {
            assertEquals(Distance.meters(1), Distance.of(new BigDecimal("1.000"), DistanceUnit.METER));
            assertEquals(Distance.meters(1), Distance.kilometers(0.001));
            assertEquals(Distance.meters(1).hashCode(), Distance.kilometers(0.001).hashCode());
            assertEquals(Distance.inches(12), Distance.feet(1));
            assertEquals(Distance.inches(12).hashCode(), Distance.feet(1).hashCode());
            Distance a = Distance.of(new BigDecimal("0.0000001"), DistanceUnit.MILLIMETER);
            Distance b = Distance.of(new BigDecimal("0.0000002"), DistanceUnit.MILLIMETER);
            assertNotEquals(a, b, "both rounded to 0 m before, so they were equal");
            assertTrue(a.compareTo(b) < 0);
            assertNotEquals(Distance.ZERO, a);
            Set<Distance> set = new HashSet<>(Set.of(Distance.meters(1000)));
            assertTrue(set.contains(Distance.kilometers(1)));
            assertNotEquals(Distance.meters(1), "1 m");
        }

        @Test
        void arithmetic() {
            assertEquals("5.50 km", Distance.kilometers(5).add(Distance.meters(500)).formatted());
            assertNumber("5.5", Distance.kilometers(5).add(Distance.meters(500)).value());
            assertEquals(DistanceUnit.KILOMETER, Distance.kilometers(5).add(Distance.meters(500)).unit());
            assertNumber("4.5", Distance.kilometers(5).subtract(Distance.meters(500)).value());
            assertNumber("0", Distance.feet(1).subtract(Distance.inches(12)).value(), "exact: no rounding residue");
            assertNumber("15", Distance.meters(5).multiply(3).value());
            assertNumber("0.25", Distance.meters(1).divide(4).value());
            assertNumber("0.3333333333333333333333333333333333", Distance.meters(1).divide(3).value());
            assertNumber("4", Distance.kilometers(1).ratio(Distance.meters(250)));
            assertNumber("12", Distance.feet(1).ratio(Distance.inches(1)));
        }

        @Test
        void badInputIsAValueObjectException() {
            assertThrows(ValueObjectException.class, () -> Distance.meters(-1));
            assertThrows(ValueObjectException.class, () -> Distance.meters(Double.NaN));
            assertThrows(ValueObjectException.class, () -> Distance.of(Double.POSITIVE_INFINITY, DistanceUnit.METER));
            assertThrows(ValueObjectException.class, () -> new Distance(null, DistanceUnit.METER));
            assertThrows(ValueObjectException.class, () -> new Distance(BigDecimal.ONE, null));
            assertThrows(ValueObjectException.class, () -> Distance.of(1L, null));
            assertThrows(ValueObjectException.class, () -> Distance.meters(1).subtract(Distance.meters(2)));
            assertThrows(ValueObjectException.class, () -> Distance.meters(1).add(null));
            assertThrows(ValueObjectException.class, () -> Distance.meters(1).subtract(null));
            assertThrows(ValueObjectException.class, () -> Distance.meters(1).multiply(-1));
            assertThrows(ValueObjectException.class, () -> Distance.meters(1).multiply((BigDecimal) null));
            assertThrows(ValueObjectException.class, () -> Distance.meters(1).divide(0));
            assertThrows(ValueObjectException.class, () -> Distance.meters(1).divide(-2));
            assertThrows(ValueObjectException.class, () -> Distance.meters(1).divide((BigDecimal) null));
            assertThrows(ValueObjectException.class, () -> Distance.meters(1).ratio(Distance.ZERO));
            assertThrows(ValueObjectException.class, () -> Distance.meters(1).ratio(null));
            assertThrows(ValueObjectException.class, () -> Distance.meters(1).convertTo(null));
            assertThrows(ValueObjectException.class, () -> Distance.meters(1).compareTo(null));
        }

        @Test
        void comparingAndFormatting() {
            assertTrue(Distance.miles(1).isGreaterThan(Distance.kilometers(1)));
            assertTrue(Distance.feet(1).isLessThan(Distance.meters(1)));
            assertEquals("1.50 m", Distance.meters(1.5).toString());
            assertEquals("1.5 m", Distance.meters(1.5).formatted(1));
            assertEquals("0 mm", Distance.millimeters(0).formatted(0));
            assertTrue(Distance.zero().isZero());
            assertTrue(Distance.centimeters(1).isPositive());
            assertEquals(DistanceUnit.YARD, Distance.yards(1).unit());
        }

        @ParameterizedTest(name = "\"{0}\" -> {1}")
        @CsvSource({"km, KILOMETER", "KM, KILOMETER", "kilometers, KILOMETER", "Kilometer, KILOMETER",
                "nautical mile, NAUTICAL_MILE", "Nautical-Mile, NAUTICAL_MILE", "nmi, NAUTICAL_MILE", "in, INCH",
                "' ft ', FOOT", "feet, FOOT"})
        void units(String text, DistanceUnit unit) {
            assertEquals(unit, DistanceUnit.parse(text));
            assertEquals(unit, DistanceUnit.tryParse(text).orElseThrow());
            assertTrue(DistanceUnit.isValid(text));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "parsec", "kms", "k m"})
        void notUnits(String text) {
            assertThrows(ValueObjectException.class, () -> DistanceUnit.parse(text));
            assertTrue(DistanceUnit.tryParse(text).isEmpty());
            assertFalse(DistanceUnit.isValid(text));
        }

        @Test
        void unitProperties() {
            assertTrue(DistanceUnit.METER.isMetric());
            assertTrue(DistanceUnit.MILE.isImperial());
            assertFalse(DistanceUnit.NAUTICAL_MILE.isMetric() || DistanceUnit.NAUTICAL_MILE.isImperial());
            assertEquals(DistanceUnit.METER, DistanceUnit.FOOT.baseUnit());
            assertEquals("feet", DistanceUnit.FOOT.unitName());
            assertNumber("0.3048", DistanceUnit.FOOT.convertTo(BigDecimal.ONE, DistanceUnit.METER));
            assertThrows(ValueObjectException.class, () -> DistanceUnit.FOOT.convertTo(null, DistanceUnit.METER));
            assertThrows(ValueObjectException.class, () -> DistanceUnit.FOOT.convertTo(BigDecimal.ONE, null));
        }
    }

    @Nested
    class Weights {

        @Test
        void conversions() {
            assertNumber("4.5359237", Weight.pounds(10).toKilograms().value());
            assertNumber("1.5", Weight.grams(1500).toKilograms().value());
            assertNumber("1500", Weight.kilograms(1.5).toGrams().value());
            assertNumber("16", Weight.pounds(1).toOunces().value());
            assertEquals("2.2046 lb", Weight.kilograms(1).toPounds().formatted(4));
            assertNumber("1E-12", Weight.of(new BigDecimal("0.000001"), WeightUnit.MILLIGRAM).toKilograms().value(),
                    "was rounded to 0 before");
        }

        @Test
        void equality() {
            assertEquals(Weight.ounces(16), Weight.pounds(1));
            assertEquals(Weight.stones(1), Weight.pounds(14));
            assertEquals(Weight.stones(1).hashCode(), Weight.pounds(14).hashCode());
            assertEquals(Weight.metricTons(1), Weight.kilograms(1000));
            assertEquals(Weight.milligrams(1000), Weight.grams(1));
            Weight a = Weight.of(new BigDecimal("0.0000001"), WeightUnit.MILLIGRAM);
            Weight b = Weight.of(new BigDecimal("0.0000004"), WeightUnit.MILLIGRAM);
            assertNotEquals(a, b, "both rounded to 0 kg before");
            assertTrue(a.isLessThan(b));
            assertTrue(Weight.pounds(1).isGreaterThan(Weight.kilograms(0.45)));
        }

        @Test
        void arithmetic() {
            assertEquals("10.50 lb", Weight.pounds(10).add(Weight.ounces(8)).formatted());
            assertNumber("10.5", Weight.pounds(10).add(Weight.ounces(8)).value());
            assertNumber("0", Weight.pounds(1).subtract(Weight.ounces(16)).value());
            assertNumber("2", Weight.kilograms(1).multiply(new BigDecimal("2")).value());
            assertNumber("0.5", Weight.kilograms(1).divide(2).value());
            assertNumber("16", Weight.pounds(1).ratio(Weight.ounces(1)));
            assertThrows(ValueObjectException.class, () -> Weight.ounces(1).subtract(Weight.pounds(1)));
            assertThrows(ValueObjectException.class, () -> Weight.kilograms(1).ratio(Weight.zero()));
        }

        @Test
        void badInputIsAValueObjectException() {
            assertThrows(ValueObjectException.class, () -> Weight.kilograms(-0.1));
            assertThrows(ValueObjectException.class, () -> Weight.kilograms(Double.NaN));
            assertThrows(ValueObjectException.class, () -> new Weight(null, WeightUnit.GRAM));
            assertThrows(ValueObjectException.class, () -> new Weight(BigDecimal.ONE, null));
            assertThrows(ValueObjectException.class, () -> Weight.grams(1).add(null));
            assertThrows(ValueObjectException.class, () -> Weight.grams(1).convertTo(null));
            assertThrows(ValueObjectException.class, () -> Weight.grams(1).divide(0));
            assertThrows(ValueObjectException.class, () -> Weight.grams(1).compareTo(null));
        }

        @ParameterizedTest(name = "\"{0}\" -> {1}")
        @CsvSource({"kg, KILOGRAM", "KG, KILOGRAM", "LB, POUND", "Pounds, POUND", "metric ton, METRIC_TON", "t, METRIC_TON",
                "us ton, US_TON", "ton, US_TON", "st, STONE", "Oz, OUNCE"})
        void units(String text, WeightUnit unit) {
            assertEquals(unit, WeightUnit.parse(text));
            assertEquals(unit, WeightUnit.tryParse(text).orElseThrow());
            assertTrue(WeightUnit.isValid(text));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "grain", "kgz"})
        void notUnits(String text) {
            assertThrows(ValueObjectException.class, () -> WeightUnit.parse(text));
            assertTrue(WeightUnit.tryParse(text).isEmpty());
            assertFalse(WeightUnit.isValid(text));
        }

        @Test
        void unitProperties() {
            assertTrue(WeightUnit.GRAM.isMetric());
            assertTrue(WeightUnit.STONE.isImperial());
            assertEquals(WeightUnit.KILOGRAM, WeightUnit.OUNCE.baseUnit());
        }
    }

    @Nested
    class FileSizes {

        @Test
        void conversions() {
            assertEquals("1.50 MiB", FileSize.bytes(1_572_864).toHumanString());
            assertNumber("1.572864", FileSize.bytes(1_572_864).toMegabytes().value());
            assertNumber("1536", FileSize.bytes(1_572_864).toKibibytes().value());
            assertNumber("1.5", FileSize.bytes(1_572_864).toMebibytes().value());
            assertNumber("1", FileSize.gibibytes(1).toMebibytes().value().divide(new BigDecimal("1024")));
            assertNumber("0.001", FileSize.megabytes(1).toGigabytes().value());
            assertNumber("1000", FileSize.megabytes(1).toKilobytes().value());
            assertNumber("1048576", FileSize.mebibytes(1).toBytes().value());
            assertNumber("0.125", FileSize.of(1, DataUnit.BIT).toBytes().value());
            assertTrue(FileSize.bytes(1).convertTo(DataUnit.PEBIBYTE).value().signum() > 0, "was 0.000000 PiB before");
            assertNumber("8.8817841970012523233890533447265625E-16", FileSize.bytes(1).convertTo(DataUnit.PEBIBYTE).value());
            assertEquals(FileSize.bytes(1), FileSize.bytes(1).convertTo(DataUnit.PEBIBYTE), "and converts back exactly");
        }

        @Test
        void equality() {
            assertEquals(FileSize.kibibytes(1), FileSize.bytes(1024));
            assertEquals(FileSize.kibibytes(1).hashCode(), FileSize.bytes(1024).hashCode());
            assertEquals(FileSize.of(1, DataUnit.BIT), FileSize.of(new BigDecimal("0.125"), DataUnit.BYTE));
            assertNotEquals(FileSize.megabytes(1), FileSize.mebibytes(1));
            assertTrue(FileSize.megabytes(1).isLessThan(FileSize.mebibytes(1)));
            assertTrue(FileSize.terabytes(1).isGreaterThan(FileSize.gigabytes(999)));
            FileSize tiny = FileSize.of(new BigDecimal("1E-10"), DataUnit.KIBIBYTE);
            assertNotEquals(FileSize.ZERO, tiny, "rounded to 0 bytes before, so it equalled ZERO");
            assertTrue(tiny.compareTo(FileSize.ZERO) > 0);
        }

        @Test
        void humanReadable() {
            assertEquals("500.00 B", FileSize.bytes(500).toHumanString());
            assertEquals("1.00 KiB", FileSize.bytes(1024).toHumanString());
            assertEquals("1.00 GiB", FileSize.gibibytes(1).toHumanString());
            assertEquals("1.00 TiB", FileSize.of(1, DataUnit.TEBIBYTE).toHumanString());
            assertEquals("2.00 PiB", FileSize.of(2, DataUnit.PEBIBYTE).toHumanString());
            assertEquals("953.67 MiB", FileSize.gigabytes(1).toHumanString());
        }

        @Test
        void arithmetic() {
            assertEquals(FileSize.bytes(2048), FileSize.kibibytes(1).add(FileSize.bytes(1024)));
            assertEquals(DataUnit.KIBIBYTE, FileSize.kibibytes(1).add(FileSize.bytes(1024)).unit());
            assertNumber("0.5", FileSize.kibibytes(1).subtract(FileSize.bytes(512)).value());
            assertNumber("3", FileSize.megabytes(1).multiply(3).value());
            assertNumber("0.5", FileSize.megabytes(1).divide(2).value());
            assertThrows(ValueObjectException.class, () -> FileSize.bytes(1).subtract(FileSize.bytes(2)));
            assertThrows(ValueObjectException.class, () -> FileSize.bytes(1).divide(0));
            assertThrows(ValueObjectException.class, () -> FileSize.bytes(1).multiply(-1));
        }

        @Test
        void badInputIsAValueObjectException() {
            assertThrows(ValueObjectException.class, () -> FileSize.bytes(-1));
            assertThrows(ValueObjectException.class, () -> FileSize.megabytes(Double.NaN));
            assertThrows(ValueObjectException.class, () -> new FileSize(null, DataUnit.BYTE));
            assertThrows(ValueObjectException.class, () -> new FileSize(BigDecimal.ONE, null));
            assertThrows(ValueObjectException.class, () -> FileSize.bytes(1).add(null));
            assertThrows(ValueObjectException.class, () -> FileSize.bytes(1).convertTo(null));
            assertThrows(ValueObjectException.class, () -> FileSize.bytes(1).compareTo(null));
        }
    }

    @Nested
    class DataUnits {

        @ParameterizedTest(name = "\"{0}\" -> {1}")
        @CsvSource({"B, BYTE", "b, BIT", "KB, KILOBYTE", "kB, KILOBYTE", "MB, MEGABYTE", "mB, MEGABYTE", "GB, GIGABYTE",
                "gB, GIGABYTE", "KiB, KIBIBYTE", "KIB, KIBIBYTE", "kiB, KIBIBYTE", "MIB, MEBIBYTE", "TIB, TEBIBYTE",
                "megabytes, MEGABYTE", "Megabytes, MEGABYTE", "MEGABYTE, MEGABYTE", "bits, BIT", "Bytes, BYTE",
                "' MB ', MEGABYTE", "pebibyte, PEBIBYTE"})
        void readsSymbolsAndNames(String text, DataUnit unit) {
            assertEquals(unit, DataUnit.parse(text));
            assertEquals(unit, DataUnit.tryParse(text).orElseThrow());
            assertTrue(DataUnit.isValid(text));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "mb", "Mb", "kb", "Kb", "gb", "kib", "Mbit", "XB", "bytez"})
        void bitsAndBytesStayDistinct(String text) {
            assertThrows(ValueObjectException.class, () -> DataUnit.parse(text));
            assertTrue(DataUnit.tryParse(text).isEmpty());
            assertFalse(DataUnit.isValid(text));
        }

        @Test
        void properties() {
            assertTrue(DataUnit.KIBIBYTE.isBinary());
            assertTrue(DataUnit.KILOBYTE.isDecimal());
            assertFalse(DataUnit.BYTE.isBinary() || DataUnit.BYTE.isDecimal());
            assertEquals(DataUnit.MEBIBYTE, DataUnit.MEGABYTE.toBinaryEquivalent());
            assertEquals(DataUnit.MEGABYTE, DataUnit.MEBIBYTE.toDecimalEquivalent());
            assertEquals(DataUnit.BIT, DataUnit.BIT.toBinaryEquivalent());
            assertEquals(DataUnit.BYTE, DataUnit.BIT.baseUnit());
            for (DataUnit u : DataUnit.values()) assertEquals(u, DataUnit.parse(u.symbol()), u.symbol());
        }
    }
}
