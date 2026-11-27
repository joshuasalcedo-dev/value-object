package io.joshuasalcedo.valueobject.format;

import io.joshuasalcedo.valueobject.core.ValueObject;
import io.joshuasalcedo.valueobject.contact.FullName;
import io.joshuasalcedo.valueobject.financial.BillingDocument;
import io.joshuasalcedo.valueobject.financial.Receipt;
import io.joshuasalcedo.valueobject.table.Table;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class FormatTest {

    @Test
    void scanAsJsonAndCsvKeepsTheShape() {
        var found = ValueObject.scan("Mail jane@example.com, pay USD 42.50");
        String json = Json.of(found);
        assertTrue(json.startsWith("[{\"type\":\"EMAIL\",\"value\":\"jane@example.com\",\"text\":\"jane@example.com\",\"start\":5"), json);
        assertTrue(json.contains("\"type\":\"MONEY\",\"value\":{\"amount\":42.50,\"currency\":\"USD\"}"), json);
        assertTrue(json.contains("\"shape\":"), json);

        String csv = Csv.of(found);
        assertTrue(csv.startsWith("type,value,text,start,end,shape\r\n"), csv);
        assertTrue(csv.contains("MONEY,42.50 USD,USD 42.50,"), csv);
    }

    @Test
    void tableAsCsvAndJson() {
        Table t = Table.parse("""
                Name      Email
                Jane      jane@example.com
                Bob, Jr.  bob@example.org
                """);
        assertEquals("Name,Email\r\nJane,jane@example.com\r\n\"Bob, Jr.\",bob@example.org\r\n", t.toCsv());
        assertTrue(t.toJson().contains("\"rows\":[[\"Jane\",\"jane@example.com\"]"), t.toJson());
    }

    @Test
    void documentAsJsonAndLineItemsAsCsv() throws Exception {
        String text;
        try (InputStream in = getClass().getResourceAsStream("/samples/receipt-store.txt")) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        Receipt r = Receipt.parse(text);
        String json = r.toJson();
        assertTrue(json.contains("\"kind\": \"Receipt\""), json);
        assertTrue(json.contains("\"currency\": \"PHP\""), json);
        assertTrue(json.contains("\"change\": {"), json);
        assertTrue(json.contains("\"sources\": {"), json);

        String csv = r.lineItemsCsv();
        assertTrue(csv.startsWith("description,quantity,unitPrice,amount,other,printed\r\n"), csv);
        assertTrue(csv.contains("Cafe Latte,2,,260.00 PHP,"), csv);
        assertNotNull(BillingDocument.detect(text).orElseThrow());
    }

    @Test
    void escapesJsonStrings() {
        assertEquals("\"a\\\"b\\\\c\\nd\"", Json.of("a\"b\\c\nd"));
        assertEquals("null", Json.of(null));
        assertEquals("{\"firstName\":\"Juan\",\"middleName\":null,\"lastName\":\"dela Cruz\"}",
                Json.of(FullName.parse("Juan dela Cruz")));
    }
}
