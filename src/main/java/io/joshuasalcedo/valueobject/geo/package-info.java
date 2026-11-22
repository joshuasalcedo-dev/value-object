/**
 * Geographic value objects.
 *
 * <p>This package provides value objects for geographic data:
 * <ul>
 *     <li>{@link io.joshuasalcedo.valueobject.geo.Coordinates} - Latitude/longitude coordinates</li>
 *     <li>{@link io.joshuasalcedo.valueobject.geo.PostalCode} - Postal/ZIP codes with country validation</li>
 *     <li>{@link io.joshuasalcedo.valueobject.geo.Country} - Countries with ISO codes</li>
 *     <li>{@link io.joshuasalcedo.valueobject.geo.Language} - Languages with ISO codes</li>
 * </ul>
 *
 * <p>Coordinates supports distance calculations using the Haversine formula
 * and integrates with {@link io.joshuasalcedo.valueobject.measurement.Distance}.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
@NullMarked
package io.joshuasalcedo.valueobject.geo;

import org.jspecify.annotations.NullMarked;
