package io.joshuasalcedo.valueobject.social;

import java.util.Locale;
import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.jspecify.annotations.Nullable;
import io.joshuasalcedo.valueobject.core.ValueObject;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Value object representing a Telegram account username.
 *
 * <p>Telegram usernames must follow these rules:
 * <ul>
 *   <li>5-32 characters in length</li>
 *   <li>Can contain a-z, A-Z, 0-9, and underscores</li>
 *   <li>Must start with a letter</li>
 *   <li>Cannot end with an underscore</li>
 *   <li>Cannot have consecutive underscores</li>
 * </ul>
 *
 * <p>It is read from {@code "username"}, {@code "@username"}, {@code "https://t.me/username"} or
 * {@code "tg://resolve?domain=username"}, trimmed. Accounts are equal when their usernames match ignoring case. Bad
 * input makes the constructor, {@link #of(String)} and {@link #parse(String)} throw
 * {@link ValueObjectException}; {@link #tryParse(String)} and {@link #isValid(String)} never throw.
 *
 * @param username the username, without {@code @} or URL, case kept as given
 * @author JoshuaSalcedo
 * @since 2/3/2026 2:43 AM
 */
public record TelegramAccount(String username) implements ValueObject {

	private static final int MIN_LENGTH = 5;
	private static final int MAX_LENGTH = 32;

	private static final Pattern VALID_USERNAME_PATTERN = Pattern.compile(
			"^[a-zA-Z][a-zA-Z0-9_]{3,30}[a-zA-Z0-9]$"
	);

	private static final Pattern CONSECUTIVE_UNDERSCORES = Pattern.compile("__");

	/** Telegram url prefix: {@code "https://t.me/"}. */
	public static final String TELEGRAM_URL_PREFIX = "https://t.me/";
	/** Telegram app prefix: {@code "tg://resolve?domain="}. */
	public static final String TELEGRAM_APP_PREFIX = "tg://resolve?domain=";

	/**
	 * Canonical constructor with validation.
	 * Normalizes the username by removing any leading @ symbol.
	 *
	 * @param username the Telegram username
	 * @throws ValueObjectException if username is null or invalid
	 */
	public TelegramAccount {
		username = normalize(username);
		validate(username);
	}

	/**
	 * Creates a TelegramAccount from a username string.
	 * Accepts formats: "username", "@username", or full URL "https://t.me/username"
	 *
	 * @param input the username in any accepted format
	 * @return a new TelegramAccount instance
	 * @throws ValueObjectException if input is null or invalid
	 */
	public static TelegramAccount of(String input) {
		return new TelegramAccount(input);
	}

	/**
	 * Creates a TelegramAccount if the input is valid, otherwise returns empty.
	 *
	 * @param input the username to parse
	 * @return Optional containing TelegramAccount if valid, empty otherwise
	 */
	public static Optional<TelegramAccount> tryParse(@Nullable String input) {
		try {
			return Optional.of(new TelegramAccount(input));
		} catch (ValueObjectException e) {
			return Optional.empty();
		}
	}

	/**
	 * Checks if the given input would be a valid Telegram username.
	 *
	 * @param input the username to validate
	 * @return true if valid, false otherwise
	 */
	public static boolean isValid(@Nullable String input) {
		if (input == null || input.isBlank()) {
			return false;
		}
		String normalized = normalize(input);
		return isValidFormat(normalized);
	}

	/**
	 * Returns the username with @ prefix.
	 *
	 * @return username prefixed with @
	 */
	public String withAtSign() {
		return "@" + username;
	}

	/**
	 * Returns the direct URL to this Telegram account.
	 *
	 * @return the t.me URL
	 */
	public String toUrl() {
		return TELEGRAM_URL_PREFIX + username;
	}

	/**
	 * Returns the deep link URL that opens directly in Telegram app.
	 *
	 * @return the tg:// protocol URL
	 */
	public String toDeepLink() {
		return TELEGRAM_APP_PREFIX + username;
	}

	/**
	 * Returns the username in lowercase for case-insensitive comparison.
	 *
	 * @return lowercase username
	 */
	public String normalized() {
		return username.toLowerCase(Locale.ROOT);
	}

	/**
	 * Checks if this account matches another, ignoring case.
	 *
	 * @param other the other account to compare
	 * @return true if usernames match (case-insensitive)
	 */
	public boolean equalsIgnoreCase(TelegramAccount other) {
		if (other == null) {
			return false;
		}
		return this.normalized().equals(other.normalized());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TelegramAccount that = (TelegramAccount) o;
		return normalized().equals(that.normalized());
	}

	@Override
	public int hashCode() {
		return Objects.hash(normalized());
	}

	@Override
	public String toString() {
		return username;
	}

	// ==================== Private Helper Methods ====================

	private static String normalize(String input) {
		if (input == null) {
			throw new ValueObjectException("Telegram username cannot be null");
		}

		String trimmed = input.trim();

		// Handle full URL format
		if (trimmed.toLowerCase(Locale.ROOT).startsWith(TELEGRAM_URL_PREFIX.toLowerCase(Locale.ROOT))) {
			trimmed = trimmed.substring(TELEGRAM_URL_PREFIX.length());
		}

		// Handle tg:// deep link format
		if (trimmed.toLowerCase(Locale.ROOT).startsWith(TELEGRAM_APP_PREFIX.toLowerCase(Locale.ROOT))) {
			trimmed = trimmed.substring(TELEGRAM_APP_PREFIX.length());
		}

		// Remove leading @ if present
		if (trimmed.startsWith("@")) {
			trimmed = trimmed.substring(1);
		}

		// Remove any trailing path or query params from URL
		int slashIndex = trimmed.indexOf('/');
		if (slashIndex > 0) {
			trimmed = trimmed.substring(0, slashIndex);
		}

		int queryIndex = trimmed.indexOf('?');
		if (queryIndex > 0) {
			trimmed = trimmed.substring(0, queryIndex);
		}

		return trimmed;
	}

	private static void validate(String username) {
		if (username == null || username.isBlank()) {
			throw new ValueObjectException("Telegram username cannot be blank");
		}

		if (username.length() < MIN_LENGTH) {
			throw new ValueObjectException(
					String.format(Locale.ROOT, "Telegram username must be at least %d characters, got %d: '%s'",
							MIN_LENGTH, username.length(), username)
			);
		}

		if (username.length() > MAX_LENGTH) {
			throw new ValueObjectException(
					String.format(Locale.ROOT, "Telegram username cannot exceed %d characters, got %d: '%s'",
							MAX_LENGTH, username.length(), username)
			);
		}

		if (!Character.isLetter(username.charAt(0))) {
			throw new ValueObjectException(
					"Telegram username must start with a letter: '" + username + "'"
			);
		}

		if (username.endsWith("_")) {
			throw new ValueObjectException(
					"Telegram username cannot end with an underscore: '" + username + "'"
			);
		}

		if (CONSECUTIVE_UNDERSCORES.matcher(username).find()) {
			throw new ValueObjectException(
					"Telegram username cannot contain consecutive underscores: '" + username + "'"
			);
		}

		if (!VALID_USERNAME_PATTERN.matcher(username).matches()) {
			throw new ValueObjectException(
					"Telegram username contains invalid characters (only a-z, A-Z, 0-9, _ allowed): '" + username + "'"
			);
		}
	}

	private static boolean isValidFormat(String username) {
		if (username == null || username.isBlank()) {
			return false;
		}
		if (username.length() < MIN_LENGTH || username.length() > MAX_LENGTH) {
			return false;
		}
		if (!Character.isLetter(username.charAt(0))) {
			return false;
		}
		if (username.endsWith("_")) {
			return false;
		}
		if (CONSECUTIVE_UNDERSCORES.matcher(username).find()) {
			return false;
		}
		return VALID_USERNAME_PATTERN.matcher(username).matches();
	}

    /**
     * Parses an account from any of the accepted forms, as {@link #tryParse(String)} does.
     *
     * @param text the username, {@code @username} or link
     * @return the account
     * @throws ValueObjectException if it is {@code null} or not a valid Telegram username
     */
    public static TelegramAccount parse(String text) {
        return tryParse(text).orElseThrow(() -> new ValueObjectException("Not a TelegramAccount: '" + text + "'"));
    }
}
