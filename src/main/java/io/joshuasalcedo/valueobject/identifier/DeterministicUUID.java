package io.joshuasalcedo.valueobject.identifier;




import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * DeterministicUUID class.
 *
 * @author JoshuaSalcedo
 * @since 2/8/2026 7:48 PM
 */
public final class DeterministicUUID {
	private DeterministicUUID(){}

	public static UUID create(Object ... params){
		StringBuilder builder = new StringBuilder();
		for(Object param : params){
			builder.append(param);
		}
		return generateUUIDFromString(builder.toString());
	}

	public static UUID create(UniqueId... aggregateIds){
		StringBuilder builder = new StringBuilder();
		for(UniqueId param : aggregateIds){
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