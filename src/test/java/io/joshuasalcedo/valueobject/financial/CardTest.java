package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Nested
    class Numbers {

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({
                "4242424242424242, VISA, '4242 4242 4242 4242', '**** **** **** 4242'",
                "'4242 4242 4242 4242', VISA, '4242 4242 4242 4242', '**** **** **** 4242'",
                "4242-4242-4242-4242, VISA, '4242 4242 4242 4242', '**** **** **** 4242'",
                "378282246310005, AMERICAN_EXPRESS, '3782 822463 10005', '**** ****** *0005'",
                "5555555555554444, MASTERCARD, '5555 5555 5555 4444', '**** **** **** 4444'",
                "2223003122003222, MASTERCARD, '2223 0031 2200 3222', '**** **** **** 3222'",
                "6011111111111117, DISCOVER, '6011 1111 1111 1117', '**** **** **** 1117'",
                "3566002020360505, JCB, '3566 0020 2036 0505', '**** **** **** 0505'",
                "6200000000000005, UNIONPAY, '6200 0000 0000 0005', '**** **** **** 0005'",
                "3056930009020004, DINERS_CLUB, '3056 9300 0902 0004', '**** **** **** 0004'",
                "36227206271667, DINERS_CLUB, '3622 720627 1667', '**** ****** 1667'",
                "4222222222222, VISA, '4 2222 2222 2222', '* **** **** 2222'",
                "4242424242424242428, VISA, '424 2424 2424 2424 2428', '*** **** **** **** 2428'"
        })
        void brandGroupsAndMask(String text, CardBrand brand, String formatted, String masked) {
            CardNumber card = CardNumber.parse(text);
            assertEquals(brand, card.brand());
            assertEquals(formatted, card.formatted());
            assertEquals(masked, card.masked());
            assertEquals(masked, card.toString());
            assertEquals(card, CardNumber.tryParse(text).orElseThrow());
            assertTrue(CardNumber.isValid(text));
        }

        @Test
        void theLastFourAlwaysStayTogether() {
            for (String n : new String[]{"4242424242424242428", "4222222222222", "378282246310005", "36227206271667",
                    "4242424242424242"}) {
                CardNumber card = CardNumber.parse(n);
                assertTrue(card.masked().endsWith(" " + card.last4()) || card.masked().endsWith("*" + card.last4()),
                        card.masked());
                String lastGroup = card.formatted().substring(card.formatted().lastIndexOf(' ') + 1);
                assertTrue(lastGroup.endsWith(card.last4()) && lastGroup.length() >= 4, card.formatted());
                assertEquals(n, card.formatted().replace(" ", ""));
            }
            assertNotEquals("**** **** **** ***2 428", CardNumber.parse("4242424242424242428").masked(),
                    "the old left-to-right grouping split the last four");
        }

        @Test
        void parts() {
            CardNumber visa = CardNumber.parse("4242 4242 4242 4242");
            assertEquals("4242424242424242", visa.value());
            assertEquals("4242", visa.last4());
            assertEquals("424242", visa.iin());
            assertEquals(new MaskedCard(CardBrand.VISA, "4242"), visa.toMasked());
            assertFalse(visa.toString().contains("42424242"), "toString never shows the whole number");
        }

        @ParameterizedTest(name = "\"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "4242 4242 4242 4241", "1234", "42424242424", "42424242424242424242",
                "4242 4242 4242 424a", "abcd efgh ijkl mnop", "4242_4242_4242_4242"})
        void rejectsBadNumbers(String text) {
            assertThrows(ValueObjectException.class, () -> CardNumber.parse(text));
            assertTrue(CardNumber.tryParse(text).isEmpty());
            assertFalse(CardNumber.isValid(text));
        }

        @Test
        void unknownBrandIsStillACardNumber() {
            CardNumber card = CardNumber.parse("9999999999999995");
            assertEquals(CardBrand.UNKNOWN, card.brand());
            assertEquals("9999 9999 9999 9995", card.formatted());
        }
    }

    @Nested
    class Brands {

        @ParameterizedTest(name = "\"{0}\" -> {1}")
        @CsvSource({"Visa, VISA", "VISA DEBIT, VISA", "Visa Electron, VISA", "MC, MASTERCARD", "master card, MASTERCARD",
                "AMEX, AMERICAN_EXPRESS", "American Express, AMERICAN_EXPRESS", "discover, DISCOVER", "JCB, JCB",
                "China UnionPay, UNIONPAY", "CUP, UNIONPAY", "Diners, DINERS_CLUB", "Diners Club, DINERS_CLUB",
                "Maestro, MAESTRO", "4242424242424242, VISA", "378282246310005, AMERICAN_EXPRESS"})
        void readsNamesAndNumbers(String text, CardBrand brand) {
            assertEquals(brand, CardBrand.parse(text));
            assertEquals(brand, CardBrand.tryParse(text).orElseThrow());
            assertTrue(CardBrand.isValid(text));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "Card", "Paypal", "9999999999999995", "Unknown"})
        void rejectsOthers(String text) {
            assertThrows(ValueObjectException.class, () -> CardBrand.parse(text));
            assertTrue(CardBrand.tryParse(text).isEmpty());
            assertFalse(CardBrand.isValid(text));
        }

        @Test
        void recognisesNumbersByPrefixAndLength() {
            assertEquals(CardBrand.VISA, CardBrand.of("4242 4242 4242 4242"));
            assertEquals(CardBrand.UNKNOWN, CardBrand.of("4242"), "wrong length");
            assertEquals(CardBrand.MASTERCARD, CardBrand.of("2720990000000000"));
            assertEquals(CardBrand.UNKNOWN, CardBrand.of("2721000000000000"));
            assertEquals(CardBrand.UNKNOWN, CardBrand.of(""));
            assertThrows(ValueObjectException.class, () -> CardBrand.of(null));
            assertTrue(CardBrand.fromName(null).isEmpty());
            assertTrue(CardBrand.fromName("nope").isEmpty());
        }

        @Test
        void names() {
            assertEquals("American Express", CardBrand.AMERICAN_EXPRESS.displayName());
            assertEquals("Card", CardBrand.UNKNOWN.displayName());
            assertTrue(CardBrand.UNKNOWN.isUnknown());
            assertFalse(CardBrand.VISA.isUnknown());
        }
    }

    @Nested
    class Masked {

        @ParameterizedTest(name = "\"{0}\" -> {1} {2}")
        @CsvSource({"Visa - 4242, VISA, 4242", "Mastercard ending in 4444, MASTERCARD, 4444",
                "American Express ending in 1005, AMERICAN_EXPRESS, 1005", "AMEX ****1005, AMERICAN_EXPRESS, 1005",
                "visa: 4242, VISA, 4242", "MC xxxx 4444, MASTERCARD, 4444", "'**** **** **** 4242', UNKNOWN, 4242",
                "'**** 4242', UNKNOWN, 4242", "'XXXX-XXXX-XXXX-4242', UNKNOWN, 4242"})
        void readsReceiptForms(String text, CardBrand brand, String last4) {
            MaskedCard card = MaskedCard.parse(text);
            assertEquals(brand, card.brand());
            assertEquals(last4, card.last4());
            assertEquals(card, MaskedCard.tryParse(text).orElseThrow());
            assertTrue(MaskedCard.isValid(text));
        }

        @ParameterizedTest(name = "\"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "hello", "Visa", "4242", "Visa - 424", "Visa - 42424", "Paypal - 4242"})
        void rejectsOthers(String text) {
            assertThrows(ValueObjectException.class, () -> MaskedCard.parse(text));
            assertTrue(MaskedCard.tryParse(text).isEmpty());
            assertFalse(MaskedCard.isValid(text));
        }

        @Test
        void constructorAndMatching() {
            assertEquals(CardBrand.UNKNOWN, new MaskedCard(null, "4242").brand());
            assertThrows(ValueObjectException.class, () -> new MaskedCard(CardBrand.VISA, "42a2"));
            assertThrows(ValueObjectException.class, () -> new MaskedCard(CardBrand.VISA, null));
            CardNumber visa = CardNumber.parse("4242424242424242");
            assertTrue(new MaskedCard(CardBrand.VISA, "4242").matches(visa));
            assertTrue(new MaskedCard(CardBrand.UNKNOWN, "4242").matches(visa));
            assertFalse(new MaskedCard(CardBrand.MASTERCARD, "4242").matches(visa));
            assertFalse(new MaskedCard(CardBrand.VISA, "4243").matches(visa));
            assertThrows(ValueObjectException.class, () -> new MaskedCard(CardBrand.VISA, "4242").matches(null));
        }

        @Test
        void text() {
            assertEquals("Visa •••• 4242", new MaskedCard(CardBrand.VISA, "4242").toString());
            assertEquals("Card •••• 4242", new MaskedCard(CardBrand.UNKNOWN, "4242").toString());
        }
    }
}
