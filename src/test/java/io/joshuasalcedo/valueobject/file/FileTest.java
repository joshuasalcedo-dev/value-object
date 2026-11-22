package io.joshuasalcedo.valueobject.file;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class FileTest {

    private static final byte[] PDF = "%PDF-1.7\n%âãÏÓ\n1 0 obj\n<<>>\nendobj\n%%EOF".getBytes(StandardCharsets.ISO_8859_1);

    // ---------------- FileType

    @Test
    void looksUpByExtensionFileNameAndMimeType() {
        assertEquals(FileType.PDF, FileType.fromFileName("invoice.PDF").orElseThrow());
        assertEquals(FileType.JPEG, FileType.fromExtension(" .JPEG ").orElseThrow());
        assertEquals(FileType.PNG, FileType.fromMimeType("IMAGE/PNG; charset=binary").orElseThrow());
        assertEquals(FileType.PNG, FileType.parse("image/png"));
        assertEquals(FileType.PDF, FileType.parse("a.pdf"));
        assertEquals(FileType.MARKDOWN, FileType.parse("md"));
        assertTrue(FileType.fromFileName("README").isEmpty());
        assertTrue(FileType.fromExtension(null).isEmpty());
        assertEquals("jpg", FileType.JPEG.extension());
        assertEquals(FileType.Category.IMAGE, FileType.PNG.category());
        assertTrue(FileType.PDF.hasExtension("x.pdf"));
        assertFalse(FileType.PDF.hasExtension("x.pdf.exe"));
        assertTrue(FileType.UNKNOWN.isUnknown());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "nope", "x.nope", "application/x-unknown"})
    void fileTypeRejectsUnknown(String text) {
        assertThrows(ValueObjectException.class, () -> FileType.parse(text));
        assertTrue(FileType.tryParse(text).isEmpty());
        assertFalse(FileType.isValid(text));
    }

    @Test
    void detectsMagicBytes() {
        assertEquals(FileType.PDF, FileType.detect(PDF));
        assertEquals(FileType.PNG, FileType.detect(new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0}));
        assertEquals(FileType.ZIP, FileType.detect(new byte[]{'P', 'K', 3, 4}));
        assertEquals(FileType.UNKNOWN, FileType.detect(new byte[]{1, 2, 3}));
        assertEquals(FileType.UNKNOWN, FileType.detect(null));
        assertTrue(FileType.PDF.hasMagicBytes());
        assertFalse(FileType.DOCX.hasMagicBytes());
        assertFalse(FileType.DOCX.matches(PDF));
    }

    // ---------------- PDFFile

    @Test
    void pdfFileHoldsACopy() throws IOException {
        byte[] bytes = PDF.clone();
        PDFFile f = PDFFile.of("invoice.pdf", bytes);
        bytes[0] = 'X';
        assertEquals('%', f.content()[0], "copied in");
        assertEquals(PDF.length, f.sizeInBytes());
        assertEquals("application/pdf", f.mimeType());
        assertFalse(f.isEmpty());
        assertNotNull(f.createdAt());
        assertEquals(f, PDFFile.fromBase64("other.pdf", f.toBase64()), "equality by content");
        assertTrue(f.toDataUri().startsWith("data:application/pdf;base64,"));
        assertEquals("renamed.pdf", f.rename("renamed.pdf").fileName());
        assertEquals(PDF.length + " B", f.fileSizeFormatted());
        assertArrayEquals(PDF, f.toInputStream().readAllBytes());
    }

    @Test
    void pdfFileSizeIsFormattedWithADot() {
        Locale before = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            byte[] big = new byte[1536];
            System.arraycopy(PDF, 0, big, 0, 4);
            assertEquals("1.5 KB", PDFFile.of("big.pdf", big).fileSizeFormatted());
        } finally {
            Locale.setDefault(before);
        }
    }

    @Test
    void pdfFileBadInputIsValueObjectException() {
        assertThrows(ValueObjectException.class, () -> PDFFile.of(null, PDF));
        assertThrows(ValueObjectException.class, () -> PDFFile.of("a.pdf", null));
        assertThrows(ValueObjectException.class, () -> PDFFile.of("a.txt", PDF));
        assertThrows(ValueObjectException.class, () -> PDFFile.of("a.pdf", new byte[0]));
        assertThrows(ValueObjectException.class, () -> PDFFile.of("a.pdf", "hello".getBytes()));
        assertThrows(ValueObjectException.class, () -> PDFFile.fromBase64("a.pdf", "!!!not base64"));
        assertThrows(ValueObjectException.class, () -> PDFFile.fromBase64("a.pdf", null));
        assertThrows(ValueObjectException.class, () -> PDFFile.of("a.pdf", PDF).rename("a.doc"));
        assertThrows(ValueObjectException.class, () -> new PDFFile("a.pdf", PDF, 0, Instant.EPOCH).rename(null));
        assertThrows(ValueObjectException.class, () -> PDFFile.fromPath(null));
    }

    @Test
    void pdfFileRoundTripsThroughDisk(@TempDir Path dir) throws IOException {
        Path p = dir.resolve("in.pdf");
        Files.write(p, PDF);
        PDFFile f = PDFFile.fromPath(p);
        assertEquals("in.pdf", f.fileName());
        Path out = dir.resolve("out.pdf");
        f.writeTo(out);
        assertArrayEquals(PDF, Files.readAllBytes(out));
        assertEquals(Instant.EPOCH, new PDFFile("a.pdf", PDF, 0, Instant.EPOCH).createdAt());
        assertEquals(PDF.length, new PDFFile("a.pdf", PDF, 999, null).sizeInBytes());
    }
}
