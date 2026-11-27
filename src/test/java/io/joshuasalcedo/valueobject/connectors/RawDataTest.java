package io.joshuasalcedo.valueobject.connectors;

import io.joshuasalcedo.valueobject.core.ValueObject;
import io.joshuasalcedo.valueobject.financial.BillingDocument;
import io.joshuasalcedo.valueobject.financial.LineItem;
import io.joshuasalcedo.valueobject.format.Json;
import io.joshuasalcedo.valueobject.table.Table;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Whatever was read keeps what it was read from, so a caller can read it again when a part came out wrong. */
class RawDataTest {

    static final String INVOICE = """
            INVOICE
            Invoice No.  INV-77
            Lumen Design Studio

            Item                      Qty    Unit Price      Amount
            Logo design                 1      500.00 USD    500.00 USD
            Brand guide, 24 pages,
            print-ready                 1      750.00 USD    750.00 USD

            Total                                          1,250.00 USD
            """;

    @Test
    void aTableKeepsItsLines() {
        Table t = ValueObject.tables(INVOICE).getFirst();
        assertTrue(t.text().startsWith("Item "), t.text());
        assertTrue(t.text().contains("print-ready"), t.text());
        assertEquals(t.text(), Json.read(Json.of(t), Table.class).text(), "and it survives JSON");
    }

    @Test
    void aLineItemKeepsItsRowAsPrinted() {
        BillingDocument doc = BillingDocument.parse(INVOICE);
        // a line with no numbers between two rows could belong to either; the reader joins it to the row above,
        // and what was printed is kept so a caller can tell and read it its own way
        LineItem guide = doc.lineItems().get(1);
        assertEquals(Map.of("Item", "print-ready", "Qty", "1", "Unit Price", "750.00 USD", "Amount", "750.00 USD"),
                guide.printed());
        assertTrue(doc.tables().getFirst().text().contains("Brand guide, 24 pages,\nprint-ready"));
        assertEquals(INVOICE, doc.text(), "the whole input, as given");
        assertFalse(doc.sources().isEmpty(), "and how each field was read");
    }

    @Test
    void madeInCodeHasNoRawText() {
        LineItem li = LineItem.of("Fee", io.joshuasalcedo.valueobject.financial.Money.parse("1.00 USD"));
        assertTrue(li.printed().isEmpty());
    }
}
