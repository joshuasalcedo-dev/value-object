package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.core.ValueObject;


import java.math.BigDecimal;

/**
 * Interface for value objects that represent measurable quantities with units.
 *
 * @param <T> the concrete measurable type (for fluent API)
 * @param <U> the unit type
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public interface Measurable<T extends Measurable<T, U>, U extends MeasurementUnit<U>> extends ValueObject {

    /**
     * Returns the numeric value of this measurement.
     *
     * @return the value
     */
    BigDecimal value();

    /**
     * Returns the unit of this measurement.
     *
     * @return the unit
     */
    U unit();

    /**
     * Converts this measurement to the specified unit.
     *
     * @param targetUnit the target unit
     * @return a new measurement in the target unit
     */
    T convertTo(U targetUnit);

    /**
     * Adds another measurement to this one.
     * The result will be in this measurement's unit.
     *
     * @param other the measurement to add
     * @return a new measurement with the sum
     */
    T add(T other);

    /**
     * Subtracts another measurement from this one.
     * The result will be in this measurement's unit.
     *
     * @param other the measurement to subtract
     * @return a new measurement with the difference
     */
    T subtract(T other);

    /**
     * Multiplies this measurement by a scalar.
     *
     * @param multiplier the multiplier
     * @return a new measurement with the product
     */
    T multiply(BigDecimal multiplier);

    /**
     * Divides this measurement by a scalar.
     *
     * @param divisor the divisor
     * @return a new measurement with the quotient
     */
    T divide(BigDecimal divisor);

    /**
     * Checks if this measurement is zero.
     *
     * @return true if zero
     */
    default boolean isZero() {
        return value().compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Checks if this measurement is positive.
     *
     * @return true if positive
     */
    default boolean isPositive() {
        return value().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Checks if this measurement is negative.
     *
     * @return true if negative
     */
    default boolean isNegative() {
        return value().compareTo(BigDecimal.ZERO) < 0;
    }
}
