package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.ValueObject;

import org.jspecify.annotations.Nullable;

import java.util.Optional;


/**
 * A currency: a {@link FiatCurrency} or a {@link CryptoCoin}.
 */
public interface Currency extends ValueObject {

        /**
         * @param code an ISO 4217 code or a known crypto ticker, any case, e.g. {@code "usd"}, {@code "BTC"}
         * @return the fiat currency with that code, else the known crypto coin, else empty
         */
        static Optional<Currency> parse(@Nullable String code) {
            if (code == null || code.isBlank()) return Optional.empty();
            FiatCurrency fiat = FiatCurrency.fromCode(code);
            if (fiat != FiatCurrency.UNKNOWN) return Optional.of(fiat);
            return CryptoCoin.parse(code).map(c -> c);
        }

        String code();
        String displayName();
        String symbol();
        int decimalPlaces();

        default String type() {
            return this.getClass().getSimpleName();
        }

        /**
         * Compares currencies by their code alphabetically.
         *
         * @param other the Currency to compare to
         * @return negative if this comes before, zero if equal, positive if after
         */
        default int compareByCode(Currency other) {
            return this.code().compareTo(other.code());
        }

        /**
         * Compares currencies by their display name alphabetically.
         *
         * @param other the Currency to compare to
         * @return negative if this comes before, zero if equal, positive if after
         */
        default int compareByName(Currency other) {
            return this.displayName().compareToIgnoreCase(other.displayName());
        }
    }
