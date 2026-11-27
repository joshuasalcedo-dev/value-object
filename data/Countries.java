import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Wikidata + IANA tzdata → {@code countries.json}: every ISO 3166-1 country, with its codes, names, capital,
 * currencies, calling codes, languages, continents, top-level domains, demonyms, population, coordinates,
 * driving side and time zones.
 *
 * <pre>
 * java data/Countries.java             # writes countries.json from data/raw/wikidata-countries.csv
 * java data/Countries.java --refresh   # asks Wikidata again first, rewriting the raw CSV
 * java data/Countries.java --check     # CI: fails when countries.json is not what the raw CSV gives
 * </pre>
 *
 * <p>The country list is IANA's {@code iso3166.tab} (the same 249 codes as {@code Country}); Wikidata fills in the
 * rest. Wikidata changes every day, so its answer is kept in {@code data/raw/wikidata-countries.csv}: a run without
 * {@code --refresh} never goes online for it, and a refresh shows up as a readable diff of that file. Wikidata is
 * CC0; tzdata is public domain.
 *
 * <p>To add a field: add a line to {@link #FIELDS}, run with {@code --refresh}, and put it in {@link #country}.
 */
public class Countries {

    static final Path RAW_CSV = Extract.RAW.resolve("wikidata-countries.csv");

    /**
     * Field name → the SPARQL that binds {@code ?value} for an {@code ?item}. One query per field, kept small. Names of
     * linked items go through the label service with {@code en,mul}: many items now keep their name only as the
     * {@code mul} (every language) label, and a plain {@code LANG = "en"} filter misses them (Oslo, {@code .gr}).
     */
    static final Map<String, String> FIELDS = fields(
            "label", "BIND(?item AS ?x) SERVICE wikibase:label { bd:serviceParam wikibase:language \"en,mul\". ?x rdfs:label ?value }",
            "official_name", "?item wdt:P1448 ?value FILTER(LANG(?value) = \"en\")",
            "alpha3", "?item wdt:P298 ?value",
            "numeric", "?item wdt:P299 ?value",
            "capital", "?item wdt:P36 ?x . SERVICE wikibase:label { bd:serviceParam wikibase:language \"en,mul\". ?x rdfs:label ?value }",
            "currency", "?item wdt:P38 ?x . ?x wdt:P498 ?value",
            "calling_code", "?item wdt:P474 ?value",
            "language", "?item wdt:P37 ?x . ?x wdt:P218 ?value",
            "continent", "?item wdt:P30 ?x . SERVICE wikibase:label { bd:serviceParam wikibase:language \"en,mul\". ?x rdfs:label ?value }",
            "tld", "?item wdt:P78 ?x . SERVICE wikibase:label { bd:serviceParam wikibase:language \"en,mul\". ?x rdfs:label ?value }",
            "demonym", "?item wdt:P1549 ?value FILTER(LANG(?value) = \"en\")",
            "population", "?item wdt:P1082 ?value",
            "coordinates", "?item wdt:P625 ?value",
            "driving_side", "?item wdt:P1622 ?x . SERVICE wikibase:label { bd:serviceParam wikibase:language \"en,mul\". ?x rdfs:label ?value }");

    public static void main(String[] argv) throws Exception {
        var args = Extract.Args.of(argv);
        Map<String, byte[]> tz = Tzdata.archive();
        Map<String, String> ianaNames = Tzdata.countryNames(tz);
        Map<String, List<String>> zones = Tzdata.zonesByCountry(tz);

        if (args.refresh() || !Files.exists(RAW_CSV)) refresh(ianaNames.keySet());

        // alpha2 → field → values, from the raw CSV
        Map<String, Map<String, Set<String>>> raw = new TreeMap<>();
        Map<String, String> items = new TreeMap<>();
        List<List<String>> rows = Extract.csv(Files.readString(RAW_CSV, StandardCharsets.UTF_8));
        for (List<String> row : rows.subList(1, rows.size())) {
            String item = row.get(0), alpha2 = row.get(1), field = row.get(2), value = row.get(3);
            items.put(alpha2, item);
            raw.computeIfAbsent(alpha2, k -> new TreeMap<>()).computeIfAbsent(field, k -> new LinkedHashSet<>()).add(value);
        }

        var countries = new ArrayList<Object>();
        var missing = new TreeMap<String, List<String>>();
        for (var e : ianaNames.entrySet()) {
            String code = e.getKey();
            Map<String, Set<String>> fields = raw.getOrDefault(code, Map.of());
            if (!items.containsKey(code)) throw new IllegalStateException(RAW_CSV + " has no Wikidata item for " + code
                    + ": run with --refresh");
            countries.add(country(code, e.getValue(), items.get(code), fields, zones.getOrDefault(code, List.of())));
            for (String f : FIELDS.keySet()) {
                if (!fields.containsKey(f)) missing.computeIfAbsent(f, k -> new ArrayList<>()).add(code);
            }
        }

        var json = new LinkedHashMap<String, Object>();
        json.put("source", "Wikidata (CC0) and IANA tzdata (public domain)");
        json.put("tzdata_version", Extract.source(Extract.sources(), "tzdata.version"));
        json.put("wikidata_snapshot", "data/raw/wikidata-countries.csv");
        json.put("generated_by", "data/Countries.java");
        json.put("countries", countries);

        System.out.println(countries.size() + " countries");
        missing.forEach((f, codes) -> System.out.println("  no " + f + " in Wikidata for " + codes.size()
                + (codes.size() <= 12 ? ": " + codes : "")));
        Extract.writeOrCheck(Extract.OUT.resolve("countries.json"), Extract.json(json), args.check());
    }

    static Map<String, Object> country(String code, String ianaName, String item, Map<String, Set<String>> f,
                                       List<String> zones) {
        var c = new LinkedHashMap<String, Object>();
        c.put("alpha2", code);
        c.put("alpha3", one(f, "alpha3"));
        c.put("numeric", one(f, "numeric"));
        c.put("name", one(f, "label") != null ? one(f, "label") : ianaName);
        c.put("iana_name", ianaName);
        c.put("official_names", list(f, "official_name"));
        c.put("wikidata", item);
        c.put("flag", flag(code));
        c.put("capitals", list(f, "capital"));
        c.put("currencies", sorted(f, "currency"));
        c.put("calling_codes", sorted(f, "calling_code").stream()
                .map(s -> s.replace(" ", "").replace("-", ""))
                .map(s -> s.startsWith("+") ? s : "+" + s).distinct().toList());
        c.put("languages", sorted(f, "language"));
        c.put("continents", sorted(f, "continent"));
        c.put("tlds", sorted(f, "tld"));
        c.put("demonyms", list(f, "demonym"));
        c.put("population", f.getOrDefault("population", Set.of()).stream()
                .map(s -> (long) Double.parseDouble(s)).max(Long::compare).orElse(null));
        Double[] at = point(one(f, "coordinates"));
        c.put("latitude", at[0]);
        c.put("longitude", at[1]);
        c.put("driving_side", one(f, "driving_side"));
        c.put("timezones", zones);
        return c;
    }

    // ---- asking Wikidata ----------------------------------------------------------------------------------

    /** Finds each code's item, then asks for every field of those items; writes the raw CSV. */
    static void refresh(Set<String> codes) throws Exception {
        Properties sources = Extract.sources();
        String endpoint = Extract.source(sources, "wikidata.sparql");

        // 1. alpha-2 → item, for the codes IANA lists; dissolved states (East Germany, the USSR) are left out
        Map<String, Set<String>> candidates = new TreeMap<>();
        for (List<String> row : query(endpoint, """
                SELECT ?item ?alpha2 WHERE {
                  ?item wdt:P297 ?alpha2 .
                  FILTER NOT EXISTS { ?item wdt:P576 [] }
                }""")) {
            if (codes.contains(row.get(1))) {
                candidates.computeIfAbsent(row.get(1), k -> new TreeSet<>()).add(qid(row.get(0)));
            }
        }
        Map<String, String> itemOf = new TreeMap<>();
        for (String code : codes) {
            Set<String> found = candidates.getOrDefault(code, Set.of());
            String pinned = sources.getProperty("wikidata.item." + code);
            if (pinned != null) {
                if (!found.contains(pinned.strip())) throw new IllegalStateException("wikidata.item." + code + "="
                        + pinned + " but Wikidata gives " + code + " to " + found);
                itemOf.put(code, pinned.strip());
            } else if (found.size() == 1) {
                itemOf.put(code, found.iterator().next());
            } else if (found.isEmpty()) {
                throw new IllegalStateException("no Wikidata item has ISO 3166-1 alpha-2 " + code);
            } else {
                throw new IllegalStateException(code + " is on several Wikidata items " + found
                        + ": add wikidata.item." + code + "=<the country's item> to data/sources.properties");
            }
        }
        Map<String, String> codeOf = itemOf.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        String values = itemOf.values().stream().map(q -> "wd:" + q).collect(Collectors.joining(" "));

        // 2. every field, one query each
        var lines = new TreeSet<String>();
        for (var field : FIELDS.entrySet()) {
            System.out.println("asking Wikidata for " + field.getKey());
            for (List<String> row : query(endpoint, "SELECT ?item ?value WHERE { VALUES ?item { " + values + " } "
                    + field.getValue() + " }")) {
                String qid = qid(row.get(0));
                lines.add(String.join(",", qid, codeOf.get(qid), field.getKey(), Extract.csvCell(row.get(1).strip())));
            }
            Thread.sleep(1000);   // the query service asks bots to go slowly
        }
        var csv = new StringBuilder("item,alpha2,field,value\n");
        // sorted by country, then field, then value: a refresh diffs line by line
        lines.stream().sorted((a, b) -> {
            String[] x = a.split(",", 2), y = b.split(",", 2);
            return x[1].compareTo(y[1]);
        }).forEach(l -> csv.append(l).append('\n'));
        Files.createDirectories(Extract.RAW);
        Files.writeString(RAW_CSV, csv.toString(), StandardCharsets.UTF_8);
        System.out.println("wrote data/raw/wikidata-countries.csv (" + lines.size() + " values)");
    }

    static List<List<String>> query(String endpoint, String sparql) {
        String body = "query=" + URLEncoder.encode(sparql, StandardCharsets.UTF_8);
        var response = Extract.send(HttpRequest.newBuilder(URI.create(endpoint))
                .header("Accept", "text/csv")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)));
        List<List<String>> rows = Extract.csv(Extract.utf8(response.body()));
        return rows.isEmpty() ? rows : rows.subList(1, rows.size());
    }

    static String qid(String uri) {
        return uri.substring(uri.lastIndexOf('/') + 1);
    }

    // ---- shaping values -----------------------------------------------------------------------------------

    static String one(Map<String, Set<String>> f, String field) {
        Set<String> v = f.get(field);
        return v == null || v.isEmpty() ? null : new TreeSet<>(v).first();
    }

    static List<String> list(Map<String, Set<String>> f, String field) {
        return List.copyOf(new TreeSet<>(f.getOrDefault(field, Set.of())));
    }

    static List<String> sorted(Map<String, Set<String>> f, String field) {
        return list(f, field);
    }

    /** {@code Point(121.0 14.6)} (longitude first, as WKT has it) → [latitude, longitude]. */
    static Double[] point(String wkt) {
        if (wkt == null || !wkt.startsWith("Point(")) return new Double[] {null, null};
        String[] lonLat = wkt.substring(6, wkt.length() - 1).strip().split("\\s+");
        return new Double[] {round(Double.parseDouble(lonLat[1])), round(Double.parseDouble(lonLat[0]))};
    }

    static double round(double d) {
        return Math.round(d * 1e6) / 1e6;
    }

    /** The flag emoji: the code's two letters as regional indicator symbols. */
    static String flag(String alpha2) {
        return alpha2.codePoints().map(c -> 0x1F1E6 + (c - 'A'))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    static Map<String, String> fields(String... nameThenSparql) {
        var m = new LinkedHashMap<String, String>();
        for (int i = 0; i < nameThenSparql.length; i += 2) m.put(nameThenSparql[i], nameThenSparql[i + 1]);
        return m;
    }
}
