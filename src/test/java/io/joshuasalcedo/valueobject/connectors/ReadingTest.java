package io.joshuasalcedo.valueobject.connectors;

import io.joshuasalcedo.valueobject.core.Found;
import io.joshuasalcedo.valueobject.core.ValueObject;
import io.joshuasalcedo.valueobject.contact.Email;
import io.joshuasalcedo.valueobject.contact.Party;
import io.joshuasalcedo.valueobject.core.ValueObjectException;
import io.joshuasalcedo.valueobject.financial.BillingDocument;
import io.joshuasalcedo.valueobject.financial.Currency;
import io.joshuasalcedo.valueobject.financial.FiatCurrency;
import io.joshuasalcedo.valueobject.financial.Invoice;
import io.joshuasalcedo.valueobject.financial.LineItem;
import io.joshuasalcedo.valueobject.financial.Money;
import io.joshuasalcedo.valueobject.financial.Receipt;
import io.joshuasalcedo.valueobject.format.Csv;
import io.joshuasalcedo.valueobject.format.Json;
import io.joshuasalcedo.valueobject.measurement.Quantity;
import io.joshuasalcedo.valueobject.measurement.UnitOfMeasure;
import io.joshuasalcedo.valueobject.extract.Ocr;
import io.joshuasalcedo.valueobject.extract.Pdf;
import io.joshuasalcedo.valueobject.table.Table;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Reading back what the library writes (JSON, CSV), PDFs, and the newer reading rules. */
class ReadingTest {

    static final LineItem LINE = new LineItem("Bolts", Optional.of(Quantity.of(10, UnitOfMeasure.PIECE)),
            Optional.of(Money.of("0.25", FiatCurrency.USD)), Optional.of(Money.of("2.50", FiatCurrency.USD)), Map.of());

    @Nested
    class JsonAndCsv {

        @Test
        void jsonRoundTrips() {
            Email e = Email.of("jane@example.com");
            assertEquals(e, Json.read(Json.of(e), Email.class));
            assertEquals(LINE, Json.read(Json.of(LINE), LineItem.class));
            Party p = new Party("Northwind Cloud", Optional.empty(), Optional.of(e), Optional.empty(), Optional.empty(),
                    Optional.empty(), List.of("Northwind Cloud"));
            assertEquals(p, Json.read(Json.pretty(p), Party.class));
            List<Found> found = ValueObject.scan("mail jane@example.com, pay USD 42.50");
            assertEquals(found, Json.readList(Json.of(found), Found.class), "a found value's type names what to read");
        }

        @Test
        void jsonParsesStrictly() {
            assertEquals(Map.of("a", List.of(new BigDecimal("1"), "x", true)), Json.parse("{\"a\": [1, \"x\", true]}"));
            assertEquals("é\n\"", Json.parse("\"\\u00e9\\n\\\"\""));
            assertThrows(ValueObjectException.class, () -> Json.parse("{\"a\": 1,}"));
            assertThrows(ValueObjectException.class, () -> Json.parse("[1] 2"));
            assertThrows(ValueObjectException.class, () -> Json.read("\"nope\"", Email.class));
        }

        @Test
        void csvRoundTripsRecords() {
            String csv = Csv.of(List.of(LINE));
            assertEquals(List.of(LINE), Csv.read(csv, LineItem.class));
        }

        @Test
        void csvReadsQuotedCellsAndTypesTables() {
            List<Map<String, String>> rows = Csv.read("name,note\r\n\"Bob, Jr.\",\"said \"\"hi\"\"\nthen left\"\r\n");
            assertEquals("Bob, Jr.", rows.getFirst().get("name"));
            assertEquals("said \"hi\"\nthen left", rows.getFirst().get("note"));
            Table t = Csv.readTable("Name,Email,Price\nJane,jane@example.com,USD 1.50\nBob,bob@example.org,USD 2.00\n");
            assertEquals(Optional.of(ValueObject.Type.EMAIL), t.columns().get(1).type());
            assertEquals(Optional.of(ValueObject.Type.MONEY), t.columns().get(2).type());
            assertThrows(ValueObjectException.class, () -> Csv.read("a\n\"open"));
        }
    }

    @Nested
    class Pdfs {

        static Path sample(String name) throws Exception {
            return Path.of(ReadingTest.class.getResource("/samples/" + name).toURI());
        }

        @Test
        void readsTheTextLayerLaidOut() throws Exception {
            Invoice inv = assertInstanceOf(Invoice.class, BillingDocument.parse(Pdf.text(sample("invoice-contractor.pdf"))));
            assertEquals("INV-2026-03", inv.number().orElseThrow());
            assertEquals(Money.of("1250.00", FiatCurrency.USD), inv.total().orElseThrow());
            assertEquals("Maria Santos Dizon", inv.issuer().orElseThrow().name());
        }

        @Test
        void aScanHasNoTextLayerButReadsWithOcr() throws Exception {
            assertTrue(Pdf.text(sample("receipt-store-scan.pdf")).isBlank());
            assumeTrue(Ocr.findTessdata("eng").isPresent(), "no OCR language data on this machine");
            try (Ocr ocr = Ocr.english()) {
                Receipt r = assertInstanceOf(Receipt.class, BillingDocument.parse(Pdf.text(sample("receipt-store-scan.pdf"), ocr)));
                assertEquals(Money.of("110.00", FiatCurrency.PHP), r.change().orElseThrow());
            }
        }

        @Test
        void notAPdf() throws Exception {
            assertThrows(ValueObjectException.class, () -> Pdf.text(sample("receipt-store.txt")));
            assertThrows(ValueObjectException.class, () -> Pdf.text(Path.of("no-such-file.pdf")));
        }
    }

    @Nested
    class Rules {

        @Test
        void rowsCentredOnTheirNumbers() {
            Table t = ValueObject.tables("""
                    No.   Description                 Qty    Amount
                          Al Wakra Station
                    45    Lift envelope glass           1    4,495.50
                          Al Sadd Station
                    46    Vitrine glass                 3    2,469.75
                          Katara Station
                    47    Washroom glass                1    1,200.00
                    """).getFirst();
            assertEquals(3, t.height(), t::toMarkdown);
            assertEquals("Al Wakra Station Lift envelope glass", t.cell(0, 1));
            assertEquals("Al Sadd Station Vitrine glass", t.cell(1, 1));
            assertEquals("Katara Station Washroom glass", t.cell(2, 1));
        }

        @Test
        void dollarsInACountryWithoutThemAreUsDollars() {
            Receipt r = assertInstanceOf(Receipt.class,
                    BillingDocument.parse("Receipt\nManila, Philippines\nReceipt No. 99\nTotal   $34.32\nAmount paid  $34.32\n"));
            assertEquals(FiatCurrency.USD, r.currency().orElseThrow());
        }

        @Test
        void dollarsInACountryWithThemAreItsDollars() {
            Receipt r = assertInstanceOf(Receipt.class,
                    BillingDocument.parse("Receipt\nSydney NSW 2000, Australia\nReceipt No. 7\nTotal   $20.00\n"));
            assertEquals("AUD", r.currency().map(Currency::code).orElseThrow());
        }

        @Test
        void filipinoReceipt() {
            Receipt r = assertInstanceOf(Receipt.class, BillingDocument.parse("""
                      TINDAHAN NI ALING NENA
                   12 Rizal St., Lungsod ng Davao
                        Philippines
                  OPISYAL NA RESIBO Blg. 004512

              Petsa:  2026-04-02
              Mamimili:  Juan dela Cruz

              Item                   Dami      Halaga
              Bigas, 5 kg               1      320.00
              Mantika                   2      230.00

              Kabuuan                          550.00
              Ibinayad                       1,000.00
              Sukli                            450.00

                     Salamat po!
              """));
            assertEquals("004512", r.number().orElseThrow());
            assertEquals(LocalDate.of(2026, 4, 2), r.date().orElseThrow());
            assertEquals(Money.of("550.00", FiatCurrency.PHP), r.total().orElseThrow());
            assertEquals(Money.of("450.00", FiatCurrency.PHP), r.change().orElseThrow());
            assertEquals("Juan dela Cruz", r.customer().orElseThrow().name());
            assertEquals(2, r.lineItems().size());
        }
    }
}
