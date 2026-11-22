package io.joshuasalcedo.valueobject.contact;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.geo.Country;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Value object representing a physical address.
 * Supports international address formats with optional components.
 * Only country is required; all other fields are optional.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Address(
        @Nullable String street,
        @Nullable String city,
        @Nullable String stateOrProvince,
        @Nullable String postalCode,
        Country country,
        @Nullable String addressLine2
) implements ValueObject {

    /** Null object representing an absent Address. */
    public static final Address NULL = new Address(Country.UNKNOWN);

    public Address(Country country) {
        this(null, null, null, null, country, null);
    }

    public Address(String street, String city, Country country) {
        this(street, city, null, null, country, null);
    }

    
    public boolean isNull() {
        return country == Country.UNKNOWN;
    }

    public Address {
        Objects.requireNonNull(country, "Country cannot be null");

        street = normalizeToNull(street);
        city = normalizeToNull(city);
        stateOrProvince = normalizeToNull(stateOrProvince);
        postalCode = normalizeToNull(postalCode);
        addressLine2 = normalizeToNull(addressLine2);
    }

    /**
     * Trims the string and returns null if blank.
     */
    private static @Nullable String normalizeToNull(@Nullable String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    /**
     * Creates an Address with country only.
     */
    public static Address of(Country country) {
        return new Address(country);
    }

    /**
     * Creates an Address with basic fields.
     */
    public static Address of(@Nullable String street, @Nullable String city, Country country) {
        return new Address(street, city, null, null, country, null);
    }

    /**
     * Creates an Address with state/province.
     */
    public static Address of(@Nullable String street, @Nullable String city,
                             @Nullable String stateOrProvince, Country country) {
        return new Address(street, city, stateOrProvince, null, country, null);
    }

    /**
     * Creates a complete Address.
     */
    public static Address of(@Nullable String street, @Nullable String city,
                             @Nullable String stateOrProvince, @Nullable String postalCode,
                             Country country) {
        return new Address(street, city, stateOrProvince, postalCode, country, null);
    }

    /**
     * Creates a complete Address with address line 2.
     */
    public static Address full(@Nullable String street, @Nullable String addressLine2,
                               @Nullable String city, @Nullable String stateOrProvince,
                               @Nullable String postalCode, Country country) {
        return new Address(street, city, stateOrProvince, postalCode, country, addressLine2);
    }

    /**
     * Builder for creating addresses with fluent API.
     */
    /**
     * Returns the null object representation of Address.
     *
     * @return the NULL address constant
     */
    public static Address nullValue() {
        return NULL;
    }

    public static Builder builder(Country country) {
        return new Builder(country);
    }



    public Optional<String> state() {
        return Optional.ofNullable(stateOrProvince);
    }

    public Optional<String> zipCode() {
        return Optional.ofNullable(postalCode);
    }

    public Optional<String> line2() {
        return Optional.ofNullable(addressLine2);
    }

    // With methods

    public Address withStreet(@Nullable String newStreet) {
        return new Address(newStreet, city, stateOrProvince, postalCode, country, addressLine2);
    }

    public Address withCity(@Nullable String newCity) {
        return new Address(street, newCity, stateOrProvince, postalCode, country, addressLine2);
    }

    public Address withStateOrProvince(@Nullable String newState) {
        return new Address(street, city, newState, postalCode, country, addressLine2);
    }

    public Address withPostalCode(@Nullable String newPostalCode) {
        return new Address(street, city, stateOrProvince, newPostalCode, country, addressLine2);
    }

    public Address withCountry(Country newCountry) {
        return new Address(street, city, stateOrProvince, postalCode, newCountry, addressLine2);
    }

    public Address withAddressLine2(@Nullable String newLine2) {
        return new Address(street, city, stateOrProvince, postalCode, country, newLine2);
    }

    /**
     * Checks if this address has any location details beyond country.
     */
    public boolean hasDetails() {
        return street != null || city != null || stateOrProvince != null || postalCode != null;
    }

    /**
     * Checks if this address is complete (has at least street, city, and country).
     */
    public boolean isComplete() {
        return street != null && city != null;
    }

    /**
     * Formats the address for display with line breaks.
     */
    public String format() {
        return formatWith("\n");
    }

    /**
     * Formats the address with a custom separator.
     */
    public String formatWith(String separator) {
        StringBuilder sb = new StringBuilder();
        boolean needsSeparator = false;

        if (street != null) {
            sb.append(street);
            needsSeparator = true;
        }

        if (addressLine2 != null) {
            if (needsSeparator) sb.append(separator);
            sb.append(addressLine2);
            needsSeparator = true;
        }

        if (city != null || stateOrProvince != null || postalCode != null) {
            if (needsSeparator) sb.append(separator);

            if (city != null) {
                sb.append(city);
            }

            if (stateOrProvince != null) {
                if (city != null) sb.append(", ");
                sb.append(stateOrProvince);
            }

            if (postalCode != null) {
                sb.append(" ").append(postalCode);
            }
            needsSeparator = true;
        }

        if (needsSeparator) sb.append(separator);
        sb.append(country.displayName());

        return sb.toString();
    }

    /**
     * Formats the address as a single line.
     */
    public String singleLine() {
        return formatWith(", ");
    }

    @Override
    public String toString() {
        return hasDetails() ? singleLine() : country.displayName();
    }

    /**
     * Builder for Address with fluent API.
     */
    public static class Builder {
        private final Country country;
        private String street;
        private String city;
        private String stateOrProvince;
        private String postalCode;
        private String addressLine2;

        private Builder(Country country) {
            this.country = Objects.requireNonNull(country, "Country cannot be null");
        }

        public Builder street(String street) {
            this.street = street;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder stateOrProvince(String stateOrProvince) {
            this.stateOrProvince = stateOrProvince;
            return this;
        }

        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public Builder addressLine2(String addressLine2) {
            this.addressLine2 = addressLine2;
            return this;
        }

        public Address build() {
            return new Address(street, city, stateOrProvince, postalCode, country, addressLine2);
        }
    }
}