package io.joshuasalcedo.valueobject.core;

/**
 * Thrown when a value object cannot be made from the value given: missing, malformed or out of range. It is
 * unchecked; the {@code tryParse} and {@code isValid} methods of the value objects let callers avoid it.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026 1:49 PM
 */

public class ValueObjectException extends RuntimeException {
	/**
	 * Creates the exception with a message.
	 *
	 * @param message what was wrong with the value
	 */
	public ValueObjectException(String message) {
		super(message);
	}

	/**
	 * Creates the exception with a message and the exception that caused it.
	 *
	 * @param message what was wrong with the value
	 * @param cause   the underlying exception
	 */
	public ValueObjectException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates the exception from the exception that caused it, taking its message.
	 *
	 * @param cause the underlying exception
	 */
	public ValueObjectException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates the exception with full control over suppression and the stack trace, as
	 * {@link RuntimeException#RuntimeException(String, Throwable, boolean, boolean)}.
	 *
	 * @param message            what was wrong with the value
	 * @param cause              the underlying exception, or {@code null}
	 * @param enableSuppression  whether suppressed exceptions can be added
	 * @param writableStackTrace whether the stack trace is recorded
	 */
	public ValueObjectException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/** Creates the exception with no message. */
	public ValueObjectException() {
	}
}
