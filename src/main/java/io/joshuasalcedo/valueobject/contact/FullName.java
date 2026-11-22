package io.joshuasalcedo.valueobject.contact;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

public record FullName(
		String firstName,

		@Nullable String middleName,

		@Nullable String lastName
) implements ValueObject, Comparable<FullName> {

	// Allows letters, spaces, hyphens, apostrophes, and common accented characters
	private static final Pattern VALID_NAME_PATTERN =
			Pattern.compile("^[\\p{L}][\\p{L}\\s'\\-]*$");

	private static final int MIN_LENGTH = 1;
	private static final int MAX_LENGTH = 50;

	/** Null object representing an absent FullName. */
	public static final FullName NULL = new FullName("", null, null);

	public FullName {
		firstName = normalize(firstName);
		middleName = normalizeNullable(middleName);
		lastName = normalizeNullable(lastName);

		if (!firstName.isEmpty()) {
			validateRequired(firstName);
			validateName(firstName, "First name");

			if (middleName != null) {
				validateName(middleName, "Middle name");
			}
			if (lastName != null) {
				validateName(lastName, "Last name");
			}
		}
	}

	
	public boolean isNull() {
		return firstName.isEmpty();
	}

	private static final Pattern TITLE = Pattern.compile(
			"(?i)^(mr|mrs|ms|miss|mx|dr|engr|atty|prof|sir|madam|hon|rev|fr|sr\\.? ?nun)\\.?\\s+");
	private static final Pattern SUFFIX = Pattern.compile("(?i),?\\s+(jr|sr|ii|iii|iv|v|phd|md|cpa|esq)\\.?$");
	/** Words that start a surname: "dela Cruz", "de los Santos", "van der Berg". */
	private static final java.util.Set<String> PARTICLES = java.util.Set.of(
			"de", "del", "dela", "della", "de la", "de los", "de las", "delos", "van", "von", "van der", "da", "das", "dos",
			"di", "du", "la", "le", "los", "las", "der", "den", "st", "san", "santa", "bin", "binti", "al", "el", "mac", "ter");
	private static final Pattern BUSINESS = Pattern.compile(
			"(?i)\\b(inc|llc|ltd|corp|corporation|company|co|gmbh|plc|pty|bank|cafe|store|shop|telecom|services|enterprises?"
					+ "|trading|district|cooperative|association|foundation|university|school|hospital|hotel|restaurant|media|labs?"
					+ "|studio|group|holdings|ventures|solutions|systems|digital|cloud|global|international|properties)\\b");

	/**
	 * Reads a person's name as printed: titles ("Ms.", "Engr.") and suffixes ("Jr.", "III") are dropped,
	 * {@code "Bautista, Andrea Lim"} puts the surname first, and surname particles stay with the surname
	 * ({@code "Juan dela Cruz"} -> first Juan, last dela Cruz).
	 *
	 * @param name e.g. {@code "Ms. Andrea Lim Bautista"}
	 * @return first Andrea, middle Lim, last Bautista
	 * @throws ValueObjectException if it is not a person's name
	 */
	public static FullName parse(String name) {
		if (name == null || name.isBlank()) throw new ValueObjectException("A name is required");
		String n = name.strip().replaceAll("\\s+", " ");
		n = TITLE.matcher(n).replaceFirst("");
		n = SUFFIX.matcher(n).replaceFirst("");
		String first;
		String middle = null;
		String last = null;
		if (n.contains(",")) {
			String[] parts = n.split(",", 2);
			last = parts[0].strip();
			String[] given = parts[1].strip().split(" ");
			first = given[0];
			if (given.length > 1) middle = String.join(" ", java.util.Arrays.asList(given).subList(1, given.length));
		} else {
			String[] words = n.split(" ");
			first = words[0];
			if (words.length > 1) {
				int lastStart = words.length - 1;
				// "dela Cruz", "de los Santos": take the particles into the surname
				while (lastStart - 1 > 0) {
					String two = lastStart - 2 > 0 ? (words[lastStart - 2] + " " + words[lastStart - 1]).toLowerCase() : null;
					if (two != null && PARTICLES.contains(two)) lastStart -= 2;
					else if (PARTICLES.contains(words[lastStart - 1].toLowerCase())) lastStart -= 1;
					else break;
				}
				last = String.join(" ", java.util.Arrays.asList(words).subList(lastStart, words.length));
				if (lastStart > 1) middle = String.join(" ", java.util.Arrays.asList(words).subList(1, lastStart));
			}
		}
		return new FullName(capitalize(first), middle == null ? null : capitalize(middle), last == null ? null : capitalize(last));
	}

	/**
	 * @param name any text
	 * @return its name, or empty when it is not a person's name
	 */
	public static java.util.Optional<FullName> tryParse(@Nullable String name) {
		if (!looksLikePerson(name)) return java.util.Optional.empty();
		try {
			return java.util.Optional.of(parse(name));
		} catch (ValueObjectException e) {
			return java.util.Optional.empty();
		}
	}

	/**
	 * @param text any text
	 * @return whether it reads as a person's name: two to five words of letters, no digits, no business words
	 *         ("Inc", "Bank", "Cafe", ...); a title ("Ms.") makes it one
	 */
	public static boolean looksLikePerson(@Nullable String text) {
		if (text == null || text.isBlank() || text.length() > 60) return false;
		String t = text.strip();
		if (TITLE.matcher(t).find()) return true;
		if (BUSINESS.matcher(t).find() || t.matches(".*\\d.*") || t.contains("@")) return false;
		String core = SUFFIX.matcher(t).replaceFirst("");
		String[] words = core.replace(",", " ").strip().split("\\s+");
		if (words.length < 2 || words.length > 5) return false;
		for (String w : words) if (!w.matches("[\\p{L}][\\p{L}'.-]*")) return false;
		return true;
	}

	/** "ANDREA" and "andrea" -> "Andrea"; "dela" stays lower case; "McDonald" stays as is. */
	private static String capitalize(String s) {
		StringBuilder out = new StringBuilder();
		for (String w : s.split(" ")) {
			if (!out.isEmpty()) out.append(' ');
			if (PARTICLES.contains(w.toLowerCase())) out.append(w.toLowerCase());
			else if (w.equals(w.toUpperCase()) || w.equals(w.toLowerCase()))
				out.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase());
			else out.append(w);
		}
		return out.toString().replace(".", "");
	}

	private static void validateRequired(String value) {
		if (value.isBlank()) {
			throw new ValueObjectException("First name" + " is required");
		}
	}

	private static void validateName(String value, String fieldName) {
		if (value.isEmpty() || value.length() > MAX_LENGTH) {
			throw new ValueObjectException(
					fieldName + " must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters"
			);
		}

		if (!VALID_NAME_PATTERN.matcher(value).matches()) {
			throw new ValueObjectException(
					fieldName + " contains invalid characters. Only letters, spaces, hyphens, and apostrophes are allowed"
			);
		}
	}

	private static String normalize(String value) {
		return value.trim().replaceAll("\\s+", " ");
	}

	private static @Nullable String normalizeNullable(@Nullable String value) {
		if (value == null || value.isBlank()) return null;
		return value.trim().replaceAll("\\s+", " ");
	}

	public String fullName() {
		StringBuilder sb = new StringBuilder(firstName);
		if (middleName != null) {
			sb.append(" ").append(middleName);
		}
		if (lastName != null) {
			sb.append(" ").append(lastName);
		}
		return sb.toString();
	}

	public String initials() {
		StringBuilder sb = new StringBuilder();
		sb.append(Character.toUpperCase(firstName.charAt(0)));
		if (middleName != null && !middleName.isEmpty()) {
			sb.append(Character.toUpperCase(middleName.charAt(0)));
		}
		if (lastName != null && !lastName.isEmpty()) {
			sb.append(Character.toUpperCase(lastName.charAt(0)));
		}
		return sb.toString();
	}

	public String formalName() {
		if (lastName != null) {
			return lastName + ", " + firstName + (middleName != null ? " " + middleName : "");
		}
		return fullName();
	}

	/**
	 * Returns the null object representation of FullName.
	 *
	 * @return the NULL full name constant
	 */
	public static FullName nullValue() {
		return NULL;
	}

	@Override
	public String toString() {
		return fullName();
	}

	/**
	 * Compares this FullName to another FullName.
	 * Comparison order: lastName, firstName, middleName (nulls last).
	 *
	 * @param other the FullName to compare to
	 * @return negative if this comes before, zero if equal, positive if after
	 */
	@Override
	public int compareTo(FullName other) {
		// Compare lastName first (nulls last)
		int result = compareNullable(this.lastName, other.lastName);
		if (result != 0) return result;

		// Then firstName
		result = this.firstName.compareToIgnoreCase(other.firstName);
		if (result != 0) return result;

		// Finally middleName (nulls last)
		return compareNullable(this.middleName, other.middleName);
	}

	private static int compareNullable(String a, String b) {
		if (a == null && b == null) return 0;
		if (a == null) return 1;  // nulls last
		if (b == null) return -1;
		return a.compareToIgnoreCase(b);
	}
}
