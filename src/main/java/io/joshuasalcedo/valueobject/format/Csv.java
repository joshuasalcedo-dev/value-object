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
     * {@return its column names, then its rows}
     *
     * @param table a table
     */
    public static String of(Table table) {
        StringBuilder out = new StringBuilder();
        row(table.columns().stream().map(Table.Column::name).toList(), out);
        for (List<String> r : table.rows()) row(r, out);
        return out.toString();
    }

    /**
     * {@return each as CSV, separated by a blank line}
     *
     * @param tables tables
     */
    public static String ofTables(List<Table> tables) {
        return tables.stream().map(Csv::of).collect(Collectors.joining(EOL));
    }

    /**
     * {@return a header of their parts' names, then one row each}
     *
     * @param rows records (line items, found values, ...) or maps (a table's records)
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
     * {@return it as one CSV cell's text (not yet quoted)}
     *
     * @param value anything
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

    // ── reading ───────────────────────────────────────────────────────────

    /**
     * Reads CSV (RFC 4180: commas, quotes doubled inside quoted cells, CRLF or LF line ends) into rows.
     *
     * @param csv the CSV, its first row the column names
     * @return one map per row, column name to cell text, in column order; an empty cell is {@code ""}
     * @throws io.joshuasalcedo.valueobject.exception.ValueObjectException if a quoted cell is not closed
     */
    public static List<Map<String, String>> read(String csv) {
        List<List<String>> lines = cells(csv);
        if (lines.isEmpty()) return List.of();
        List<String> header = lines.getFirst();
        List<Map<String, String>> out = new ArrayList<>();
        for (List<String> line : lines.subList(1, lines.size())) {
            Map<String, String> row = new java.util.LinkedHashMap<>();
            for (int i = 0; i < header.size(); i++) row.put(header.get(i), i < line.size() ? line.get(i) : "");
            out.add(row);
        }
        return out;
    }

    /**
     * Reads CSV into records, a row each, matching column names to the record's parts. Cells are read as
     * {@link #cell(Object)} writes them ({@code "42.50 USD"} for {@code Money}, {@code "2.5 kg"} for a quantity, a
     * value object's text); an empty cell is an empty {@code Optional} or null.
     *
     * @param csv        the CSV, its first row the column names
     * @param recordType the record each row is, e.g. {@code LineItem.class}
     * @param <R>        that record
     * @return the records, in order
     * @throws io.joshuasalcedo.valueobject.exception.ValueObjectException if a cell is not what its part holds
     */
    public static <R extends Record> List<R> read(String csv, Class<R> recordType) {
        List<R> out = new ArrayList<>();
        for (Map<String, String> row : read(csv)) {
            Map<String, Object> plain = new java.util.LinkedHashMap<>();
            row.forEach((k, v) -> plain.put(k, v.isEmpty() ? null : v));
            out.add(recordType.cast(Values.convert(plain, recordType)));
        }
        return out;
    }

    /**
     * Reads CSV into a {@link Table}: its first row names the columns, and each column is typed by the value objects
     * most of its cells are, as tables found in text are.
     *
     * @param csv the CSV
     * @return the table
     * @throws io.joshuasalcedo.valueobject.exception.ValueObjectException if it has no header row
     */
    public static Table readTable(String csv) {
        List<List<String>> lines = cells(csv);
        if (lines.isEmpty()) throw new io.joshuasalcedo.valueobject.exception.ValueObjectException("CSV with no rows");
        List<String> header = lines.getFirst();
        List<List<String>> rows = new ArrayList<>();
        for (List<String> line : lines.subList(1, lines.size())) {
            List<String> row = new ArrayList<>(line);
            while (row.size() < header.size()) row.add("");
            rows.add(row.subList(0, header.size()));
        }
        List<Table.Column> columns = new ArrayList<>();
        for (int c = 0; c < header.size(); c++) {
            int col = c;
            List<String> cells = rows.stream().map(r -> r.get(col)).filter(s -> !s.isBlank()).toList();
            Map<ValueObject.Type, Long> votes = cells.stream().map(ValueObject::identify).flatMap(Optional::stream)
                    .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
            Optional<ValueObject.Type> type = votes.entrySet().stream().max(Map.Entry.comparingByValue())
                    .filter(e -> e.getValue() >= cells.size() * 0.6).map(Map.Entry::getKey);
            boolean numeric = !cells.isEmpty() && cells.stream().filter(s -> s.strip().matches("[-+(]?[$€£¥₱]?\\d[\\d,]*(\\.\\d+)?\\)?%?")).count()
                    >= cells.size() * 0.6;
            columns.add(new Table.Column(c, header.get(c), type, numeric));
        }
        return new Table(1, 0, lines.size() - 1, true, columns, rows, csv);
    }

    /** The cells of every line, quotes resolved. */
    private static List<List<String>> cells(String csv) {
        if (csv == null) throw new io.joshuasalcedo.valueobject.exception.ValueObjectException("CSV is required");
        List<List<String>> lines = new ArrayList<>();
        List<String> line = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        boolean any = false;
        for (int i = 0; i < csv.length(); i++) {
            char c = csv.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < csv.length() && csv.charAt(i + 1) == '"') {
                    cell.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    cell.append(c);
                }
            } else if (c == '"') {
                quoted = true;
                any = true;
            } else if (c == ',') {
                line.add(cell.toString());
                cell.setLength(0);
                any = true;
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < csv.length() && csv.charAt(i + 1) == '\n') i++;
                if (any || !cell.isEmpty()) {
                    line.add(cell.toString());
                    lines.add(line);
                }
                line = new ArrayList<>();
                cell.setLength(0);
                any = false;
            } else {
                cell.append(c);
                any = true;
            }
        }
        if (quoted) throw new io.joshuasalcedo.valueobject.exception.ValueObjectException("A quoted CSV cell is not closed");
        if (any || !cell.isEmpty()) {
            line.add(cell.toString());
            lines.add(line);
        }
        return lines;
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
