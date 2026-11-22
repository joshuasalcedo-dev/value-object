package io.joshuasalcedo.valueobject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.financial.FiatCurrency;
import io.joshuasalcedo.valueobject.financial.Invoice;
import io.joshuasalcedo.valueobject.financial.Money;
import io.joshuasalcedo.valueobject.financial.PaymentTerms;
import io.joshuasalcedo.valueobject.format.Json;
import io.joshuasalcedo.valueobject.measurement.Quantity;
import io.joshuasalcedo.valueobject.measurement.UnitOfMeasure;
import io.joshuasalcedo.valueobject.measurement.WeightUnit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UnitsAndTermsTest {

    @Nested
    class Units {

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({
                "pcs, PIECE, H87", "Nos, PIECE, H87", "ea, EACH, EA", "KGM, KILOGRAM, KGM", "kgs, KILOGRAM, KGM",
                "L, LITRE, LTR", "m², SQUARE_METRE, MTK", "sqm, SQUARE_METRE, MTK", "hrs, HOUR, HUR", "box, BOX, XBX",
                "kWh, KILOWATT_HOUR, KWH", "C62, ONE, C62", "dozen, DOZEN, DZN",
        })
        void readsCodesAndPrintedSpellings(String text, UnitOfMeasure unit, String rec20) {
            assertEquals(unit, UnitOfMeasure.parse(text));
            assertEquals(rec20, unit.code(), "UN/ECE Rec 20 code");
        }

        @Test
        void convertsWithinACategoryOnly() {
            assertEquals(new BigDecimal("2500"), UnitOfMeasure.KILOGRAM.convert(new BigDecimal("2.5"), UnitOfMeasure.GRAM));
            assertEquals(new BigDecimal("12"), UnitOfMeasure.DOZEN.convert(BigDecimal.ONE, UnitOfMeasure.PIECE));
            assertEquals(new BigDecimal("90"), UnitOfMeasure.HOUR.convert(new BigDecimal("1.5"), UnitOfMeasure.MINUTE));
            assertThrows(ValueObjectException.class, () -> UnitOfMeasure.KILOGRAM.convert(BigDecimal.ONE, UnitOfMeasure.LITRE));
            assertThrows(ValueObjectException.class, () -> UnitOfMeasure.MONTH.convert(BigDecimal.ONE, UnitOfMeasure.DAY),
                    "months have no fixed length");
            assertThrows(ValueObjectException.class, () -> UnitOfMeasure.BOX.convert(BigDecimal.ONE, UnitOfMeasure.PIECE),
                    "a box holds whatever it holds");
            assertEquals(WeightUnit.KILOGRAM, UnitOfMeasure.KILOGRAM.weightUnit().orElseThrow());
        }
    }

    @Nested
    class Quantities {

        @ParameterizedTest(name = "{0}")
        @CsvSource(delimiter = '|', value = {
                "10 pcs    | 10    | PIECE",
                "2.5kg     | 2.5   | KILOGRAM",
                "1,200 m   | 1200  | METRE",
                "Nos 3     | 3     | PIECE",
                "12        | 12    | ONE",
                "3 hrs     | 3     | HOUR",
        })
        void parsesPrintedQuantities(String text, BigDecimal amount, UnitOfMeasure unit) {
            Quantity q = Quantity.parse(text);
            assertEquals(0, amount.compareTo(q.amount()));
            assertEquals(unit, q.unit());
        }

        @Test
        void convertsAddsAndCompares() {
            assertEquals(Quantity.parse("2000 g"), Quantity.parse("2 kg").to(UnitOfMeasure.GRAM));
            assertEquals(Quantity.parse("2.5 kg"), Quantity.parse("2 kg").plus(Quantity.parse("500 g")));
            assertTrue(Quantity.parse("1 kg").compareTo(Quantity.parse("900 g")) > 0);
            assertThrows(ValueObjectException.class, () -> Quantity.parse("-1 kg"));
            assertEquals("{\"amount\":2.5,\"unit\":\"KGM\"}", Json.of(Quantity.parse("2.5 kg")), "JSON carries the Rec 20 code");
        }

        @Test
        void detectedInText() {
            var found = ValueObject.find("Ship 10 pcs and 2.5 kg of bolts, plus 3 hrs labour.").get(ValueObject.Type.QUANTITY);
            assertEquals(3, found.size(), String.valueOf(found));
        }
    }

    @Nested
    class Terms {

        static final LocalDate JAN_5 = LocalDate.of(2026, 1, 5);

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource(delimiter = '|', value = {
                "Net 30              | Net 30          | 2026-02-04",
                "N30                 | Net 30          | 2026-02-04",
                "30 days             | Net 30          | 2026-02-04",
                "EOM                 | EOM             | 2026-01-31",
                "Net 30 EOM          | Net 30 EOM      | 2026-03-02",
                "EOM + 15            | Net 15 EOM      | 2026-02-15",
                "10th MFI            | 10 MFI          | 2026-02-10",
                "2/10 Net 30         | 2/10 Net 30     | 2026-02-04",
                "2% 10 Net 30        | 2/10 Net 30     | 2026-02-04",
                "Due on receipt      | Due on receipt  | 2026-01-05",
                "Payment terms: CIA  | CIA             | 2026-01-05",
        })
        void readsTheNotationAndWorksOutTheDueDate(String text, String canonical, LocalDate due) {
            PaymentTerms t = PaymentTerms.parse(text);
            assertEquals(canonical, t.toString());
            assertEquals(due, t.dueDate(JAN_5).orElseThrow());
        }

        @Test
        void cashOnDeliveryHasNoDateFromTheInvoice() {
            assertTrue(PaymentTerms.parse("COD").dueDate(JAN_5).isEmpty());
        }

        @Test
        void earlyPaymentDiscount() {
            PaymentTerms t = PaymentTerms.parse("2/10 Net 30");
            Money total = Money.of("1000.00", FiatCurrency.USD);
            assertEquals(LocalDate.of(2026, 1, 15), t.discountDate(JAN_5).orElseThrow());
            assertEquals(0, new BigDecimal("980.00").compareTo(t.amountDue(total, LocalDate.of(2026, 1, 15), JAN_5).amount()));
            assertEquals(total, t.amountDue(total, LocalDate.of(2026, 1, 16), JAN_5));
        }

        @Test
        void anInvoiceWithoutADueDateTakesItFromItsTerms() {
            Invoice inv = Invoice.parse("""
                    INVOICE
                    Invoice No.:    INV-0099
                    Invoice Date:   January 5, 2026
                    Terms:          Net 30 EOM

                    Description          Qty    UOM     Unit Price     Amount
                    Steel bolts M8       200    pcs     USD 0.25       USD 50.00
                    Paint, white          10    L       USD 12.00      USD 120.00

                    TOTAL                                              USD 170.00
                    """);
            assertEquals(PaymentTerms.endOfMonth(30), inv.paymentTerms().orElseThrow());
            assertEquals(LocalDate.of(2026, 3, 2), inv.dueDate().orElseThrow(), "Jan 31 + 30 days");
            assertEquals(io.joshuasalcedo.valueobject.financial.BillingDocument.Form.INFERRED, inv.sources().get("dueDate").form());
            assertEquals(Quantity.of(200, UnitOfMeasure.PIECE), inv.lineItems().getFirst().quantity().orElseThrow());
            assertEquals(Quantity.of(10, UnitOfMeasure.LITRE), inv.lineItems().get(1).quantity().orElseThrow());
        }
    }
}
