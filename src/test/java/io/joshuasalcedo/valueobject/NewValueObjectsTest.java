package io.joshuasalcedo.valueobject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.financial.Bic;
import io.joshuasalcedo.valueobject.financial.CardBrand;
import io.joshuasalcedo.valueobject.financial.CardNumber;
import io.joshuasalcedo.valueobject.financial.Iban;
import io.joshuasalcedo.valueobject.financial.MaskedCard;
import io.joshuasalcedo.valueobject.format.Csv;
import io.joshuasalcedo.valueobject.format.Json;
import io.joshuasalcedo.valueobject.geo.Country;
import io.joshuasalcedo.valueobject.identifier.Gtin;
import io.joshuasalcedo.valueobject.identifier.TaxId;
import io.joshuasalcedo.valueobject.network.DomainName;
import io.joshuasalcedo.valueobject.temporal.CalendarDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NewValueObjectsTest {

    @Nested
    class Dates {

        @ParameterizedTest(name = "{0}")
        @CsvSource(delimiter = '|', value = {
                "2026-03-10          | 2026-03-10",
                "January 15, 2026    | 2026-01-15",
                "Jan 15 2026         | 2026-01-15",
                "15 January 2026     | 2026-01-15",
                "15-Jan-2026         | 2026-01-15",
                "03/10/2026          | 2026-03-10",
                "26/01/2026          | 2026-01-26",
                "01/26/26            | 2026-01-26",
                "Sept 3rd, 2026      | 2026-09-03",
                "10.03.2026          | 2026-03-10",
        })
        void readsPrintedForms(String text, LocalDate expected) {
            assertEquals(expected, CalendarDate.parse(text).value());
        }

        @Test
        void findsTheFirstDateInText() {
            assertEquals(CalendarDate.of(2026, 2, 5), CalendarDate.find("Pay by Feb 5, 2026 please.").orElseThrow());
            assertFalse(CalendarDate.isValid("version 1.2.30"), "a version is not a date");
            assertThrows(ValueObjectException.class, () -> CalendarDate.of(2026, 2, 30));
        }
    }

    @Nested
    class Cards {

        @Test
        void fullNumbersAreCheckedAndMasked() {
            CardNumber visa = CardNumber.parse("4242 4242 4242 4242");
            assertEquals(CardBrand.VISA, visa.brand());
            assertEquals("4242", visa.last4());
            assertEquals("**** **** **** 4242", visa.masked());
            assertEquals("**** **** **** 4242", visa.toString(), "toString never shows the whole number");
            assertEquals(CardBrand.AMERICAN_EXPRESS, CardNumber.parse("378282246310005").brand());
            assertEquals("3782 822463 10005", CardNumber.parse("378282246310005").formatted());
            assertEquals(CardBrand.MASTERCARD, CardNumber.parse("5555555555554444").brand());
            assertFalse(CardNumber.isValid("4242 4242 4242 4241"), "Luhn check");
        }

        @Test
        void jsonAndCsvNeverWriteTheWholeNumber() {
            CardNumber visa = CardNumber.parse("4242424242424242");
            assertFalse(Json.of(List.of(visa)).contains("4242424242424242"));
            assertFalse(Csv.cell(visa).contains("4242424242424242"));
        }

        @ParameterizedTest(name = "{0}")
        @CsvSource(delimiter = '|', value = {
                "Visa - 4242                      | VISA             | 4242",
                "Mastercard ending in 4444        | MASTERCARD       | 4444",
                "American Express - 1005          | AMERICAN_EXPRESS | 1005",
                "AMEX ****1005                    | AMERICAN_EXPRESS | 1005",
                "**** **** **** 4242              | UNKNOWN          | 4242",
        })
        void maskedCardsAsReceiptsPrintThem(String text, CardBrand brand, String last4) {
            MaskedCard c = MaskedCard.parse(text);
            assertEquals(brand, c.brand());
            assertEquals(last4, c.last4());
            assertTrue(c.matches(CardNumber.parse("4242424242424242")) == (last4.equals("4242")));
        }
    }

    @Nested
    class Banking {

        @Test
        void ibanIsCheckedWithMod97() {
            Iban iban = Iban.parse("gb82 west 1234 5698 7654 32");
            assertEquals("GB82WEST12345698765432", iban.value());
            assertEquals("GB82 WEST 1234 5698 7654 32", iban.formatted());
            assertEquals(Country.UNITED_KINGDOM, iban.country());
            assertEquals("WEST12345698765432", iban.bban());
            assertFalse(Iban.isValid("GB83 WEST 1234 5698 7654 32"), "wrong check digits");
            assertFalse(Iban.isValid("GB82 WEST 1234 5698 7654"), "wrong length for GB");
        }

        @Test
        void bicHasBankCountryAndBranch() {
            Bic bic = Bic.parse("DEUTDEFF500");
            assertEquals("DEUT", bic.bankCode());
            assertEquals(Country.GERMANY, bic.country());
            assertEquals("500", bic.branchCode());
            assertTrue(Bic.parse("BOPIPHMM").isHeadOffice());
            assertFalse(Bic.isValid("customer"), "words in lower case are not codes");
        }
    }

    @Nested
    class Identifiers {

        @Test
        void taxIdsKnowTheirCountryFromTheirForm() {
            assertEquals(Country.PHILIPPINES, TaxId.parse("555-123-456-000").country());
            assertEquals(Country.UNITED_STATES, TaxId.parse("12-3456789").country());
            TaxId vat = TaxId.parse("DE 123456789");
            assertEquals(Country.GERMANY, vat.country());
            assertTrue(vat.isVat());
            assertEquals(Country.UNKNOWN, TaxId.parse("555012345").country());
            assertFalse(TaxId.isValid("n/a"));
        }

        @Test
        void gtinChecksItsCheckDigit() {
            Gtin ean = Gtin.parse("4006381333931");
            assertEquals(Gtin.Kind.EAN_13, ean.kind());
            assertEquals(Gtin.Kind.UPC_A, Gtin.parse("036000291452").kind());
            assertEquals("00036000291452", Gtin.parse("036000291452").toGtin14());
            assertFalse(Gtin.isValid("4006381333932"));
        }

        @Test
        void domainNames() {
            DomainName d = DomainName.parse("https://Shop.Example.com.ph/cart?x=1");
            assertEquals("shop.example.com.ph", d.value());
            assertEquals("ph", d.tld());
            assertEquals("example.com.ph", d.registrable().value());
            assertEquals("northwind.example", DomainName.parse("billing@northwind.example").value());
            assertTrue(DomainName.parse("a.b.example").isSubdomainOf(DomainName.parse("b.example")));
            assertFalse(DomainName.isValid("invoice"));
            assertFalse(DomainName.isValid("-bad-.com"));
        }
    }

    @Test
    void detectedInText() {
        var counts = ValueObject.detect("""
                Paid with Visa - 4242 on January 15, 2026, due 15-Feb-2026.
                Bank: GB82 WEST 1234 5698 7654 32
                Card on file 4242 4242 4242 4242
                """);
        assertEquals(1, counts.get(ValueObject.Type.MASKED_CARD), counts::toString);
        assertEquals(2, counts.get(ValueObject.Type.CALENDAR_DATE), counts::toString);
        assertEquals(1, counts.get(ValueObject.Type.IBAN), counts::toString);
        assertEquals(1, counts.get(ValueObject.Type.CARD_NUMBER), counts::toString);
    }
}
