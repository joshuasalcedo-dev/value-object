/**
 * The contract every value object keeps, and one way into all of them.
 *
 * <ul>
 *     <li>{@link io.joshuasalcedo.valueobject.core.ValueObject}: the interface every value object implements, and its
 *     generated entry points ({@code detect}, {@code find}, {@code scan}, {@code identify}, {@code parse},
 *     {@code Parse}, {@code Validate}, {@code Type})</li>
 *     <li>{@link io.joshuasalcedo.valueobject.core.ValueObjectException}: what a value object throws when its input is
 *     not one</li>
 *     <li>{@link io.joshuasalcedo.valueobject.core.Found}, {@link io.joshuasalcedo.valueobject.core.Count}: what
 *     scanning a text gives back</li>
 *     <li>{@link io.joshuasalcedo.valueobject.core.Rule}, {@link io.joshuasalcedo.valueobject.core.Check}: tests on
 *     any input that never throw</li>
 * </ul>
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
@NullMarked
@SuppressWarnings(value = {"unused", "ResultOfMethodCallIgnored"})
package io.joshuasalcedo.valueobject.core;

import org.jspecify.annotations.NullMarked;
