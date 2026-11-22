package io.joshuasalcedo.valueobject.table;

import io.joshuasalcedo.valueobject.ValueObject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Finds the tables in a long string. The string can be
 * <ul>
 *   <li><b>Tesseract TSV</b> ({@code tesseract page.png out tsv}, or {@code -c tessedit_create_tsv=1}): words with
 *       their boxes, page by page;</li>
 *   <li><b>positioned words</b>, one a line, {@code "x y width height<TAB>text"} (optionally after a {@code #v3}
 *       line);</li>
 *   <li><b>plain text</b>: columns set apart by two or more spaces, by tabs, or by {@code |} (Markdown or ASCII
 *       tables; their {@code ---} rule lines are skipped).</li>
 * </ul>
 *
 * <p>How a table is found:
 * <ol>
 *   <li>Words on the same line are grouped; a gap wider than about two characters (two spaces in plain text)
 *       starts a new cell.</li>
 *   <li>A run of lines with two or more cells is a candidate table. A single-cell line inside it is a wrapped cell
 *       of the row above; a blank line (or a wide vertical gap) ends it.</li>
 *   <li>Columns are where the cells line up: the cell ranges of the most common row shape, merged where they
 *       overlap, with other rows' cells joining the column they overlap most.</li>
 *   <li>A row whose first column is empty and that fills less than half the columns continues the row above.</li>
 *   <li>Each column is typed by the {@link ValueObject} most of its cells are ({@code EMAIL}, {@code MONEY}, ...),
 *       or marked numeric. Two neighbouring columns whose cells only make a value object together
 *       ({@code "149.99"} and {@code "USD"}) are one column.</li>
 *   <li>The first row is the header when none of its cells is a number or a value object; a second such row whose
 *       cells sit over the typed columns is the rest of the header ({@code "Unit"} over {@code "Price"}).</li>
 * </ol>
 * A candidate needs at least two columns and two rows that use them, so prose with a double space is not a table.
 *
 * @author JoshuaSalcedo
 */
public final class TableDetector {

    /**
     * A word and its box, as OCR gives it.
     *
     * @param text   the word
     * @param x      its left edge
     * @param y      its bottom edge (the baseline)
     * @param width  its width
     * @param height its height
     */
    public record Word(String text, double x, double y, double width, double height) {
        public Word {
            Objects.requireNonNull(text, "text");
        }
    }

    /** At least this share of a column's cells must agree for it to get a type. */
    private static final double MAJORITY = 0.6;

    private static final Pattern NUMBER = Pattern.compile(
            "^[(+-]?\\s*[$€£¥₱₹]?\\s*\\d[\\d,' ]*(?:[.]\\d+)?\\s*\\)?\\s*%?$");
    private static final Pattern RULE_LINE = Pattern.compile("^[\\s\\-=_+|:~*]{3,}$");
    private static final Pattern PLAIN_CELL = Pattern.compile("\\S+(?: \\S+)*");
    private static final Pattern TSV_HEADER = Pattern.compile("^level\\tpage_num\\tblock_num", Pattern.MULTILINE);
    private static final Pattern POSITIONED = Pattern.compile("^(-?[\\d.]+) (-?[\\d.]+) ([\\d.]+) ([\\d.]+)\\t(.*)$");

    private TableDetector() {}

    // ── Entry points ──────────────────────────────────────────────────────

    /**
     * @param text Tesseract TSV, positioned words, or plain text
     * @return the tables in it, in reading order; empty when there are none
     */
    public static List<Table> detect(@Nullable String text) {
        if (text == null || text.isBlank()) return List.of();
        if (TSV_HEADER.matcher(text).find()) return fromTsv(text);
        List<Word> positioned = positioned(text);
        if (positioned != null) return detect(1, positioned);
        return tables(1, plainLines(text), false);
    }

    /**
     * @param page  the page number to report
     * @param words the page's words with their boxes, in any order
     * @return the tables among them
     */
    public static List<Table> detect(int page, List<Word> words) {
        return tables(page, wordLines(words), true);
    }

    // ── Reading the input ─────────────────────────────────────────────────

    /** A cell: its text and horizontal extent. */
    private record Cell(String text, double x, double right) {
        double overlap(double left, double r) {
            return Math.min(right, r) - Math.max(x, left);
        }
    }

    /** A line of cells; {@code gapBefore} when a blank line (or a wide gap) comes before it. */
    private record Line(int number, List<Cell> cells, boolean gapBefore, boolean rule) {}

    private static List<Table> fromTsv(String tsv) {
        List<Table> out = new ArrayList<>();
        Map<Integer, List<Word>> pages = tsvWords(tsv);
        pages.forEach((page, words) -> out.addAll(detect(page, words)));
        return out;
    }

    /**
     * The words of Tesseract TSV (by page) or of positioned words (page 1), or null when the text is neither.
     */
    static @Nullable Map<Integer, List<Word>> words(String text) {
        if (TSV_HEADER.matcher(text).find()) return tsvWords(text);
        List<Word> positioned = positioned(text);
        if (positioned == null) return null;
        Map<Integer, List<Word>> one = new TreeMap<>();
        one.put(1, positioned);
        return one;
    }

    private static Map<Integer, List<Word>> tsvWords(String tsv) {
        Map<Integer, List<Word>> pages = new TreeMap<>();
        for (String line : tsv.split("\\R")) {
            String[] f = line.split("\t", -1);
            if (f.length < 12 || !f[0].equals("5")) continue;
            String text = f[11].strip();
            if (text.isEmpty()) continue;
            try {
                int page = Integer.parseInt(f[1]);
                double left = Double.parseDouble(f[6]);
                double top = Double.parseDouble(f[7]);
                double width = Double.parseDouble(f[8]);
                double height = Double.parseDouble(f[9]);
                pages.computeIfAbsent(page, p -> new ArrayList<>()).add(new Word(text, left, top + height, width, height));
            } catch (NumberFormatException skip) {
                // a damaged line: the rest of the page still reads
            }
        }
        return pages;
    }

    /** The words of {@code "x y w h<TAB>text"} lines, or null when the text is not in that form. */
    private static @Nullable List<Word> positioned(String text) {
        List<Word> words = new ArrayList<>();
        int other = 0;
        for (String line : text.split("\\R")) {
            if (line.isBlank() || line.startsWith("#")) continue;
            Matcher m = POSITIONED.matcher(line);
            if (!m.matches()) {
                if (++other > 2) return null;
                continue;
            }
            try {
                words.add(new Word(m.group(5), Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)),
                        Double.parseDouble(m.group(3)), Double.parseDouble(m.group(4))));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return words.size() >= 4 && words.size() > other * 10 ? words : null;
    }

    private static List<Line> plainLines(String text) {
        String[] raw = text.split("\\R", -1);
        List<Line> lines = new ArrayList<>(raw.length);
        boolean blank = false;
        for (int i = 0; i < raw.length; i++) {
            String s = raw[i];
            if (s.isBlank()) {
                blank = true;
                continue;
            }
            if (RULE_LINE.matcher(s).matches()) {
                lines.add(new Line(i, List.of(), blank, true));
                blank = false;
                continue;
            }
            List<Cell> cells = new ArrayList<>();
            if (s.indexOf('|') >= 0 && s.chars().filter(c -> c == '|').count() >= 2) {
                hardCells(s.strip().replaceAll("^\\|", "").replaceAll("\\|$", "").split("(?<!\\\\)\\|", -1), cells);
            } else if (s.indexOf('\t') >= 0) {
                hardCells(s.split("\t", -1), cells);
            } else {
                Matcher m = PLAIN_CELL.matcher(s);
                while (m.find()) cells.add(new Cell(m.group(), m.start(), m.end()));
            }
            lines.add(new Line(i, cells, blank, false));
            blank = false;
        }
        return lines;
    }

    /** Cells split by a delimiter: placed by position, so the n-th cells of all rows line up. */
    private static void hardCells(String[] parts, List<Cell> cells) {
        for (int i = 0; i < parts.length; i++) {
            String t = parts[i].strip().replace("\\|", "|");
            if (!t.isEmpty()) cells.add(new Cell(t, i * 1_000_000.0, i * 1_000_000.0 + 1));
        }
    }

    /** Words grouped into lines (baselines within a third of their height), then into cells at wide gaps. */
    private static List<Line> wordLines(List<Word> words) {
        List<Word> sorted = words.stream().filter(w -> !w.text().isBlank())
                .sorted(Comparator.comparingDouble(Word::y).thenComparingDouble(Word::x)).toList();
        List<List<Word>> grouped = new ArrayList<>();
        List<Word> current = new ArrayList<>();
        double y = Double.NaN;
        for (Word w : sorted) {
            if (!current.isEmpty() && Math.abs(w.y() - y) > Math.max(2, w.height() / 3)) {
                grouped.add(current);
                current = new ArrayList<>();
            }
            if (current.isEmpty()) y = w.y();
            current.add(w);
        }
        if (!current.isEmpty()) grouped.add(current);

        List<Line> lines = new ArrayList<>();
        double previousY = Double.NaN;
        for (int i = 0; i < grouped.size(); i++) {
            List<Word> line = grouped.get(i).stream().sorted(Comparator.comparingDouble(Word::x)).toList();
            double lineY = line.getFirst().y();
            double height = line.stream().mapToDouble(Word::height).max().orElse(10);
            boolean gap = !Double.isNaN(previousY) && lineY - previousY > 2.6 * height;
            previousY = lineY;
            String joined = line.stream().map(Word::text).collect(Collectors.joining(" "));
            if (RULE_LINE.matcher(joined).matches()) {
                lines.add(new Line(i, List.of(), gap, true));
                continue;
            }
            List<Cell> cells = new ArrayList<>();
            List<Word> cell = new ArrayList<>();
            for (Word w : line) {
                if (!cell.isEmpty()) {
                    Word last = cell.getLast();
                    double charWidth = Math.max(1, last.width() / Math.max(1, last.text().length()));
                    if (w.x() - (last.x() + last.width()) > 2.2 * charWidth) {
                        cells.add(cellOf(cell));
                        cell = new ArrayList<>();
                    }
                }
                cell.add(w);
            }
            if (!cell.isEmpty()) cells.add(cellOf(cell));
            cells.removeIf(TableDetector::isScanNoise);
            lines.add(new Line(i, cells, gap, false));
        }
        return lines;
    }

    /** A cell OCR made of a scanned border or speck: no letters or digits, or one lone letter or mark. */
    private static boolean isScanNoise(Cell c) {
        String core = c.text().replaceAll("[^\\p{L}\\p{N}]", "");
        return core.isEmpty() || (core.length() == 1 && !Character.isDigit(core.charAt(0)));
    }

    private static Cell cellOf(List<Word> words) {
        Word last = words.getLast();
        return new Cell(words.stream().map(Word::text).collect(Collectors.joining(" ")), words.getFirst().x(),
                last.x() + last.width());
    }

    // ── Finding tables ────────────────────────────────────────────────────

    private static List<Table> tables(int page, List<Line> lines, boolean positioned) {
        // 1. candidate blocks: runs of lines with two or more cells (a one-cell line inside may be a wrapped cell)
        List<List<Line>> blocks = new ArrayList<>();
        List<Line> block = new ArrayList<>();
        for (Line line : lines) {
            if (line.rule()) continue;                       // borders and --- separators never end a table
            if (line.gapBefore() && !block.isEmpty()) close(block, blocks);
            if (line.cells().size() >= 2) {
                block.add(line);
            } else if (!block.isEmpty() && line.cells().size() == 1 && block.getLast().cells().size() >= 2
                    && line.cells().getFirst().text().length() <= 60) {
                block.add(line);                             // maybe a wrapped cell; kept only if a row follows
            } else {
                close(block, blocks);
                if (line.cells().size() == 1) block.add(line); // a one-cell line can still open a block
            }
        }
        close(block, blocks);

        // 2. rows are often apart (tall bordered rows on a page, blank lines in OCR text): blocks whose columns line
        //    up are one table; so is a block of wrapped one-cell lines between two such blocks
        List<List<Line>> merged = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++) {
            List<Line> b = blocks.get(i);
            boolean oneCells = b.stream().allMatch(l -> l.cells().size() < 2);
            if (!merged.isEmpty() && !oneCells && aligned(merged.getLast(), b)) {
                merged.getLast().addAll(b);
            } else if (!merged.isEmpty() && oneCells && i + 1 < blocks.size() && aligned(merged.getLast(), blocks.get(i + 1))) {
                merged.getLast().addAll(b);
            } else {
                merged.add(new ArrayList<>(b));
            }
        }
        blocks = merged;

        // 3. a table needs two rows that use its columns; one-cell lines at its edges are not its rows
        List<Table> out = new ArrayList<>();
        for (List<Line> b : blocks) {
            while (!b.isEmpty() && b.getLast().cells().size() < 2) b.removeLast();
            while (!b.isEmpty() && b.getFirst().cells().size() < 2) b.removeFirst();
            if (b.stream().filter(l -> l.cells().size() >= 2).count() < 2) continue;
            Table t = build(page, List.copyOf(b));
            if (t != null) out.add(t);
        }
        return out;
    }

    private static void close(List<Line> block, List<List<Line>> blocks) {
        if (!block.isEmpty()) blocks.add(new ArrayList<>(block));
        block.clear();
    }

    /** Whether most columns of {@code next} sit under columns of {@code previous}, and the other way round. */
    private static boolean aligned(List<Line> previous, List<Line> next) {
        List<Line> pa = previous.stream().filter(l -> l.cells().size() >= 2).toList();
        List<Line> pb = next.stream().filter(l -> l.cells().size() >= 2).toList();
        if (pa.isEmpty() || pb.isEmpty()) return false;
        List<Span> a = spans(pa);
        List<Span> b = spans(pb);
        if (a.size() < 2 || b.size() < 2) return false;
        return share(a, b) >= 0.6 && share(b, a) >= 0.6;
    }

    /** The share of columns in {@code of} that line up with one in {@code in}: same left edge, or same right edge. */
    private static double share(List<Span> of, List<Span> in) {
        long hits = of.stream().filter(s -> in.stream().anyMatch(o -> edges(s, o))).count();
        return (double) hits / of.size();
    }

    /** Two columns line up when they start or end together (left- or right-aligned), within a little. */
    private static boolean edges(Span a, Span b) {
        double tolerance = Math.max(4, Math.min(a.right - a.left, b.right - b.left) * 0.15);
        if (a.left >= 1_000_000 || b.left >= 1_000_000) return Math.abs(a.left - b.left) < 1;   // split by delimiters
        return Math.abs(a.left - b.left) <= tolerance || Math.abs(a.right - b.right) <= tolerance;
    }

    /** A column's horizontal extent. */
    private static final class Span {
        double left;
        double right;

        Span(double left, double right) {
            this.left = left;
            this.right = right;
        }
    }

    private static @Nullable Table build(int page, List<Line> lines) {
        List<Span> spans = spans(lines);
        if (spans.size() < 2) return null;

        // cells into the grid
        List<String[]> grid = new ArrayList<>();
        List<Integer> lineNumbers = new ArrayList<>();
        for (Line line : lines) {
            String[] row = new String[spans.size()];
            Arrays.fill(row, "");
            for (Cell c : line.cells()) {
                int best = bestSpan(spans, c);
                row[best] = row[best].isEmpty() ? c.text() : row[best] + " " + c.text();
            }
            grid.add(row);
            lineNumbers.add(line.number());
        }

        // narrow neighbouring columns never filled in the same row are one column a character off ("2" and "1" of Qty)
        for (int c = 0; c + 1 < spans.size(); c++) {
            int a = c;
            boolean apart = grid.stream().noneMatch(r -> !r[a].isEmpty() && !r[a + 1].isEmpty());
            double gap = spans.get(a + 1).left - spans.get(a).right;
            boolean narrow = spans.get(a).right - spans.get(a).left <= 6 && spans.get(a + 1).right - spans.get(a + 1).left <= 6;
            if (apart && narrow && gap <= 3 && spans.get(a).left < 1_000_000) {
                for (String[] r : grid) if (r[a].isEmpty()) r[a] = r[a + 1];
                for (int k = 0; k < grid.size(); k++) {
                    String[] r = grid.get(k);
                    String[] n = new String[r.length - 1];
                    System.arraycopy(r, 0, n, 0, a + 1);
                    System.arraycopy(r, a + 2, n, a + 1, r.length - a - 2);
                    grid.set(k, n);
                }
                spans.get(a).right = spans.get(a + 1).right;
                spans.remove(a + 1);
                c--;
            }
        }

        // a header row a line or two in (the lines above it are not the table's): start the table there
        int header = -1;
        for (int h = 0; h < Math.min(3, grid.size() - 2) && header < 0; h++) {
            if (isHeaderRow(grid.get(h)) && !isHeaderRow(grid.get(h + 1))) header = h;
        }
        if (header > 0) {
            // the lines above the header are not the table's: work out its columns again without them, as their
            // cells ("Cashier: Rio") may have joined two of its columns
            return build(page, lines.subList(header, lines.size()));
        }

        // a row that starts with an empty first column and fills less than half continues the row above
        List<String[]> rows = new ArrayList<>();
        List<int[]> rowLines = new ArrayList<>();
        for (int i = 0; i < grid.size(); i++) {
            String[] row = grid.get(i);
            long filled = Arrays.stream(row).filter(s -> !s.isEmpty()).count();
            // a wrapped cell: one cell alone in a wide table, or a line with its first column empty and few cells
            boolean continuation = !rows.isEmpty()
                    && ((filled == 1 && row.length > 2) || (row[0].isEmpty() && filled * 2 <= row.length));
            if (continuation) {
                String[] above = rows.getLast();
                for (int c = 0; c < row.length; c++) {
                    if (!row[c].isEmpty()) above[c] = above[c].isEmpty() ? row[c] : above[c] + " " + row[c];
                }
                rowLines.getLast()[1] = lineNumbers.get(i);
            } else {
                rows.add(row.clone());
                rowLines.add(new int[] {lineNumbers.get(i), lineNumbers.get(i)});
            }
        }

        rows = dropEmptyColumns(rows);
        if (rows.isEmpty() || rows.getFirst().length < 2) return null;

        // the columns must really be used: at least two columns filled in at least two rows
        int width = rows.getFirst().length;
        long usedColumns = 0;
        for (int c = 0; c < width; c++) {
            int col = c;
            if (rows.stream().filter(r -> !r[col].isEmpty()).count() >= 2) usedColumns++;
        }
        if (usedColumns < 2) return null;

        rows = mergeByValueObjects(rows);
        width = rows.getFirst().length;

        // header: first row(s) of plain words, over typed columns
        int headerRows = 0;
        if (rows.size() >= 2 && isHeaderLike(rows.getFirst()) && rows.getFirst().length >= 2) {
            headerRows = 1;
            List<String[]> body = rows.subList(1, rows.size());
            Profile[] profiles = profiles(body, width);
            while (headerRows < rows.size() - 1 && headerRows < 3 && isHeaderLike(rows.get(headerRows))
                    && overTypedColumns(rows.get(headerRows), profiles)) {
                headerRows++;
                profiles = profiles(rows.subList(headerRows, rows.size()), width);
            }
        }
        List<String[]> body = rows.subList(headerRows, rows.size());
        if (body.size() < 2) return null;                   // a header and one line is a label, not a table
        Profile[] profiles = profiles(body, width);

        List<Table.Column> columns = new ArrayList<>();
        Map<String, Integer> seen = new HashMap<>();
        for (int c = 0; c < width; c++) {
            StringBuilder name = new StringBuilder();
            for (int h = 0; h < headerRows; h++) {
                String part = rows.get(h)[c];
                if (!part.isEmpty()) name.append(name.isEmpty() ? "" : " ").append(part);
            }
            String n = name.isEmpty() ? "Column " + (c + 1) : name.toString();
            int count = seen.merge(n.toLowerCase(), 1, Integer::sum);
            if (count > 1) n = n + " " + count;
            columns.add(new Table.Column(c, n, Optional.ofNullable(profiles[c].type), profiles[c].numeric));
        }
        List<List<String>> cells = body.stream().map(List::of).toList();
        return new Table(page, rowLines.getFirst()[0], rowLines.getLast()[1], headerRows > 0, columns, cells);
    }

    /** Column extents: the cells of the most common row shape, merged where they overlap; other cells join or add. */
    private static List<Span> spans(List<Line> lines) {
        Map<Integer, Long> shapes = lines.stream().filter(l -> l.cells().size() >= 2)
                .collect(Collectors.groupingBy(l -> l.cells().size(), Collectors.counting()));
        int mode = shapes.entrySet().stream()
                .max(Map.Entry.<Integer, Long>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey).orElse(2);

        List<Cell> seed = lines.stream().filter(l -> l.cells().size() == mode).flatMap(l -> l.cells().stream())
                .sorted(Comparator.comparingDouble(Cell::x)).toList();
        List<Span> spans = new ArrayList<>();
        for (Cell c : seed) {
            if (!spans.isEmpty() && c.x() < spans.getLast().right) {
                spans.getLast().right = Math.max(spans.getLast().right, c.right());
            } else {
                spans.add(new Span(c.x(), c.right()));
            }
        }
        for (Line l : lines) {
            if (l.cells().size() == mode) continue;
            for (Cell c : l.cells()) {
                boolean overlaps = spans.stream().anyMatch(s -> c.overlap(s.left, s.right) > 0);
                if (!overlaps) {
                    spans.add(new Span(c.x(), c.right()));
                    spans.sort(Comparator.comparingDouble(s -> s.left));
                }
            }
        }
        return spans;
    }

    private static int bestSpan(List<Span> spans, Cell c) {
        int best = 0;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < spans.size(); i++) {
            Span s = spans.get(i);
            double overlap = c.overlap(s.left, s.right);
            double score = overlap > 0 ? overlap : -Math.abs((c.x() + c.right()) / 2 - (s.left + s.right) / 2);
            if (score > bestScore) {
                bestScore = score;
                best = i;
            }
        }
        return best;
    }

    private static List<String[]> dropEmptyColumns(List<String[]> rows) {
        int width = rows.getFirst().length;
        List<Integer> keep = new ArrayList<>();
        for (int c = 0; c < width; c++) {
            int col = c;
            if (rows.stream().anyMatch(r -> !r[col].isEmpty())) keep.add(c);
        }
        return rows.stream().map(r -> keep.stream().map(c -> r[c]).toArray(String[]::new)).collect(Collectors.toList());
    }

    /**
     * Joins neighbouring columns whose cells, read together, are one value object that neither is alone
     * ({@code "149.99" | "USD"} is MONEY; {@code "3:30" | "PM"} is TIME).
     */
    private static List<String[]> mergeByValueObjects(List<String[]> rows) {
        boolean merged = true;
        while (merged && rows.getFirst().length > 2) {
            merged = false;
            int width = rows.getFirst().length;
            for (int c = 0; c + 1 < width; c++) {
                int a = c;
                List<String[]> both = rows.stream().filter(r -> !r[a].isEmpty() && !r[a + 1].isEmpty()).toList();
                if (both.size() < 2) continue;
                Map<ValueObject.Type, Long> joined = both.stream()
                        .map(r -> ValueObject.identify(r[a] + " " + r[a + 1])).flatMap(Optional::stream)
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
                Optional<ValueObject.Type> together = majority(joined, both.size());
                if (together.isEmpty()) continue;
                ValueObject.Type t = together.get();
                long aloneA = both.stream().filter(r -> ValueObject.identify(r[a]).filter(t::equals).isPresent()).count();
                long aloneB = both.stream().filter(r -> ValueObject.identify(r[a + 1]).filter(t::equals).isPresent()).count();
                if (aloneA >= both.size() * MAJORITY || aloneB >= both.size() * MAJORITY) continue;
                rows = rows.stream().map(r -> {
                    String[] n = new String[r.length - 1];
                    System.arraycopy(r, 0, n, 0, a);
                    n[a] = (r[a] + " " + r[a + 1]).strip();
                    System.arraycopy(r, a + 2, n, a + 1, r.length - a - 2);
                    return n;
                }).collect(Collectors.toList());
                merged = true;
                break;
            }
        }
        return rows;
    }

    // ── Column profiles ───────────────────────────────────────────────────

    /** What a column's cells mostly are. */
    private record Profile(ValueObject.@Nullable Type type, boolean numeric) {}

    private static Profile[] profiles(List<String[]> body, int width) {
        Profile[] out = new Profile[width];
        for (int c = 0; c < width; c++) {
            int col = c;
            List<String> cells = body.stream().map(r -> r[col]).filter(s -> !s.isEmpty()).toList();
            if (cells.isEmpty()) {
                out[c] = new Profile(null, false);
                continue;
            }
            Map<ValueObject.Type, Long> types = new LinkedHashMap<>();
            for (String s : cells) ValueObject.identify(s).ifPresent(t -> types.merge(t, 1L, Long::sum));
            long numbers = cells.stream().filter(TableDetector::isNumber).count();
            out[c] = new Profile(majority(types, cells.size()).orElse(null), numbers >= cells.size() * MAJORITY);
        }
        return out;
    }

    private static Optional<ValueObject.Type> majority(Map<ValueObject.Type, Long> votes, int total) {
        return votes.entrySet().stream().max(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() >= total * MAJORITY).map(Map.Entry::getKey);
    }

    /** A row that names two or more columns. */
    private static boolean isHeaderRow(String[] row) {
        return isHeaderLike(row) && Arrays.stream(row).filter(x -> !x.isEmpty()).count() >= 2
                && Arrays.stream(row).noneMatch(x -> x.contains(":"));
    }

    /** No numbers, no value objects: words that name columns. */
    private static boolean isHeaderLike(String[] row) {
        long filled = Arrays.stream(row).filter(s -> !s.isEmpty()).count();
        if (filled == 0) return false;
        for (String s : row) {
            if (s.isEmpty()) continue;
            if (isNumber(s) || ValueObject.identify(s).isPresent() || !s.matches(".*\\p{L}.*")) return false;
        }
        return true;
    }

    /** A second header row sits over typed or numeric columns, where a body row would hold a value. */
    private static boolean overTypedColumns(String[] row, Profile[] profiles) {
        for (int c = 0; c < row.length; c++) {
            if ((profiles[c].type() != null || profiles[c].numeric()) && !row[c].isEmpty()) return true;
        }
        return false;
    }

    private static boolean isNumber(String s) {
        return NUMBER.matcher(s.strip()).matches();
    }
}
