package io.joshuasalcedo.valueobject.table;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A table, a value object like any other: equal by its cells. Usually found in text by {@link TableDetector}
 * ({@code ValueObject.tables(text)}, or {@code Table.parse(text)} for text that holds one); its columns (named by the header row when it has one, and typed by
 * the value objects most of their cells are) and its rows of cell text.
 *
 * <pre>{@code
 * for (Table t : ValueObject.tables(ocrText)) {
 *     t.columns();                          // [Item, Qty (numeric), Unit Price (MONEY), Email (EMAIL)]
 *     t.records();                          // [{Item=Bolt M8, Qty=10, Unit Price=1.50 USD, ...}, ...]
 *     t.value(0, "Unit Price");             // Optional[Money 1.50 USD]
 * }
 * }</pre>
 *
 * @param page      the page it is on (1 for plain text)
 * @param firstLine the line (0-based, within the page) its first row or header starts on
 * @param lastLine  the line its last row ends on
 * @param hasHeader whether its first line(s) named the columns (else they are "Column 1", "Column 2", ...)
 * @param columns   its columns, left to right
 * @param rows      its body rows, top to bottom; each has one cell per column, "" when empty
 * @author JoshuaSalcedo
 */
public record Table(int page, int firstLine, int lastLine, boolean hasHeader, List<Column> columns, List<List<String>> rows)
        implements ValueObject {

    /**
     * A column.
     *
     * @param index   its position, from 0
     * @param name    the header text, or "Column N"
     * @param type    the value object most of its cells are, if any
     * @param numeric whether most of its cells are numbers (amounts, quantities)
     */
    public record Column(int index, String name, Optional<ValueObject.Type> type, boolean numeric) {

        @Override
        public String toString() {
            return name + type.map(t -> " (" + t + ")").orElse(numeric ? " (numeric)" : "");
        }
    }

    /**
     * @throws ValueObjectException if it has no columns, or a row has not one cell per column
     */
    public Table {
        if (columns == null || columns.isEmpty()) throw new ValueObjectException("A table needs at least one column");
        if (rows == null) throw new ValueObjectException("A table's rows are required");
        columns = List.copyOf(columns);
        rows = rows.stream().map(List::copyOf).toList();
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).size() != columns.size())
                throw new ValueObjectException("Table row " + i + " has " + rows.get(i).size() + " cells for "
                        + columns.size() + " columns");
        }
    }

    /**
     * @param text text that holds a table (Tesseract TSV, positioned words, or plain text)
     * @return its first table
     * @throws ValueObjectException if it holds none
     */
    public static Table parse(String text) {
        List<Table> found = TableDetector.detect(text);
        if (found.isEmpty()) throw new ValueObjectException("No table found in the text");
        return found.getFirst();
    }

    /** @return how many body rows it has */
    public int height() {
        return rows.size();
    }

    /** @return how many columns it has */
    public int width() {
        return columns.size();
    }

    /**
     * @param name a column name, any case
     * @return that column, or empty
     */
    public Optional<Column> column(String name) {
        return columns.stream().filter(c -> c.name().equalsIgnoreCase(name.trim())).findFirst();
    }

    /**
     * @param row    a body row, from 0
     * @param column a column, from 0
     * @return the cell's text, "" when empty
     */
    public String cell(int row, int column) {
        return rows.get(row).get(column);
    }

    /**
     * @param row    a body row, from 0
     * @param column a column name
     * @return the cell's text, or empty when there is no such column
     */
    public Optional<String> cell(int row, String column) {
        return column(column).map(c -> cell(row, c.index()));
    }

    /**
     * @param row    a body row, from 0
     * @param column a column, from 0
     * @return the cell as its column's value object, or empty when the column has no type or the cell is not one
     */
    public Optional<ValueObject> value(int row, int column) {
        return columns.get(column).type().flatMap(t -> t.tryParse(cell(row, column)));
    }

    /**
     * @param row    a body row, from 0
     * @param column a column name
     * @return the cell as its column's value object, or empty
     */
    public Optional<ValueObject> value(int row, String column) {
        return column(column).flatMap(c -> value(row, c.index()));
    }

    /** @return every row as column name to cell text, in column order */
    public List<Map<String, String>> records() {
        List<Map<String, String>> out = new ArrayList<>(rows.size());
        for (List<String> row : rows) {
            Map<String, String> record = new LinkedHashMap<>();
            for (Column c : columns) record.put(c.name(), row.get(c.index()));
            out.add(record);
        }
        return out;
    }

    /** @return it as a Markdown table */
    public String toMarkdown() {
        StringBuilder out = new StringBuilder();
        out.append("| ").append(columns.stream().map(Column::name).map(Table::escape).collect(Collectors.joining(" | "))).append(" |\n");
        out.append("|").append(columns.stream().map(c -> c.numeric() ? " ---: " : " --- ").collect(Collectors.joining("|"))).append("|\n");
        for (List<String> row : rows) {
            out.append("| ").append(row.stream().map(Table::escape).collect(Collectors.joining(" | "))).append(" |\n");
        }
        return out.toString();
    }

    /** @return it as CSV: the column names, then the rows */
    public String toCsv() {
        return io.joshuasalcedo.valueobject.format.Csv.of(this);
    }

    /** @return it as JSON: its columns (name, type, numeric) and rows */
    public String toJson() {
        return io.joshuasalcedo.valueobject.format.Json.of(this);
    }

    private static String escape(String s) {
        return s.replace("|", "\\|");
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "Table[page %d, lines %d-%d, %d x %d, columns=%s]",
                page, firstLine, lastLine, height(), width(), columns);
    }
}
