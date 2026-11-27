package io.joshuasalcedo.valueobject.file;

import java.util.Locale;
import io.joshuasalcedo.valueobject.core.ValueObject;
import io.joshuasalcedo.valueobject.core.ValueObjectException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;

/**
 * Value object representing an immutable PDF file.
 *
 * <p>Encapsulates the binary content and metadata of a PDF document. Equality is
 * determined by file content, not by name or creation timestamp — two {@code PDFFile}
 * instances holding identical bytes are considered the same value.
 *
 * <p>Instances are always validated at construction time: the content must be non-empty
 * and must begin with the PDF magic bytes ({@code %PDF}). The internal byte array is
 * defensively copied to guarantee immutability.
 *
 * <p>Example usage:
 * <pre>{@code
 * PDFFile invoice = PDFFile.fromPath(Path.of("/reports/invoice.pdf"));
 * PDFFile renamed = invoice.rename("invoice-2024.pdf");
 * response.setHeader("Content-Type", invoice.mimeType());
 * }</pre>
 *
 * @param fileName    the original file name, must end with {@code .pdf} (case-insensitive)
 * @param content     the raw PDF bytes; defensively copied on construction
 * @param sizeInBytes the number of bytes in {@code content}; always equal to {@code content.length}
 * @param createdAt   the instant this value object was created; defaults to {@link Instant#now()} if {@code null}
 */
public record PDFFile(
        String fileName,
        byte[] content,
        long sizeInBytes,
        Instant createdAt
) implements ValueObject {

    /**
     * Compact constructor that validates and defensively copies all fields.
     *
     * @throws ValueObjectException if {@code fileName} or {@code content} is {@code null}, if
     *                              {@code fileName} does not end with {@code .pdf}, if {@code content} is
     *                              empty, or if the bytes do not start with the PDF magic header
     *                              ({@code %PDF})
     */
    public PDFFile {
        if (fileName == null)
            throw new ValueObjectException("fileName must not be null");
        if (content == null)
            throw new ValueObjectException("content must not be null");
        if (!FileType.PDF.hasExtension(fileName))
            throw new ValueObjectException("File must have .pdf extension");
        if (content.length == 0)
            throw new ValueObjectException("PDF content must not be empty");
        if (!isValidPdf(content))
            throw new ValueObjectException("Content is not a valid PDF (missing %PDF header)");
        content = Arrays.copyOf(content, content.length);
        sizeInBytes = content.length;
        createdAt = createdAt != null ? createdAt : Instant.now();
    }

    // -------------------------------------------------------------------------
    // Factory Methods
    // -------------------------------------------------------------------------

    /**
     * Creates a {@code PDFFile} from a file name and raw byte array, using the current
     * instant as the creation timestamp.
     *
     * @param fileName the file name (must end with {@code .pdf})
     * @param content  the raw PDF bytes
     * @return a new {@code PDFFile} instance
     * @throws ValueObjectException if validation fails (see compact constructor)
     */
    public static PDFFile of(String fileName, byte[] content) {
        return new PDFFile(fileName, content, 0, Instant.now());
    }

    /**
     * Creates a {@code PDFFile} by reading all bytes from the given {@link Path}.
     * The file name is derived from the last path component.
     *
     * @param path the path to the PDF file on disk
     * @return a new {@code PDFFile} wrapping the file's contents
     * @throws IOException          if the file cannot be read
     * @throws ValueObjectException if {@code path} is {@code null} or the file fails PDF validation
     */
    public static PDFFile fromPath(Path path) throws IOException {
        if (path == null || path.getFileName() == null)
            throw new ValueObjectException("A path to a PDF file is required");
        byte[] bytes = Files.readAllBytes(path);
        return new PDFFile(path.getFileName().toString(), bytes, bytes.length, Instant.now());
    }

    /**
     * Creates a {@code PDFFile} by decoding a Base64-encoded string.
     *
     * <p>Useful when receiving PDF data through JSON APIs or data URIs.
     *
     * @param fileName the file name to assign (must end with {@code .pdf})
     * @param base64   the Base64-encoded PDF content (standard encoding, no line breaks required)
     * @return a new {@code PDFFile} instance
     * @throws ValueObjectException if {@code base64} is {@code null} or not valid Base64, or the decoded bytes
     *                              fail PDF validation
     */
    public static PDFFile fromBase64(String fileName, String base64) {
        if (base64 == null)
            throw new ValueObjectException("Base64 content must not be null");
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64.trim());
        } catch (IllegalArgumentException e) {
            throw new ValueObjectException("Not valid Base64", e);
        }
        return new PDFFile(fileName, decoded, decoded.length, Instant.now());
    }

    // -------------------------------------------------------------------------
    // Convenient Methods
    // -------------------------------------------------------------------------

    /**
     * Returns the PDF content as a fresh {@link InputStream}.
     *
     * <p>A new stream is created on each invocation, so callers may independently
     * consume the stream without interfering with one another.
     *
     * @return a {@link ByteArrayInputStream} backed by the internal byte array
     */
    public InputStream toInputStream() {
        return new ByteArrayInputStream(content);
    }

    /**
     * Encodes the PDF content as a Base64 string using the standard encoding alphabet.
     *
     * @return a non-null Base64-encoded representation of the PDF bytes
     */
    public String toBase64() {
        return Base64.getEncoder().encodeToString(content);
    }

    /**
     * Returns a Base64 data URI suitable for embedding the PDF directly in HTML or CSS.
     *
     * <p>The returned string has the form:
     * <pre>{@code data:application/pdf;base64,<encoded-content>}</pre>
     *
     * @return a data URI string containing the full PDF content
     */
    public String toDataUri() {
        return "data:" + FileType.PDF.mimeType() + ";base64," + toBase64();
    }

    /**
     * Writes the PDF content to the specified destination path, creating or overwriting
     * the file as needed.
     *
     * @param destination the target {@link Path} to write to
     * @throws IOException if an I/O error occurs during the write
     */
    public void writeTo(Path destination) throws IOException {
        Files.write(destination, content);
    }

    /**
     * Returns a new {@code PDFFile} with the given file name, sharing the same
     * content, size, and creation timestamp as this instance.
     *
     * <p>This instance is unmodified; consistent with value object immutability.
     *
     * @param newFileName the replacement file name (must end with {@code .pdf})
     * @return a new {@code PDFFile} with the updated name
     * @throws ValueObjectException if {@code newFileName} is {@code null} or does not end with {@code .pdf}
     */
    public PDFFile rename(String newFileName) {
        return new PDFFile(newFileName, content, sizeInBytes, createdAt);
    }

    /**
     * Returns a human-readable representation of the file size, automatically
     * selecting the most appropriate unit (B, KB, or MB).
     *
     * <p>Examples: {@code "512 B"}, {@code "23.4 KB"}, {@code "1.75 MB"}.
     *
     * @return a formatted file size string
     */
    public String fileSizeFormatted() {
        if (sizeInBytes < 1024) return sizeInBytes + " B";
        if (sizeInBytes < 1024 * 1024) return String.format(Locale.ROOT, "%.1f KB", sizeInBytes / 1024.0);
        return String.format(Locale.ROOT, "%.2f MB", sizeInBytes / (1024.0 * 1024));
    }

    /**
     * Returns the MIME type for PDF content.
     *
     * <p>Useful when setting the {@code Content-Type} HTTP response header.
     *
     * @return the string {@code "application/pdf"}
     */
    public String mimeType() {
        return FileType.PDF.mimeType();
    }

    /**
     * Returns {@code true} if the content byte array is empty.
     *
     * <p>Note: under normal usage this will always return {@code false} because
     * the compact constructor rejects empty content. This method exists for
     * defensive checks in downstream code.
     *
     * @return {@code true} if {@code content.length == 0}
     */
    public boolean isEmpty() {
        return content.length == 0;
    }

    // -------------------------------------------------------------------------
    // Equality — content-based, not name/timestamp-based
    // -------------------------------------------------------------------------

    /**
     * Compares this {@code PDFFile} to another object for equality based solely on
     * the raw byte content.
     *
     * <p>Two {@code PDFFile} instances with identical bytes but different names or
     * timestamps are considered equal, consistent with value object semantics.
     *
     * @param o the object to compare
     * @return {@code true} if {@code o} is a {@code PDFFile} with the same content
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PDFFile other)) return false;
        return Arrays.equals(content, other.content);
    }

    /**
     * Returns a hash code derived solely from the byte content, consistent with
     * the content-based {@link #equals(Object)} implementation.
     *
     * @return the hash code of the content byte array
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(content);
    }

    // -------------------------------------------------------------------------
    // Private Helpers
    // -------------------------------------------------------------------------

    /**
     * Checks whether the given byte array begins with the PDF magic bytes ({@code %PDF},
     * i.e. {@code 0x25 0x50 0x44 0x46}).
     *
     * @param bytes the bytes to inspect
     * @return {@code true} if the array starts with the PDF signature
     */
    private static boolean isValidPdf(byte[] bytes) {
        return FileType.PDF.matches(bytes);
    }
}