package io.joshuasalcedo.valueobject.contact;

import java.util.Locale;
import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * A person's name: a first name and an optional middle and last name. Each part is trimmed with runs of
 * whitespace collapsed to one space, and must be 1 to 50 characters of letters, spaces, hyphens and apostrophes,
 * starting with a letter. Blank middle and last names become {@code null}.
 *
 * <pre>{@code
 * FullName n = FullName.parse("Ms. Andrea Lim Bautista");
 * n.formalName();   // "Bautista, Andrea Lim"
 * n.initials();     // "ALB"
 * }</pre>
 *
 * @param firstName  the first (given) name
 * @param middleName the middle name or names, or {@code null}
 * @param lastName   the last name (surname), or {@code null}
 */
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


	/**
	 * Creates a name, normalizing the whitespace in each part.
	 *
	 * @throws ValueObjectException if the first name is {@code null} or blank, or a part is longer than 50 characters or has
	 *                              characters other than letters, spaces, hyphens and apostrophes
	 */
	public FullName {
		if (firstName == null) {
			throw new ValueObjectException("First name is required");
		}
		firstName = normalize(firstName);
		middleName = normalizeNullable(middleName);
		lastName = normalizeNullable(lastName);

		{
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
	 * ({@code "Juan dela Cruz"} -> first Juan, last dela Cruz). A single word ({@code "Cher"}) is a first name
	 * only.
	 *
	 * <p>Text with a business word ("Inc", "Bank", "Cafe", "Cloud", "Co", ...; see
	 * {@link #looksLikePerson(String)}) is rejected unless it starts with a title, so {@code "Northwind Cloud"}
	 * is not read as a person. That also rejects the rare surname that is such a word ({@code "Jose Co"}): build
	 * those with the constructor.
	 *
	 * @param name e.g. {@code "Ms. Andrea Lim Bautista"}
	 * @return first Andrea, middle Lim, last Bautista
	 * @throws ValueObjectException if it is {@code null}, blank, has a business word, has no given name
	 *                              ({@code "Bautista,"}), or a part is not a valid name part
	 */
	public static FullName parse(String name) {
		if (name == null || name.isBlank()) throw new ValueObjectException("A name is required");
		String n = name.strip().replaceAll("\\s+", " ");
		if (!TITLE.matcher(n).find() && BUSINESS.matcher(n).find()) {
			throw new ValueObjectException("Reads as a business, not a person's name: " + name);
		}
		n = TITLE.matcher(n).replaceFirst("");
		n = SUFFIX.matcher(n).replaceFirst("");
		String first;
		String middle = null;
		String last = null;
		if (n.contains(",")) {
			String[] parts = n.split(",", 2);
			last = parts[0].strip();
			if (last.isEmpty()) last = null;
			String givenText = parts[1].strip();
			if (givenText.isEmpty()) {
				throw new ValueObjectException("A name needs a given name after the comma: " + name);
			}
			String[] given = givenText.split(" ");
			first = given[0];
			if (given.length > 1) middle = String.join(" ", java.util.Arrays.asList(given).subList(1, given.length));
		} else {
			String[] words = n.split(" ");
			first = words[0];
			if (words.length > 1) {
				int lastStart = words.length - 1;
				// "dela Cruz", "de los Santos": take the particles into the surname
				while (lastStart - 1 > 0) {
					String two = lastStart - 2 > 0 ? (words[lastStart - 2] + " " + words[lastStart - 1]).toLowerCase(Locale.ROOT) : null;
					if (two != null && PARTICLES.contains(two)) lastStart -= 2;
					else if (PARTICLES.contains(words[lastStart - 1].toLowerCase(Locale.ROOT))) lastStart -= 1;
					else break;
				}
				last = String.join(" ", java.util.Arrays.asList(words).subList(lastStart, words.length));
				if (lastStart > 1) middle = String.join(" ", java.util.Arrays.asList(words).subList(1, lastStart));
			}
		}
		return new FullName(capitalize(first), middle == null ? null : capitalize(middle), last == null ? null : capitalize(last));
	}

	/**
	 * Parses a person's name as {@link #parse(String)} does, without throwing: it accepts exactly what
	 * {@code parse} accepts, single-word names such as {@code "Cher"} included. For the stricter test that a
	 * text reads as a person's full name, use {@link #looksLikePerson(String)}.
	 *
	 * @param name any text
	 * @return its name, or empty when {@code parse} would throw; never throws
	 */
	public static Optional<FullName> tryParse(@Nullable String name) {
		if (name == null) return Optional.empty();
		try {
			return Optional.of(parse(name));
		} catch (RuntimeException e) {
			return Optional.empty();
		}
	}

	/**
	 * Tells whether a text reads as a person's name rather than a business or something else.
	 *
	 * @param text any text
	 * @return whether it reads as a person's name: at most 60 characters, two to five words of letters, no
	 *         digits, no {@code @}, no business words ("Inc", "Bank", "Cafe", ...); a leading title ("Ms.") makes
	 *         it one
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
			if (w.isEmpty()) continue;
			if (!out.isEmpty()) out.append(' ');
			if (PARTICLES.contains(w.toLowerCase(Locale.ROOT))) out.append(w.toLowerCase(Locale.ROOT));
			else if (w.equals(w.toUpperCase(Locale.ROOT)) || w.equals(w.toLowerCase(Locale.ROOT)))
				out.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase(Locale.ROOT));
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

	/** {@return the parts that are set, joined by spaces: first, middle, last} */
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

	/** {@return the upper-case first letters of the first, middle and last name, e.g. {@code "ALB"}} */
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

	/**
	 * {@return the name surname first, {@code "Last, First Middle"}, or the {@link #fullName() full name} when
	 * there is no last name}
	 */
	public String formalName() {
		if (lastName != null) {
			return lastName + ", " + firstName + (middleName != null ? " " + middleName : "");
		}
		return fullName();
	}


	@Override
	public String toString() {
		return fullName();
	}

	/**
	 * Compares names by last name, then first name, then middle name, ignoring case; a missing last or middle
	 * name sorts after a present one.
	 *
	 * @param other the name to compare to
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


    /**
     * Tells whether {@link #parse(String)} reads a name from a text; a single word ({@code "Cher"}) is enough.
     *
     * @param text any text
     * @return whether it is a {@link FullName}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
