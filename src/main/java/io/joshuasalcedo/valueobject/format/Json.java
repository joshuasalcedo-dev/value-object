package io.joshuasalcedo.valueobject.format;

import io.joshuasalcedo.valueobject.ValueObject;
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
     * @param value anything
     * @return it as compact JSON
     */
    public static String of(@Nullable Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out, -1, 0);
        return out.toString();
    }

    /**
     * @param value anything
     * @return it as JSON indented by two spaces
     */
    public static String pretty(@Nullable Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out, 2, 0);
        return out.toString();
    }

    /**
     * @param value anything
     * @return it as the plain Java value JSON would hold: a map, a list, a string, a number, a boolean or null
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
