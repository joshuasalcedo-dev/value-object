package io.joshuasalcedo.valueobject.demo;

import atlantafx.base.controls.Message;
import atlantafx.base.controls.Spacer;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Theme;
import io.joshuasalcedo.valueobject.core.Found;
import io.joshuasalcedo.valueobject.financial.Bill;
import io.joshuasalcedo.valueobject.financial.BillingDocument;
import io.joshuasalcedo.valueobject.financial.Invoice;
import io.joshuasalcedo.valueobject.financial.LineItem;
import io.joshuasalcedo.valueobject.financial.Receipt;
import io.joshuasalcedo.valueobject.format.Csv;
import io.joshuasalcedo.valueobject.format.Json;
import io.joshuasalcedo.valueobject.table.Table;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Value Object Studio: open a folder of bills, invoices and receipts (images or text) and see what the library reads
 * from them, as spreadsheets (ControlsFX) and charts, in an AtlantaFX theme.
 */
public class DemoApp extends Application {

    private static final Map<String, Supplier<Theme>> THEMES = new LinkedHashMap<>();

    static {
        THEMES.put("Primer Light", PrimerLight::new);
        THEMES.put("Primer Dark", PrimerDark::new);
        THEMES.put("Nord Light", NordLight::new);
        THEMES.put("Nord Dark", NordDark::new);
        THEMES.put("Cupertino Light", CupertinoLight::new);
        THEMES.put("Cupertino Dark", CupertinoDark::new);
        THEMES.put("Dracula", Dracula::new);
    }

    private final List<Path> folders = new ArrayList<>();
    private List<Scan.FileResult> results = List.of();
    private Path screenshots;
    private String theme = "Primer Light";

    private Stage stage;
    private final Label path = new Label("No folder open");
    private final Label status = new Label("Open a folder of bills, invoices and receipts, or drop one here.");
    private final ProgressBar progress = new ProgressBar(0);
    private final ToggleSwitch deep = new ToggleSwitch("Subfolders");
    private final MenuButton export = new MenuButton("Export");
    private final VBox tiles = new VBox();
    private final TabPane tabs = new TabPane();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        readArgs(getParameters().getRaw());
        Application.setUserAgentStylesheet(THEMES.get(theme).get().getUserAgentStylesheet());

        BorderPane root = new BorderPane();
        root.setTop(new VBox(header(), tiles));
        root.setCenter(tabs);
        root.setBottom(statusBar());
        tabs.getStyleClass().add(Styles.TABS_FLOATING);
        tiles.setPadding(new Insets(0, 12, 8, 12));
        showEmpty();

        root.setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) e.acceptTransferModes(TransferMode.COPY);
            e.consume();
        });
        root.setOnDragDropped(e -> {
            List<Path> dropped = e.getDragboard().getFiles().stream().map(File::toPath).filter(Files::isDirectory).toList();
            if (!dropped.isEmpty()) open(dropped);
            e.setDropCompleted(!dropped.isEmpty());
            e.consume();
        });

        Scene scene = new Scene(root, 1440, 900);
        scene.getStylesheets().add(css());
        stage.setTitle("Value Object Studio");
        stage.setScene(scene);
        stage.show();
        if (!folders.isEmpty()) open(List.copyOf(folders));
    }

    private void readArgs(List<String> args) {
        for (int i = 0; i < args.size(); i++) {
            String a = args.get(i);
            if (a.equals("--folder") && i + 1 < args.size()) folders.add(Path.of(args.get(++i)));
            else if (a.equals("--screenshots") && i + 1 < args.size()) screenshots = Path.of(args.get(++i));
            else if (a.equals("--theme") && i + 1 < args.size()) theme = args.get(++i);
            else if (!a.startsWith("--")) folders.add(Path.of(a));
        }
        if (!THEMES.containsKey(theme)) theme = "Primer Light";
    }

    // ── chrome ────────────────────────────────────────────────────────────

    private Node header() {
        Label title = new Label("Value Object Studio");
        title.getStyleClass().add(Styles.TITLE_3);
        Label subtitle = new Label("bills · invoices · receipts → sheets & charts");
        subtitle.getStyleClass().addAll(Styles.TEXT_MUTED, Styles.TEXT_SMALL);

        Button open = new Button("Open folder", new FontIcon(Feather.FOLDER));
        open.getStyleClass().add(Styles.ACCENT);
        open.setDefaultButton(true);
        open.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("A folder of bills, invoices and receipts");
            File dir = chooser.showDialog(stage);
            if (dir != null) open(List.of(dir.toPath()));
        });
        Button reload = new Button(null, new FontIcon(Feather.REFRESH_CW));
        reload.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        reload.setOnAction(e -> {
            if (!folders.isEmpty()) open(List.copyOf(folders));
        });
        path.getStyleClass().addAll(Styles.TEXT_MUTED, Styles.TEXT_SMALL);

        export.setGraphic(new FontIcon(Feather.DOWNLOAD));
        export.getStyleClass().add(Styles.BUTTON_OUTLINED);
        export.setDisable(true);
        export.getItems().addAll(
                item("Documents (CSV)", "documents.csv", () -> Csv.of(documentRows())),
                item("Line items (CSV)", "line-items.csv", () -> Csv.of(lineItemRows())),
                item("Values found (CSV)", "values.csv", () -> Csv.of(foundRows())),
                item("Everything (JSON)", "documents.json", () -> Json.pretty(results.stream()
                        .map(r -> Map.of("file", r.name(), "kind", r.kind(), "document", r.document().map(d -> (Object) d).orElse(""),
                                "tables", r.tables(), "found", r.found())).toList())));

        ComboBox<String> themes = new ComboBox<>();
        themes.getItems().addAll(THEMES.keySet());
        themes.setValue(theme);
        themes.setOnAction(e -> {
            theme = themes.getValue();
            Application.setUserAgentStylesheet(THEMES.get(theme).get().getUserAgentStylesheet());
        });

        HBox bar = new HBox(10, new VBox(0, title, subtitle), new Spacer(16), open, reload, deep, path, new Spacer(), export,
                new FontIcon(Feather.SUN), themes);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12));
        return bar;
    }

    private MenuItem item(String text, String file, Supplier<String> content) {
        MenuItem m = new MenuItem(text);
        m.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName(file);
            File target = chooser.showSaveDialog(stage);
            if (target == null) return;
            try {
                Files.writeString(target.toPath(), content.get(), StandardCharsets.UTF_8);
                status.setText("Saved " + target);
            } catch (IOException ex) {
                status.setText("Could not save: " + ex.getMessage());
            }
        });
        return m;
    }

    private Node statusBar() {
        progress.setPrefWidth(220);
        progress.setVisible(false);
        status.getStyleClass().add(Styles.TEXT_SMALL);
        HBox bar = new HBox(10, status, new Spacer(), progress);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(6, 12, 8, 12));
        return bar;
    }

    private void showEmpty() {
        Message hint = new Message("Nothing open yet",
                "Open a folder (or drop one on the window). PDFs are read from their text (scanned pages with OCR), "
                        + "images (png, jpg, tiff) with OCR in the JVM, and .txt and .tsv files as they are. Each file becomes a row: a bill, invoice or receipt with its parties, "
                        + "totals and line items, its tables, and every value object in it.",
                new FontIcon(Feather.INFO));
        hint.getStyleClass().add(Styles.ACCENT);
        hint.setMaxWidth(720);
        StackPane pane = new StackPane(hint);
        pane.setPadding(new Insets(40));
        tabs.getTabs().setAll(tab("Start", Feather.HOME, pane));
        tiles.getChildren().clear();
    }

    // ── reading ───────────────────────────────────────────────────────────

    private void open(List<Path> chosen) {
        folders.clear();
        folders.addAll(chosen);
        path.setText(String.join(", ", chosen.stream().map(Path::toString).toList()));
        boolean subfolders = deep.isSelected();
        Task<List<Scan.FileResult>> task = new Task<>() {
            @Override
            protected List<Scan.FileResult> call() throws Exception {
                return Scan.folders(chosen, subfolders, (done, total) -> {
                    updateProgress(done, total);
                    updateMessage("Read " + done + " of " + total + " files…");
                });
            }
        };
        progress.progressProperty().bind(task.progressProperty());
        status.textProperty().bind(task.messageProperty());
        progress.setVisible(true);
        task.setOnSucceeded(e -> {
            unbind();
            results = task.getValue();
            long docs = results.stream().filter(r -> r.document().isPresent()).count();
            status.setText("Read " + results.size() + " files: " + docs + " bills, invoices and receipts.");
            show();
            if (screenshots != null) takeScreenshots();
        });
        task.setOnFailed(e -> {
            unbind();
            status.setText("Could not read the folder: " + task.getException().getMessage());
            if (screenshots != null) Platform.exit();
        });
        Thread t = new Thread(task, "scan");
        t.setDaemon(true);
        t.start();
    }

    private void unbind() {
        progress.progressProperty().unbind();
        status.textProperty().unbind();
        progress.setVisible(false);
    }

    // ── what was read ─────────────────────────────────────────────────────

    private void show() {
        export.setDisable(results.isEmpty());
        tiles.getChildren().setAll(Analysis.tiles(results));
        ScrollPane charts = new ScrollPane(Analysis.charts(results, screenshots == null));
        charts.setFitToWidth(true);
        tabs.getTabs().setAll(
                tab("Documents", Feather.FILE_TEXT, Sheets.of(
                        List.of("File", "Kind", "Number", "Date", "Due / paid", "Issuer", "Customer", "Currency", "Subtotal",
                                "Tax", "Total", "Lines", "Terms / method"),
                        List.of(210, 80, 130, 100, 100, 200, 200, 80, 100, 90, 110, 60, 160),
                        documentRows())),
                tab("Line items", Feather.LIST, Sheets.of(
                        List.of("File", "Issuer", "Description", "Quantity", "Unit (Rec 20)", "Unit price", "Amount", "Currency"),
                        List.of(210, 190, 340, 90, 110, 110, 110, 80),
                        lineItemRows())),
                tab("Tables", Feather.GRID, tables()),
                tab("Values found", Feather.SEARCH, Sheets.of(
                        List.of("File", "Line", "Type", "Value", "As printed", "Shape (regex)"),
                        List.of(210, 60, 140, 240, 240, 420),
                        foundRows())),
                tab("Files", Feather.FOLDER, files()),
                tab("Analysis", Feather.BAR_CHART_2, charts));
    }

    private static Tab tab(String name, Feather icon, Node content) {
        Tab t = new Tab(name, content);
        t.setGraphic(new FontIcon(icon));
        t.setClosable(false);
        return t;
    }

    private List<List<?>> documentRows() {
        List<List<?>> rows = new ArrayList<>();
        for (Scan.FileResult r : results) {
            if (r.document().isEmpty()) continue;
            BillingDocument d = r.document().get();
            String terms = switch (d) {
                case Invoice i -> i.paymentTerms().map(Object::toString).orElse("");
                case Receipt rc -> rc.card().map(Object::toString).or(rc::paymentMethod).orElse("");
                case Bill b -> b.accountNumber().map(a -> "Account " + a).orElse("");
                default -> "";
            };
            rows.add(List.of(r.name(), r.kind(), d.number().orElse(""), d.date(), r.dueOrPaid(),
                    d.issuer().map(p -> p.name()).orElse(""), d.customer().map(p -> p.name()).orElse(""),
                    d.currency().map(c -> c.code()).orElse(""), d.subtotal(), d.tax(), d.total(), d.lineItems().size(), terms));
        }
        return rows;
    }

    private List<List<?>> lineItemRows() {
        List<List<?>> rows = new ArrayList<>();
        for (Scan.FileResult r : results) {
            r.document().ifPresent(d -> {
                for (LineItem li : d.lineItems()) {
                    rows.add(List.of(r.name(), d.issuer().map(p -> p.name()).orElse(""), li.description(),
                            li.quantity().map(q -> (Object) q.amount()), li.quantity().map(q -> q.unit().code() + " · " + q.unit().symbol()).orElse(""),
                            li.unitPrice(), li.total(), li.total().map(m -> m.currency().code()).orElse(d.currency().map(c -> c.code()).orElse(""))));
                }
            });
        }
        return rows;
    }

    private List<List<?>> foundRows() {
        List<List<?>> rows = new ArrayList<>();
        for (Scan.FileResult r : results) {
            for (Found f : r.found()) {
                rows.add(List.of(r.name(), f.line(r.text()) + 1, f.type().name(), String.valueOf(f.value()), f.text(), f.shape()));
            }
        }
        return rows;
    }

    /** Every table found, listed on the left; the chosen one as a sheet on the right. */
    private Node tables() {
        record Entry(Scan.FileResult file, int index, Table table) {
            @Override
            public String toString() {
                return file.name() + " · table " + (index + 1) + " (" + table.height() + " × " + table.width() + ")";
            }
        }
        ListView<Entry> list = new ListView<>();
        for (Scan.FileResult r : results) for (int i = 0; i < r.tables().size(); i++) list.getItems().add(new Entry(r, i, r.tables().get(i)));
        StackPane right = new StackPane(new Label("Choose a table"));
        list.getSelectionModel().selectedItemProperty().addListener((o, a, e) -> {
            if (e == null) return;
            List<String> headers = e.table().columns().stream().map(Table.Column::name).toList();
            List<List<?>> rows = new ArrayList<>(e.table().rows());
            right.getChildren().setAll(Sheets.of(headers, headers.stream().map(h -> Math.max(110, h.length() * 8)).toList(), rows));
        });
        // open on the biggest table found
        list.getItems().stream().max(java.util.Comparator.comparingInt(e -> e.table().height() * e.table().width()))
                .ifPresent(e -> {
                    list.getSelectionModel().select(e);
                    list.scrollTo(e);
                });
        SplitPane split = new SplitPane(list, right);
        split.setDividerPositions(0.27);
        return split;
    }

    /** Every file: what it was read as, and its text and image side by side. */
    private Node files() {
        ListView<Scan.FileResult> list = new ListView<>();
        list.getItems().setAll(results);
        list.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(Scan.FileResult r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(r.name() + "  —  " + r.kind() + " · " + r.source() + " · " + r.took().toMillis() + " ms");
                setGraphic(new FontIcon(!r.error().isEmpty() ? Feather.ALERT_TRIANGLE : switch (r.source()) {
                    case "OCR" -> Feather.IMAGE;
                    case "PDF" -> Feather.FILE;
                    default -> Feather.FILE_TEXT;
                }));
            }
        });
        TextArea text = new TextArea();
        text.setEditable(false);
        text.setStyle("-fx-font-family: 'Consolas', 'JetBrains Mono', monospace; -fx-font-size: 12px;");
        ImageView image = new ImageView();
        image.setPreserveRatio(true);
        image.setFitWidth(420);
        ScrollPane imagePane = new ScrollPane(image);
        list.getSelectionModel().selectedItemProperty().addListener((o, a, r) -> {
            if (r == null) return;
            text.setText(r.error().isEmpty() ? r.text() : "Could not read: " + r.error());
            Optional<Path> picture = picture(r.file());
            image.setImage(picture.map(p -> new Image(p.toUri().toString())).orElse(null));
        });
        if (!list.getItems().isEmpty()) list.getSelectionModel().select(0);
        SplitPane split = new SplitPane(list, text, imagePane);
        split.setDividerPositions(0.3, 0.68);
        return split;
    }

    /** The file itself when it is an image, else an image of the same name beside it. */
    private static Optional<Path> picture(Path file) {
        if (Scan.IMAGES.contains(Scan.ext(file))) return Optional.of(file);
        for (String ext : Scan.IMAGES) {
            Path p = file.resolveSibling(Scan.stem(file) + "." + ext);
            if (Files.exists(p)) return Optional.of(p);
        }
        return Optional.empty();
    }

    // ── screenshots ───────────────────────────────────────────────────────

    /** Saves the window with each tab showing, then quits: for the README. */
    private void takeScreenshots() {
        List<Tab> all = List.copyOf(tabs.getTabs());
        shoot(all, 0);
    }

    private void shoot(List<Tab> all, int i) {
        if (i >= all.size()) {
            Platform.exit();
            return;
        }
        tabs.getSelectionModel().select(all.get(i));
        PauseTransition settle = new PauseTransition(Duration.millis(900));
        settle.setOnFinished(e -> {
            try {
                Files.createDirectories(screenshots);
                String name = "demo-" + all.get(i).getText().toLowerCase().replace(' ', '-') + ".png";
                var image = stage.getScene().snapshot(null);
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", screenshots.resolve(name).toFile());
                System.out.println("Saved " + screenshots.resolve(name));
            } catch (IOException ex) {
                System.err.println("Could not save a screenshot: " + ex.getMessage());
            }
            shoot(all, i + 1);
        });
        settle.play();
    }

    private static String css() {
        // ControlsFX's sheet styles look up Modena's colour names, which AtlantaFX themes do not define: map them to
        // the theme's colours, so the sheets follow whichever theme is chosen
        String css = """
                .root {
                    -fx-background: -color-bg-default;
                    -fx-control-inner-background: -color-bg-default;
                    -fx-control-inner-background-alt: -color-bg-subtle;
                    -fx-table-cell-border-color: -color-border-muted;
                    -fx-text-inner-color: -color-fg-default;
                    -fx-text-base-color: -color-fg-default;
                    -fx-selection-bar: -color-accent-subtle;
                    -fx-selection-bar-non-focused: -color-bg-subtle;
                    -fx-selection-bar-text: -color-fg-default;
                    -fx-box-border: -color-border-default;
                    -fx-body-color: -color-bg-subtle;
                }
                .spreadsheet-cell.number { -fx-alignment: center-right; }
                .spreadsheet-view .column-header { -fx-font-weight: bold; }
                .chart { -fx-padding: 4; }
                """;
        return "data:text/css;base64," + java.util.Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
    }
}
