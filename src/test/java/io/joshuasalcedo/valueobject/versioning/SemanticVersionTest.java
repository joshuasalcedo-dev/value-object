package io.joshuasalcedo.valueobject.versioning;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SemanticVersionTest {

    private static SemanticVersion v(String s) {
        return SemanticVersion.parse(s);
    }

    @Test
    void parseReadsFullVersions() {
        SemanticVersion sv = v("1.2.3-rc.1+build.5");
        assertEquals(1, sv.major());
        assertEquals(2, sv.minor());
        assertEquals(3, sv.patch());
        assertEquals("rc.1", sv.preRelease());
        assertEquals("build.5", sv.buildMetadata());
        assertEquals(Optional.of("rc.1"), sv.getPreRelease());
        assertEquals(Optional.of("build.5"), sv.getBuildMetadata());
        assertEquals("1.2.3-rc.1+build.5", sv.toString());
        assertEquals("v1.2.3-rc.1+build.5", sv.toTagString());
        assertEquals("1.2.3", sv.toCore());
        assertEquals(sv, v("  v1.2.3-rc.1+build.5 "));
        assertEquals(sv, v("V1.2.3-rc.1"));
        assertEquals(SemanticVersion.of(0, 0, 0), v("0.0.0"));
        assertEquals("1.0.0-0A.is.legal", v("1.0.0-0A.is.legal").toString());
        assertEquals("1.0.0+001", v("1.0.0+001").toString());
        assertEquals(Integer.MAX_VALUE, v("2147483647.0.0").major());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "1", "1.2", "1.2.3.4", "01.2.3", "1.02.3", "1.2.03", "-1.2.3", "1.2.3-",
            "1.2.3+", "1.2.3-01", "1.2.3-rc..1", "1.2.3-rc_1", "1.2.3+build..5", "vv1.2.3", "1.2.3 beta",
            "2147483648.0.0", "1.99999999999999999999.0", "1.2.4294967296"})
    void badTextIsRejectedEverywhereWithoutOtherExceptions(String bad) {
        assertThrows(ValueObjectException.class, () -> SemanticVersion.parse(bad));
        assertEquals(Optional.empty(), SemanticVersion.tryParse(bad));
        assertFalse(SemanticVersion.isValid(bad));
        assertEquals(SemanticVersion.DEFAULT, SemanticVersion.parseOrDefault(bad));
    }

    @Test
    void nullIsHandledWithoutNullPointerException() {
        assertThrows(ValueObjectException.class, () -> SemanticVersion.parse(null));
        assertEquals(Optional.empty(), SemanticVersion.tryParse(null));
        assertFalse(SemanticVersion.isValid(null));
        assertEquals(SemanticVersion.DEFAULT, SemanticVersion.parseOrDefault(null));
        assertThrows(ValueObjectException.class, () -> SemanticVersion.of((SemanticVersion.Version) null));
        assertThrows(ValueObjectException.class, () -> v("1.0.0").compareTo(null));
        assertThrows(ValueObjectException.class, () -> v("1.0.0").isNewerThan(null));
        assertThrows(ValueObjectException.class, () -> v("1.0.0").isCompatibleWith(null));
    }

    @Test
    void tryParseAndIsValidOnGoodInput() {
        assertEquals(Optional.of(v("1.2.3")), SemanticVersion.tryParse("v1.2.3"));
        assertTrue(SemanticVersion.isValid("1.2.3-alpha+exp.sha.5114f85"));
        assertEquals(v("4.5.6"), SemanticVersion.parseOrDefault("4.5.6"));
    }

    @Test
    void constructionValidatesIdentifiersSoToStringRoundTrips() {
        assertThrows(ValueObjectException.class, () -> SemanticVersion.preRelease(1, 0, 0, "rc 1"));
        assertThrows(ValueObjectException.class, () -> SemanticVersion.preRelease(1, 0, 0, "01"));
        assertThrows(ValueObjectException.class, () -> SemanticVersion.preRelease(1, 0, 0, "rc..1"));
        assertThrows(ValueObjectException.class, () -> SemanticVersion.preRelease(1, 0, 0, "a+b"));
        assertThrows(ValueObjectException.class, () -> SemanticVersion.of(1, 0, 0, null, "build!"));
        assertThrows(ValueObjectException.class, () -> SemanticVersion.of(1, 0, 0, null, ".x"));
        assertThrows(ValueObjectException.class, () -> v("1.0.0").withPreRelease("é"));
        assertThrows(ValueObjectException.class, () -> v("1.0.0").withBuildMetadata("a b"));
        assertThrows(ValueObjectException.class, () -> SemanticVersion.of(-1, 0, 0));
        assertThrows(ValueObjectException.class, () -> SemanticVersion.of(0, -1, 0));
        assertThrows(ValueObjectException.class, () -> SemanticVersion.of(0, 0, -1));
        assertThrows(ValueObjectException.class, () -> SemanticVersion.Version.V1_0_0.toVersion("bad meta"));

        List<SemanticVersion> built = List.of(
                SemanticVersion.of(1, 2, 3, " rc.1 ", " build-7 "),
                SemanticVersion.preRelease(0, 1, 0, "alpha.0.x-y"),
                SemanticVersion.of(2, 0, 0, null, "001"),
                SemanticVersion.of(2, 0, 0, "", "  "),
                new SemanticVersion(null, null, 4, null, null),
                SemanticVersion.Version.V1_0_0_SNAPSHOT.toVersion("20260124"));
        for (SemanticVersion sv : built) {
            SemanticVersion back = SemanticVersion.parse(sv.toString());
            assertEquals(sv, back, sv.toString());
            assertEquals(sv.toString(), back.toString());
        }
        assertEquals("1.2.3-rc.1+build-7", built.get(0).toString());
        assertEquals("2.0.0", built.get(3).toString());
        assertEquals("0.0.4", built.get(4).toString());
    }

    @Test
    void nullNumbersCountAsZero() {
        SemanticVersion sv = new SemanticVersion(null, null, null, null, null);
        assertEquals(SemanticVersion.DEFAULT, sv);
        assertEquals(SemanticVersion.DEFAULT.hashCode(), sv.hashCode());
        assertTrue(sv.isDefault());
        assertEquals(0, sv.majorValue());
    }

    @Test
    void precedenceFollowsSemVer() {
        List<String> ordered = List.of("1.0.0-alpha", "1.0.0-alpha.1", "1.0.0-alpha.beta", "1.0.0-beta",
                "1.0.0-beta.2", "1.0.0-beta.11", "1.0.0-rc.1", "1.0.0", "1.0.1", "1.1.0", "2.0.0");
        List<SemanticVersion> versions = new ArrayList<>(ordered.stream().map(SemanticVersion::parse).toList());
        Collections.reverse(versions);
        Collections.sort(versions);
        assertEquals(ordered, versions.stream().map(SemanticVersion::toString).toList());
        assertEquals(0, v("1.0.0+a").compareTo(v("1.0.0+b")));
        assertEquals(v("1.0.0+a"), v("1.0.0+b"));
        assertTrue(v("1.0.0").isNewerThan(v("1.0.0-rc.1")));
        assertTrue(v("1.0.0-rc.1").isOlderThan(v("1.0.0")));
    }

    @Test
    void largeNumericPreReleaseIdentifiersCompareWithoutThrowing() {
        SemanticVersion a = v("1.0.0-99999999999999999999");
        SemanticVersion b = v("1.0.0-100000000000000000000");
        SemanticVersion c = v("1.0.0-9");
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertTrue(c.compareTo(a) < 0);
        assertEquals(0, a.compareTo(v("1.0.0-99999999999999999999")));
        assertTrue(a.compareTo(v("1.0.0-alpha")) < 0); // numbers before words
    }

    @Test
    void compatibilityFollowsSemVer() {
        assertTrue(v("1.2.0").isCompatibleWith(v("1.9.3")));
        assertTrue(v("1.9.3").isCompatibleWith(v("1.2.0")));
        assertFalse(v("1.2.0").isCompatibleWith(v("2.0.0")));
        assertTrue(v("0.3.1").isCompatibleWith(v("0.3.8")));
        assertFalse(v("0.3.1").isCompatibleWith(v("0.4.0")));
        assertFalse(v("0.3.1").isCompatibleWith(v("1.3.1")));
        assertTrue(v("0.0.3").isCompatibleWith(v("0.0.3")));
        assertTrue(v("0.0.3").isCompatibleWith(v("0.0.3-rc.1")));
        assertFalse(v("0.0.3").isCompatibleWith(v("0.0.4")));
        assertFalse(v("0.0.3").isCompatibleWith(v("0.1.3")));
    }

    @Test
    void bumpsAndQueries() {
        SemanticVersion sv = v("1.4.2-rc.1+b");
        assertEquals(v("2.0.0"), sv.nextMajor());
        assertEquals(v("1.5.0"), sv.nextMinor());
        assertEquals(v("1.4.3"), sv.nextPatch());
        assertEquals("1.4.2+b", sv.toRelease().toString());
        assertEquals("1.4.2-beta+b", sv.withPreRelease("beta").toString());
        assertEquals("1.4.2+b", sv.withPreRelease(null).toString());
        assertEquals("1.4.2-rc.1+c", sv.withBuildMetadata("c").toString());
        assertTrue(sv.isPreRelease());
        assertTrue(sv.hasBuildMetadata());
        assertFalse(sv.isStable());
        assertTrue(v("1.0.0").isStable());
        assertTrue(v("0.9.0").isInitialDevelopment());
        assertFalse(v("0.0.0+meta").isDefault());
        assertThrows(ValueObjectException.class, () -> v("2147483647.0.0").nextMajor());
        assertThrows(ValueObjectException.class, () -> v("0.2147483647.0").nextMinor());
        assertThrows(ValueObjectException.class, () -> v("0.0.2147483647").nextPatch());
    }

    @Test
    void wellKnownVersions() {
        assertEquals("0.1.0", SemanticVersion.initial().toString());
        assertEquals(SemanticVersion.DEFAULT, SemanticVersion.defaultVersion());
        assertEquals("1.0.0-SNAPSHOT", SemanticVersion.of(SemanticVersion.Version.V1_0_0_SNAPSHOT).toString());
        for (SemanticVersion.Version c : SemanticVersion.Version.values()) {
            SemanticVersion sv = c.toVersion();
            assertEquals(sv, SemanticVersion.parse(sv.toString()));
            assertEquals(c.major(), sv.majorValue());
        }
    }
}
