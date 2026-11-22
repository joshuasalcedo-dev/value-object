package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.ValueObject;

import java.math.BigDecimal;

/**
 * Enumeration of distance units with conversion factors.
 * Base unit is meters.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public enum DistanceUnit implements ValueObject, MeasurementUnit<DistanceUnit> {

    // Metric units
    MILLIMETER("mm", "millimeters", new BigDecimal("0.001")),
    CENTIMETER("cm", "centimeters", new BigDecimal("0.01")),
    METER("m", "meters", BigDecimal.ONE),
    KILOMETER("km", "kilometers", new BigDecimal("1000")),

    // Imperial units
    INCH("in", "inches", new BigDecimal("0.0254")),
    FOOT("ft", "feet", new BigDecimal("0.3048")),
    YARD("yd", "yards", new BigDecimal("0.9144")),
    MILE("mi", "miles", new BigDecimal("1609.344")),

    // Nautical
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
     * Checks if this is an imperial unit.
     *
     * @return true if imperial
     */
    public boolean isImperial() {
        return this == INCH || this == FOOT ||
               this == YARD || this == MILE;
    }
}
