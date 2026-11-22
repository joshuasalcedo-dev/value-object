package io.joshuasalcedo.valueobject.versioning;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Value object representing a semantic version (SemVer 2.0.0).
 * Format: MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD]
 *
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

    /** Default version: 0.0.0 */
    public static final SemanticVersion DEFAULT = new SemanticVersion(0, 0, 0, null, null);

    // ==================== Well-Known Versions ====================

    /**
     * Common version constants as an enum.
     * Use {@code SemanticVersion.Version.INITIAL.toVersion()} or
     * {@code SemanticVersion.of(SemanticVersion.Version.V1_0_0)}.
     */
    public enum Version {
        ZERO(0, 0, 0, null),
        INITIAL(0, 1, 0, null),
        V1_0_0(1, 0, 0, null),
        V1_0_0_SNAPSHOT(1, 0, 0, "SNAPSHOT"),
        V1_1_0(1, 1, 0, null),
        V2_0_0(2, 0, 0, null),
        V2_0_0_SNAPSHOT(2, 0, 0, "SNAPSHOT"),
        ALPHA(0, 1, 0, "alpha"),
        BETA(0, 1, 0, "beta"),
        RC1(1, 0, 0, "rc.1"),
        RC2(1, 0, 0, "rc.2");

        private final int major, minor, patch;
        private final @Nullable String preRelease;

        Version(int major, int minor, int patch, @Nullable String preRelease) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.preRelease = preRelease;
        }

        public int major() { return major; }
        public int minor() { return minor; }
        public int patch() { return patch; }
        public @Nullable String preRelease() { return preRelease; }

        /**
         * Converts this version constant to a SemanticVersion instance.
         */
        public SemanticVersion toVersion() {
            return new SemanticVersion(major, minor, patch, preRelease, null);
        }

        /**
         * Converts this version constant with build metadata.
         */
        public SemanticVersion toVersion(String buildMetadata) {
            return new SemanticVersion(major, minor, patch, preRelease, buildMetadata);
        }
    }

    // ==================== Compact Constructor ====================

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
    }

    // ==================== Safe Accessors ====================

    /** Returns the major version, defaulting to 0 if null. */
    public int majorValue() {
        return major != null ? major : 0;
    }

    /** Returns the minor version, defaulting to 0 if null. */
    public int minorValue() {
        return minor != null ? minor : 0;
    }

    /** Returns the patch version, defaulting to 0 if null. */
    public int patchValue() {
        return patch != null ? patch : 0;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a SemanticVersion from a {@link Version} enum constant.
     */
    public static SemanticVersion of(Version version) {
        return version.toVersion();
    }

    /**
     * Creates a semantic version with all components.
     */
    public static SemanticVersion of(int major, int minor, int patch,
                                     String preRelease, String buildMetadata) {
        return new SemanticVersion(major, minor, patch, preRelease, buildMetadata);
    }

    /**
     * Creates a semantic version without pre-release or build metadata.
     */
    public static SemanticVersion of(int major, int minor, int patch) {
        return new SemanticVersion(major, minor, patch, null, null);
    }

    /**
     * Creates a semantic version with pre-release identifier.
     */
    public static SemanticVersion preRelease(int major, int minor, int patch, String preRelease) {
        return new SemanticVersion(major, minor, patch, preRelease, null);
    }

    /**
     * Creates initial development version (0.1.0).
     */
    public static SemanticVersion initial() {
        return Version.INITIAL.toVersion();
    }

    /**
     * Returns the default version (0.0.0).
     */
    public static SemanticVersion defaultVersion() {
        return DEFAULT;
    }

    /**
     * Parses a semantic version string.
     */
    public static SemanticVersion parse(String version) {
        Objects.requireNonNull(version, "Version string cannot be null");
        version = version.trim();

        if (version.startsWith("v") || version.startsWith("V")) {
            version = version.substring(1);
        }

        Matcher matcher = SEMVER_PATTERN.matcher(version);
        if (!matcher.matches()) {
            throw new ValueObjectException("Invalid semantic version: " + version);
        }

        return new SemanticVersion(
            Integer.parseInt(matcher.group(1)),
            Integer.parseInt(matcher.group(2)),
            Integer.parseInt(matcher.group(3)),
            matcher.group(4),
            matcher.group(5)
        );
    }

    /**
     * Attempts to parse a version string, returning the default (0.0.0) if invalid.
     */
    public static SemanticVersion parseOrDefault(String version) {
        try {
            return parse(version);
        } catch (Exception e) {
            return DEFAULT;
        }
    }

    /**
     * Attempts to parse a version string, returning null if invalid.
     */
    public static @Nullable SemanticVersion tryParse(String version) {
        try {
            return parse(version);
        } catch (ValueObjectException e) {
            return null;
        }
    }

    /**
     * Checks if a string is a valid semantic version.
     */
    public static boolean isValid(String version) {
        if (version == null || version.isBlank()) {
            return false;
        }
        String v = version.trim();
        if (v.startsWith("v") || v.startsWith("V")) {
            v = v.substring(1);
        }
        return SEMVER_PATTERN.matcher(v).matches();
    }

    // ==================== Query Methods ====================

    public Optional<String> getPreRelease() {
        return preRelease.isEmpty() ? Optional.empty() : Optional.of(preRelease);
    }

    public Optional<String> getBuildMetadata() {
        return buildMetadata.isEmpty() ? Optional.empty() : Optional.of(buildMetadata);
    }

    public boolean isPreRelease() {
        return !preRelease.isEmpty();
    }

    public boolean hasBuildMetadata() {
        return !buildMetadata.isEmpty();
    }

    public boolean isInitialDevelopment() {
        return majorValue() == 0;
    }

    public boolean isStable() {
        return majorValue() >= 1 && !isPreRelease();
    }

    public boolean isDefault() {
        return majorValue() == 0 && minorValue() == 0 && patchValue() == 0
            && !isPreRelease() && !hasBuildMetadata();
    }

    // ==================== Version Bump Methods ====================

    public SemanticVersion nextMajor() {
        return new SemanticVersion(majorValue() + 1, 0, 0, null, null);
    }

    public SemanticVersion nextMinor() {
        return new SemanticVersion(majorValue(), minorValue() + 1, 0, null, null);
    }

    public SemanticVersion nextPatch() {
        return new SemanticVersion(majorValue(), minorValue(), patchValue() + 1, null, null);
    }

    public SemanticVersion withPreRelease(String preReleaseId) {
        return new SemanticVersion(majorValue(), minorValue(), patchValue(), preReleaseId, buildMetadata);
    }

    public SemanticVersion withBuildMetadata(String metadata) {
        return new SemanticVersion(majorValue(), minorValue(), patchValue(), preRelease, metadata);
    }

    public SemanticVersion toRelease() {
        return new SemanticVersion(majorValue(), minorValue(), patchValue(), null, buildMetadata);
    }

    // ==================== Comparison ====================

    @Override
    public int compareTo(SemanticVersion other) {
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
            return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
        }
        if (aNumeric) return -1;
        if (bNumeric) return 1;
        return a.compareTo(b);
    }

    public boolean isNewerThan(SemanticVersion other) {
        return compareTo(other) > 0;
    }

    public boolean isOlderThan(SemanticVersion other) {
        return compareTo(other) < 0;
    }

    public boolean isCompatibleWith(SemanticVersion other) {
        return this.majorValue() == other.majorValue();
    }

    // ==================== Formatting ====================

    public String toCore() {
        return String.format("%d.%d.%d", majorValue(), minorValue(), patchValue());
    }

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

    public String toTagString() {
        return "v" + toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SemanticVersion that)) return false;
        return majorValue() == that.majorValue()
            && minorValue() == that.minorValue()
            && patchValue() == that.patchValue()
            && preRelease.equals(that.preRelease);
    }

    @Override
    public int hashCode() {
        return Objects.hash(majorValue(), minorValue(), patchValue(), preRelease);
    }
}