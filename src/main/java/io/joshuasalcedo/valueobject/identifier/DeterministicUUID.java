package io.joshuasalcedo.valueobject.identifier;

import io.joshuasalcedo.valueobject.core.ValueObjectException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Makes UUIDs that are always the same for the same inputs: a version 3 (MD5, name-based) UUID of the inputs'
 * text, encoded in UTF-8.
 *
 * <p>For {@link #create(Object...)} each input is written as its length, a colon and its text
 * ({@code "2:ab1:c"}), so the boundaries between inputs count: {@code create("ab", "c")} and
 * {@code create("a", "bc")} give different UUIDs. Earlier versions joined the texts with no separator, so the
 * UUIDs this method gives changed when the length prefix was added; UUIDs stored from the old version are not
 * reproduced. {@link #create(UniqueId...)} joins the fixed-length UUID texts, which is already unambiguous, and
 * is unchanged.
 *
 * @author JoshuaSalcedo
 * @since 2/8/2026 7:48 PM
 */
public final class DeterministicUUID {
	private DeterministicUUID(){}

	/**
	 * Creates a UUID from the {@link String#valueOf(Object) text} of each value, in order, each prefixed with
	 * its length so that different splits of the same characters give different UUIDs; a {@code null} value
	 * counts as {@code "null"}.
	 *
	 * @param params the values to derive it from
	 * @return the same UUID for the same values
	 * @throws ValueObjectException if {@code params} itself is {@code null}
	 */
	public static UUID create(Object ... params){
		if (params == null) {
			throw new ValueObjectException("The values to derive a UUID from are required");
		}
		StringBuilder builder = new StringBuilder();
		for(Object param : params){
			String text = String.valueOf(param);
			builder.append(text.length()).append(':').append(text);
		}
		return generateUUIDFromString(builder.toString());
	}

	/**
	 * Creates a UUID from the UUIDs of several identifiers, in order.
	 *
	 * @param aggregateIds the identifiers to derive it from
	 * @return the same UUID for the same identifiers in the same order
	 * @throws ValueObjectException if {@code aggregateIds} or one of them is {@code null}
	 */
	public static UUID create(UniqueId... aggregateIds){
		if (aggregateIds == null) {
			throw new ValueObjectException("The identifiers to derive a UUID from are required");
		}
		StringBuilder builder = new StringBuilder();
		for(UniqueId param : aggregateIds){
			if (param == null) {
				throw new ValueObjectException("An identifier to derive a UUID from is null");
			}
			builder.append(param.value());
		}
		return generateUUIDFromString(builder.toString());
	}

	private static UUID generateUUIDFromString(String input) {
		// Get the bytes of the input string using UTF-8 encoding
		byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
		// Generate a deterministic UUID v3 from the bytes
		return UUID.nameUUIDFromBytes(bytes);
	}
}
