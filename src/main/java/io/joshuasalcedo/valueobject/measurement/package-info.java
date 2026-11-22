/**
 * Measurement value objects with unit conversions.
 *
 * <p>This package provides a framework for measurable quantities with units:
 *
 * <h2>Core Interfaces</h2>
 * <ul>
 *     <li>{@link io.joshuasalcedo.valueobject.measurement.MeasurementUnit} - Interface for unit types</li>
 *     <li>{@link io.joshuasalcedo.valueobject.measurement.Measurable} - Interface for measurable values</li>
 * </ul>
 *
 * <h2>Distance</h2>
 * <ul>
 *     <li>{@link io.joshuasalcedo.valueobject.measurement.DistanceUnit} - Metric and imperial distance units</li>
 *     <li>{@link io.joshuasalcedo.valueobject.measurement.Distance} - Distance measurements</li>
 * </ul>
 *
 * <h2>Weight</h2>
 * <ul>
 *     <li>{@link io.joshuasalcedo.valueobject.measurement.WeightUnit} - Metric and imperial weight units</li>
 *     <li>{@link io.joshuasalcedo.valueobject.measurement.Weight} - Weight/mass measurements</li>
 * </ul>
 *
 * <h2>File Size</h2>
 * <ul>
 *     <li>{@link io.joshuasalcedo.valueobject.measurement.DataUnit} - Binary (IEC) and decimal (SI) units</li>
 *     <li>{@link io.joshuasalcedo.valueobject.measurement.FileSize} - File/data size measurements</li>
 * </ul>
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
@NullMarked
package io.joshuasalcedo.valueobject.measurement;

import org.jspecify.annotations.NullMarked;
