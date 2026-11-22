package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.contact.FullName;
import io.joshuasalcedo.valueobject.geo.Country;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reads the sample documents in src/test/resources/samples: made-up bills, invoices and receipts (fictional data,
 * real layouts), rendered from codegen/samples/*.html and read back with OCR, so the text has genuine OCR noise.
 */
class BillingDocumentTest {

    static String sample(String name) throws IOException {
        try (InputStream in = BillingDocumentTest.class.getResourceAsStream("/samples/" + name + ".txt")) {
            assertNotNull(in, "missing sample " + name + ": run codegen/samples.py and codegen/OcrSamples.java");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static Money php(String amount) {
        return Money.of(amount, FiatCurrency.PHP);
    }

    static Money usd(String amount) {
        return Money.of(amount, FiatCurrency.USD);
    }

    @Test
    void cloudReceipt() throws IOException {
        Receipt r = assertInstanceOf(Receipt.class, BillingDocument.parse(sample("receipt-cloud")));
        assertEquals("4418-6072", r.number().orElseThrow(), "a receipt's own number wins over its invoice number");
        assertEquals("7F3D21C8-0012", r.metadata().get("Invoice number"));
        assertEquals(LocalDate.of(2026, 1, 15), r.paidOn().orElseThrow());
        assertEquals(FiatCurrency.USD, r.currency().orElseThrow());
        assertEquals("Northwind Cloud", r.issuer().orElseThrow().name());
        assertEquals(Country.UNITED_STATES, r.issuer().orElseThrow().address().orElseThrow().country());
        assertEquals("billing@northwind.example", r.issuer().orElseThrow().email().orElseThrow().value());
        assertEquals("Kalayaan Digital", r.customer().orElseThrow().name());

        assertEquals(3, r.lineItems().size(), r.lineItems()::toString);
        LineItem compute = r.lineItems().getFirst();
        assertTrue(compute.description().startsWith("Compute (per vCPU / hour)"), compute.description());
        assertTrue(compute.description().contains("Jan 14, 2026"), "the wrapped date line joins its item");
        assertEquals(io.joshuasalcedo.valueobject.measurement.Quantity.of(120, io.joshuasalcedo.valueobject.measurement.UnitOfMeasure.ONE), compute.quantity().orElseThrow());
        assertEquals(usd("0.25"), compute.unitPrice().orElseThrow());
        assertEquals(usd("30.00"), compute.amount().orElseThrow());

        assertEquals(usd("42.50"), r.subtotal().orElseThrow());
        assertEquals(usd("42.50"), r.total().orElseThrow());
        assertEquals(usd("42.50"), r.amountPaid().orElseThrow());
    }

    @Test
    void contractorInvoice() throws IOException {
        Invoice inv = assertInstanceOf(Invoice.class, BillingDocument.parse(sample("invoice-contractor")));
        assertEquals("INV-2026-03", inv.number().orElseThrow());
        assertEquals(LocalDate.of(2026, 1, 5), inv.date().orElseThrow());
        assertEquals(LocalDate.of(2026, 2, 5), inv.dueDate().orElseThrow());
        assertEquals(usd("1250.00"), inv.total().orElseThrow());
        assertEquals(usd("0"), inv.tax().orElseThrow());

        var from = inv.issuer().orElseThrow();
        assertEquals("Maria Santos Dizon", from.name());
        assertEquals(Country.PHILIPPINES, from.address().orElseThrow().country());
        assertEquals("9000", from.address().orElseThrow().postalCode());
        assertEquals(new FullName("Maria", "Santos", "Dizon"), from.person().orElseThrow());
        assertTrue(from.phone().isPresent());

        var to = inv.customer().orElseThrow();
        assertEquals("Brightpath Media LLC", to.name());
        assertFalse(to.isPerson(), "an LLC is not a person");
        assertEquals(Country.UNITED_STATES, to.address().orElseThrow().country());
        assertEquals("555012345", to.taxId().orElseThrow().value());

        assertEquals(1, inv.lineItems().size(), inv.lineItems()::toString);
        assertEquals(usd("1250.00"), inv.lineItems().getFirst().amount().orElseThrow());
        assertEquals(BillingDocument.Form.BLOCK, inv.sources().get("issuer").form());
        assertEquals("Bill from", inv.sources().get("issuer").label());
    }

    @Test
    void telcoBill() throws IOException {
        Bill bill = assertInstanceOf(Bill.class, BillingDocument.parse(sample("bill-telco")));
        assertEquals("8812-4407", bill.accountNumber().orElseThrow());
        assertEquals(LocalDate.of(2026, 1, 26), bill.date().orElseThrow(), "01/26/26 is 2026");
        assertEquals(LocalDate.of(2026, 2, 12), bill.dueDate().orElseThrow());
        assertEquals(LocalDate.of(2025, 12, 27), bill.billingPeriod().orElseThrow().startDate());
        assertEquals(LocalDate.of(2026, 1, 26), bill.billingPeriod().orElseThrow().endDate());
        assertEquals(php("1799.00"), bill.total().orElseThrow());

        assertTrue(bill.issuer().orElseThrow().name().startsWith("Skyline Telecom"));
        var holder = bill.customer().orElseThrow();
        assertEquals(new FullName("Andrea", "Lim", "Bautista"), holder.person().orElseThrow(), "the title Ms. is dropped");
        assertEquals(Country.PHILIPPINES, holder.address().orElseThrow().country(), "Cagayan de Oro is in the Philippines");

        assertEquals(BillingDocument.Form.LABEL_ABOVE, bill.sources().get("dueDate").form(), "Due Date sits over its value");
    }

    @Test
    void billsPayReceipt() throws IOException {
        Receipt r = assertInstanceOf(Receipt.class, BillingDocument.parse(sample("receipt-billspay")));
        assertEquals("8820471163", r.reference().orElseThrow(), "\"Paylo Ref. No.\" is a reference number");
        assertEquals(LocalDate.of(2026, 2, 19), r.date().orElseThrow());
        assertEquals(php("5025.00"), r.total().orElseThrow());
        assertEquals("MINDANAO WATER DISTRICT", r.issuer().orElseThrow().name());
        assertEquals(php("25.00"), r.metadata().get("Fee"));
    }

    @Test
    void transferReceipt() throws IOException {
        Receipt r = assertInstanceOf(Receipt.class, BillingDocument.parse(sample("receipt-transfer")),
                "\"You received ... Payment details\" is a receipt");
        assertEquals(Money.of("1480.00", FiatCurrency.AUD), r.amountPaid().orElseThrow());
        assertEquals("HARRIS DANIEL J", r.issuer().orElseThrow().name());
        assertEquals("#5520918736", r.metadata().get("Transfer Number"));
    }

    @Test
    void storeReceiptWithNoCurrencyTakesTheCountrys() throws IOException {
        Receipt r = assertInstanceOf(Receipt.class, BillingDocument.parse(sample("receipt-store")));
        assertEquals(FiatCurrency.PHP, r.currency().orElseThrow(), "no currency printed; Cebu City, Philippines -> PHP");
        assertEquals(BillingDocument.Form.INFERRED, r.sources().get("currency").form());
        assertEquals("000123", r.number().orElseThrow());
        assertEquals(LocalDate.of(2026, 3, 10), r.date().orElseThrow());
        assertTrue(r.issuer().orElseThrow().name().contains("BEAN & LEAF CAFE"));
        assertEquals(3, r.lineItems().size(), r.lineItems()::toString);
        assertEquals("Cafe Latte", r.lineItems().getFirst().description());
        assertEquals(php("260.00"), r.lineItems().getFirst().amount().orElseThrow());
        assertEquals(php("390.00"), r.subtotal().orElseThrow());
        assertEquals(php("41.79"), r.tax().orElseThrow());
        assertEquals(php("390.00"), r.total().orElseThrow());
        assertEquals(php("500.00"), r.amountPaid().orElseThrow());
        assertEquals(php("110.00"), r.change().orElseThrow());
        assertEquals("Mia", r.metadata().get("Cashier"));
    }

    @Test
    void notABillingDocument() {
        assertTrue(BillingDocument.detect("Just a note to say hello.").isEmpty());
        assertTrue(BillingDocument.detect(null).isEmpty());
    }

    @Test
    void everyFoundValueKeepsItsShape() throws IOException {
        var r = BillingDocument.parse(sample("invoice-contractor"));
        var phone = r.found().stream().filter(f -> f.type() == ValueObject.Type.PHONE_NUMBER).findFirst().orElseThrow();
        assertEquals("+63 917 555 0142", phone.text());
        assertEquals(ValueObject.Type.PHONE_NUMBER.shape(), phone.shape());
        assertTrue(phone.line(r.text()) > 0);
    }
}
