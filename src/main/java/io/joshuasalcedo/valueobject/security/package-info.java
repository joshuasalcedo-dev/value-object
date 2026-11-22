/**
 * Security-related value objects.
 *
 * <p>This package provides value objects for security concepts:
 * <ul>
 *     <li>{@link io.joshuasalcedo.valueobject.security.HashedPassword} - Password hashes</li>
 * </ul>
 *
 * <p>HashedPassword stores only the hash, never plain text. Hash and verify with a
 * BCrypt, Argon2 or SCrypt library of your choice.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
@NullMarked
package io.joshuasalcedo.valueobject.security;

import org.jspecify.annotations.NullMarked;
