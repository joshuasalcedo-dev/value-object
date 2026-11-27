package io.joshuasalcedo.valueobject.versioning;

import io.joshuasalcedo.valueobject.core.ValueObject;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Value object representing a semantic version (SemVer 2.0.0).
 * Format: {@code MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD]}
 *
 * <p>A missing (null) major, minor or patch number counts as 0 everywhere ({@link #majorValue()} and so on).
 * Ordering follows SemVer precedence: numbers first, then a pre-release sorts before the release, and build
 * metadata is ignored. {@link #equals(Object)} matches that ordering: it also ignores build metadata, and treats
 * a null number as 0.
 *
 * <p>The pre-release and build metadata are checked against SemVer 2.0.0 wherever a version is made, so every
 * version's {@link #toString()} reads back with {@link #parse(String)}: dot-separated, non-empty identifiers of
 * ASCII letters, digits and hyphens, and pre-release identifiers made only of digits have no leading zero.
 *
 * @param major         the major number, 0 or more; null counts as 0
 * @param minor         the minor number, 0 or more; null counts as 0
 * @param patch         the patch number, 0 or more; null counts as 0
 * @param preRelease    the pre-release identifiers without the {@code -}, e.g. {@code "rc.1"}; trimmed, and
 *                      stored as {@code ""} when null or blank (so the accessor never returns null)
 * @param buildMetadata the build metadata without the {@code +}, e.g. {@code "20260124"}; trimmed, and stored as
 *                      {@code ""} when null or blank (so the accessor never returns null)
 * @see <a href="https://semver.org/">Semantic Versioning 2.0.0</a>
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record SemanticVersion(
        @Nullable Integer major,
        @Nullable Integer minor,
        @Nullable Integer patch,
        @Nullable String preRelease,
        @Nullable String buildMetadata
) implements ValueObject, Comparable<SemanticVersion> {

    // SemVer regex pattern
    private static final Pattern SEMVER_PATTERN = Pattern.compile(
        "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)" +
        "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?" +
        "(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$"
    );

    private static final Pattern PRE_RELEASE_PATTERN = Pattern.compile(
        "(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*");

    private static final Pattern BUILD_PATTERN = Pattern.compile("[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*");

    /** Default version: 0.0.0 */
    public static final SemanticVersion DEFAULT = new SemanticVersion(0, 0, 0, null, null);

    // ==================== Well-Known Versions ====================

    /**
     * Common version constants as an enum.
     * Use {@code SemanticVersion.Version.INITIAL.toVersion()} or
     * {@code SemanticVersion.of(SemanticVersion.Version.V1_0_0)}.
     */
    public enum Version {
        /** Zero. */
        ZERO(0, 0, 0, null),
        /** Initial. */
        INITIAL(0, 1, 0, null),
        /** V1 0 0. */
        V1_0_0(1, 0, 0, null),
        /** SNAPSHOT. */
        V1_0_0_SNAPSHOT(1, 0, 0, "SNAPSHOT"),
        /** V1 1 0. */
        V1_1_0(1, 1, 0, null),
        /** V2 0 0. */
        V2_0_0(2, 0, 0, null),
        /** SNAPSHOT. */
        V2_0_0_SNAPSHOT(2, 0, 0, "SNAPSHOT"),
        /** alpha. */
        ALPHA(0, 1, 0, "alpha"),
        /** beta. */
        BETA(0, 1, 0, "beta"),
        /** rc.1. */
        RC1(1, 0, 0, "rc.1"),
        /** rc.2. */
        RC2(1, 0, 0, "rc.2");

        private final int major, minor, patch;
        private final @Nullable String preRelease;

        Version(int major, int minor, int patch, @Nullable String preRelease) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.preRelease = preRelease;
        }

        /**
         * The major number of this constant.
         *
         * @return the major number
         */
        public int major() { return major; }
        /**
         * The minor number of this constant.
         *
         * @return the minor number
         */
        public int minor() { return minor; }
        /**
         * The patch number of this constant.
         *
         * @return the patch number
         */
        public int patch() { return patch; }
        /**
         * The pre-release identifiers of this constant.
         *
         * @return e.g. {@code "SNAPSHOT"}, or null for a release
         */
        public @Nullable String preRelease() { return preRelease; }

        /**
         * Converts this version constant to a SemanticVersion instance.
         *
         * @return the version, with no build metadata
         */
        public SemanticVersion toVersion() {
            return new SemanticVersion(major, minor, patch, preRelease, null);
        }

        /**
         * Converts this version constant with build metadata.
         *
         * @param buildMetadata the build metadata without the {@code +}; null or blank for none
         * @return the version with that build metadata
         * @throws ValueObjectException if the build metadata is not valid SemVer build metadata
         */
        public SemanticVersion toVersion(String buildMetadata) {
            return new SemanticVersion(major, minor, patch, preRelease, buildMetadata);
        }
    }

    // ==================== Compact Constructor ====================

    /**
     * Checks that no number is negative, normalizes the pre-release and build metadata (trimmed, {@code ""}
     * for null or blank), and checks their identifiers against SemVer 2.0.0, so that the version's
     * {@link #toString()} {@link #parse(String) parses} back to it.
     *
     * @throws ValueObjectException if {@code major}, {@code minor} or {@code patch} is negative, or the
     *                              pre-release or build metadata is not made of valid SemVer identifiers
     */
    public SemanticVersion {
        if (major != null && major < 0) {
            throw new ValueObjectException("Major version cannot be negative: " + major);
        }
        if (minor != null && minor < 0) {
            throw new ValueObjectException("Minor version cannot be negative: " + minor);
        }
        if (patch != null && patch < 0) {
            throw new ValueObjectException("Patch version cannot be negative: " + patch);
        }

        preRelease = preRelease == null ? "" : preRelease.trim();
        buildMetadata = buildMetadata == null ? "" : buildMetadata.trim();
        if (!preRelease.isEmpty() && !PRE_RELEASE_PATTERN.matcher(preRelease).matches()) {
            throw new ValueObjectException("Invalid pre-release identifiers: '" + preRelease + "'");
        }
        if (!buildMetadata.isEmpty() && !BUILD_PATTERN.matcher(buildMetadata).matches()) {
            throw new ValueObjectException("Invalid build metadata: '" + buildMetadata + "'");
        }
    }

    // ==================== Safe Accessors ====================

    /**
     * Returns the major version, defaulting to 0 if null.
     *
     * @return the major number
     */
    public int majorValue() {
        return major != null ? major : 0;
    }

    /**
     * Returns the minor version, defaulting to 0 if null.
     *
     * @return the minor number
     */
    public int minorValue() {
        return minor != null ? minor : 0;
    }

    /**
     * Returns the patch version, defaulting to 0 if null.
     *
     * @return the patch number
     */
    public int patchValue() {
        return patch != null ? patch : 0;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a SemanticVersion from a {@link Version} enum constant.
     *
     * @param version the constant
     * @return its version, with no build metadata
     * @throws ValueObjectException if {@code version} is null
     */
    public static SemanticVersion of(Version version) {
        if (version == null) throw new ValueObjectException("A Version is required");
        return version.toVersion();
    }

    /**
     * Creates a semantic version with all components.
     *
     * @param major         the major number, 0 or more
     * @param minor         the minor number, 0 or more
     * @param patch         the patch number, 0 or more
     * @param preRelease    the pre-release identifiers without the {@code -}; null or blank for none
     * @param buildMetadata the build metadata without the {@code +}; null or blank for none
     * @return the version
     * @throws ValueObjectException if a number is negative, or the pre-release or build metadata is not valid
     */
    public static SemanticVersion of(int major, int minor, int patch,
                                     String preRelease, String buildMetadata) {
        return new SemanticVersion(major, minor, patch, preRelease, buildMetadata);
    }

    /**
     * Creates a semantic version without pre-release or build metadata.
     *
     * @param major the major number, 0 or more
     * @param minor the minor number, 0 or more
     * @param patch the patch number, 0 or more
     * @return the version
     * @throws ValueObjectException if a number is negative
     */
    public static SemanticVersion of(int major, int minor, int patch) {
        return new SemanticVersion(major, minor, patch, null, null);
    }

    /**
     * Creates a semantic version with pre-release identifier.
     *
     * @param major      the major number, 0 or more
     * @param minor      the minor number, 0 or more
     * @param patch      the patch number, 0 or more
     * @param preRelease the pre-release identifiers without the {@code -}, e.g. {@code "beta.2"}
     * @return the version, with no build metadata
     * @throws ValueObjectException if a number is negative or the pre-release is not valid
     */
    public static SemanticVersion preRelease(int major, int minor, int patch, String preRelease) {
        return new SemanticVersion(major, minor, patch, preRelease, null);
    }

    /**
     * Creates initial development version (0.1.0).
     *
     * @return {@code 0.1.0}
     */
    public static SemanticVersion initial() {
        return Version.INITIAL.toVersion();
    }

    /**
     * Returns the default version (0.0.0).
     *
     * @return {@link #DEFAULT}
     */
    public static SemanticVersion defaultVersion() {
        return DEFAULT;
    }

    /**
     * Parses a semantic version string, ignoring blanks around it and one leading {@code v} or {@code V}
     * ({@code "v1.2.3-rc.1+build.5"}). All three numbers are required, without leading zeros.
     *
     * @param version the text
     * @return the version
     * @throws ValueObjectException if the text is null, not a semantic version, or has a number too large for an
     *                              {@code int}
     */
    public static SemanticVersion parse(String version) {
        if (version == null) throw new ValueObjectException("A SemanticVersion is required");
        version = version.trim();

        if (version.startsWith("v") || version.startsWith("V")) {
            version = version.substring(1);
        }

        Matcher matcher = SEMVER_PATTERN.matcher(version);
        if (!matcher.matches()) {
            throw new ValueObjectException("Invalid semantic version: " + version);
        }

        return new SemanticVersion(
            number(matcher.group(1), "Major"),
            number(matcher.group(2), "Minor"),
            number(matcher.group(3), "Patch"),
            matcher.group(4),
            matcher.group(5)
        );
    }

    private static int number(String digits, String what) {
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            throw new ValueObjectException(what + " version too large: " + digits, e);
        }
    }

    /**
     * Attempts to parse a version string, returning the default (0.0.0) if invalid.
     *
     * @param version the text, or null
     * @return the version, or {@link #DEFAULT} when the text is null or not a version
     */
    public static SemanticVersion parseOrDefault(@Nullable String version) {
        return tryParse(version).orElse(DEFAULT);
    }

    /**
     * Parses a version string like {@link #parse(String)}, without throwing.
     *
     * @param version any text, or null
     * @return the version, or empty when the text is null or not a semantic version (a number too large for an
     *         {@code int} included); never throws
     */
    public static Optional<SemanticVersion> tryParse(@Nullable String version) {
        if (version == null) return Optional.empty();
        try {
            return Optional.of(parse(version));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Checks if a string is a valid semantic version that {@link #parse(String)} accepts; a number too large for
     * an {@code int} is not valid.
     *
     * @param version the text, or null
     * @return whether it is a semantic version ({@code false} for null or blank); never throws
     */
    public static boolean isValid(@Nullable String version) {
        return tryParse(version).isPresent();
    }

    // ==================== Query Methods ====================

    /**
     * The pre-release identifiers, if any.
     *
     * @return e.g. {@code "rc.1"}, or empty for a release
     */
    public Optional<String> getPreRelease() {
        return preRelease.isEmpty() ? Optional.empty() : Optional.of(preRelease);
    }

    /**
     * The build metadata, if any.
     *
     * @return e.g. {@code "build.5"}, or empty
     */
    public Optional<String> getBuildMetadata() {
        return buildMetadata.isEmpty() ? Optional.empty() : Optional.of(buildMetadata);
    }

    /**
     * Tells whether this is a pre-release.
     *
     * @return whether it has pre-release identifiers
     */
    public boolean isPreRelease() {
        return !preRelease.isEmpty();
    }

    /**
     * Tells whether this version carries build metadata.
     *
     * @return whether it has build metadata
     */
    public boolean hasBuildMetadata() {
        return !buildMetadata.isEmpty();
    }

    /**
     * Tells whether this is an initial-development version, major 0, whose public API is not yet stable.
     *
     * @return whether the major number is 0
     */
    public boolean isInitialDevelopment() {
        return majorValue() == 0;
    }

    /**
     * Tells whether this is a stable release: major 1 or more and not a pre-release.
     *
     * @return whether it is stable
     */
    public boolean isStable() {
        return majorValue() >= 1 && !isPreRelease();
    }

    /**
     * Tells whether this is exactly {@code 0.0.0}, with no pre-release and no build metadata.
     *
     * @return whether it is the default version
     */
    public boolean isDefault() {
        return majorValue() == 0 && minorValue() == 0 && patchValue() == 0
            && !isPreRelease() && !hasBuildMetadata();
    }

    // ==================== Version Bump Methods ====================

    /**
     * The next major version: major plus one, minor and patch 0, no pre-release or build metadata.
     *
     * @return e.g. {@code 2.0.0} for {@code 1.4.2-rc.1}
     * @throws ValueObjectException if the number to bump is already {@link Integer#MAX_VALUE}
     */
    public SemanticVersion nextMajor() {
        try {
            return new SemanticVersion(Math.addExact(majorValue(), 1), 0, 0, null, null);
        } catch (ArithmeticException e) {
            throw new ValueObjectException("Version number too large to bump: " + this, e);
        }
    }

    /**
     * The next minor version: minor plus one, patch 0, no pre-release or build metadata.
     *
     * @return e.g. {@code 1.5.0} for {@code 1.4.2}
     * @throws ValueObjectException if the number to bump is already {@link Integer#MAX_VALUE}
     */
    public SemanticVersion nextMinor() {
        try {
            return new SemanticVersion(majorValue(), Math.addExact(minorValue(), 1), 0, null, null);
        } catch (ArithmeticException e) {
            throw new ValueObjectException("Version number too large to bump: " + this, e);
        }
    }

    /**
     * The next patch version: patch plus one, no pre-release or build metadata. (For a pre-release such as
     * {@code 1.4.2-rc.1} this gives {@code 1.4.3}, not the release {@code 1.4.2}; see {@link #toRelease()}.)
     *
     * @return e.g. {@code 1.4.3} for {@code 1.4.2}
     * @throws ValueObjectException if the number to bump is already {@link Integer#MAX_VALUE}
     */
    public SemanticVersion nextPatch() {
        try {
            return new SemanticVersion(majorValue(), minorValue(), Math.addExact(patchValue(), 1), null, null);
        } catch (ArithmeticException e) {
            throw new ValueObjectException("Version number too large to bump: " + this, e);
        }
    }

    /**
     * The same version with other pre-release identifiers; the build metadata is kept.
     *
     * @param preReleaseId the pre-release identifiers without the {@code -}; null or blank for none
     * @return the new version
     * @throws ValueObjectException if the pre-release is not made of valid SemVer identifiers
     */
    public SemanticVersion withPreRelease(@Nullable String preReleaseId) {
        return new SemanticVersion(majorValue(), minorValue(), patchValue(), preReleaseId, buildMetadata);
    }

    /**
     * The same version with other build metadata; the pre-release is kept.
     *
     * @param metadata the build metadata without the {@code +}; null or blank for none
     * @return the new version
     * @throws ValueObjectException if the metadata is not made of valid SemVer identifiers
     */
    public SemanticVersion withBuildMetadata(@Nullable String metadata) {
        return new SemanticVersion(majorValue(), minorValue(), patchValue(), preRelease, metadata);
    }

    /**
     * The same version without its pre-release; the build metadata is kept.
     *
     * @return e.g. {@code 1.4.2} for {@code 1.4.2-rc.1}
     */
    public SemanticVersion toRelease() {
        return new SemanticVersion(majorValue(), minorValue(), patchValue(), null, buildMetadata);
    }

    // ==================== Comparison ====================

    /**
     * Orders by SemVer precedence: major, minor and patch numerically; then a pre-release before the release;
     * then pre-release identifiers one by one (numbers numerically and before words, words in ASCII order, and
     * fewer identifiers first when all shared ones are equal). Build metadata is ignored.
     *
     * @param other the version to compare to
     * @return negative, zero or positive as this version is older than, equal to or newer than {@code other}
     * @throws ValueObjectException if {@code other} is null
     */
    @Override
    public int compareTo(SemanticVersion other) {
        if (other == null) throw new ValueObjectException("A SemanticVersion to compare to is required");
        int cmp = Integer.compare(this.majorValue(), other.majorValue());
        if (cmp != 0) return cmp;

        cmp = Integer.compare(this.minorValue(), other.minorValue());
        if (cmp != 0) return cmp;

        cmp = Integer.compare(this.patchValue(), other.patchValue());
        if (cmp != 0) return cmp;

        if (this.preRelease.isEmpty() && !other.preRelease.isEmpty()) return 1;
        if (!this.preRelease.isEmpty() && other.preRelease.isEmpty()) return -1;
        if (this.preRelease.isEmpty() && other.preRelease.isEmpty()) return 0;

        return comparePreRelease(this.preRelease, other.preRelease);
    }

    private int comparePreRelease(String a, String b) {
        String[] partsA = a.split("\\.");
        String[] partsB = b.split("\\.");

        int length = Math.min(partsA.length, partsB.length);
        for (int i = 0; i < length; i++) {
            int cmp = comparePreReleasePart(partsA[i], partsB[i]);
            if (cmp != 0) return cmp;
        }

        return Integer.compare(partsA.length, partsB.length);
    }

    private int comparePreReleasePart(String a, String b) {
        boolean aNumeric = a.matches("\\d+");
        boolean bNumeric = b.matches("\\d+");

        if (aNumeric && bNumeric) {
            // No leading zeros (checked on construction), so a longer number is a larger one; this works for
            // numbers of any size.
            String x = stripZeros(a), y = stripZeros(b);
            int byLength = Integer.compare(x.length(), y.length());
            return byLength != 0 ? byLength : x.compareTo(y);
        }
        if (aNumeric) return -1;
        if (bNumeric) return 1;
        return a.compareTo(b);
    }

    private static String stripZeros(String digits) {
        int i = 0;
        while (i < digits.length() - 1 && digits.charAt(i) == '0') i++;
        return digits.substring(i);
    }

    /**
     * Tells whether this version has higher precedence than another.
     *
     * @param other the version to compare with
     * @return whether this one is newer
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isNewerThan(SemanticVersion other) {
        return compareTo(other) > 0;
    }

    /**
     * Tells whether this version has lower precedence than another.
     *
     * @param other the version to compare with
     * @return whether this one is older
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isOlderThan(SemanticVersion other) {
        return compareTo(other) < 0;
    }

    /**
     * Tells whether two versions are API-compatible under SemVer, by their numbers alone (pre-release and build
     * metadata are not considered), with the usual rule for initial development:
     * <ul>
     *   <li>from {@code 1.0.0} on, the same major number ({@code 1.2.0} and {@code 1.9.3});</li>
     *   <li>for {@code 0.y.z} with {@code y > 0}, the same major and minor numbers ({@code 0.3.1} and
     *       {@code 0.3.8}, but not {@code 0.4.0});</li>
     *   <li>for {@code 0.0.z}, the same patch number too, i.e. only the very same {@code 0.0.z}.</li>
     * </ul>
     * The relation is symmetric.
     *
     * @param other the version to compare with
     * @return whether the two versions are compatible
     * @throws ValueObjectException if {@code other} is null
     */
    public boolean isCompatibleWith(SemanticVersion other) {
        if (other == null) throw new ValueObjectException("A SemanticVersion to compare with is required");
        if (this.majorValue() != other.majorValue()) return false;
        if (this.majorValue() > 0) return true;
        if (this.minorValue() != other.minorValue()) return false;
        if (this.minorValue() > 0) return true;
        return this.patchValue() == other.patchValue();
    }

    // ==================== Formatting ====================

    /**
     * Writes only the three numbers.
     *
     * @return e.g. {@code "1.4.2"}
     */
    public String toCore() {
        return majorValue() + "." + minorValue() + "." + patchValue();
    }

    /**
     * Writes the full version, with the pre-release and build metadata when present.
     *
     * @return e.g. {@code "1.4.2-rc.1+build.5"}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(majorValue()).append('.').append(minorValue()).append('.').append(patchValue());

        if (!preRelease.isEmpty()) {
            sb.append('-').append(preRelease);
        }
        if (!buildMetadata.isEmpty()) {
            sb.append('+').append(buildMetadata);
        }

        return sb.toString();
    }

    /**
     * Writes the full version with a leading {@code v}, as used for Git tags.
     *
     * @return e.g. {@code "v1.4.2"}
     */
    public String toTagString() {
        return "v" + toString();
    }

    /**
     * Tells whether another object is a version with the same numbers (null counting as 0) and pre-release;
     * build metadata is ignored.
     *
     * @param o the object to compare with
     * @return whether it is an equal version
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SemanticVersion that)) return false;
        return majorValue() == that.majorValue()
            && minorValue() == that.minorValue()
            && patchValue() == that.patchValue()
            && preRelease.equals(that.preRelease);
    }

    /**
     * A hash of the numbers and pre-release, consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(majorValue(), minorValue(), patchValue(), preRelease);
    }


}
