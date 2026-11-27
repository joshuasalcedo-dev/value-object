package io.joshuasalcedo.valueobject.extract;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.tesseract.TessBaseAPI;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.bytedeco.leptonica.global.leptonica.pixDestroy;
import static org.bytedeco.leptonica.global.leptonica.pixRead;
import static org.bytedeco.leptonica.global.leptonica.pixReadMem;

/**
 * Tesseract OCR inside the JVM: no {@code tesseract} command, no Docker. The native libraries come with the
 * {@code org.bytedeco:tesseract-platform} dependency (optional in this library: add it to use this class).
 *
 * <pre>{@code
 * try (Ocr ocr = Ocr.english()) {
 *     String text = ocr.text(Path.of("receipt.png"));     // laid out as on the page
 *     Receipt r = Receipt.parse(text);
 *     List<Table> tables = ValueObject.tables(ocr.tsv(Path.of("statement.png")));   // words with their boxes
 * }
 * }</pre>
 *
 * <p>One instance is not for concurrent use: open one per thread. Language data ({@code eng.traineddata}) is found
 * in {@code TESSDATA_PREFIX}, then the usual install folders; or pass the folder to {@link #open(Path, String)}.
 *
 * @author JoshuaSalcedo
 */
public final class Ocr implements AutoCloseable {

    private final TessBaseAPI api;
    private boolean closed;

    private Ocr(TessBaseAPI api) {
        this.api = api;
    }

    /**
     * {@return OCR for English, with the language data found on this machine}
     *
     * @throws ValueObjectException if no {@code eng.traineddata} is found
     */
    public static Ocr english() {
        return open(findTessdata("eng").orElseThrow(() -> new ValueObjectException(
                "No eng.traineddata found: set TESSDATA_PREFIX to the folder that holds it")), "eng");
    }

    /**
     * Opens OCR for one or more languages, with the language data found on this machine.
     *
     * <pre>{@code
     * Ocr.of("eng", "fil");   // English and Filipino, for Philippine receipts
     * }</pre>
     *
     * @param languages Tesseract language codes, e.g. {@code "eng"}, {@code "fil"}
     * @return OCR for those languages together
     * @throws ValueObjectException if a folder holding every one of them is not found
     */
    public static Ocr of(String... languages) {
        if (languages == null || languages.length == 0) return english();
        Path folder = findTessdata(languages[0]).filter(dir -> java.util.Arrays.stream(languages)
                        .allMatch(l -> Files.isRegularFile(dir.resolve(l + ".traineddata"))))
                .orElseThrow(() -> new ValueObjectException("No folder holds the language data for "
                        + String.join(", ", languages) + ": set TESSDATA_PREFIX to the folder that holds them"));
        return open(folder, String.join("+", languages));
    }

    /**
     * Opens OCR with the language data in a given folder.
     *
     * @param tessdata the folder holding {@code <language>.traineddata}
     * @param language one or more languages, e.g. {@code "eng"} or {@code "eng+fil"}
     * @return OCR for those languages
     * @throws ValueObjectException if Tesseract cannot start with them
     */
    public static Ocr open(Path tessdata, String language) {
        TessBaseAPI api = new TessBaseAPI();
        if (api.Init(tessdata.toAbsolutePath().toString(), language) != 0) {
            api.close();
            throw new ValueObjectException("Tesseract could not load '" + language + "' from " + tessdata);
        }
        // keep the spaces between words, so columns stay apart in the text (as TableDetector reads them)
        api.SetVariable("preserve_interword_spaces", "1");
        api.SetPageSegMode(org.bytedeco.tesseract.global.tesseract.PSM_SINGLE_BLOCK);
        return new Ocr(api);
    }

    /**
     * {@return the folder that holds {@code <language>.traineddata}: TESSDATA_PREFIX (or its {@code tessdata}), then the usual Linux, macOS and Windows (scoop, installer) folders}
     *
     * @param language e.g. {@code "eng"}
     */
    public static Optional<Path> findTessdata(String language) {
        List<Path> candidates = new ArrayList<>();
        String prefix = System.getenv("TESSDATA_PREFIX");
        if (prefix != null && !prefix.isBlank()) {
            candidates.add(Path.of(prefix));
            candidates.add(Path.of(prefix, "tessdata"));
        }
        String home = System.getProperty("user.home");
        candidates.add(Path.of(home, "scoop", "apps", "tesseract-languages", "current"));
        candidates.add(Path.of(home, "scoop", "apps", "tesseract", "current", "tessdata"));
        candidates.add(Path.of("C:", "Program Files", "Tesseract-OCR", "tessdata"));
        candidates.add(Path.of("/usr/share/tesseract-ocr/5/tessdata"));
        candidates.add(Path.of("/usr/share/tesseract-ocr/4.00/tessdata"));
        candidates.add(Path.of("/usr/share/tessdata"));
        candidates.add(Path.of("/opt/homebrew/share/tessdata"));
        candidates.add(Path.of("/usr/local/share/tessdata"));
        return candidates.stream().filter(p -> Files.isRegularFile(p.resolve(language + ".traineddata"))).findFirst();
    }

    /**
     * {@return its text laid out as on the page: each word at the column of its position, so table columns line up and labels stay beside their values ({@link io.joshuasalcedo.valueobject.table.Layout})}
     *
     * @param image a PNG, JPEG, TIFF or BMP file
     */
    public synchronized String text(Path image) {
        return io.joshuasalcedo.valueobject.table.Layout.text(read(load(image), true));
    }

    /**
     * {@return its text laid out as on the page}
     *
     * @param image the bytes of a PNG, JPEG, TIFF or BMP image
     */
    public synchronized String text(byte[] image) {
        return io.joshuasalcedo.valueobject.table.Layout.text(read(load(image), true));
    }

    /**
     * {@return Tesseract's own text, as it spaces it}
     *
     * @param image a PNG, JPEG, TIFF or BMP file
     */
    public synchronized String rawText(Path image) {
        return read(load(image), false);
    }

    /**
     * {@return its words with their boxes, as Tesseract TSV (what {@code ValueObject.tables} reads best)}
     *
     * @param image a PNG, JPEG, TIFF or BMP file
     */
    public synchronized String tsv(Path image) {
        return read(load(image), true);
    }

    /**
     * {@return its words with their boxes, as Tesseract TSV}
     *
     * @param image the bytes of a PNG, JPEG, TIFF or BMP image
     */
    public synchronized String tsv(byte[] image) {
        return read(load(image), true);
    }

    private PIX load(Path image) {
        if (!Files.isRegularFile(image)) throw new ValueObjectException("No image at " + image);
        PIX pix = pixRead(image.toAbsolutePath().toString());
        if (pix == null) throw new ValueObjectException("Not a readable image: " + image);
        return pix;
    }

    private PIX load(byte[] image) {
        PIX pix = pixReadMem(image, image.length);
        if (pix == null) throw new ValueObjectException("Not a readable image (" + image.length + " bytes)");
        return pix;
    }

    private String read(PIX pix, boolean tsv) {
        if (closed) throw new IllegalStateException("Ocr is closed");
        try {
            api.SetImage(pix);
            @Nullable BytePointer out = tsv ? api.GetTSVText(1) : api.GetUTF8Text();
            if (out == null) return "";
            try {
                String text = out.getString(java.nio.charset.StandardCharsets.UTF_8);
                return tsv ? TSV_HEADER + text : text;
            } finally {
                out.deallocate();
            }
        } finally {
            api.Clear();
            pixDestroy(pix);
        }
    }

    /** The header line the tesseract command writes and the API leaves out. */
    private static final String TSV_HEADER =
            "level\tpage_num\tblock_num\tpar_num\tline_num\tword_num\tleft\ttop\twidth\theight\tconf\ttext\n";

    @Override
    public synchronized void close() {
        if (!closed) {
            closed = true;
            api.End();
            api.close();
        }
    }
}
