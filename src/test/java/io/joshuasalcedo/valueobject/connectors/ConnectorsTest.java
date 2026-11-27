package io.joshuasalcedo.valueobject.connectors;

import io.joshuasalcedo.valueobject.core.ValueObject;
import io.joshuasalcedo.valueobject.contact.Email;
import io.joshuasalcedo.valueobject.financial.FiatCurrency;
import io.joshuasalcedo.valueobject.financial.LineItem;
import io.joshuasalcedo.valueobject.financial.Money;
import io.joshuasalcedo.valueobject.geo.Country;
import io.joshuasalcedo.valueobject.jpa.Converters;
import io.joshuasalcedo.valueobject.jpa.ValueObjectConverter;
import io.joshuasalcedo.valueobject.measurement.Quantity;
import io.joshuasalcedo.valueobject.measurement.UnitOfMeasure;
import io.joshuasalcedo.valueobject.validation.ValidEmail;
import io.joshuasalcedo.valueobject.validation.ValidIban;
import io.joshuasalcedo.valueobject.validation.ValidMoney;
import io.joshuasalcedo.valueobject.validation.ValidValue;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ConnectorsTest {

    static final Email EMAIL = Email.of("jane@example.com");
    static final Money MONEY = Money.of("149.99", FiatCurrency.USD);

    /** A record holding value objects, as an application would. */
    record Customer(Email email, Money balance, Country country, List<Email> others) {}

    @Nested
    class Jackson2 {
        final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper()
                .registerModule(new io.joshuasalcedo.valueobject.jackson.ValueObjectModule());

        @Test
        void writesAndReadsValueObjects() throws Exception {
            assertEquals("\"jane@example.com\"", mapper.writeValueAsString(EMAIL));
            assertEquals(EMAIL, mapper.readValue("\"jane@example.com\"", Email.class));
            assertEquals("{\"amount\":149.99,\"currency\":\"USD\"}", mapper.writeValueAsString(MONEY));
            assertEquals(MONEY, mapper.readValue("{\"amount\":149.99,\"currency\":\"USD\"}", Money.class));
            assertEquals(Country.PHILIPPINES, mapper.readValue("\"PH\"", Country.class));
        }

        @Test
        void insideOtherObjects() throws Exception {
            Customer c = new Customer(EMAIL, MONEY, Country.PHILIPPINES, List.of(Email.of("bob@example.org")));
            String json = mapper.writeValueAsString(c);
            assertEquals(c, mapper.readValue(json, Customer.class), json);
        }

        @Test
        void keepsAmountsExact() throws Exception {
            Money m = mapper.readValue("{\"amount\":0.10,\"currency\":\"USD\"}", Money.class);
            assertEquals(new BigDecimal("0.10"), m.amount());
        }

        @Test
        void rejectsBadValues() {
            assertThrows(Exception.class, () -> mapper.readValue("\"not an email\"", Email.class));
        }
    }

    @Nested
    class Jackson3 {
        final tools.jackson.databind.json.JsonMapper mapper = tools.jackson.databind.json.JsonMapper.builder()
                .addModule(new io.joshuasalcedo.valueobject.jackson3.ValueObjectModule()).build();

        @Test
        void writesAndReadsValueObjects() {
            assertEquals("\"jane@example.com\"", mapper.writeValueAsString(EMAIL));
            assertEquals(EMAIL, mapper.readValue("\"jane@example.com\"", Email.class));
            assertEquals(MONEY, mapper.readValue(mapper.writeValueAsString(MONEY), Money.class));
            Customer c = new Customer(EMAIL, MONEY, Country.PHILIPPINES, List.of());
            assertEquals(c, mapper.readValue(mapper.writeValueAsString(c), Customer.class));
        }

        @Test
        void recordsOfValueObjects() {
            LineItem li = new LineItem("Bolts", Optional.of(Quantity.of(10, UnitOfMeasure.PIECE)),
                    Optional.of(Money.of("0.25", FiatCurrency.USD)), Optional.of(Money.of("2.50", FiatCurrency.USD)), Map.of());
            assertEquals(li, mapper.readValue(mapper.writeValueAsString(li), LineItem.class));
        }
    }

    @Nested
    class Jpa {
        @Test
        void everyConverterRoundTrips() {
            List<Object> values = List.of(EMAIL, MONEY, Country.PHILIPPINES, FiatCurrency.PHP,
                    io.joshuasalcedo.valueobject.financial.CryptoCoin.Known.BTC,
                    Quantity.of(new BigDecimal("2.5"), UnitOfMeasure.KILOGRAM), UnitOfMeasure.HOUR,
                    io.joshuasalcedo.valueobject.financial.PaymentTerms.parse("Net 30 EOM"),
                    io.joshuasalcedo.valueobject.financial.Iban.parse("GB82 WEST 1234 5698 7654 32"),
                    io.joshuasalcedo.valueobject.financial.Bic.parse("DEUTDEFF500"),
                    io.joshuasalcedo.valueobject.identifier.TaxId.parse("555-123-456-000"),
                    io.joshuasalcedo.valueobject.identifier.Gtin.parse("4006381333931"),
                    io.joshuasalcedo.valueobject.network.DomainName.parse("example.com"),
                    io.joshuasalcedo.valueobject.temporal.CalendarDate.parse("2026-01-15"),
                    io.joshuasalcedo.valueobject.geo.Language.ENGLISH,
                    io.joshuasalcedo.valueobject.financial.Percentage.of(new BigDecimal("12.5")));
            Map<Class<?>, ValueObjectConverter<?>> byType = converters();
            for (Object v : values) {
                @SuppressWarnings("unchecked")
                ValueObjectConverter<ValueObject> c = (ValueObjectConverter<ValueObject>) byType.entrySet().stream()
                        .filter(e -> e.getKey().isInstance(v)).map(Map.Entry::getValue).findFirst()
                        .orElseThrow(() -> new AssertionError("no converter for " + v.getClass()));
                String column = c.convertToDatabaseColumn((ValueObject) v);
                assertEquals(v, c.convertToEntityAttribute(column), v.getClass().getSimpleName() + " stored as " + column);
            }
        }

        @Test
        void storesReadableText() {
            assertEquals("149.99 USD", new Converters.MoneyConverter().convertToDatabaseColumn(MONEY));
            assertEquals("PH", new Converters.CountryConverter().convertToDatabaseColumn(Country.PHILIPPINES));
            assertEquals("2.5 KGM", new Converters.QuantityConverter().convertToDatabaseColumn(Quantity.parse("2.5 kg")));
            assertNull(new Converters.EmailConverter().convertToDatabaseColumn(null));
            assertNull(new Converters.EmailConverter().convertToEntityAttribute("  "));
        }

        private Map<Class<?>, ValueObjectConverter<?>> converters() {
            return java.util.Arrays.stream(Converters.class.getDeclaredClasses())
                    .filter(ValueObjectConverter.class::isAssignableFrom)
                    .map(c -> {
                        try {
                            return (ValueObjectConverter<?>) c.getDeclaredConstructor().newInstance();
                        } catch (ReflectiveOperationException e) {
                            throw new AssertionError(e);
                        }
                    })
                    .collect(Collectors.toMap(ValueObjectConverter::type, c -> c, (a, b) -> a));
        }
    }

    @Nested
    class BeanValidation {
        final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        record SignUp(@ValidEmail String email, @ValidIban String iban, @ValidMoney String deposit,
                      @ValidValue(ValueObject.Type.COUNTRY) String country) {}

        @Test
        void acceptsValidValues() {
            assertTrue(validator.validate(new SignUp("jane@example.com", "GB82 WEST 1234 5698 7654 32", "100.00 PHP", "PH")).isEmpty());
            assertTrue(validator.validate(new SignUp(null, null, null, null)).isEmpty(), "null passes; add @NotNull for required");
        }

        @Test
        void reportsInvalidValues() {
            Set<ConstraintViolation<SignUp>> violations = validator.validate(new SignUp("nope", "GB00 0000", "lots", "Atlantis"));
            Set<String> fields = violations.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
            assertEquals(Set.of("email", "iban", "deposit", "country"), fields, violations::toString);
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("must be an email address")), violations::toString);
        }
    }
}
