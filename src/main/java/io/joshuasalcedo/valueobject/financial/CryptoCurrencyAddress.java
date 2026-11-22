package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;


/**
 * A wallet address on a crypto currency's network, paired with its coin. The address is checked only for its
 * shape, which every network shares: surrounding spaces are removed, and what is left must be
 * {@value #MIN_LENGTH} to {@value #MAX_LENGTH} visible ASCII characters with no spaces inside (a Bitcoin address
 * is 26-62 characters, an Ethereum one 42, a Stellar one 56, a Cardano one up to about 110). Network-specific
 * checksums (Base58Check, Bech32, EIP-55) are not verified, and neither is that the address belongs to the coin's
 * network.
 *
 * <pre>{@code
 * new CryptoCurrencyAddress("0x52908400098527886E0F7030069857D2E4169EE7", CryptoCoin.Known.ETH);
 * }</pre>
 *
 * @param address the wallet address, trimmed
 * @param coin    the coin the address holds
 * @author JoshuaSalcedo
 * @since 1/24/2026 6:52 PM
 */

public record CryptoCurrencyAddress(
		String address,
		CryptoCoin coin
) implements ValueObject {

	/** The fewest characters an address may have. */
	public static final int MIN_LENGTH = 20;
	/** The most characters an address may have. */
	public static final int MAX_LENGTH = 128;

	/**
	 * Checks the address and coin; the address is trimmed.
	 *
	 * @param address the wallet address, {@value #MIN_LENGTH} to {@value #MAX_LENGTH} visible ASCII characters
	 *                with no spaces inside
	 * @param coin    the coin the address holds
	 * @throws ValueObjectException if the address is {@code null}, blank, has spaces or other characters that are
	 *                              not visible ASCII inside, or is too short or too long, or the coin is {@code null}
	 */
	public CryptoCurrencyAddress {
		if (coin == null) throw new ValueObjectException("A crypto address needs its coin");
		if (address == null || address.isBlank()) throw new ValueObjectException("A crypto address is required");
		address = address.strip();
		if (!address.matches("[\\x21-\\x7E]+"))
			throw new ValueObjectException("A crypto address has no spaces and only visible ASCII characters: '" + address + "'");
		if (address.length() < MIN_LENGTH || address.length() > MAX_LENGTH)
			throw new ValueObjectException("A crypto address is " + MIN_LENGTH + "-" + MAX_LENGTH
					+ " characters, not " + address.length());
	}

	/**
	 * Tells whether text has the shape of a crypto address, as the constructor checks it.
	 *
	 * @param address any text
	 * @return whether it could be a wallet address; never throws
	 */
	public static boolean isValidAddress(@org.jspecify.annotations.Nullable String address) {
		if (address == null) return false;
		String a = address.strip();
		return a.length() >= MIN_LENGTH && a.length() <= MAX_LENGTH && a.matches("[\\x21-\\x7E]+");
	}
}
