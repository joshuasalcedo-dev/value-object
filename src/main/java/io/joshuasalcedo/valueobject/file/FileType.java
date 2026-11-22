package io.joshuasalcedo.valueobject.file;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.ValueObject;

import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A well-known file format: its extensions, MIME type, {@link Category} and, where it has one, the
 * magic bytes its content starts with.
 *
 * <p>Look one up by extension, file name, MIME type, or by sniffing the first bytes of the content:
 * <pre>{@code
 * FileType.fromFileName("invoice.PDF");      // PDF
 * FileType.fromMimeType("image/png");        // PNG
 * FileType.detect(bytes);                    // whatever the bytes say, or UNKNOWN
 * }</pre>
 *
 * @author JoshuaSalcedo
 */
public enum FileType implements ValueObject {

    // ── Documents ───────────────────────────────────────────────────────────
    /** application/pdf. */
    PDF("application/pdf", Category.DOCUMENT, bytes("%PDF"), "pdf"),
    /** application/msword. */
    DOC("application/msword", Category.DOCUMENT, hex("D0CF11E0A1B11AE1"), "doc"),
    /** application/vnd.openxmlformats-officedocument.wordprocessingml.document. */
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", Category.DOCUMENT, null, "docx"),
    /** application/vnd.ms-excel. */
    XLS("application/vnd.ms-excel", Category.SPREADSHEET, hex("D0CF11E0A1B11AE1"), "xls"),
    /** application/vnd.openxmlformats-officedocument.spreadsheetml.sheet. */
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Category.SPREADSHEET, null, "xlsx"),
    /** application/vnd.ms-powerpoint. */
    PPT("application/vnd.ms-powerpoint", Category.PRESENTATION, hex("D0CF11E0A1B11AE1"), "ppt"),
    /** application/vnd.openxmlformats-officedocument.presentationml.presentation. */
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", Category.PRESENTATION, null, "pptx"),
    /** application/vnd.oasis.opendocument.text. */
    ODT("application/vnd.oasis.opendocument.text", Category.DOCUMENT, null, "odt"),
    /** application/rtf. */
    RTF("application/rtf", Category.DOCUMENT, bytes("{\\rtf"), "rtf"),

    // ── Text / data ─────────────────────────────────────────────────────────
    /** text/plain. */
    TXT("text/plain", Category.TEXT, null, "txt", "text", "log"),
    /** text/csv. */
    CSV("text/csv", Category.SPREADSHEET, null, "csv"),
    /** text/markdown. */
    MARKDOWN("text/markdown", Category.TEXT, null, "md", "markdown"),
    /** text/html. */
    HTML("text/html", Category.TEXT, null, "html", "htm"),
    /** application/json. */
    JSON("application/json", Category.DATA, null, "json"),
    /** application/xml. */
    XML("application/xml", Category.DATA, bytes("<?xml"), "xml"),
    /** application/yaml. */
    YAML("application/yaml", Category.DATA, null, "yaml", "yml"),

    // ── Images ──────────────────────────────────────────────────────────────
    /** image/png. */
    PNG("image/png", Category.IMAGE, hex("89504E470D0A1A0A"), "png"),
    /** image/jpeg. */
    JPEG("image/jpeg", Category.IMAGE, hex("FFD8FF"), "jpg", "jpeg"),
    /** image/gif. */
    GIF("image/gif", Category.IMAGE, bytes("GIF8"), "gif"),
    /** image/webp. */
    WEBP("image/webp", Category.IMAGE, null, "webp"),
    /** image/bmp. */
    BMP("image/bmp", Category.IMAGE, bytes("BM"), "bmp"),
    /** image/tiff. */
    TIFF("image/tiff", Category.IMAGE, hex("49492A00"), "tif", "tiff"),
    /** image/svg+xml. */
    SVG("image/svg+xml", Category.IMAGE, null, "svg"),
    /** image/vnd.microsoft.icon. */
    ICO("image/vnd.microsoft.icon", Category.IMAGE, hex("00000100"), "ico"),
    /** image/heic. */
    HEIC("image/heic", Category.IMAGE, null, "heic"),

    // ── Audio ───────────────────────────────────────────────────────────────
    /** audio/mpeg. */
    MP3("audio/mpeg", Category.AUDIO, bytes("ID3"), "mp3"),
    /** audio/wav. */
    WAV("audio/wav", Category.AUDIO, null, "wav"),
    /** audio/ogg. */
    OGG("audio/ogg", Category.AUDIO, bytes("OggS"), "ogg", "oga"),
    /** audio/flac. */
    FLAC("audio/flac", Category.AUDIO, bytes("fLaC"), "flac"),
    /** audio/aac. */
    AAC("audio/aac", Category.AUDIO, null, "aac"),
    /** audio/mp4. */
    M4A("audio/mp4", Category.AUDIO, null, "m4a"),

    // ── Video ───────────────────────────────────────────────────────────────
    /** video/mp4. */
    MP4("video/mp4", Category.VIDEO, null, "mp4", "m4v"),
    /** video/quicktime. */
    MOV("video/quicktime", Category.VIDEO, null, "mov"),
    /** video/webm. */
    WEBM("video/webm", Category.VIDEO, hex("1A45DFA3"), "webm"),
    /** video/x-matroska. */
    MKV("video/x-matroska", Category.VIDEO, hex("1A45DFA3"), "mkv"),
    /** video/x-msvideo. */
    AVI("video/x-msvideo", Category.VIDEO, null, "avi"),

    // ── Archives ────────────────────────────────────────────────────────────
    /** application/zip. */
    ZIP("application/zip", Category.ARCHIVE, hex("504B0304"), "zip"),
    /** application/gzip. */
    GZIP("application/gzip", Category.ARCHIVE, hex("1F8B"), "gz"),
    /** application/x-tar. */
    TAR("application/x-tar", Category.ARCHIVE, null, "tar"),
    /** application/x-7z-compressed. */
    SEVEN_ZIP("application/x-7z-compressed", Category.ARCHIVE, hex("377ABCAF271C"), "7z"),
    /** application/vnd.rar. */
    RAR("application/vnd.rar", Category.ARCHIVE, bytes("Rar!"), "rar"),

    // ── Unknown / Default ───────────────────────────────────────────────────
    /** Any other content: application/octet-stream. */
    UNKNOWN("application/octet-stream", Category.OTHER, null, "bin");

    private static final Map<String, FileType> BY_EXTENSION = Arrays.stream(values())
            .flatMap(t -> t.extensions.stream().map(e -> Map.entry(e, t)))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, (first, second) -> first));

    private static final Map<String, FileType> BY_MIME_TYPE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(FileType::mimeType, Function.identity(), (first, second) -> first));

    private final String mimeType;
    private final Category category;
    private final byte @Nullable [] magic;
    private final List<String> extensions;

    FileType(String mimeType, Category category, byte @Nullable [] magic, String... extensions) {
        this.mimeType = mimeType;
        this.category = category;
        this.magic = magic;
        this.extensions = List.of(extensions);
    }

    // ── Lookups ─────────────────────────────────────────────────────────────

    /**
     * Looks a type up by MIME type, then by file name, then by extension; never throws.
     *
     * @param text a MIME type ({@code "image/png"}), a file name ({@code "a.pdf"}) or an extension ({@code "pdf"})
     * @return the type it names, or empty
     */
    public static Optional<FileType> tryParse(@Nullable String text) {
        if (text == null || text.isBlank()) return Optional.empty();
        return fromMimeType(text).or(() -> fromFileName(text)).or(() -> fromExtension(text));
    }

    /**
     * Looks a type up by extension, ignoring case, surrounding whitespace and a leading dot.
     *
     * @param extension e.g. {@code "pdf"}, {@code ".PDF"}
     * @return the type with that extension, or empty
     */
    public static Optional<FileType> fromExtension(@Nullable String extension) {
        if (extension == null) return Optional.empty();
        String e = extension.trim().toLowerCase(Locale.ROOT);
        if (e.startsWith(".")) e = e.substring(1);
        return Optional.ofNullable(BY_EXTENSION.get(e));
    }

    /**
     * Looks a type up by the extension after the last dot of a file name, ignoring case.
     *
     * @param fileName e.g. {@code "invoice.pdf"}
     * @return the type its extension names, or empty when it has none or an unknown one
     */
    public static Optional<FileType> fromFileName(@Nullable String fileName) {
        if (fileName == null) return Optional.empty();
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? Optional.empty() : fromExtension(fileName.substring(dot + 1));
    }

    /**
     * Looks a type up by MIME type, ignoring case.
     *
     * @param mimeType e.g. {@code "image/png"}; parameters such as {@code ;charset=utf-8} are ignored
     * @return the type with that MIME type, or empty
     */
    public static Optional<FileType> fromMimeType(@Nullable String mimeType) {
        if (mimeType == null) return Optional.empty();
        String m = mimeType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        return Optional.ofNullable(BY_MIME_TYPE.get(m));
    }

    /**
     * Sniffs the content's first bytes. Container formats are reported as the first type with their header:
     * DOCX, XLSX, PPTX and ODT come back as {@link #ZIP}, DOC, XLS and PPT as {@link #DOC}, WEBM and MKV as
     * {@link #WEBM}; so prefer the file name when you have it.
     *
     * @param content the file's bytes, or at least its first few
     * @return the type its magic bytes match, or {@link #UNKNOWN}
     */
    public static FileType detect(byte @Nullable [] content) {
        if (content == null) return UNKNOWN;
        for (FileType t : values()) {
            if (t.matches(content)) return t;
        }
        return UNKNOWN;
    }

    // ── Accessors ───────────────────────────────────────────────────────────

    /** {@return the MIME type, e.g. {@code "application/pdf"}} */
    public String mimeType() {
        return mimeType;
    }

    /** {@return what kind of content it holds} */
    public Category category() {
        return category;
    }

    /** {@return the extensions, lower case without the dot, the usual one first} */
    public List<String> extensions() {
        return extensions;
    }

    /** {@return the usual extension, e.g. {@code "jpg"}} */
    public String extension() {
        return extensions.getFirst();
    }

    /** {@return whether its content can be recognised by its first bytes} */
    public boolean hasMagicBytes() {
        return magic != null;
    }

    /**
     * Checks a file's content against this type's magic bytes.
     *
     * @param content a file's bytes
     * @return whether they start with this type's magic bytes; always false for a type without them
     */
    public boolean matches(byte @Nullable [] content) {
        if (magic == null || content == null || content.length < magic.length) return false;
        return Arrays.equals(content, 0, magic.length, magic, 0, magic.length);
    }

    /**
     * Checks a file name's extension against this type's, ignoring case.
     *
     * @param fileName e.g. {@code "invoice.pdf"}
     * @return whether its extension is one of this type's
     */
    public boolean hasExtension(@Nullable String fileName) {
        return fromFileName(fileName).filter(t -> t == this).isPresent();
    }

    /** {@return whether this is {@link #UNKNOWN}} */
    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private static byte[] bytes(String ascii) {
        return ascii.getBytes(StandardCharsets.US_ASCII);
    }

    private static byte[] hex(String hex) {
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return out;
    }

    /** What kind of content a {@link FileType} holds. */
    public enum Category {
        /** Document. */
        DOCUMENT,
        /** Spreadsheet. */
        SPREADSHEET,
        /** Presentation. */
        PRESENTATION,
        /** Text. */
        TEXT,
        /** Data. */
        DATA,
        /** Image. */
        IMAGE,
        /** Audio. */
        AUDIO,
        /** Video. */
        VIDEO,
        /** Archive. */
        ARCHIVE,
        /** Other. */
        OTHER
    }

    /**
     * Looks a type up by MIME type, file name or extension, as {@link #tryParse(String)} does.
     *
     * @param text a MIME type, file name or extension
     * @return the type it names
     * @throws ValueObjectException if it names no known type
     */
    public static FileType parse(String text) {
        return tryParse(text).orElseThrow(() -> new ValueObjectException("Not a FileType: '" + text + "'"));
    }

    /**
     * Tells whether a text names a known type, as {@link #tryParse(String)} reads it.
     *
     * @param text any text
     * @return whether it is a {@link FileType}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
