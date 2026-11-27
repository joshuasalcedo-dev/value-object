import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * IANA time zone database → {@code tzdata.json}.
 *
 * <pre>
 * java data/Tzdata.java            # writes src/main/resources/io/joshuasalcedo/valueobject/data/tzdata.json
 * java data/Tzdata.java --check    # CI: fails when tzdata.json is not what the pinned release gives
 * java data/Tzdata.java --latest   # moves the pin in data/sources.properties to IANA's newest release, then writes
 * </pre>
 *
 * <p>Reads, from the release's tar.gz: {@code iso3166.tab} (country names), {@code zone.tab} (one row per
 * country and zone), {@code zone1970.tab} (zones shared by countries since 1970), and the rule files (every
 * {@code Zone}, its current offset, and every {@code Link}, the old names). The tz database is public domain.
 */
public class Tzdata {

    static final List<String> RULE_FILES = List.of(
            "africa", "antarctica", "asia", "australasia", "europe", "northamerica", "southamerica",
            "etcetera", "backward", "factory");

    public static void main(String[] argv) {
        var args = Extract.Args.of(argv);
        if (args.latest()) pinLatest();
        var sources = Extract.sources();
        String version = Extract.source(sources, "tzdata.version");
        String sha256 = Extract.source(sources, "tzdata.sha256");
        String url = Extract.source(sources, "tzdata.url").replace("{version}", version);
        Map<String, byte[]> files = archive();

        var json = new LinkedHashMap<String, Object>();
        json.put("source", "IANA tzdata");
        json.put("license", "public domain");
        json.put("version", version);
        json.put("sha256", sha256);
        json.put("source_url", url);
        json.put("generated_by", "data/Tzdata.java");

        // ---- Zone and Link lines ----
        Map<String, Zone> zones = new TreeMap<>();
        Map<String, String> links = new TreeMap<>();
        for (String name : RULE_FILES) {
            readRules(name, text(files, name), zones, links);
        }

        // ---- iso3166.tab ----
        var countries = new ArrayList<Object>();
        for (String[] row : tab(text(files, "iso3166.tab"))) {
            countries.add(map("country_code", row[0], "country_name", row[1]));
        }
        json.put("countries", countries);

        // ---- zone.tab: the same rows as before, plus where each zone's rules are and its current offset ----
        var zoneTab = new ArrayList<Object>();
        for (String[] row : tab(text(files, "zone.tab"))) {
            String tzid = row[2];
            Zone zone = zones.get(canonical(tzid, links));
            if (zone == null) throw new IllegalStateException("zone.tab: " + tzid + " is not a Zone or Link");
            double[] at = coordinates(row[1]);
            var z = map("country_code", row[0], "tzid", tzid, "latitude", at[0], "longitude", at[1],
                    "comments", row.length > 3 ? row[3] : "");
            z.put("canonical", zone.tzid);
            z.put("utc_offset", zone.utcOffset());
            z.put("rules", zone.rules);
            z.put("format", zone.format);
            zoneTab.add(z);
        }
        json.put("zones", zoneTab);

        // ---- zone1970.tab ----
        var zone1970 = new ArrayList<Object>();
        for (String[] row : tab(text(files, "zone1970.tab"))) {
            double[] at = coordinates(row[1]);
            zone1970.add(map("countries", List.of(row[0].split(",")), "tzid", row[2], "latitude", at[0],
                    "longitude", at[1], "comments", row.length > 3 ? row[3] : ""));
        }
        json.put("zone1970", zone1970);

        // ---- every Zone, and every Link to the Zone it ends at ----
        var all = new ArrayList<Object>();
        for (Zone zone : zones.values()) {
            all.add(map("tzid", zone.tzid, "utc_offset", zone.utcOffset(), "rules", zone.rules, "format", zone.format));
        }
        json.put("all_zones", all);
        var linkList = new ArrayList<Object>();
        for (String alias : links.keySet()) {
            linkList.add(map("alias", alias, "target", canonical(alias, links)));
        }
        json.put("links", linkList);

        System.out.println("tzdata " + version + ": " + countries.size() + " countries, " + zoneTab.size()
                + " zone.tab rows, " + zones.size() + " zones, " + links.size() + " links");
        Extract.writeOrCheck(Extract.OUT.resolve("tzdata.json"), Extract.json(json), args.check());
    }

    /** The files of the pinned release (downloaded once, then from data/cache), checked against its version. */
    static Map<String, byte[]> archive() {
        var sources = Extract.sources();
        String version = Extract.source(sources, "tzdata.version");
        String url = Extract.source(sources, "tzdata.url").replace("{version}", version);
        Map<String, byte[]> files = Extract.untarGz(
                Extract.download(url, "tzdata" + version + ".tar.gz", Extract.source(sources, "tzdata.sha256")));
        String inArchive = Extract.utf8(files.get("version")).strip();
        if (!inArchive.equals(version)) {
            throw new IllegalStateException(url + " says it is version " + inArchive + ", not " + version);
        }
        return files;
    }

    /** zone.tab's zones of each country, in the file's order (its most populous zone first): {@code PH → [Asia/Manila]}. */
    static Map<String, List<String>> zonesByCountry(Map<String, byte[]> files) {
        Map<String, List<String>> byCountry = new TreeMap<>();
        for (String[] row : tab(text(files, "zone.tab"))) {
            byCountry.computeIfAbsent(row[0], k -> new ArrayList<>()).add(row[2]);
        }
        return byCountry;
    }

    /** iso3166.tab: every code IANA uses, with its name: {@code PH → Philippines}. */
    static Map<String, String> countryNames(Map<String, byte[]> files) {
        Map<String, String> names = new TreeMap<>();
        for (String[] row : tab(text(files, "iso3166.tab"))) names.put(row[0], row[1]);
        return names;
    }

    /** A Zone: its name and its last line (the one with no UNTIL), which is the rule in force now. */
    record Zone(String tzid, String stdoff, String rules, String format) {
        /** The standard offset as {@code +08:00} / {@code -03:30} / {@code +00:00}. */
        String utcOffset() {
            boolean negative = stdoff.startsWith("-");
            String[] parts = stdoff.replace("-", "").split(":");
            int h = Integer.parseInt(parts[0]);
            int m = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int s = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            String sign = negative && (h | m | s) != 0 ? "-" : "+";
            return sign + "%02d:%02d".formatted(h, m) + (s != 0 ? ":%02d".formatted(s) : "");
        }
    }

    static void readRules(String file, String text, Map<String, Zone> zones, Map<String, String> links) {
        String current = null;
        int number = 0;
        for (String raw : text.split("\n")) {
            number++;
            int hash = raw.indexOf('#');
            String line = (hash >= 0 ? raw.substring(0, hash) : raw).stripTrailing();
            if (line.isBlank()) continue;
            String[] t = line.strip().split("\\s+");
            boolean continuation = Character.isWhitespace(line.charAt(0));
            if (continuation) {
                if (current == null) throw new IllegalStateException(file + ":" + number + ": continuation with no Zone");
                zones.put(current, new Zone(current, t[0], t[1], t[2]));
                if (t.length <= 3) current = null;
            } else if (t[0].equals("Zone")) {
                zones.put(t[1], new Zone(t[1], t[2], t[3], t[4]));
                current = t.length > 5 ? t[1] : null;
            } else if (t[0].equals("Link")) {
                links.put(t[2], t[1]);
                current = null;
            } else if (t[0].equals("Rule")) {
                current = null;
            } else {
                throw new IllegalStateException(file + ":" + number + ": cannot read " + raw);
            }
        }
    }

    static String canonical(String tzid, Map<String, String> links) {
        String at = tzid;
        for (int hops = 0; links.containsKey(at); hops++) {
            if (hops > 10) throw new IllegalStateException("link loop at " + tzid);
            at = links.get(at);
        }
        return at;
    }

    /** {@code +1430+12100} or {@code +143512+1210130}: ISO 6709 degrees, minutes (and seconds), to decimals. */
    static double[] coordinates(String iso6709) {
        int split = Math.max(iso6709.lastIndexOf('+'), iso6709.lastIndexOf('-'));
        return new double[] {degrees(iso6709.substring(0, split), 2), degrees(iso6709.substring(split), 3)};
    }

    private static double degrees(String s, int degreeDigits) {
        int sign = s.charAt(0) == '-' ? -1 : 1;
        String d = s.substring(1);
        double deg = Integer.parseInt(d.substring(0, degreeDigits));
        double min = Integer.parseInt(d.substring(degreeDigits, degreeDigits + 2));
        double sec = d.length() > degreeDigits + 2 ? Integer.parseInt(d.substring(degreeDigits + 2)) : 0;
        return sign * Math.round((deg + min / 60 + sec / 3600) * 1e6) / 1e6;
    }

    static List<String[]> tab(String text) {
        var rows = new ArrayList<String[]>();
        for (String line : text.split("\n")) {
            if (line.isBlank() || line.startsWith("#")) continue;
            rows.add(line.split("\t"));
        }
        return rows;
    }

    static String text(Map<String, byte[]> files, String name) {
        byte[] bytes = files.get(name);
        if (bytes == null) throw new IllegalStateException("the tzdata archive has no " + name);
        return Extract.utf8(bytes).replace("\r\n", "\n");
    }

    static LinkedHashMap<String, Object> map(Object... keyValues) {
        var m = new LinkedHashMap<String, Object>();
        for (int i = 0; i < keyValues.length; i += 2) m.put((String) keyValues[i], keyValues[i + 1]);
        return m;
    }

    /** Downloads IANA's latest release, reads its version, and pins it (version and sha256). */
    static void pinLatest() {
        byte[] latest = Extract.get("https://data.iana.org/time-zones/tzdata-latest.tar.gz");
        String version = Extract.utf8(Extract.untarGz(latest).get("version")).strip();
        String sha256 = Extract.sha256(latest);
        Extract.pin("tzdata.version", version);
        Extract.pin("tzdata.sha256", sha256);
        System.out.println("pinned tzdata " + version + " (" + sha256 + ")");
    }
}
