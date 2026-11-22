package io.joshuasalcedo.valueobject.table;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Turns OCR words with boxes back into plain text that keeps the page's layout: each word starts at the character
 * column of its position, so columns line up and labels stay beside their values; a wide vertical gap becomes one
 * blank line. Tesseract's own text output loses this (its spacing drifts, and it puts blank lines between rows).
 *
 * <pre>{@code
 * String text = Layout.text(tsv);          // Tesseract TSV in, laid-out text out
 * Receipt r = Receipt.parse(text);
 * }</pre>
 *
 * @author JoshuaSalcedo
 */
public final class Layout {

    private Layout() {}

    /**
     * {@return the text laid out as on the page; pages are separated by a form feed; the input as is when it is neither}
     *
     * @param tsv Tesseract TSV (every page), or positioned words ({@code "x y w h<TAB>text"})
     */
    public static String text(@Nullable String tsv) {
        if (tsv == null || tsv.isBlank()) return "";
        Map<Integer, List<TableDetector.Word>> pages = TableDetector.words(tsv);
        if (pages == null) return tsv;
        List<String> out = new ArrayList<>();
        pages.values().forEach(words -> out.add(text(words)));
        return String.join("\n\f\n", out);
    }

    /**
     * {@return the page as text, words at the columns of their positions}
     *
     * @param words one page's words with their boxes
     */
    public static String text(List<TableDetector.Word> words) {
        List<TableDetector.Word> sorted = words.stream().filter(w -> !w.text().isBlank())
                .sorted(Comparator.comparingDouble(TableDetector.Word::y).thenComparingDouble(TableDetector.Word::x)).toList();
        if (sorted.isEmpty()) return "";

        // one character's width: the median over the words
        double[] widths = sorted.stream().mapToDouble(w -> w.width() / Math.max(1, w.text().length())).sorted().toArray();
        double charWidth = Math.max(1, widths[widths.length / 2]);
        double[] heights = sorted.stream().mapToDouble(TableDetector.Word::height).sorted().toArray();
        double lineHeight = Math.max(1, heights[heights.length / 2]);
        double left = sorted.stream().mapToDouble(TableDetector.Word::x).min().orElse(0);

        // lines: baselines within a third of the height
        List<List<TableDetector.Word>> lines = new ArrayList<>();
        List<TableDetector.Word> current = new ArrayList<>();
        double y = Double.NaN;
        for (TableDetector.Word w : sorted) {
            if (!current.isEmpty() && Math.abs(w.y() - y) > Math.max(2, w.height() / 3)) {
                lines.add(current);
                current = new ArrayList<>();
            }
            if (current.isEmpty()) y = w.y();
            current.add(w);
        }
        lines.add(current);

        StringBuilder out = new StringBuilder();
        double previousY = Double.NaN;
        for (List<TableDetector.Word> line : lines) {
            double lineY = line.getFirst().y();
            if (!Double.isNaN(previousY) && lineY - previousY > 2.2 * lineHeight) out.append('\n');
            previousY = lineY;
            StringBuilder s = new StringBuilder();
            TableDetector.Word last = null;
            for (TableDetector.Word w : line.stream().sorted(Comparator.comparingDouble(TableDetector.Word::x)).toList()) {
                int column = (int) Math.round((w.x() - left) / charWidth);
                if (last != null) {
                    double gap = w.x() - (last.x() + last.width());
                    // the gap in the line's own character width: big text has wide spaces
                    double own = Math.max(charWidth, last.width() / Math.max(1, last.text().length()));
                    boolean wide = gap > 1.6 * own;
                    int minimum = s.length() + (wide ? 2 : 1);   // a wide gap stays a cell break
                    column = wide ? Math.max(column, minimum) : minimum;  // a word gap is one space
                }
                while (s.length() < column) s.append(' ');
                s.append(w.text());
                last = w;
            }
            out.append(s.toString().stripTrailing()).append('\n');
        }
        return out.toString();
    }

}
