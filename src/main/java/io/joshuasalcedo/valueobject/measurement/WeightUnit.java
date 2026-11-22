package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.ValueObject;

import java.math.BigDecimal;

/**
 * Enumeration of weight/mass units with conversion factors.
 * Base unit is kilograms.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public enum WeightUnit implements ValueObject, MeasurementUnit<WeightUnit> {

    // Metric units
    MILLIGRAM("mg", "milligrams", new BigDecimal("0.000001")),
    GRAM("g", "grams", new BigDecimal("0.001")),
    KILOGRAM("kg", "kilograms", BigDecimal.ONE),
    METRIC_TON("t", "metric tons", new BigDecimal("1000")),

    // Imperial units
    OUNCE("oz", "ounces", new BigDecimal("0.028349523125")),
    POUND("lb", "pounds", new BigDecimal("0.45359237")),
    STONE("st", "stones", new BigDecimal("6.35029318")),
    US_TON("ton", "US tons", new BigDecimal("907.18474"));

    private final String symbol;
    private final String unitName;
    private final BigDecimal toKilogramsFactor;

    WeightUnit(String symbol, String unitName, BigDecimal toKilogramsFactor) {
        this.symbol = symbol;
        this.unitName = unitName;
        this.toKilogramsFactor = toKilogramsFactor;
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
        return toKilogramsFactor;
    }

    @Override
    public WeightUnit baseUnit() {
        return KILOGRAM;
    }

    /**
     * Checks if this is a metric unit.
     *
     * @return true if metric
     */
    public boolean isMetric() {
        return this == MILLIGRAM || this == GRAM ||
               this == KILOGRAM || this == METRIC_TON;
    }

    /**
     * Checks if this is an imperial unit.
     *
     * @return true if imperial
     */
    public boolean isImperial() {
        return this == OUNCE || this == POUND ||
               this == STONE || this == US_TON;
    }
}
