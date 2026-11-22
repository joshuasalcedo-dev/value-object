package io.joshuasalcedo.valueobject.demo;

import atlantafx.base.controls.Card;
import atlantafx.base.theme.Styles;
import io.joshuasalcedo.valueobject.Found;
import io.joshuasalcedo.valueobject.financial.BillingDocument;
import io.joshuasalcedo.valueobject.financial.Money;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Summary tiles and charts over what a folder held. Amounts are never added across currencies: every money chart has
 * one series per currency.
 */
final class Analysis {

    private Analysis() {}

    /** The row of summary tiles: files, documents, lines, values found, and totals by currency. */
    static HBox tiles(List<Scan.FileResult> results) {
        long docs = results.stream().filter(r -> r.document().isPresent()).count();
        long lines = results.stream().mapToLong(r -> r.document().map(d -> d.lineItems().size()).orElse(0)).sum();
        long values = results.stream().mapToLong(r -> r.found().size()).sum();
        long errors = results.stream().filter(r -> !r.error().isEmpty()).count();
        String byKind = results.stream().filter(r -> r.document().isPresent())
                .collect(Collectors.groupingBy(Scan.FileResult::kind, TreeMap::new, Collectors.counting()))
                .entrySet().stream().map(e -> e.getValue() + " " + e.getKey().toLowerCase() + (e.getValue() > 1 ? "s" : ""))
                .collect(Collectors.joining(", "));
        String totals = totalsByCurrency(results).entrySet().stream()
                .map(e -> e.getKey() + " " + String.format("%,.2f", e.getValue()))
                .collect(Collectors.joining("   "));

        HBox row = new HBox(12,
                tile("Files read", String.valueOf(results.size()), errors == 0 ? "all read" : errors + " could not be read"),
                tile("Documents", String.valueOf(docs), byKind.isEmpty() ? "none recognised" : byKind),
                tile("Line items", String.valueOf(lines), "from line-item tables"),
                tile("Values found", String.valueOf(values), "emails, amounts, dates, ..."),
                tile("Totals", totals.isEmpty() ? "—" : totals, "by currency, never mixed"));
        row.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));
        return row;
    }

    private static Card tile(String title, String value, String caption) {
        Label t = new Label(title);
        t.getStyleClass().addAll(Styles.TEXT_MUTED, Styles.TEXT_SMALL);
        Label v = new Label(value);
        v.getStyleClass().add(Styles.TITLE_3);
        v.setWrapText(true);
        Label c = new Label(caption);
        c.getStyleClass().addAll(Styles.TEXT_SUBTLE, Styles.TEXT_SMALL);
        c.setWrapText(true);
        Card card = new Card();
        card.setBody(new VBox(4, t, v, c));
        card.getStyleClass().add(Styles.ELEVATED_1);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    /** The charts, in a 2 x 2 grid. */
    static Node charts(List<Scan.FileResult> results, boolean animated) {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(12));
        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(50);
            grid.getColumnConstraints().add(cc);
        }
        grid.add(card("Documents by kind", "what each file was read as", byKind(results, animated)), 0, 0);
        grid.add(card("Total by issuer", "the eight largest; a bar's colour is its currency", byIssuer(results, animated)), 1, 0);
        grid.add(card("Total by month", "by document date, one series per currency", byMonth(results, animated)), 0, 1);
        grid.add(card("Values found", "value objects detected in the text, by type", byType(results, animated)), 1, 1);
        return grid;
    }

    private static Card card(String title, String subtitle, Node chart) {
        Label t = new Label(title);
        t.getStyleClass().add(Styles.TITLE_4);
        Label s = new Label(subtitle);
        s.getStyleClass().addAll(Styles.TEXT_MUTED, Styles.TEXT_SMALL);
        Card card = new Card();
        card.setHeader(new VBox(2, t, s));
        card.setBody(chart);
        card.getStyleClass().add(Styles.ELEVATED_1);
        return card;
    }

    private static PieChart byKind(List<Scan.FileResult> results, boolean animated) {
        PieChart pie = new PieChart();
        results.stream().collect(Collectors.groupingBy(Scan.FileResult::kind, TreeMap::new, Collectors.counting()))
                .forEach((kind, n) -> pie.getData().add(new PieChart.Data(kind + " (" + n + ")", n)));
        pie.setLegendVisible(false);
        pie.setAnimated(animated);
        pie.setPrefHeight(230);
        return pie;
    }

    /**
     * One horizontal bar per issuer (the eight with the largest totals), coloured by its currency: an issuer bills in
     * one currency, so a bar is never a sum of two.
     */
    private static StackedBarChart<Number, String> byIssuer(List<Scan.FileResult> results, boolean animated) {
        Map<String, Map<String, BigDecimal>> byIssuer = new LinkedHashMap<>();   // issuer -> currency -> total
        for (Scan.FileResult r : results) {
            r.document().ifPresent(d -> d.total().ifPresent(t -> {
                String issuer = d.issuer().map(p -> shorten(p.name())).orElse("(unknown)");
                byIssuer.computeIfAbsent(issuer, k -> new TreeMap<>()).merge(t.currency().code(), t.amount(), BigDecimal::add);
            }));
        }
        List<String> top = byIssuer.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<String, Map<String, BigDecimal>> e) ->
                        e.getValue().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add)).reversed())
                .limit(8).map(Map.Entry::getKey).toList().reversed();
        NumberAxis x = new NumberAxis();
        x.setLabel("Total");
        CategoryAxis y = new CategoryAxis();
        y.getCategories().setAll(top);
        StackedBarChart<Number, String> chart = new StackedBarChart<>(x, y);
        Map<String, XYChart.Series<Number, String>> series = new TreeMap<>();
        for (String issuer : top) {
            byIssuer.get(issuer).forEach((currency, total) -> series.computeIfAbsent(currency, c -> {
                XYChart.Series<Number, String> s = new XYChart.Series<>();
                s.setName(c);
                return s;
            }).getData().add(new XYChart.Data<>(total.doubleValue(), issuer)));
        }
        chart.getData().addAll(series.values());
        chart.setAnimated(animated);
        chart.setPrefHeight(230);
        chart.setCategoryGap(6);
        return chart;
    }

    private static BarChart<String, Number> byMonth(List<Scan.FileResult> results, boolean animated) {
        Map<String, Map<String, BigDecimal>> data = new TreeMap<>();
        DateTimeFormatter month = DateTimeFormatter.ofPattern("uuuu-MM");
        for (Scan.FileResult r : results) {
            r.document().ifPresent(d -> d.total().ifPresent(t -> d.date().ifPresent(date ->
                    data.computeIfAbsent(t.currency().code(), k -> new TreeMap<>()).merge(date.format(month), t.amount(), BigDecimal::add))));
        }
        // every series lists every month, in order, so the axis is in order
        List<String> months = data.values().stream().flatMap(m -> m.keySet().stream()).distinct().sorted().toList();
        Map<String, Map<String, BigDecimal>> filled = new TreeMap<>();
        data.forEach((cur, m) -> {
            Map<String, BigDecimal> all = new LinkedHashMap<>();
            for (String mo : months) all.put(mo, m.getOrDefault(mo, BigDecimal.ZERO));
            filled.put(cur, all);
        });
        return bars(filled, "Month", "Total", animated);
    }

    private static BarChart<String, Number> byType(List<Scan.FileResult> results, boolean animated) {
        Map<String, Long> counts = results.stream().flatMap(r -> r.found().stream())
                .collect(Collectors.groupingBy(f -> label(f), Collectors.counting()));
        Map<String, BigDecimal> top = new LinkedHashMap<>();
        counts.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(12)
                .forEach(e -> top.put(e.getKey(), BigDecimal.valueOf(e.getValue())));
        Map<String, Map<String, BigDecimal>> one = new TreeMap<>();
        one.put("Found", top);
        BarChart<String, Number> chart = bars(one, "Type", "Count", animated);
        chart.setLegendVisible(false);
        return chart;
    }

    private static String label(Found f) {
        String n = f.type().name().replace('_', ' ').toLowerCase();
        return Character.toUpperCase(n.charAt(0)) + n.substring(1);
    }

    private static BarChart<String, Number> bars(Map<String, Map<String, BigDecimal>> series, String x, String y, boolean animated) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(x);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(y);
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        series.forEach((name, points) -> {
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.setName(name);
            points.forEach((k, v) -> s.getData().add(new XYChart.Data<>(k, v.doubleValue())));
            chart.getData().add(s);
        });
        chart.setAnimated(animated);
        chart.setPrefHeight(230);
        chart.setCategoryGap(18);
        return chart;
    }

    /** Totals by currency code, over every document with a total. */
    static Map<String, Double> totalsByCurrency(List<Scan.FileResult> results) {
        Map<String, Double> out = new TreeMap<>();
        for (Scan.FileResult r : results) {
            r.document().flatMap(BillingDocument::total).ifPresent(t -> out.merge(t.currency().code(), t.amount().doubleValue(), Double::sum));
        }
        return out;
    }

    private static String shorten(String name) {
        return name.length() <= 22 ? name : name.substring(0, 21) + "…";
    }

    static Comparator<Money> byAmount() {
        return Comparator.comparing(Money::amount);
    }
}
