package io.joshuasalcedo.valueobject;

import io.joshuasalcedo.valueobject.ValueObject.Type;
import io.joshuasalcedo.valueobject.contact.Email;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.file.FileType;
import io.joshuasalcedo.valueobject.financial.CryptoCoin;
import io.joshuasalcedo.valueobject.financial.FiatCurrency;
import io.joshuasalcedo.valueobject.financial.Money;
import io.joshuasalcedo.valueobject.geo.Country;
import io.joshuasalcedo.valueobject.identity.Gender;
import io.joshuasalcedo.valueobject.network.Port;
import io.joshuasalcedo.valueobject.temporal.Meridiem;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectTest {

    @Nested
    class Identify {

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource(delimiter = '|', value = {
                "jane@example.com                                   | EMAIL",
                "https://example.com/path                           | LINK",
                "@joshua_salcedo                                    | TELEGRAM_ACCOUNT",
                "https://t.me/joshua_salcedo                        | TELEGRAM_ACCOUNT",
                "192.168.0.1                                        | IPV4_ADDRESS",
                "2001:db8::1                                        | IPV6_ADDRESS",
                "00:1A:2B:3C:4D:5E                                  | MAC_ADDRESS",
                "123e4567-e89b-12d3-a456-426614174000               | UNIQUE_ID",
                "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjMifQ.abc123     | JWT_TOKEN",
                "2026-11-23T10:15:30Z                               | TIMESTAMP",
                "2026-11-23T10:15:30+08:00                          | DATE_TIME_WITH_ZONE",
                "1990-05-17                                         | CALENDAR_DATE",
                "January 15, 2026                                   | CALENDAR_DATE",
                "4242 4242 4242 4242                                | CARD_NUMBER",
                "Visa - 4242                                        | MASKED_CARD",
                "GB82 WEST 1234 5698 7654 32                        | IBAN",
                "DEUTDEFF500                                        | BIC",
                "555-123-456-000                                    | TAX_ID",
                "4006381333931                                      | GTIN",
                "shop.example.com.ph                                | DOMAIN_NAME",
                "1.2.3                                              | SEMANTIC_VERSION",
                "'14.5995, 120.9842'                                | COORDINATES",
                "3:30 PM                                            | TIME",
                "50%                                                | PERCENTAGE",
                "149.99 USD                                         | MONEY",
                "PHP 1,250.00                                       | MONEY",
                "#ff8800                                            | COLOR",
                "+63 917 123 4567                                   | PHONE_NUMBER",
                "application/pdf                                    | FILE_TYPE",
                "USD                                                | FIAT_CURRENCY",
                "BTC                                                | CRYPTO_COIN",
                "PH                                                 | COUNTRY",
                "Philippines                                        | COUNTRY",
                "male                                               | GENDER",
        })
        void findsTheMostSpecificType(String text, Type expected) {
            assertEquals(expected, ValueObject.identify(text).orElse(null), () -> "parsed: " + ValueObject.parse(text));
        }

        @Test
        void isEmptyForTextThatIsNoValueObject() {
            assertTrue(ValueObject.identify("just some words here!").isEmpty());
            assertTrue(ValueObject.identify("").isEmpty());
            assertTrue(ValueObject.identify(null).isEmpty());
        }

        @Test
        void candidatesListEveryMatchMostSpecificFirst() {
            var all = ValueObject.candidates("PH");
            assertEquals(Type.COUNTRY, all.getFirst());
        }
    }

    @Nested
    class Detect {

        @Test
        void countsEachValueObjectInAText() {
            String text = """
                    Contact jane@example.com or bob@example.org, or call +63 917 123 4567.
                    Docs at https://example.com/docs (server 10.0.0.1), Telegram @joshua_salcedo.
                    Price: 149.99 USD, 20% off, listed since 2020-01-15.
                    """;
            Map<Type, Integer> counts = ValueObject.detect(text);
            assertEquals(2, counts.get(Type.EMAIL), counts::toString);
            assertEquals(1, counts.get(Type.PHONE_NUMBER), counts::toString);
            assertEquals(1, counts.get(Type.LINK), counts::toString);
            assertEquals(1, counts.get(Type.IPV4_ADDRESS), counts::toString);
            assertEquals(1, counts.get(Type.TELEGRAM_ACCOUNT), counts::toString);
            assertEquals(1, counts.get(Type.MONEY), counts::toString);
            assertEquals(1, counts.get(Type.PERCENTAGE), counts::toString);
            assertEquals(1, counts.get(Type.CALENDAR_DATE), counts::toString);
            assertFalse(counts.containsKey(Type.COUNTRY), "codes are only recognised as whole strings");
        }

        @Test
        void findReturnsTheValuesThemselves() {
            var found = ValueObject.find("a@b.co and c@d.co");
            assertEquals(java.util.List.of(Email.of("a@b.co"), Email.of("c@d.co")), found.get(Type.EMAIL));
        }

        @Test
        void isEmptyForNoText() {
            assertTrue(ValueObject.detect(null).isEmpty());
            assertTrue(ValueObject.detect("nothing to see").isEmpty());
        }
    }

    @Nested
    class Parse {

        @Test
        void makesValueObjectsFromWhatTheirFactoriesTake() {
            assertEquals(Money.of("149.99", FiatCurrency.USD), ValueObject.Parse.Money("149.99 USD"));
            assertEquals(Money.of("0.5", CryptoCoin.Known.BTC), ValueObject.Parse.Money("0.5 btc"));
            assertEquals(Port.of(8080), ValueObject.Parse.Port(8080));
            assertEquals(Port.of(8080), ValueObject.Parse.Port(8080L));
            assertEquals(Port.of(8080), ValueObject.Parse.Port("8080"));
            assertEquals(Gender.MALE, ValueObject.Parse.Gender("male"));
            assertEquals(Country.PHILIPPINES, ValueObject.Parse.Country("PH"));
            assertEquals(Meridiem.PM, ValueObject.Parse.Meridiem("pm"));
            UUID id = UUID.randomUUID();
            assertEquals(id, ValueObject.Parse.UniqueId(id).value());
        }

        @Test
        void returnsAValueObjectAsIs() {
            Email email = Email.of("jane@example.com");
            assertSame(email, ValueObject.Parse.Email(email));
        }

        @Test
        void throwsValueObjectExceptionForBadInput() {
            assertThrows(ValueObjectException.class, () -> ValueObject.Parse.Email("nope"));
            assertThrows(ValueObjectException.class, () -> ValueObject.Parse.Email(42));
            assertThrows(ValueObjectException.class, () -> ValueObject.Parse.Email(null));
            assertThrows(ValueObjectException.class, () -> ValueObject.Parse.Country("XX"));
        }

        @Test
        void typeParsesToo() {
            assertEquals(Email.of("a@b.co"), Type.EMAIL.parse("a@b.co"));
            assertTrue(Type.EMAIL.tryParse("nope").isEmpty());
            assertEquals(Type.CRYPTO_COIN, Type.of(CryptoCoin.Known.ETH.getClass()).orElseThrow());
            assertEquals(Type.EMAIL, Type.of(Email.class).orElseThrow());
        }
    }

    @Nested
    class Validate {

        @Test
        void checksEnumValues() {
            assertTrue(ValueObject.Validate.GENDER.is.MALE.test("male"));
            assertTrue(ValueObject.Validate.GENDER.is.MALE.test(Gender.MALE));
            assertFalse(ValueObject.Validate.GENDER.is.MALE.test(Gender.FEMALE));
            assertFalse(ValueObject.Validate.GENDER.is.MALE.test("not a gender"));
        }

        @Test
        void checksTheEnumPartsOfARecord() {
            assertTrue(ValueObject.Validate.MONEY.is.USD.test("149.99 USD"));
            assertFalse(ValueObject.Validate.MONEY.is.USD.test("149.99 PHP"));
            assertTrue(ValueObject.Validate.MONEY.is.BTC.test(Money.of("1", CryptoCoin.Known.BTC)));
            assertTrue(ValueObject.Validate.TIME.is.PM.test("3:30 PM"));
        }

        @Test
        void checksValidity() {
            assertTrue(ValueObject.Validate.EMAIL.valid.test("jane@example.com"));
            assertFalse(ValueObject.Validate.EMAIL.isValid("nope"));
            assertEquals(Email.of("a@b.co"), ValueObject.Validate.EMAIL.parse("a@b.co").orElseThrow());
        }

        @Test
        void requireThrowsWithAMessage() {
            var e = assertThrows(ValueObjectException.class, () -> ValueObject.Validate.MONEY.is.USD.require("1 PHP"));
            assertTrue(e.getMessage().contains("MONEY is USD"), e.getMessage());
        }
    }

    @Nested
    class Crypto {

        @Test
        void knownAndCustomCoins() {
            assertEquals(CryptoCoin.Known.ETH, CryptoCoin.of("eth"));
            CryptoCoin pepe = CryptoCoin.custom("pepe", "Pepe", "PEPE", 18);
            assertEquals("PEPE", pepe.code());
            assertEquals(Money.of(new BigDecimal("1000"), pepe).currency(), pepe);
            assertThrows(ValueObjectException.class, () -> CryptoCoin.custom("BTC", "Bitcoin", "B", 8));
            assertThrows(ValueObjectException.class, () -> CryptoCoin.of("NOPE"));
        }
    }

    @Test
    void fileTypeLooksUpByNameMimeAndContent() {
        assertEquals(FileType.PDF, FileType.fromFileName("invoice.PDF").orElseThrow());
        assertEquals(FileType.JPEG, FileType.fromExtension(".jpeg").orElseThrow());
        assertEquals(FileType.PNG, FileType.fromMimeType("image/png; charset=binary").orElseThrow());
        assertEquals(FileType.PDF, FileType.detect("%PDF-1.7 ...".getBytes(StandardCharsets.US_ASCII)));
        assertEquals(FileType.UNKNOWN, FileType.detect(new byte[] {1, 2, 3}));
        assertEquals(FileType.PNG, ValueObject.Parse.FileType("photo.png"));
    }

    @Test
    void generatedFileIsUpToDate() {
        // The generator's --check mode is what CI runs; here just make sure every value object is listed.
        assertTrue(Type.values().length >= 50);
        for (Type t : Type.values()) assertNotNull(t.check(), t::name);
    }
}
