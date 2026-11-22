package io.joshuasalcedo.valueobject.exception;

/**
 * ValueObjectException class.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026 1:49 PM
 */

public class ValueObjectException extends RuntimeException {
	public ValueObjectException(String message) {
		super(message);
	}

	public ValueObjectException(String message, Throwable cause) {
		super(message, cause);
	}

	public ValueObjectException(Throwable cause) {
		super(cause);
	}

	public ValueObjectException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ValueObjectException() {
	}
}
