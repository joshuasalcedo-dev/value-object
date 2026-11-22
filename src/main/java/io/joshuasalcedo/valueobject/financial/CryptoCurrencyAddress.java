package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.ValueObject;


/**
 * CryptoCurrencyAddress class.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026 6:52 PM
 */

public record CryptoCurrencyAddress(
		String address,
		CryptoCoin coin
) implements ValueObject {
}
