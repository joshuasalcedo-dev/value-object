package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.ValueObject;


import java.math.BigDecimal;

/**
 * Interface for measurement units that support conversions.
 * Each unit type (distance, weight, file size) has its own implementation.
 *
 * @param <U> the concrete unit type
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public interface MeasurementUnit<U extends MeasurementUnit<U>> extends ValueObject {

    /**
     * Returns the symbol for this unit (e.g., "km", "kg", "MB").
     *
     * @return the unit symbol
     */
    String symbol();

    /**
     * Returns the full name of this unit (e.g., "kilometers", "kilograms", "megabytes").
     *
     * @return the unit name
     */
    String unitName();

    /**
     * Returns the conversion factor to the base unit.
     * For example, if the base unit is meters, then kilometers would return 1000.
     *
     * @return the conversion factor to base unit
     */
    BigDecimal toBaseUnitFactor();

    /**
     * Returns the base unit for this measurement type.
     *
     * @return the base unit
     */
    U baseUnit();

    /**
     * Converts a value from this unit to the target unit, through the base unit. The result is exact when it has
     * a finite decimal expansion (kilometres to metres, mebibytes to bytes), else rounded half-even to 34
     * significant digits ({@link java.math.MathContext#DECIMAL128}); trailing zeros are dropped.
     *
     * @param value the value in this unit
     * @param targetUnit the target unit
     * @return the value converted to the target unit
     * @throws io.joshuasalcedo.valueobject.exception.ValueObjectException if either is {@code null}, or the
     *         target unit's factor is zero
     */
    default BigDecimal convertTo(BigDecimal value, U targetUnit) {
        if (value == null || targetUnit == null)
            throw new io.joshuasalcedo.valueobject.exception.ValueObjectException("Value and target unit are required");
        BigDecimal divisor = targetUnit.toBaseUnitFactor();
        if (divisor.signum() == 0)
            throw new io.joshuasalcedo.valueobject.exception.ValueObjectException("Cannot convert to " + targetUnit.unitName());
        BigDecimal inBase = value.multiply(toBaseUnitFactor());
        BigDecimal out;
        try {
            out = inBase.divide(divisor);
        } catch (ArithmeticException nonTerminating) {
            out = inBase.divide(divisor, java.math.MathContext.DECIMAL128);
        }
        out = out.stripTrailingZeros();
        return out.scale() < 0 ? out.setScale(0) : out;
    }
}
