import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

/**
 * What every extractor in {@code data/} shares: the repo's paths, pinned sources, downloads, tar.gz and CSV
 * reading, a JSON writer, and the write-or-{@code --check} step at the end.
 *
 * <p>Not run on its own: {@code java data/Tzdata.java} and {@code java data/Countries.java} compile it with them.
 */
final class Extract {

    private Extract() {}

    /** The repo root: the folder that holds {@code data/}. */
    static final Path ROOT = Path.of(System.getProperty("user.dir")).toAbsolutePath();
    static final Path DATA = ROOT.resolve("data");
    /** Downloads, kept between runs; not in git. */
    static final Path CACHE = DATA.resolve("cache");
    /** Raw snapshots that are small and change often (Wikidata), kept in git so a run is repeatable. */
    static final Path RAW = DATA.resolve("raw");
    /** Where the generated files go: shipped in the jar. */
    static final Path OUT = ROOT.resolve("src/main/resources/io/joshuasalcedo/valueobject/data");
    static final Path SOURCES = DATA.resolve("sources.properties");

    static final String USER_AGENT = "value-object-data-extractor/1.0 (Java HttpClient; build-time data export)";

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    // ---- arguments ----------------------------------------------------------------------------------------

    /** The flags a run was given: {@code --check}, {@code --refresh}, {@code --latest}. */
    record Args(boolean check, boolean refresh, boolean latest) {
        static Args of(String[] args) {
            var list = List.of(args);
            for (String a : list) {
                if (!a.equals("--check") && !a.equals("--refresh") && !a.equals("--latest")) {
                    throw new IllegalArgumentException("unknown argument: " + a + " (use --check, --refresh, --latest)");
                }
            }
            if (!Files.isDirectory(DATA)) {
                throw new IllegalStateException("run from the repo root: java data/<Extractor>.java (no data/ in " + ROOT + ")");
            }
            return new Args(list.contains("--check"), list.contains("--refresh"), list.contains("--latest"));
        }
    }

    // ---- pinned sources -----------------------------------------------------------------------------------

    static Properties sources() {
        var p = new Properties();
        try (var in = Files.newBufferedReader(SOURCES, StandardCharsets.UTF_8)) {
            p.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return p;
    }

    static String source(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null || v.isBlank()) throw new IllegalStateException(SOURCES + ": no " + key);
        return v.strip();
    }

    /** Sets one key in sources.properties, keeping every other line (and its comments) as it is. */
    static void pin(String key, String value) {
        try {
            List<String> lines = new ArrayList<>(Files.readAllLines(SOURCES, StandardCharsets.UTF_8));
            boolean set = false;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith(key + "=")) {
                    lines.set(i, key + "=" + value);
                    set = true;
                }
            }
            if (!set) lines.add(key + "=" + value);
            Files.write(SOURCES, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ---- downloads ----------------------------------------------------------------------------------------

    static HttpResponse<byte[]> send(HttpRequest.Builder request) {
        request.header("User-Agent", USER_AGENT).timeout(Duration.ofMinutes(2));
        IOException last = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                var response = HTTP.send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() == 429 || response.statusCode() >= 500) {
                    last = new IOException("HTTP " + response.statusCode() + " from " + response.uri());
                } else if (response.statusCode() != 200) {
                    throw new IllegalStateException("HTTP " + response.statusCode() + " from " + response.uri() + ": "
                            + new String(response.body(), StandardCharsets.UTF_8).lines().limit(5).toList());
                } else {
                    return response;
                }
            } catch (IOException e) {
                last = e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
            try {
                Thread.sleep(2000L * attempt);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
        throw new UncheckedIOException("gave up after 3 tries", last);
    }

    static byte[] get(String url) {
        return send(HttpRequest.newBuilder(URI.create(url)).GET()).body();
    }

    /** The file at {@code url}, from the cache when it is there and matches {@code sha256}; downloaded otherwise. */
    static byte[] download(String url, String fileName, String sha256) {
        try {
            Files.createDirectories(CACHE);
            Path cached = CACHE.resolve(fileName);
            if (Files.exists(cached)) {
                byte[] bytes = Files.readAllBytes(cached);
                if (sha256(bytes).equals(sha256)) return bytes;
            }
            System.out.println("downloading " + url);
            byte[] bytes = get(url);
            String actual = sha256(bytes);
            if (!actual.equals(sha256)) {
                throw new IllegalStateException(url + ": sha256 is " + actual + ", sources.properties pins " + sha256);
            }
            Files.write(cached, bytes);
            return bytes;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // ---- tar.gz -------------------------------------------------------------------------------------------

    /** Every regular file in a .tar.gz, by its path in the archive. Plain ustar is all tzdata uses. */
    static Map<String, byte[]> untarGz(byte[] tarGz) {
        try (InputStream in = new GZIPInputStream(new java.io.ByteArrayInputStream(tarGz))) {
            byte[] tar = in.readAllBytes();
            Map<String, byte[]> files = new LinkedHashMap<>();
            int pos = 0;
            while (pos + 512 <= tar.length) {
                String name = cString(tar, pos, 100);
                if (name.isEmpty()) break;
                String prefix = cString(tar, pos + 345, 155);
                if (!prefix.isEmpty()) name = prefix + "/" + name;
                long size = Long.parseLong(cString(tar, pos + 124, 12).strip().isEmpty() ? "0" : cString(tar, pos + 124, 12).strip(), 8);
                char type = (char) tar[pos + 156];
                pos += 512;
                if (type == '0' || type == '\0') {
                    byte[] content = new byte[(int) size];
                    System.arraycopy(tar, pos, content, 0, (int) size);
                    files.put(name.startsWith("./") ? name.substring(2) : name, content);
                }
                pos += (int) ((size + 511) / 512 * 512);
            }
            return files;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String cString(byte[] b, int offset, int length) {
        int end = offset;
        while (end < offset + length && b[end] != 0) end++;
        return new String(b, offset, end - offset, StandardCharsets.UTF_8);
    }

    // ---- CSV (RFC 4180, as the Wikidata query service writes it) ------------------------------------------

    static List<List<String>> csv(String text) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        var cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < text.length() && text.charAt(i + 1) == '"') {
                    cell.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    cell.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                row.add(cell.toString());
                cell.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') i++;
                row.add(cell.toString());
                cell.setLength(0);
                rows.add(row);
                row = new ArrayList<>();
            } else {
                cell.append(c);
            }
        }
        if (!cell.isEmpty() || !row.isEmpty()) {
            row.add(cell.toString());
            rows.add(row);
        }
        return rows;
    }

    static String csvCell(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    // ---- JSON writer --------------------------------------------------------------------------------------

    /**
     * Writes maps, lists, strings, numbers, booleans and nulls as JSON: two-space indent, keys in insertion
     * order, so the same input always gives the same bytes (and a readable git diff). Lists of plain values stay
     * on one line.
     */
    static String json(Object value) {
        var sb = new StringBuilder();
        write(sb, value, 0);
        return sb.append('\n').toString();
    }

    private static void write(StringBuilder sb, Object value, int indent) {
        switch (value) {
            case null -> sb.append("null");
            case String s -> string(sb, s);
            case Boolean b -> sb.append(b);
            case Double d -> sb.append(d == Math.rint(d) && Math.abs(d) < 1e15 ? Long.toString(d.longValue()) : d.toString());
            case Number n -> sb.append(n);
            case Map<?, ?> map -> {
                if (map.isEmpty()) {
                    sb.append("{}");
                    return;
                }
                sb.append("{\n");
                int i = 0;
                for (var e : map.entrySet()) {
                    sb.repeat(' ', indent + 2);
                    string(sb, e.getKey().toString());
                    sb.append(": ");
                    write(sb, e.getValue(), indent + 2);
                    sb.append(++i < map.size() ? ",\n" : "\n");
                }
                sb.repeat(' ', indent).append('}');
            }
            case List<?> list -> {
                if (list.isEmpty()) {
                    sb.append("[]");
                } else if (list.stream().noneMatch(v -> v instanceof Map || v instanceof List)) {
                    sb.append('[');
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 0) sb.append(", ");
                        write(sb, list.get(i), indent);
                    }
                    sb.append(']');
                } else {
                    sb.append("[\n");
                    for (int i = 0; i < list.size(); i++) {
                        sb.repeat(' ', indent + 2);
                        write(sb, list.get(i), indent + 2);
                        sb.append(i + 1 < list.size() ? ",\n" : "\n");
                    }
                    sb.repeat(' ', indent).append(']');
                }
            }
            default -> throw new IllegalArgumentException("cannot write " + value.getClass() + " as JSON");
        }
    }

    private static void string(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
    }

    // ---- the last step ------------------------------------------------------------------------------------

    /**
     * Writes {@code content} to {@code file}; with {@code --check}, writes nothing and fails when the file on disk
     * is not exactly that (so CI catches a hand edit, or a generator change that was not re-run).
     */
    static void writeOrCheck(Path file, String content, boolean check) {
        try {
            String rel = ROOT.relativize(file).toString().replace('\\', '/');
            if (check) {
                String current = Files.exists(file) ? Files.readString(file, StandardCharsets.UTF_8).replace("\r\n", "\n") : null;
                if (!content.equals(current)) {
                    System.err.println(rel + " is out of date: run the extractor without --check and commit the result");
                    System.exit(1);
                }
                System.out.println(rel + " is up to date");
                return;
            }
            Files.createDirectories(file.getParent());
            Files.writeString(file, content, StandardCharsets.UTF_8);
            System.out.println("wrote " + rel + " (" + content.length() / 1024 + " KB)");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String utf8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
