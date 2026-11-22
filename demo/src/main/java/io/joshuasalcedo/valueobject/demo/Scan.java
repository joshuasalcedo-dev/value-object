package io.joshuasalcedo.valueobject.demo;

import io.joshuasalcedo.valueobject.Found;
import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.financial.Bill;
import io.joshuasalcedo.valueobject.financial.BillingDocument;
import io.joshuasalcedo.valueobject.financial.Invoice;
import io.joshuasalcedo.valueobject.financial.Receipt;
import io.joshuasalcedo.valueobject.ocr.Ocr;
import io.joshuasalcedo.valueobject.pdf.Pdf;
import io.joshuasalcedo.valueobject.table.Layout;
import io.joshuasalcedo.valueobject.table.Table;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Reads a folder: every image through OCR (Tesseract in the JVM) and every text file as it is, each into what the
 * library finds in it: a bill, invoice or receipt, its tables, and every value object in its text.
 */
final class Scan {

    static final Set<String> IMAGES = Set.of("png", "jpg", "jpeg", "tif", "tiff", "bmp");
    static final Set<String> TEXTS = Set.of("txt", "tsv", "text");
    static final Set<String> PDFS = Set.of("pdf");

    /**
     * What one file held.
     *
     * @param file     the file
     * @param source   how it was read: "OCR" or "Text"
     * @param text     its text (laid out as on the page, for images)
     * @param document the bill, invoice or receipt it is, if it is one
     * @param tables   the tables in it
     * @param found    every value object in its text
     * @param error    why it could not be read, or ""
     * @param took     how long it took
     */
    record FileResult(Path file, String source, String text, Optional<BillingDocument> document, List<Table> tables,
                      List<Found> found, String error, Duration took) {

        String name() {
            return file.getFileName().toString();
        }

        String kind() {
            return document.map(FileResult::kindOf).orElse(error.isEmpty() ? "Other" : "Error");
        }

        static String kindOf(BillingDocument d) {
            return switch (d) {
                case Receipt r -> "Receipt";
                case Invoice i -> "Invoice";
                case Bill b -> "Bill";
                default -> "Document";
            };
        }

        /** A receipt's paid-on day; an invoice's or bill's due day. */
        Optional<LocalDate> dueOrPaid() {
            return document.flatMap(d -> switch (d) {
                case Receipt r -> r.paidOn();
                case Invoice i -> i.dueDate();
                case Bill b -> b.dueDate();
                default -> Optional.empty();
            });
        }
    }

    private Scan() {}

    /**
     * @param folders  folders to read
     * @param deep     whether to read subfolders too
     * @param progress told (done, total) after each file
     * @return one result per file, in name order; when a file is there both as an image and as text (the same name),
     *         the text is read and the image skipped
     */
    static List<FileResult> folders(List<Path> folders, boolean deep, BiConsumer<Integer, Integer> progress) throws IOException {
        List<Path> files = new ArrayList<>();
        for (Path folder : folders) {
            try (Stream<Path> s = deep ? Files.walk(folder) : Files.list(folder)) {
                s.filter(Files::isRegularFile).filter(p -> IMAGES.contains(ext(p)) || TEXTS.contains(ext(p)) || PDFS.contains(ext(p)))
                        .sorted().forEach(files::add);
            }
        }
        // prefer the text of a document over reading its image again
        Map<Path, Path> byStem = new LinkedHashMap<>();
        for (Path f : files) {
            Path stem = f.resolveSibling(stem(f));
            Path seen = byStem.get(stem);
            if (seen == null || rank(f) < rank(seen)) byStem.put(stem, f);
        }
        List<Path> chosen = new ArrayList<>(byStem.values());

        List<FileResult> out = new ArrayList<>();
        Ocr ocr = null;
        try {
            for (int i = 0; i < chosen.size(); i++) {
                Path f = chosen.get(i);
                long start = System.nanoTime();
                try {
                    String text;
                    String source;
                    if (IMAGES.contains(ext(f))) {
                        if (ocr == null) ocr = Ocr.english();
                        text = ocr.text(f);
                        source = "OCR";
                    } else if (PDFS.contains(ext(f))) {
                        // the text layer; scanned pages through OCR when it is set up on this machine
                        if (ocr == null && Ocr.findTessdata("eng").isPresent()) ocr = Ocr.english();
                        text = Pdf.text(f, ocr);
                        source = "PDF";
                    } else {
                        text = Layout.text(Files.readString(f, StandardCharsets.UTF_8));
                        source = "Text";
                    }
                    out.add(new FileResult(f, source, text, BillingDocument.detect(text), ValueObject.tables(text),
                            ValueObject.scan(text), "", Duration.ofNanos(System.nanoTime() - start)));
                } catch (RuntimeException | IOException | LinkageError e) {
                    out.add(new FileResult(f, IMAGES.contains(ext(f)) ? "OCR" : PDFS.contains(ext(f)) ? "PDF" : "Text", "",
                            Optional.empty(), List.of(),
                            List.of(), String.valueOf(e.getMessage()), Duration.ofNanos(System.nanoTime() - start)));
                }
                progress.accept(i + 1, chosen.size());
            }
        } finally {
            if (ocr != null) ocr.close();
        }
        return out;
    }

    /** Text first, then a PDF, then an image: the cheapest faithful copy of a document wins. */
    private static int rank(Path p) {
        String e = ext(p);
        return TEXTS.contains(e) ? 0 : PDFS.contains(e) ? 1 : 2;
    }

    static String ext(Path p) {
        String n = p.getFileName().toString();
        int dot = n.lastIndexOf('.');
        return dot < 0 ? "" : n.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    static String stem(Path p) {
        String n = p.getFileName().toString();
        int dot = n.lastIndexOf('.');
        return dot < 0 ? n : n.substring(0, dot);
    }
}
