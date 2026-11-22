package io.joshuasalcedo.valueobject.jpa;

import io.joshuasalcedo.valueobject.contact.Email;
import io.joshuasalcedo.valueobject.contact.Link;
import io.joshuasalcedo.valueobject.financial.Bic;
import io.joshuasalcedo.valueobject.financial.Currency;
import io.joshuasalcedo.valueobject.financial.FiatCurrency;
import io.joshuasalcedo.valueobject.financial.Iban;
import io.joshuasalcedo.valueobject.financial.Money;
import io.joshuasalcedo.valueobject.financial.PaymentTerms;
import io.joshuasalcedo.valueobject.financial.Percentage;
import io.joshuasalcedo.valueobject.geo.Country;
import io.joshuasalcedo.valueobject.geo.Language;
import io.joshuasalcedo.valueobject.identifier.DisplayName;
import io.joshuasalcedo.valueobject.identifier.Gtin;
import io.joshuasalcedo.valueobject.identifier.Slug;
import io.joshuasalcedo.valueobject.identifier.TaxId;
import io.joshuasalcedo.valueobject.identifier.UniqueId;
import io.joshuasalcedo.valueobject.identifier.Username;
import io.joshuasalcedo.valueobject.identity.Gender;
import io.joshuasalcedo.valueobject.measurement.Quantity;
import io.joshuasalcedo.valueobject.measurement.UnitOfMeasure;
import io.joshuasalcedo.valueobject.network.DomainName;
import io.joshuasalcedo.valueobject.network.Ipv4Address;
import io.joshuasalcedo.valueobject.network.Ipv6Address;
import io.joshuasalcedo.valueobject.network.Port;
import io.joshuasalcedo.valueobject.temporal.CalendarDate;
import io.joshuasalcedo.valueobject.temporal.TimeZone;
import io.joshuasalcedo.valueobject.temporal.Timestamp;
import io.joshuasalcedo.valueobject.versioning.SemanticVersion;
import jakarta.persistence.Converter;

/**
 * Jakarta Persistence converters for the value objects most often stored, each into one text column. All apply
 * automatically to entity attributes of their type once your provider knows them: list this class's converters in
 * {@code persistence.xml}, or let Spring Boot / Hibernate scan the {@code io.joshuasalcedo.valueobject.jpa} package.
 *
 * <pre>{@code
 * @Entity
 * class Customer {
 *     Email email;          // stored as "jane@example.com"
 *     Money balance;        // stored as "149.99 USD"
 *     Country country;      // stored as "PH"
 * }
 * }</pre>
 *
 * @author JoshuaSalcedo
 */
public final class Converters {

    private Converters() {}

    /** {@link Email} as its address. */
    @Converter(autoApply = true)
    public static class EmailConverter extends ValueObjectConverter<Email> {
        /** Makes the converter. */
        public EmailConverter() {
            super(Email.class);
        }
    }

    /** {@link Link} as its URL. */
    @Converter(autoApply = true)
    public static class LinkConverter extends ValueObjectConverter<Link> {
        /** Makes the converter. */
        public LinkConverter() {
            super(Link.class);
        }
    }

    /** {@link Money} as its amount and currency code, e.g. {@code 149.99 USD}. */
    @Converter(autoApply = true)
    public static class MoneyConverter extends ValueObjectConverter<Money> {
        /** Makes the converter. */
        public MoneyConverter() {
            super(Money.class);
        }
    }

    /** {@link Currency} as its code, e.g. {@code PHP}, {@code BTC}. */
    @Converter(autoApply = true)
    public static class CurrencyConverter extends ValueObjectConverter<Currency> {
        /** Makes the converter. */
        public CurrencyConverter() {
            super(Currency.class);
        }

        @Override
        protected String text(Currency value) {
            return value.code();
        }
    }

    /** {@link FiatCurrency} as its ISO 4217 code. */
    @Converter(autoApply = true)
    public static class FiatCurrencyConverter extends ValueObjectConverter<FiatCurrency> {
        /** Makes the converter. */
        public FiatCurrencyConverter() {
            super(FiatCurrency.class);
        }

        @Override
        protected String text(FiatCurrency value) {
            return value.code();
        }
    }

    /** {@link Percentage} as its number, e.g. {@code 12.5}. */
    @Converter(autoApply = true)
    public static class PercentageConverter extends ValueObjectConverter<Percentage> {
        /** Makes the converter. */
        public PercentageConverter() {
            super(Percentage.class);
        }
    }

    /** {@link PaymentTerms} in the standard notation, e.g. {@code Net 30 EOM}. */
    @Converter(autoApply = true)
    public static class PaymentTermsConverter extends ValueObjectConverter<PaymentTerms> {
        /** Makes the converter. */
        public PaymentTermsConverter() {
            super(PaymentTerms.class);
        }

        @Override
        protected String text(PaymentTerms value) {
            return value.toString();
        }
    }

    /** {@link Iban} without spaces. */
    @Converter(autoApply = true)
    public static class IbanConverter extends ValueObjectConverter<Iban> {
        /** Makes the converter. */
        public IbanConverter() {
            super(Iban.class);
        }
    }

    /** {@link Bic} as its code. */
    @Converter(autoApply = true)
    public static class BicConverter extends ValueObjectConverter<Bic> {
        /** Makes the converter. */
        public BicConverter() {
            super(Bic.class);
        }
    }

    /** {@link Country} as its ISO 3166-1 alpha-2 code, e.g. {@code PH}. */
    @Converter(autoApply = true)
    public static class CountryConverter extends ValueObjectConverter<Country> {
        /** Makes the converter. */
        public CountryConverter() {
            super(Country.class);
        }

        @Override
        protected String text(Country value) {
            return value.code();
        }
    }

    /** {@link Language} as its ISO 639-1 code, e.g. {@code en}. */
    @Converter(autoApply = true)
    public static class LanguageConverter extends ValueObjectConverter<Language> {
        /** Makes the converter. */
        public LanguageConverter() {
            super(Language.class);
        }

        @Override
        protected String text(Language value) {
            return value.code();
        }
    }

    /** {@link Gender} as its constant name. */
    @Converter(autoApply = true)
    public static class GenderConverter extends ValueObjectConverter<Gender> {
        /** Makes the converter. */
        public GenderConverter() {
            super(Gender.class);
        }
    }

    /** {@link UnitOfMeasure} as its UN/ECE Rec 20 code, e.g. {@code KGM}. */
    @Converter(autoApply = true)
    public static class UnitOfMeasureConverter extends ValueObjectConverter<UnitOfMeasure> {
        /** Makes the converter. */
        public UnitOfMeasureConverter() {
            super(UnitOfMeasure.class);
        }
    }

    /** {@link Quantity} as its amount and unit, e.g. {@code 2.5 kg}. */
    @Converter(autoApply = true)
    public static class QuantityConverter extends ValueObjectConverter<Quantity> {
        /** Makes the converter. */
        public QuantityConverter() {
            super(Quantity.class);
        }

        @Override
        protected String text(Quantity value) {
            return value.amount().toPlainString() + " " + value.unit().code();
        }
    }

    /** {@link UniqueId} as its UUID. */
    @Converter(autoApply = true)
    public static class UniqueIdConverter extends ValueObjectConverter<UniqueId> {
        /** Makes the converter. */
        public UniqueIdConverter() {
            super(UniqueId.class);
        }
    }

    /** {@link Slug} as itself. */
    @Converter(autoApply = true)
    public static class SlugConverter extends ValueObjectConverter<Slug> {
        /** Makes the converter. */
        public SlugConverter() {
            super(Slug.class);
        }
    }

    /** {@link Username} as itself, lower case. */
    @Converter(autoApply = true)
    public static class UsernameConverter extends ValueObjectConverter<Username> {
        /** Makes the converter. */
        public UsernameConverter() {
            super(Username.class);
        }
    }

    /** {@link DisplayName} as itself. */
    @Converter(autoApply = true)
    public static class DisplayNameConverter extends ValueObjectConverter<DisplayName> {
        /** Makes the converter. */
        public DisplayNameConverter() {
            super(DisplayName.class);
        }
    }

    /** {@link TaxId} as printed. */
    @Converter(autoApply = true)
    public static class TaxIdConverter extends ValueObjectConverter<TaxId> {
        /** Makes the converter. */
        public TaxIdConverter() {
            super(TaxId.class);
        }

        @Override
        protected String text(TaxId value) {
            return value.value();
        }
    }

    /** {@link Gtin} as its digits. */
    @Converter(autoApply = true)
    public static class GtinConverter extends ValueObjectConverter<Gtin> {
        /** Makes the converter. */
        public GtinConverter() {
            super(Gtin.class);
        }
    }

    /** {@link DomainName} as itself. */
    @Converter(autoApply = true)
    public static class DomainNameConverter extends ValueObjectConverter<DomainName> {
        /** Makes the converter. */
        public DomainNameConverter() {
            super(DomainName.class);
        }
    }

    /** {@link Ipv4Address} as its dotted form. */
    @Converter(autoApply = true)
    public static class Ipv4AddressConverter extends ValueObjectConverter<Ipv4Address> {
        /** Makes the converter. */
        public Ipv4AddressConverter() {
            super(Ipv4Address.class);
        }
    }

    /** {@link Ipv6Address} as its text. */
    @Converter(autoApply = true)
    public static class Ipv6AddressConverter extends ValueObjectConverter<Ipv6Address> {
        /** Makes the converter. */
        public Ipv6AddressConverter() {
            super(Ipv6Address.class);
        }
    }

    /** {@link Port} as its number. */
    @Converter(autoApply = true)
    public static class PortConverter extends ValueObjectConverter<Port> {
        /** Makes the converter. */
        public PortConverter() {
            super(Port.class);
        }
    }

    /** {@link CalendarDate} as ISO-8601, e.g. {@code 2026-01-15}. */
    @Converter(autoApply = true)
    public static class CalendarDateConverter extends ValueObjectConverter<CalendarDate> {
        /** Makes the converter. */
        public CalendarDateConverter() {
            super(CalendarDate.class);
        }
    }

    /** {@link Timestamp} as ISO-8601 UTC, e.g. {@code 2026-01-15T10:15:30Z}. */
    @Converter(autoApply = true)
    public static class TimestampConverter extends ValueObjectConverter<Timestamp> {
        /** Makes the converter. */
        public TimestampConverter() {
            super(Timestamp.class);
        }

        @Override
        protected String text(Timestamp value) {
            return value.value().toString();
        }
    }

    /** {@link TimeZone} as its zone id, e.g. {@code Asia/Manila}. */
    @Converter(autoApply = true)
    public static class TimeZoneConverter extends ValueObjectConverter<TimeZone> {
        /** Makes the converter. */
        public TimeZoneConverter() {
            super(TimeZone.class);
        }

        @Override
        protected String text(TimeZone value) {
            return value.zoneId();
        }
    }

    /** {@link SemanticVersion} as its version text, e.g. {@code 1.2.3}. */
    @Converter(autoApply = true)
    public static class SemanticVersionConverter extends ValueObjectConverter<SemanticVersion> {
        /** Makes the converter. */
        public SemanticVersionConverter() {
            super(SemanticVersion.class);
        }

        @Override
        protected String text(SemanticVersion value) {
            return value.toString();
        }
    }
}
