package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;
import java.util.Optional;
import io.joshuasalcedo.valueobject.ValueObject;

import java.math.BigDecimal;

/**
 * Units of distance, each with its factor to meters, the base unit.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public enum DistanceUnit implements ValueObject, MeasurementUnit<DistanceUnit> {

    // Metric units
    /** millimeters ({@code mm}). */
    MILLIMETER("mm", "millimeters", new BigDecimal("0.001")),
    /** centimeters ({@code cm}). */
    CENTIMETER("cm", "centimeters", new BigDecimal("0.01")),
    /** meters ({@code m}). */
    METER("m", "meters", BigDecimal.ONE),
    /** kilometers ({@code km}). */
    KILOMETER("km", "kilometers", new BigDecimal("1000")),

    // Imperial units
    /** inches ({@code in}). */
    INCH("in", "inches", new BigDecimal("0.0254")),
    /** feet ({@code ft}). */
    FOOT("ft", "feet", new BigDecimal("0.3048")),
    /** yards ({@code yd}). */
    YARD("yd", "yards", new BigDecimal("0.9144")),
    /** miles ({@code mi}). */
    MILE("mi", "miles", new BigDecimal("1609.344")),

    // Nautical
    /** nautical miles ({@code nmi}). */
    NAUTICAL_MILE("nmi", "nautical miles", new BigDecimal("1852"));

    private final String symbol;
    private final String unitName;
    private final BigDecimal toMetersFactor;

    DistanceUnit(String symbol, String unitName, BigDecimal toMetersFactor) {
        this.symbol = symbol;
        this.unitName = unitName;
        this.toMetersFactor = toMetersFactor;
    }

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public String unitName() {
        return unitName;
    }

    @Override
    public BigDecimal toBaseUnitFactor() {
        return toMetersFactor;
    }

    @Override
    public DistanceUnit baseUnit() {
        return METER;
    }

    /**
     * Checks if this is a metric unit.
     *
     * @return true if metric
     */
    public boolean isMetric() {
        return this == MILLIMETER || this == CENTIMETER ||
               this == METER || this == KILOMETER;
    }

    /**
     * Checks if this is an imperial unit: inch, foot, yard or mile. The nautical mile is neither metric nor
     * imperial.
     *
     * @return true if imperial
     */
    public boolean isImperial() {
        return this == INCH || this == FOOT ||
               this == YARD || this == MILE;
    }

    /**
     * Looks up a unit by its constant name (spaces, dashes and dots allowed, e.g. {@code "nautical mile"}), its symbol
     * or its plural name, ignoring surrounding spaces. Case is ignored.
     *
     * @param text its symbol or name, e.g. {@code "km"}
     * @return the unit
     * @throws ValueObjectException if it is {@code null} or not a unit
     */
    public static DistanceUnit parse(String text) {
        if (text == null) throw new ValueObjectException("A DistanceUnit is required");
        String t = text.strip();
        String key = t.replace(".", "").replace(' ', '_').replace('-', '_');
        for (DistanceUnit v : values()) {
            if (v.name().equalsIgnoreCase(key) || v.symbol().equalsIgnoreCase(t) || v.unitName().equalsIgnoreCase(t)) return v;
        }
        throw new ValueObjectException("Not a DistanceUnit: '" + text + "'");
    }

    /**
     * Looks up a unit like {@link #parse(String)}, without throwing.
     *
     * @param text any text
     * @return it as a {@link DistanceUnit}, or empty when it is not one; never throws
     */
    public static Optional<DistanceUnit> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether text names a unit, as {@link #parse(String)} reads it.
     *
     * @param text any text
     * @return whether it is a {@link DistanceUnit}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
