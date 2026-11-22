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
     * Converts a value from this unit to the target unit.
     *
     * @param value the value in this unit
     * @param targetUnit the target unit
     * @return the value converted to the target unit
     */
    default BigDecimal convertTo(BigDecimal value, U targetUnit) {
        // Convert to base unit first, then to target
        BigDecimal inBase = value.multiply(toBaseUnitFactor());
        return inBase.divide(targetUnit.toBaseUnitFactor(), 10, java.math.RoundingMode.HALF_EVEN);
    }
}
