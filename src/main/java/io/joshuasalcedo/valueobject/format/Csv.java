package io.joshuasalcedo.valueobject.format;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.financial.Currency;
import io.joshuasalcedo.valueobject.financial.Money;
import io.joshuasalcedo.valueobject.table.Table;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Writes tables and lists of records as CSV (RFC 4180: comma-separated, quoted when needed, CRLF line ends).
 *
 * <pre>{@code
 * Csv.of(table);                       // its header row, then its rows
 * Csv.of(receipt.lineItems());         // description,quantity,unitPrice,amount,other
 * Csv.of(ValueObject.scan(text));      // type,value,text,start,end,shape
 * }</pre>
 *
 * <p>A cell is the value as text: {@link Money} as {@code "42.50 USD"}, a {@link Currency} as its code, a value object
 * with one part as that part, {@code Optional} as its value or empty, a list as its items joined by {@code "; "}.
 *
 * @author JoshuaSalcedo
 */
public final class Csv {

    private static final String EOL = "\r\n";

    private Csv() {}

    /**
     * @param table a table
     * @return its column names, then its rows
     */
    public static String of(Table table) {
        StringBuilder out = new StringBuilder();
        row(table.columns().stream().map(Table.Column::name).toList(), out);
        for (List<String> r : table.rows()) row(r, out);
        return out.toString();
    }

    /**
     * @param tables tables
     * @return each as CSV, separated by a blank line
     */
    public static String ofTables(List<Table> tables) {
        return tables.stream().map(Csv::of).collect(Collectors.joining(EOL));
    }

    /**
     * @param rows records (line items, found values, ...) or maps (a table's records)
     * @return a header of their parts' names, then one row each
     */
    public static String of(Collection<?> rows) {
        StringBuilder out = new StringBuilder();
        if (rows.isEmpty()) return "";
        Object first = rows.iterator().next();
        List<String> header = new ArrayList<>();
        if (first instanceof Map<?, ?> m) m.keySet().forEach(k -> header.add(String.valueOf(k)));
        else if (first instanceof Record r) for (RecordComponent p : r.getClass().getRecordComponents()) header.add(p.getName());
        else header.add("value");
        row(header, out);
        for (Object o : rows) {
            List<String> cells = new ArrayList<>();
            if (o instanceof Map<?, ?> m) {
                for (String h : header) cells.add(cell(m.get(h)));
            } else if (o instanceof Record r) {
                for (RecordComponent p : r.getClass().getRecordComponents()) cells.add(cell(part(r, p)));
            } else {
                cells.add(cell(o));
            }
            row(cells, out);
        }
        return out.toString();
    }

    /**
     * @param value anything
     * @return it as one CSV cell's text (not yet quoted)
     */
    public static String cell(@Nullable Object value) {
        if (value == null) return "";
        if (value instanceof Optional<?> o) return cell(o.orElse(null));
        if (value instanceof Money m) return m.amount().toPlainString() + " " + m.currency().code();
        if (value instanceof Currency c) return c.code();
        if (value instanceof io.joshuasalcedo.valueobject.measurement.UnitOfMeasure u) return u.code();
        if (value instanceof io.joshuasalcedo.valueobject.financial.CardNumber c) return c.masked();
        if (value instanceof BigDecimal d) return d.toPlainString();
        if (value instanceof Enum<?> e) return e.name();
        if (value instanceof Map<?, ?> map) {
            return map.entrySet().stream().map(e -> e.getKey() + "=" + cell(e.getValue())).collect(Collectors.joining("; "));
        }
        if (value instanceof Collection<?> c) return c.stream().map(Csv::cell).collect(Collectors.joining("; "));
        if (value instanceof ValueObject && value instanceof Record r && r.getClass().getRecordComponents().length == 1) {
            return cell(part(r, r.getClass().getRecordComponents()[0]));
        }
        return value.toString();
    }

    private static void row(List<String> cells, StringBuilder out) {
        out.append(cells.stream().map(Csv::quote).collect(Collectors.joining(","))).append(EOL);
    }

    private static String quote(@Nullable String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }

    private static @Nullable Object part(Record r, RecordComponent p) {
        try {
            return p.getAccessor().invoke(r);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
