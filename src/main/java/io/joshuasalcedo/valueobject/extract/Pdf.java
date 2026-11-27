package io.joshuasalcedo.valueobject.extract;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import io.joshuasalcedo.valueobject.extract.Ocr;
import io.joshuasalcedo.valueobject.table.Layout;
import io.joshuasalcedo.valueobject.table.TableDetector;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads PDFs into the laid-out text the rest of the library reads: every word at the column of its position on the
 * page (see {@link Layout}), so tables line up and labels stay beside their values. A page with a text layer is read
 * from it; a scanned page (no text) is rendered at 300 DPI and read with {@link Ocr} when one is given.
 *
 * <pre>{@code
 * String text = Pdf.text(Path.of("bill.pdf"));               // text-layer pages
 * try (Ocr ocr = Ocr.english()) {
 *     BillingDocument bill = BillingDocument.parse(Pdf.text(Path.of("scan.pdf"), ocr));   // scanned pages too
 * }
 * }</pre>
 *
 * <p>Needs {@code org.apache.pdfbox:pdfbox} (3.x), an optional dependency of this library: add it to read PDFs.
 * Pages are separated by a form feed ({@code \f}) between blank lines.
 *
 * @author JoshuaSalcedo
 */
public final class Pdf {

    /** The resolution scanned pages are rendered at for OCR. */
    public static final int OCR_DPI = 300;

    /** A page with fewer words than this in its text layer is taken for a scan. */
    private static final int SCAN_BELOW_WORDS = 3;

    private Pdf() {}

    /**
     * Reads a PDF's text layer; scanned pages come back empty.
     *
     * @param pdf the file
     * @return its pages' text, laid out as on the page
     * @throws ValueObjectException if it cannot be read (not a PDF, or locked with a password)
     */
    public static String text(Path pdf) {
        return text(pdf, null);
    }

    /**
     * Reads a PDF: its text layer, and its scanned pages with OCR.
     *
     * @param pdf the file
     * @param ocr OCR for scanned pages, or null to leave them empty
     * @return its pages' text, laid out as on the page, pages separated by a form feed
     * @throws ValueObjectException if it cannot be read (not a PDF, or locked with a password)
     */
    public static String text(Path pdf, @Nullable Ocr ocr) {
        return String.join("\n\f\n", pages(pdf, ocr));
    }

    /**
     * Reads a PDF page by page.
     *
     * @param pdf the file
     * @param ocr OCR for scanned pages, or null to leave them empty
     * @return one laid-out text per page, in order
     * @throws ValueObjectException if it cannot be read (not a PDF, or locked with a password)
     */
    public static List<String> pages(Path pdf, @Nullable Ocr ocr) {
        if (pdf == null || !Files.isRegularFile(pdf)) throw new ValueObjectException("No PDF at " + pdf);
        try (PDDocument doc = Loader.loadPDF(pdf.toFile())) {
            List<String> out = new ArrayList<>();
            for (int page = 1; page <= doc.getNumberOfPages(); page++) {
                List<TableDetector.Word> words = words(doc, page);
                if (words.size() < SCAN_BELOW_WORDS && ocr != null) {
                    out.add(ocr.text(render(doc, page)));
                } else {
                    out.add(Layout.text(words));
                }
            }
            return out;
        } catch (InvalidPasswordException e) {
            throw new ValueObjectException("The PDF is locked with a password: " + pdf.getFileName());
        } catch (IOException e) {
            throw new ValueObjectException("Not a readable PDF: " + pdf.getFileName() + " (" + e.getMessage() + ")", e);
        }
    }

    /**
     * Reads the words of one page's text layer, with their boxes, in points from the page's top left.
     *
     * @param pdf  the file
     * @param page the page, from 1
     * @return its words; empty for a scanned page
     * @throws ValueObjectException if it cannot be read
     */
    public static List<TableDetector.Word> words(Path pdf, int page) {
        try (PDDocument doc = Loader.loadPDF(pdf.toFile())) {
            if (page < 1 || page > doc.getNumberOfPages()) throw new ValueObjectException("No page " + page + " in " + pdf.getFileName());
            return words(doc, page);
        } catch (InvalidPasswordException e) {
            throw new ValueObjectException("The PDF is locked with a password: " + pdf.getFileName());
        } catch (IOException e) {
            throw new ValueObjectException("Not a readable PDF: " + pdf.getFileName() + " (" + e.getMessage() + ")", e);
        }
    }

    /** The page's words: the text stripper's runs of letters split at spaces, each with its box. */
    private static List<TableDetector.Word> words(PDDocument doc, int page) throws IOException {
        List<TableDetector.Word> words = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String text, List<TextPosition> positions) {
                List<TextPosition> word = new ArrayList<>();
                for (TextPosition p : positions) {
                    if (p.getUnicode() == null || p.getUnicode().isBlank()) {
                        add(word, words);
                        word = new ArrayList<>();
                    } else {
                        word.add(p);
                    }
                }
                add(word, words);
            }
        };
        stripper.setSortByPosition(true);
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        stripper.writeText(doc, new StringWriter());
        return words;
    }

    private static void add(List<TextPosition> word, List<TableDetector.Word> words) {
        if (word.isEmpty()) return;
        StringBuilder text = new StringBuilder();
        for (TextPosition p : word) text.append(p.getUnicode());
        TextPosition first = word.getFirst();
        TextPosition last = word.getLast();
        double x = first.getXDirAdj();
        double right = last.getXDirAdj() + last.getWidthDirAdj();
        double height = word.stream().mapToDouble(TextPosition::getHeightDir).max().orElse(first.getHeightDir());
        words.add(new TableDetector.Word(text.toString(), x, first.getYDirAdj(), Math.max(1, right - x), Math.max(1, height)));
    }

    /** One page as a PNG, at {@link #OCR_DPI}. */
    private static byte[] render(PDDocument doc, int page) throws IOException {
        BufferedImage image = new PDFRenderer(doc).renderImageWithDPI(page - 1, OCR_DPI, ImageType.GRAY);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);
        return png.toByteArray();
    }
}
