package io.joshuasalcedo.valueobject.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.joshuasalcedo.valueobject.geo.Country;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRulesProvider;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** The generated files in {@code data/} (see {@code data/README.md}) agree with each other, with Country and with the JDK. */
class DataFilesTest {

    static JsonNode read(String name) throws IOException {
        try (InputStream in = DataFilesTest.class.getResourceAsStream("/io/joshuasalcedo/valueobject/data/" + name)) {
            assertNotNull(in, name + " is on the classpath");
            return new ObjectMapper().readTree(in);
        }
    }

    static Set<String> codes(JsonNode list, String field) {
        var codes = new TreeSet<String>();
        list.forEach(n -> codes.add(n.get(field).asText()));
        return codes;
    }

    @Test
    void everyCountryConstantIsInBothFiles() throws IOException {
        Set<String> enumCodes = Arrays.stream(Country.values()).filter(c -> !c.isUnknown()).map(Country::code)
                .collect(Collectors.toCollection(TreeSet::new));
        assertEquals(enumCodes, codes(read("tzdata.json").get("countries"), "country_code"));
        assertEquals(enumCodes, codes(read("countries.json").get("countries"), "alpha2"));
    }

    @Test
    void countriesHaveTheirCodesAndZones() throws IOException {
        for (JsonNode c : read("countries.json").get("countries")) {
            String code = c.get("alpha2").asText();
            assertTrue(c.get("alpha3").asText().matches("[A-Z]{3}"), code + " alpha3");
            // zone.tab gives every inhabited country a zone; Bouvet Island and Heard Island have none
            assertEquals(!Set.of("BV", "HM").contains(code), !c.get("timezones").isEmpty(), code + " time zones");
            for (JsonNode zone : c.get("timezones")) ZoneId.of(zone.asText());
        }
        JsonNode ph = null;
        for (JsonNode c : read("countries.json").get("countries")) if (c.get("alpha2").asText().equals("PH")) ph = c;
        assertNotNull(ph);
        assertEquals("PHL", ph.get("alpha3").asText());
        assertEquals("608", ph.get("numeric").asText());
        assertEquals("+63", ph.get("calling_codes").get(0).asText());
        assertEquals("PHP", ph.get("currencies").get(0).asText());
        assertEquals("Asia/Manila", ph.get("timezones").get(0).asText());
    }

    /** Every zone and link is one the JDK knows, and a zone's standard offset is the JDK's (when their versions agree). */
    @Test
    void zonesAgreeWithTheJdk() throws IOException {
        JsonNode tz = read("tzdata.json");
        Set<String> jdk = ZoneId.getAvailableZoneIds();
        var unknown = new TreeSet<String>();
        tz.get("all_zones").forEach(z -> { if (!jdk.contains(z.get("tzid").asText())) unknown.add(z.get("tzid").asText()); });
        tz.get("links").forEach(l -> { if (!jdk.contains(l.get("alias").asText())) unknown.add(l.get("alias").asText()); });
        // a zone IANA added after this JDK's tzdata is fine; many would mean the parser is wrong
        assertTrue(unknown.size() < 10, "zones the JDK does not know: " + unknown);

        // offsets change between tzdata releases (2026's Canadian changes), so compare them only with the same release
        String jdkVersion = ZoneRulesProvider.getVersions("UTC").lastKey();
        Assumptions.assumeTrue(jdkVersion.equals(tz.get("version").asText()),
                "this JDK has tzdata " + jdkVersion + ", the data is " + tz.get("version").asText());
        Set<String> negativeDst = Set.of("Africa/Windhoek", "Europe/Dublin", "Africa/Casablanca", "Africa/El_Aaiun");
        Instant now = Instant.now();
        var differ = new TreeSet<String>();
        for (JsonNode z : tz.get("all_zones")) {
            String id = z.get("tzid").asText();
            if (!jdk.contains(id) || negativeDst.contains(id)) continue;
            ZoneOffset standard = ZoneId.of(id).getRules().getStandardOffset(now);
            if (!ZoneOffset.of(z.get("utc_offset").asText()).equals(standard)) {
                differ.add(id + " " + z.get("utc_offset").asText() + " (JDK " + standard + ")");
            }
        }
        assertTrue(differ.isEmpty(), "zones with a different standard offset from the JDK's: " + differ);
    }
}
