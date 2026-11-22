package io.joshuasalcedo.valueobject.geo;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.measurement.Distance;
import io.joshuasalcedo.valueobject.measurement.DistanceUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GeoTest {

    // ---------------- Coordinates

    @Test
    void coordinatesParseAndFormat() {
        Coordinates manila = Coordinates.parse(" 14.5995, 120.9842 ");
        assertEquals(14.5995, manila.latitude());
        assertEquals(120.9842, manila.longitude());
        assertEquals("14.599500, 120.984200", manila.toDecimalString());
        assertEquals("14°35'58.20\"N, 120°59'3.12\"E", manila.toDmsString());
        assertEquals("https://www.google.com/maps?q=14.599500,120.984200", manila.toGoogleMapsUrl());
        assertEquals("https://www.openstreetmap.org/?mlat=14.599500&mlon=120.984200", manila.toOpenStreetMapUrl());
        assertTrue(manila.isNorthernHemisphere());
        assertTrue(manila.isEasternHemisphere());
        assertFalse(manila.isSouthernHemisphere());
        assertFalse(manila.isWesternHemisphere());
    }

    @Test
    void formattingIgnoresTheDefaultLocale() {
        Locale before = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            Coordinates c = Coordinates.of(40.7128, -74.006);
            assertEquals("40.712800, -74.006000", c.toDecimalString());
            assertEquals(c, Coordinates.parse(c.toDecimalString()), "parse reads its own output");
            assertTrue(c.toDmsString().contains("46.08"));
        } finally {
            Locale.setDefault(before);
        }
    }

    @Test
    void coordinatesRejectNonFiniteAndOutOfRange() {
        assertThrows(ValueObjectException.class, () -> Coordinates.of(Double.NaN, 0));
        assertThrows(ValueObjectException.class, () -> Coordinates.of(0, Double.NaN));
        assertThrows(ValueObjectException.class, () -> Coordinates.of(Double.POSITIVE_INFINITY, 0));
        assertThrows(ValueObjectException.class, () -> Coordinates.of(0, Double.NEGATIVE_INFINITY));
        assertThrows(ValueObjectException.class, () -> Coordinates.of(90.0001, 0));
        assertThrows(ValueObjectException.class, () -> Coordinates.of(0, -180.0001));
        assertThrows(ValueObjectException.class, () -> Coordinates.of(null, "1"));
        assertDoesNotThrow(() -> Coordinates.of(-90, 180));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "14.5", "1,2,3", "NaN, 0", "0, Infinity", "a, b", "91, 0", "1,"})
    void coordinatesRejectBadText(String text) {
        assertThrows(ValueObjectException.class, () -> Coordinates.parse(text));
        assertTrue(Coordinates.tryParse(text).isEmpty());
        assertFalse(Coordinates.isValid(text));
    }

    @Test
    void destinationPointWrapsAcrossTheAntimeridian() {
        Coordinates fiji = Coordinates.of(-17.0, 179.5);
        Coordinates east = fiji.destinationPoint(Distance.of(200, DistanceUnit.KILOMETER), 90);
        assertTrue(east.longitude() < -178 && east.longitude() >= -180, "wrapped to " + east.longitude());
        Coordinates west = Coordinates.of(0, -179.9).destinationPoint(Distance.of(50, DistanceUnit.KILOMETER), 270);
        assertTrue(west.longitude() > 179, "wrapped to " + west.longitude());
        Coordinates north = Coordinates.of(0, 0).destinationPoint(Distance.of(111.195, DistanceUnit.KILOMETER), 0);
        assertEquals(1.0, north.latitude(), 0.001);
        assertEquals(0.0, north.longitude(), 1e-9);
    }

    @Test
    void distanceBearingAndEquality() {
        Coordinates manila = Coordinates.of(14.5995, 120.9842);
        Coordinates cebu = Coordinates.of(10.3157, 123.8854);
        double km = manila.distanceTo(cebu).convertTo(DistanceUnit.KILOMETER).value().doubleValue();
        assertEquals(570, km, 15);
        assertTrue(manila.isWithin(cebu, Distance.of(600, DistanceUnit.KILOMETER)));
        assertFalse(manila.isWithin(cebu, Distance.of(500, DistanceUnit.KILOMETER)));
        assertEquals(90.0, Coordinates.of(0, 0).bearingTo(Coordinates.of(0, 1)), 1e-9);
        assertEquals(Coordinates.of(1.0000001, 2), Coordinates.of(1.0000002, 2));
        assertEquals(Coordinates.of(1.0000001, 2).hashCode(), Coordinates.of(1.0000002, 2).hashCode());
        assertNotEquals(Coordinates.of(1.00001, 2), Coordinates.of(1.00002, 2));
        assertEquals(Coordinates.of(-33.8568, 151.2153), Coordinates.fromDMS(33, 51, 24.48, 'S', 151, 12, 55.08, 'E'));
        assertEquals(90, Coordinates.northPole().latitude());
        assertEquals(-90, Coordinates.southPole().latitude());
        assertEquals(Coordinates.of(0, 0), Coordinates.equatorPrimeMeridian());
    }

    // ---------------- Country

    @Test
    void everyIsoCountryIsPresent() {
        Set<String> iso = new TreeSet<>(Arrays.asList(Locale.getISOCountries()));
        Set<String> ours = Arrays.stream(Country.values()).filter(c -> c != Country.UNKNOWN).map(Country::code)
                .collect(Collectors.toCollection(TreeSet::new));
        assertEquals(249, ours.size());
        Set<String> missing = new TreeSet<>(iso);
        missing.removeAll(ours);
        assertEquals(Set.of(), missing, "ISO codes with no Country");
        Set<String> extra = new TreeSet<>(ours);
        extra.removeAll(iso);
        assertEquals(Set.of(), extra, "Country codes that are not ISO 3166-1");
    }

    @Test
    void countryCodesAgreeWithAlpha3() {
        assertEquals("PHL", Locale.of("", Country.PHILIPPINES.code()).getISO3Country());
        assertEquals("GBR", Locale.of("", Country.UNITED_KINGDOM.code()).getISO3Country());
        assertEquals("CHE", Locale.of("", Country.SWITZERLAND.code()).getISO3Country());
        assertEquals("USA", Locale.of("", Country.UNITED_STATES.code()).getISO3Country());
    }

    @ParameterizedTest
    @CsvSource({"PH, PHILIPPINES", "' ph ', PHILIPPINES", "Philippines, PHILIPPINES", "Filipino, PHILIPPINES",
            "PHILIPPINES, PHILIPPINES", "GB, UNITED_KINGDOM", "Swiss, SWITZERLAND", "jp, JAPAN"})
    void countryParses(String text, Country expected) {
        assertEquals(expected, Country.parse(text));
        assertTrue(Country.isValid(text));
    }

    @Test
    void countryLookups() {
        assertEquals("Philippines", Country.PHILIPPINES.displayName());
        assertEquals(Demonym.FILIPINO, Country.PHILIPPINES.demonym());
        assertEquals(Continent.ASIA, Country.PHILIPPINES.continent());
        assertEquals("🇵🇭", Country.PHILIPPINES.flagEmoji());
        assertEquals(Country.JAPAN, Country.fromCode("jp"));
        assertNull(Country.fromCode("zz"));
        assertEquals(Country.JAPAN, Country.fromName("japan").orElseThrow());
        assertTrue(Country.byContinent(Continent.ASIA).contains(Country.PHILIPPINES));
        assertTrue(Country.byDemonym(Demonym.FILIPINO).contains(Country.PHILIPPINES));
        assertNull(Country.fromAny(null));
        assertThrows(ValueObjectException.class, () -> Country.parse(null));
        assertThrows(ValueObjectException.class, () -> Country.parse("XX"));
        assertThrows(ValueObjectException.class, () -> Country.parse("Atlantis"));
        assertTrue(Country.tryParse(null).isEmpty());
        assertTrue(Country.UNKNOWN.isUnknown());
        assertTrue(Country.JAPAN.compareByCode(Country.PHILIPPINES) < 0);
        assertTrue(Country.JAPAN.compareByName(Country.PHILIPPINES) < 0);
    }

    // ---------------- Language

    @Test
    void languageParsesTrimmed() {
        assertEquals(Language.ENGLISH, Language.parse(" en"));
        assertEquals(Language.ENGLISH, Language.parse("English "));
        assertEquals(Language.TAGALOG, Language.parse("TL"));
        assertEquals(Language.JAPANESE, Language.fromLocale(Locale.JAPAN).orElseThrow());
        assertEquals("ja", Language.JAPANESE.code());
        assertEquals("Japanese", Language.JAPANESE.displayName());
        assertEquals("日本語", Language.JAPANESE.nativeName());
        assertTrue(Language.isValid(" de "));
        assertThrows(ValueObjectException.class, () -> Language.parse(null));
        assertThrows(ValueObjectException.class, () -> Language.parse("xx"));
        assertThrows(ValueObjectException.class, () -> Language.parse("klingon"));
        assertTrue(Language.tryParse(null).isEmpty());
        assertTrue(Language.UNKNOWN.isUnknown());
    }

    @Test
    void languageCodesAreIso639_1() {
        Set<String> iso = Set.of(Locale.getISOLanguages());
        for (Language l : Language.values()) {
            if (l == Language.UNKNOWN) continue;
            assertEquals(2, l.code().length(), l.name());
            assertTrue(iso.contains(l.code()), l.name() + " " + l.code());
        }
        // BENGALI is kept as an alias of BANGLA ("bn"); every other code is unique
        long distinct = Arrays.stream(Language.values()).map(Language::code).distinct().count();
        assertEquals(Language.values().length - 1, distinct, "codes are unique apart from the bn alias");
        assertEquals(Language.BANGLA, Language.parse("bn"));
    }

    // ---------------- Demonym

    @ParameterizedTest
    @CsvSource({"WELSH, Welsh", "FRENCH, French", "CHINESE, Chinese", "SWISS, Swiss", "BRITISH, British",
            "DUTCH, Dutch", "JAPANESE, Japanese", "FILIPINO, Filipinos", "AMERICAN, Americans"})
    void pluralKeepsSibilantEndings(Demonym d, String plural) {
        assertEquals(plural, d.plural());
    }

    @Test
    void demonymLookups() {
        assertEquals(Demonym.FILIPINO, Demonym.parse(" pinay "));
        assertEquals("Pinoy", Demonym.FILIPINO.masculine());
        assertEquals("Pinay", Demonym.FILIPINO.feminine());
        assertTrue(Demonym.FILIPINO.hasGenderedForms());
        assertTrue(Demonym.FILIPINO.matches("Philippine"));
        assertTrue(Demonym.FILIPINO.allNames().contains("Filipina"));
        assertEquals("American", Demonym.AMERICAN.masculineOrDefault());
        assertEquals("filipino", Demonym.FILIPINO.lowercased());
        assertEquals("FILIPINO", Demonym.FILIPINO.uppercased());
        assertTrue(Demonym.startingWith("fil").contains(Demonym.FILIPINO));
        assertThrows(ValueObjectException.class, () -> Demonym.parse(null));
        assertThrows(ValueObjectException.class, () -> Demonym.parse("Martian"));
        assertTrue(Demonym.tryParse(null).isEmpty());
        assertFalse(Demonym.isValid("Unknown"));
    }

    // ---------------- Continent, Race

    @Test
    void continentLookups() {
        assertEquals(Continent.ASIA, Continent.parse(" as "));
        assertEquals(Continent.SOUTH_AMERICA, Continent.parse("South America"));
        assertTrue(Continent.ASIA.contains(Country.PHILIPPINES));
        assertTrue(Continent.ASIA.countryCount() > 40);
        assertTrue(Continent.EUROPE.containsApproximate(48.85, 2.35));
        assertThrows(ValueObjectException.class, () -> Continent.parse(null));
        assertThrows(ValueObjectException.class, () -> Continent.parse("XX"));
        assertTrue(Continent.tryParse("Atlantis").isEmpty());
        int total = Arrays.stream(Continent.values()).mapToInt(Continent::countryCount).sum();
        assertEquals(Country.values().length, total, "every country is on exactly one continent");
    }

    @Test
    void raceLookups() {
        assertEquals(Race.WHITE, Race.parse(" caucasian "));
        assertEquals(Race.ASIAN, Race.parse("Asian"));
        assertTrue(Race.ASIAN.isUsCensus());
        assertTrue(Race.usCensusCategories().contains(Race.WHITE));
        assertThrows(ValueObjectException.class, () -> Race.parse(null));
        assertThrows(ValueObjectException.class, () -> Race.parse("Unknown"));
        assertTrue(Race.tryParse(null).isEmpty());
    }

    // ---------------- PostalCode

    @Test
    void postalCodesPerCountry() {
        assertEquals("94105-1804", PostalCode.us(" 94105-1804 ").value());
        assertEquals("SW1A 1AA", PostalCode.uk("sw1a 1aa").value());
        assertEquals("K1A 0B1", PostalCode.canada("k1a 0b1").value());
        assertTrue(PostalCode.isValid("94105", Country.UNITED_STATES));
        assertFalse(PostalCode.isValid("9410", Country.UNITED_STATES));
        assertFalse(PostalCode.isValid(null, Country.UNITED_STATES));
        assertFalse(PostalCode.isValid("94105", null));
        assertTrue(PostalCode.tryParse("abc", Country.UNITED_STATES).isEmpty());
        assertTrue(PostalCode.tryParse(null, Country.UNITED_STATES).isEmpty());
        assertThrows(ValueObjectException.class, () -> PostalCode.of(null, Country.UNITED_STATES));
        assertThrows(ValueObjectException.class, () -> PostalCode.of("94105", null));
        assertThrows(ValueObjectException.class, () -> PostalCode.parse("  ", Country.UNITED_STATES));
    }

    @Test
    void postalCodesUpperCaseWithoutTheDefaultLocale() {
        Locale before = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr"));
            // in Turkish, "i".toUpperCase() is a dotted capital I, which would not match [A-Za-z]
            assertEquals("1234 AI", PostalCode.of("1234 ai", Country.NETHERLANDS).value());
            assertEquals("KI1 1AA", PostalCode.of("ki1 1aa", Country.KENYA).value());
        } finally {
            Locale.setDefault(before);
        }
    }
}
