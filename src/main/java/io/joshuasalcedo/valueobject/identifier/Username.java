package io.joshuasalcedo.valueobject.identifier;

import io.joshuasalcedo.valueobject.ValueObject;


/**
 * Username class.
 *
 * @author JoshuaSalcedo
 * @since 2/7/2026 12:40 PM
 */

public record Username(
		String value
) implements ValueObject {

	/** Null object representing an absent Username. */
	public static final Username NULL = new Username("");

	
	public boolean isNull() {
		return value.isEmpty();
	}

	/**
	 * Returns the null object representation of Username.
	 *
	 * @return the NULL username constant
	 */
	public static Username nullValue() {
		return NULL;
	}
}
