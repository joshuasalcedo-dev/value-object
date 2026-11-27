package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.jspecify.annotations.Nullable;
import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import java.math.BigDecimal;

/**
 * Units of weight (mass), each with its factor to kilograms, the base unit.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public enum WeightUnit implements ValueObject, MeasurementUnit<WeightUnit> {

    // Metric units
    /** milligrams ({@code mg}). */
    MILLIGRAM("mg", "milligrams", new BigDecimal("0.000001")),
    /** grams ({@code g}). */
    GRAM("g", "grams", new BigDecimal("0.001")),
    /** kilograms ({@code kg}). */
    KILOGRAM("kg", "kilograms", BigDecimal.ONE),
    /** metric tons ({@code t}). */
    METRIC_TON("t", "metric tons", new BigDecimal("1000")),

    // Imperial units
    /** ounces ({@code oz}, avoirdupois). */
    OUNCE("oz", "ounces", new BigDecimal("0.028349523125")),
    /** pounds ({@code lb}). */
    POUND("lb", "pounds", new BigDecimal("0.45359237")),
    /** stones ({@code st}). */
    STONE("st", "stones", new BigDecimal("6.35029318")),
    /** US tons ({@code ton}). */
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
     * Checks if this is an imperial or US customary unit: ounce, pound, stone or US ton.
     *
     * @return true if imperial
     */
    public boolean isImperial() {
        return this == OUNCE || this == POUND ||
               this == STONE || this == US_TON;
    }

    /**
     * Looks up a unit by its constant name (spaces, dashes and dots allowed, e.g. {@code "metric ton"}), its symbol
     * or its plural name, ignoring surrounding spaces. Case is ignored.
     *
     * @param text its symbol or name, e.g. {@code "kg"}
     * @return the unit
     * @throws ValueObjectException if it is {@code null} or not a unit
     */
    public static WeightUnit parse(String text) {
        if (text == null) throw new ValueObjectException("A WeightUnit is required");
        String t = text.strip();
        String key = t.replace(".", "").replace(' ', '_').replace('-', '_');
        for (WeightUnit v : values()) {
            if (v.name().equalsIgnoreCase(key) || v.symbol().equalsIgnoreCase(t) || v.unitName().equalsIgnoreCase(t)) return v;
        }
        throw new ValueObjectException("Not a WeightUnit: '" + text + "'");
    }

    /**
     * Looks up a unit like {@link #parse(String)}, without throwing.
     *
     * @param text any text
     * @return it as a {@link WeightUnit}, or empty when it is not one; never throws
     */
    public static Optional<WeightUnit> tryParse(@Nullable String text) {
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
     * @return whether it is a {@link WeightUnit}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
