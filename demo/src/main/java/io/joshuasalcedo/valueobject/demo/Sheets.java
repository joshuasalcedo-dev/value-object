package io.joshuasalcedo.valueobject.demo;

import io.joshuasalcedo.valueobject.financial.Money;
import io.joshuasalcedo.valueobject.measurement.Quantity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** ControlsFX spreadsheets of rows: numbers right-aligned and formatted, the first column frozen, read-only. */
final class Sheets {

    private Sheets() {}

    /**
     * @param headers column names
     * @param widths  column widths (px); missing ones get 120
     * @param rows    the rows; a cell is a String, a Number, a Money (its amount), a LocalDate, an Optional, or null
     * @return the sheet
     */
    static SpreadsheetView of(List<String> headers, List<Integer> widths, List<List<?>> rows) {
        GridBase grid = new GridBase(rows.size(), headers.size());
        ObservableList<ObservableList<SpreadsheetCell>> cells = FXCollections.observableArrayList();
        for (int r = 0; r < rows.size(); r++) {
            ObservableList<SpreadsheetCell> line = FXCollections.observableArrayList();
            List<?> row = rows.get(r);
            for (int c = 0; c < headers.size(); c++) line.add(cell(r, c, c < row.size() ? row.get(c) : null));
            cells.add(line);
        }
        grid.setRows(cells);
        grid.getColumnHeaders().setAll(headers);

        SpreadsheetView view = new SpreadsheetView(grid);
        view.setEditable(false);
        view.setShowRowHeader(true);
        view.setShowColumnHeader(true);
        for (int c = 0; c < headers.size(); c++) {
            view.getColumns().get(c).setPrefWidth(c < widths.size() ? widths.get(c) : 120);
        }
        if (!view.getColumns().isEmpty() && view.getColumns().getFirst().isColumnFixable()) {
            view.getColumns().getFirst().setFixed(true);
        }
        return view;
    }

    private static SpreadsheetCell cell(int r, int c, Object value) {
        Object v = value instanceof Optional<?> o ? o.orElse(null) : value;
        SpreadsheetCell cell;
        if (v instanceof Money m) {
            cell = SpreadsheetCellType.DOUBLE.createCell(r, c, 1, 1, m.amount().doubleValue());
            cell.setFormat("#,##0.00");
            cell.getStyleClass().add("number");
        } else if (v instanceof BigDecimal d) {
            cell = SpreadsheetCellType.DOUBLE.createCell(r, c, 1, 1, d.doubleValue());
            cell.setFormat(d.scale() > 0 ? "#,##0.####" : "#,##0");
            cell.getStyleClass().add("number");
        } else if (v instanceof Integer || v instanceof Long) {
            cell = SpreadsheetCellType.INTEGER.createCell(r, c, 1, 1, ((Number) v).intValue());
            cell.getStyleClass().add("number");
        } else if (v instanceof Double d) {
            cell = SpreadsheetCellType.DOUBLE.createCell(r, c, 1, 1, d);
            cell.setFormat("#,##0.00");
            cell.getStyleClass().add("number");
        } else if (v instanceof LocalDate d) {
            cell = SpreadsheetCellType.DATE.createCell(r, c, 1, 1, d);
        } else if (v instanceof Quantity q) {
            cell = SpreadsheetCellType.STRING.createCell(r, c, 1, 1, q.toString());
            cell.getStyleClass().add("number");
        } else {
            cell = SpreadsheetCellType.STRING.createCell(r, c, 1, 1, v == null ? "" : String.valueOf(v));
        }
        cell.setEditable(false);
        return cell;
    }
}
