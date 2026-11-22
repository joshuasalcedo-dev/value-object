package io.joshuasalcedo.valueobject.contact;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.geo.Country;
import org.jspecify.annotations.Nullable;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import java.util.Optional;

/**
 * A postal address. Only the country is required; every other part is optional. Text parts are trimmed, and
 * blank ones are stored as {@code null}.
 *
 * <pre>{@code
 * Address a = Address.builder(Country.PHILIPPINES)
 *         .street("12 Mabini St").city("Makati").postalCode("1200").build();
 * a.singleLine();   // "12 Mabini St, Makati 1200, Philippines"
 * }</pre>
 *
 * @param street          the street line, or {@code null}
 * @param city            the city, or {@code null}
 * @param stateOrProvince the state, province or region, or {@code null}
 * @param postalCode      the postal or ZIP code, as written, or {@code null}
 * @param country         the country; required
 * @param addressLine2    a second address line (unit, floor, building), or {@code null}
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


    /**
     * Creates an address of a country only.
     *
     * @param country the country
     * @throws ValueObjectException if {@code country} is {@code null}
     */
    public Address(Country country) {
        this(null, null, null, null, country, null);
    }

    /**
     * Creates an address of a street, a city and a country.
     *
     * @param street  the street line, or {@code null}
     * @param city    the city, or {@code null}
     * @param country the country
     * @throws ValueObjectException if {@code country} is {@code null}
     */
    public Address(String street, String city, Country country) {
        this(street, city, null, null, country, null);
    }

    

    /**
     * Creates an address, trimming every text part and turning blank ones into {@code null}.
     *
     * @throws ValueObjectException if {@code country} is {@code null}
     */
    public Address {
        if (country == null) {
            throw new ValueObjectException("Country cannot be null");
        }

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
     * Creates an address of a country only.
     *
     * @param country the country
     * @return the address
     * @throws ValueObjectException if {@code country} is {@code null}
     */
    public static Address of(Country country) {
        return new Address(country);
    }

    /**
     * Creates an address of a street, a city and a country.
     *
     * @param street  the street line, or {@code null}
     * @param city    the city, or {@code null}
     * @param country the country
     * @return the address
     * @throws ValueObjectException if {@code country} is {@code null}
     */
    public static Address of(@Nullable String street, @Nullable String city, Country country) {
        return new Address(street, city, null, null, country, null);
    }

    /**
     * Creates an address of a street, a city, a state or province and a country.
     *
     * @param street          the street line, or {@code null}
     * @param city            the city, or {@code null}
     * @param stateOrProvince the state, province or region, or {@code null}
     * @param country         the country
     * @return the address
     * @throws ValueObjectException if {@code country} is {@code null}
     */
    public static Address of(@Nullable String street, @Nullable String city,
                             @Nullable String stateOrProvince, Country country) {
        return new Address(street, city, stateOrProvince, null, country, null);
    }

    /**
     * Creates an address with every part except a second address line.
     *
     * @param street          the street line, or {@code null}
     * @param city            the city, or {@code null}
     * @param stateOrProvince the state, province or region, or {@code null}
     * @param postalCode      the postal or ZIP code, or {@code null}
     * @param country         the country
     * @return the address
     * @throws ValueObjectException if {@code country} is {@code null}
     */
    public static Address of(@Nullable String street, @Nullable String city,
                             @Nullable String stateOrProvince, @Nullable String postalCode,
                             Country country) {
        return new Address(street, city, stateOrProvince, postalCode, country, null);
    }

    /**
     * Creates an address with every part. Note the order: the second address line follows the street.
     *
     * @param street          the street line, or {@code null}
     * @param addressLine2    a second address line, or {@code null}
     * @param city            the city, or {@code null}
     * @param stateOrProvince the state, province or region, or {@code null}
     * @param postalCode      the postal or ZIP code, or {@code null}
     * @param country         the country
     * @return the address
     * @throws ValueObjectException if {@code country} is {@code null}
     */
    public static Address full(@Nullable String street, @Nullable String addressLine2,
                               @Nullable String city, @Nullable String stateOrProvince,
                               @Nullable String postalCode, Country country) {
        return new Address(street, city, stateOrProvince, postalCode, country, addressLine2);
    }

    /**
     * Starts a {@link Builder} for an address in a country.
     *
     * @param country the country
     * @return a builder with every other part unset
     * @throws ValueObjectException if {@code country} is {@code null}
     */
    public static Builder builder(Country country) {
        return new Builder(country);
    }



    /** {@return the state, province or region, if there is one} */
    public Optional<String> state() {
        return Optional.ofNullable(stateOrProvince);
    }

    /** {@return the postal or ZIP code, if there is one} */
    public Optional<String> zipCode() {
        return Optional.ofNullable(postalCode);
    }

    /** {@return the second address line, if there is one} */
    public Optional<String> line2() {
        return Optional.ofNullable(addressLine2);
    }

    // With methods

    /**
     * Returns a copy with another street line.
     *
     * @param newStreet the street line, or {@code null} to remove it
     * @return the copy
     */
    public Address withStreet(@Nullable String newStreet) {
        return new Address(newStreet, city, stateOrProvince, postalCode, country, addressLine2);
    }

    /**
     * Returns a copy with another city.
     *
     * @param newCity the city, or {@code null} to remove it
     * @return the copy
     */
    public Address withCity(@Nullable String newCity) {
        return new Address(street, newCity, stateOrProvince, postalCode, country, addressLine2);
    }

    /**
     * Returns a copy with another state or province.
     *
     * @param newState the state, province or region, or {@code null} to remove it
     * @return the copy
     */
    public Address withStateOrProvince(@Nullable String newState) {
        return new Address(street, city, newState, postalCode, country, addressLine2);
    }

    /**
     * Returns a copy with another postal code.
     *
     * @param newPostalCode the postal or ZIP code, or {@code null} to remove it
     * @return the copy
     */
    public Address withPostalCode(@Nullable String newPostalCode) {
        return new Address(street, city, stateOrProvince, newPostalCode, country, addressLine2);
    }

    /**
     * Returns a copy in another country.
     *
     * @param newCountry the country
     * @return the copy
     * @throws ValueObjectException if {@code newCountry} is {@code null}
     */
    public Address withCountry(Country newCountry) {
        return new Address(street, city, stateOrProvince, postalCode, newCountry, addressLine2);
    }

    /**
     * Returns a copy with another second address line.
     *
     * @param newLine2 the second address line, or {@code null} to remove it
     * @return the copy
     */
    public Address withAddressLine2(@Nullable String newLine2) {
        return new Address(street, city, stateOrProvince, postalCode, country, newLine2);
    }

    /**
     * Tells whether the address has anything besides the country: a street, a second address line, a city, a
     * state or province, or a postal code.
     *
     * @return whether it has any of those parts
     */
    public boolean hasDetails() {
        return street != null || addressLine2 != null || city != null || stateOrProvince != null
                || postalCode != null;
    }

    /**
     * Tells whether the address has at least a street and a city (it always has a country).
     *
     * @return whether both are set
     */
    public boolean isComplete() {
        return street != null && city != null;
    }

    /**
     * Formats the address on several lines, as {@link #formatWith(String)} with {@code "\n"}.
     *
     * @return the address, one part per line
     */
    public String format() {
        return formatWith("\n");
    }

    /**
     * Formats the address with a separator between its lines: the street, the second address line,
     * {@code "city, state postalCode"}, then the country's display name. Missing parts are left out together
     * with the punctuation around them, so a postal code without a city or state stands alone.
     *
     * @param separator what goes between the lines, e.g. {@code "\n"} or {@code ", "}
     * @return the formatted address
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
                if (city != null || stateOrProvince != null) sb.append(" ");
                sb.append(postalCode);
            }
            needsSeparator = true;
        }

        if (needsSeparator) sb.append(separator);
        sb.append(country.displayName());

        return sb.toString();
    }

    /**
     * Formats the address on one line, as {@link #formatWith(String)} with {@code ", "}.
     *
     * @return the address on one line
     */
    public String singleLine() {
        return formatWith(", ");
    }

    @Override
    public String toString() {
        return hasDetails() ? singleLine() : country.displayName();
    }

    /**
     * A fluent builder for an {@link Address}; start one with {@link Address#builder(Country)}.
     */
    public static class Builder {
        private final Country country;
        private String street;
        private String city;
        private String stateOrProvince;
        private String postalCode;
        private String addressLine2;

        private Builder(Country country) {
            if (country == null) {
                throw new ValueObjectException("Country cannot be null");
            }
            this.country = country;
        }

        /**
         * Sets the street line.
         *
         * @param street the street line
         * @return this builder
         */
        public Builder street(String street) {
            this.street = street;
            return this;
        }

        /**
         * Sets the city.
         *
         * @param city the city
         * @return this builder
         */
        public Builder city(String city) {
            this.city = city;
            return this;
        }

        /**
         * Sets the state or province.
         *
         * @param stateOrProvince the state, province or region
         * @return this builder
         */
        public Builder stateOrProvince(String stateOrProvince) {
            this.stateOrProvince = stateOrProvince;
            return this;
        }

        /**
         * Sets the postal code.
         *
         * @param postalCode the postal or ZIP code
         * @return this builder
         */
        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        /**
         * Sets the second address line.
         *
         * @param addressLine2 the second address line (unit, floor, building)
         * @return this builder
         */
        public Builder addressLine2(String addressLine2) {
            this.addressLine2 = addressLine2;
            return this;
        }

        /** {@return the address built from the parts set so far} */
        public Address build() {
            return new Address(street, city, stateOrProvince, postalCode, country, addressLine2);
        }
    }
}