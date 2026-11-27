package io.joshuasalcedo.valueobject.format;

import io.joshuasalcedo.valueobject.core.ValueObject;
import io.joshuasalcedo.valueobject.financial.BillingDocument;
import io.joshuasalcedo.valueobject.financial.Currency;
import io.joshuasalcedo.valueobject.financial.Money;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.temporal.TemporalAccessor;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Writes value objects, tables, documents and lists of them as JSON, with no JSON library.
 *
 * <ul>
 *   <li>a value object with one part is that part: {@code Email} is {@code "jane@example.com"}</li>
 *   <li>{@link Money} is {@code {"amount": 42.50, "currency": "USD"}}; a {@link Currency} is its code</li>
 *   <li>other records are objects of their parts; {@code Optional} is its value or {@code null}</li>
 *   <li>dates and times are ISO-8601 strings; enums are their names; maps are objects; lists are arrays</li>
 *   <li>a {@link BillingDocument} is its fields, flattened, with its kind ({@code "Receipt"}, ...)</li>
 * </ul>
 *
 * <pre>{@code
 * Json.of(ValueObject.scan(text));       // [{"type":"EMAIL","value":"jane@example.com","text":...,"shape":...}, ...]
 * Json.of(ValueObject.tables(text));     // [{"columns":[...],"rows":[[...]]}, ...]
 * Json.of(Receipt.parse(text));          // {"kind":"Receipt","number":"RCPT-1001","total":{...},"lineItems":[...]}
 * Json.pretty(...)                       // the same, indented
 * }</pre>
 *
 * @author JoshuaSalcedo
 */
public final class Json {

    private Json() {}

    /**
     * {@return it as compact JSON}
     *
     * @param value anything
     */
    public static String of(@Nullable Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out, -1, 0);
        return out.toString();
    }

    /**
     * {@return it as JSON indented by two spaces}
     *
     * @param value anything
     */
    public static String pretty(@Nullable Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out, 2, 0);
        return out.toString();
    }

    /**
     * {@return it as the plain Java value JSON would hold: a map, a list, a string, a number, a boolean or null}
     *
     * @param value anything
     */
    public static @Nullable Object toPlain(@Nullable Object value) {
        if (value == null) return null;
        if (value instanceof Optional<?> o) return toPlain(o.orElse(null));
        if (value instanceof Boolean b) return b;
        if (value instanceof CharSequence s) return s.toString();
        if (value instanceof BigDecimal d) return d;
        if (value instanceof Number n) return n;
        if (value instanceof Currency c) return c.code();
        if (value instanceof io.joshuasalcedo.valueobject.measurement.UnitOfMeasure u) return u.code();
        if (value instanceof Enum<?> e) return e.name();
        if (value instanceof TemporalAccessor || value instanceof java.time.Duration || value instanceof java.time.Period
                || value instanceof java.time.ZoneId || value instanceof java.util.UUID || value instanceof java.net.URI
                || value instanceof java.nio.file.Path) return value.toString();
        if (value instanceof byte[] b) return Base64.getEncoder().encodeToString(b);
        if (value instanceof Money m) {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("amount", m.amount());
            out.put("currency", m.currency().code());
            return out;
        }
        if (value instanceof BillingDocument d) return document(d);
        if (value instanceof io.joshuasalcedo.valueobject.financial.CardNumber c) return c.masked();
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> out = new LinkedHashMap<>();
            map.forEach((k, v) -> out.put(String.valueOf(toPlain(k)), toPlain(v)));
            return out;
        }
        if (value instanceof Iterable<?> it) {
            java.util.List<Object> out = new java.util.ArrayList<>();
            for (Object o : it) out.add(toPlain(o));
            return out;
        }
        if (value.getClass().isArray()) {
            java.util.List<Object> out = new java.util.ArrayList<>();
            for (int i = 0; i < java.lang.reflect.Array.getLength(value); i++) out.add(toPlain(java.lang.reflect.Array.get(value, i)));
            return out;
        }
        if (value instanceof Record r) {
            RecordComponent[] parts = r.getClass().getRecordComponents();
            if (value instanceof ValueObject && parts.length == 1) return toPlain(part(r, parts[0]));
            Map<String, Object> out = new LinkedHashMap<>();
            for (RecordComponent p : parts) out.put(p.getName(), toPlain(part(r, p)));
            return out;
        }
        return value.toString();
    }

    // ── reading ───────────────────────────────────────────────────────────

    /**
     * Parses JSON into plain Java values.
     *
     * @param json any JSON
     * @return a {@code Map<String, Object>} for an object, a {@code List<Object>} for an array, a {@code String},
     *         a {@code BigDecimal} for a number, a {@code Boolean}, or null
     * @throws io.joshuasalcedo.valueobject.core.ValueObjectException if it is not valid JSON
     */
    public static @Nullable Object parse(String json) {
        if (json == null) throw new io.joshuasalcedo.valueobject.core.ValueObjectException("JSON is required");
        Reader r = new Reader(json);
        Object v = r.value();
        r.space();
        if (r.at < json.length()) throw r.fail("text after the JSON value");
        return v;
    }

    /**
     * Reads a value object, or a list or record of them, back from the JSON {@link #of(Object)} writes: a value object
     * of one part from its text ({@code "jane@example.com"}), {@code Money} from {@code {"amount":..,"currency":..}},
     * an enum from its name or code, a record part by part.
     *
     * @param json the JSON
     * @param type the type wanted, e.g. {@code Email.class}, {@code LineItem.class}
     * @param <T>  that type
     * @return the value
     * @throws io.joshuasalcedo.valueobject.core.ValueObjectException if it is not valid JSON, or not a {@code T}
     */
    public static <T> T read(String json, Class<T> type) {
        return type.cast(Values.convert(parse(json), type));
    }

    /**
     * Turns plain values (maps, lists, strings, numbers, booleans) into a value of the given type: the reverse of
     * {@link #toPlain(Object)}. For framework connectors that read JSON with their own parser.
     *
     * @param plain a plain value, as {@link #parse(String)} gives
     * @param type  the type wanted, possibly generic ({@code List<Email>})
     * @return the value, of that type; null for null
     * @throws io.joshuasalcedo.valueobject.core.ValueObjectException if it cannot be one
     */
    public static @Nullable Object fromPlain(@Nullable Object plain, java.lang.reflect.Type type) {
        return Values.convert(plain, type);
    }

    /**
     * Reads a JSON array back into a list.
     *
     * @param json        the JSON array
     * @param elementType the type of each element, e.g. {@code Found.class}
     * @param <T>         that type
     * @return the elements, in order
     * @throws io.joshuasalcedo.valueobject.core.ValueObjectException if it is not a JSON array of {@code T}
     */
    public static <T> java.util.List<T> readList(String json, Class<T> elementType) {
        Object plain = parse(json);
        if (!(plain instanceof java.util.List<?> list)) {
            throw new io.joshuasalcedo.valueobject.core.ValueObjectException("Expected a JSON array");
        }
        java.util.List<T> out = new java.util.ArrayList<>();
        for (Object o : list) out.add(elementType.cast(Values.convert(o, elementType)));
        return out;
    }

    /** A small, strict JSON reader (RFC 8259). */
    private static final class Reader {
        private final String s;
        private int at;

        Reader(String s) {
            this.s = s;
        }

        io.joshuasalcedo.valueobject.core.ValueObjectException fail(String what) {
            return new io.joshuasalcedo.valueobject.core.ValueObjectException("Not valid JSON at " + at + ": " + what);
        }

        void space() {
            while (at < s.length() && " \t\r\n".indexOf(s.charAt(at)) >= 0) at++;
        }

        @Nullable Object value() {
            space();
            if (at >= s.length()) throw fail("unexpected end");
            char c = s.charAt(at);
            return switch (c) {
                case '{' -> object();
                case '[' -> array();
                case '"' -> string();
                case 't' -> word("true", Boolean.TRUE);
                case 'f' -> word("false", Boolean.FALSE);
                case 'n' -> word("null", null);
                default -> number();
            };
        }

        private @Nullable Object word(String w, @Nullable Object v) {
            if (!s.startsWith(w, at)) throw fail("expected " + w);
            at += w.length();
            return v;
        }

        private Map<String, Object> object() {
            Map<String, Object> out = new LinkedHashMap<>();
            at++;
            space();
            if (at < s.length() && s.charAt(at) == '}') {
                at++;
                return out;
            }
            while (true) {
                space();
                if (at >= s.length() || s.charAt(at) != '"') throw fail("expected a name");
                String key = string();
                space();
                if (at >= s.length() || s.charAt(at) != ':') throw fail("expected ':'");
                at++;
                out.put(key, value());
                space();
                if (at >= s.length()) throw fail("unexpected end");
                char c = s.charAt(at++);
                if (c == '}') return out;
                if (c != ',') throw fail("expected ',' or '}'");
            }
        }

        private java.util.List<Object> array() {
            java.util.List<Object> out = new java.util.ArrayList<>();
            at++;
            space();
            if (at < s.length() && s.charAt(at) == ']') {
                at++;
                return out;
            }
            while (true) {
                out.add(value());
                space();
                if (at >= s.length()) throw fail("unexpected end");
                char c = s.charAt(at++);
                if (c == ']') return out;
                if (c != ',') throw fail("expected ',' or ']'");
            }
        }

        private String string() {
            StringBuilder out = new StringBuilder();
            at++;
            while (at < s.length()) {
                char c = s.charAt(at++);
                if (c == '"') return out.toString();
                if (c != '\\') {
                    out.append(c);
                    continue;
                }
                if (at >= s.length()) break;
                char e = s.charAt(at++);
                switch (e) {
                    case '"', '\\', '/' -> out.append(e);
                    case 'b' -> out.append('\b');
                    case 'f' -> out.append('\f');
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    case 't' -> out.append('\t');
                    case 'u' -> {
                        if (at + 4 > s.length()) throw fail("short \\u escape");
                        out.append((char) Integer.parseInt(s.substring(at, at + 4), 16));
                        at += 4;
                    }
                    default -> throw fail("bad escape \\" + e);
                }
            }
            throw fail("unterminated string");
        }

        private BigDecimal number() {
            int start = at;
            while (at < s.length() && "+-0123456789.eE".indexOf(s.charAt(at)) >= 0) at++;
            if (start == at) throw fail("unexpected '" + s.charAt(at) + "'");
            try {
                return new BigDecimal(s.substring(start, at));
            } catch (NumberFormatException e) {
                throw fail("bad number " + s.substring(start, at));
            }
        }
    }

    /** A document as its fields, flattened, with its kind; its raw text left out. */
    private static Map<String, Object> document(BillingDocument d) {
        Map<String, Object> out = new LinkedHashMap<>();
        Class<?> kind = d.getClass().getEnclosingClass() != null ? d.getClass().getEnclosingClass() : d.getClass();
        out.put("kind", kind.getSimpleName());
        out.put("number", toPlain(d.number()));
        out.put("date", toPlain(d.date()));
        out.put("issuer", toPlain(d.issuer()));
        out.put("customer", toPlain(d.customer()));
        out.put("currency", toPlain(d.currency()));
        out.put("subtotal", toPlain(d.subtotal()));
        out.put("tax", toPlain(d.tax()));
        out.put("total", toPlain(d.total()));
        if (d instanceof Record r) {
            for (RecordComponent p : r.getClass().getRecordComponents()) {
                if (!p.getName().equals("fields")) out.put(p.getName(), toPlain(part(r, p)));
            }
        }
        out.put("lineItems", toPlain(d.lineItems()));
        out.put("metadata", toPlain(d.metadata()));
        out.put("sources", toPlain(d.sources()));
        out.put("found", toPlain(d.found()));
        out.put("tables", toPlain(d.tables()));
        return out;
    }

    private static @Nullable Object part(Record r, RecordComponent p) {
        try {
            return p.getAccessor().invoke(r);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static void write(@Nullable Object value, StringBuilder out, int indent, int depth) {
        Object v = value instanceof Map<?, ?> || value instanceof java.util.List<?> ? value : toPlain(value);
        if (v == null) {
            out.append("null");
        } else if (v instanceof String s) {
            string(s, out);
        } else if (v instanceof BigDecimal d) {
            out.append(d.toPlainString());
        } else if (v instanceof Number || v instanceof Boolean) {
            out.append(v);
        } else if (v instanceof Map<?, ?> map) {
            if (map.isEmpty()) {
                out.append("{}");
                return;
            }
            out.append('{');
            Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<?, ?> e = it.next();
                newline(out, indent, depth + 1);
                string(String.valueOf(e.getKey()), out);
                out.append(indent >= 0 ? ": " : ":");
                write(toPlain(e.getValue()), out, indent, depth + 1);
                if (it.hasNext()) out.append(',');
            }
            newline(out, indent, depth);
            out.append('}');
        } else if (v instanceof Collection<?> list) {
            if (list.isEmpty()) {
                out.append("[]");
                return;
            }
            out.append('[');
            Iterator<?> it = list.iterator();
            while (it.hasNext()) {
                newline(out, indent, depth + 1);
                write(toPlain(it.next()), out, indent, depth + 1);
                if (it.hasNext()) out.append(',');
            }
            newline(out, indent, depth);
            out.append(']');
        } else {
            string(v.toString(), out);
        }
    }

    private static void newline(StringBuilder out, int indent, int depth) {
        if (indent < 0) return;
        out.append('\n');
        out.append(" ".repeat(indent * depth));
    }

    private static void string(String s, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        out.append('"');
    }
}
