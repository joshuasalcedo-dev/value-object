package io.joshuasalcedo.valueobject.ocr;

import io.joshuasalcedo.valueobject.financial.BillingDocument;
import io.joshuasalcedo.valueobject.financial.FiatCurrency;
import io.joshuasalcedo.valueobject.financial.Money;
import io.joshuasalcedo.valueobject.financial.Receipt;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Image to document with OCR in the JVM. Skipped where no Tesseract language data is installed (CI). */
class OcrTest {

    @Test
    void readsAReceiptImage() throws Exception {
        assumeTrue(Ocr.findTessdata("eng").isPresent(), "no eng.traineddata on this machine");
        Path image = Path.of(getClass().getResource("/samples/receipt-store.png").toURI());
        try (Ocr ocr = Ocr.english()) {
            String text = ocr.text(image);
            assertTrue(text.contains("BEAN & LEAF CAFE"), text);
            Receipt r = assertInstanceOf(Receipt.class, BillingDocument.parse(text));
            assertEquals(Money.of("110.00", FiatCurrency.PHP), r.change().orElseThrow());

            String tsv = ocr.tsv(image);
            assertTrue(tsv.startsWith("level\tpage_num"), "TSV keeps the word boxes");
            assertEquals(r.total(), BillingDocument.parse(tsv).total(), "TSV in reads the same");
        }
    }
}
