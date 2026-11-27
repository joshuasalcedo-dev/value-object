package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import io.joshuasalcedo.valueobject.core.ValueObject;

import org.jspecify.annotations.Nullable;

import java.util.Optional;


/**
 * A currency: a {@link FiatCurrency} or a {@link CryptoCoin}.
 */
public interface Currency extends ValueObject {

        /**
         * Looks up a currency by its code, without throwing. Fiat codes win over crypto tickers.
         *
         * @param code an ISO 4217 code or a known crypto ticker, any case, e.g. {@code "usd"}, {@code "BTC"}
         * @return the fiat currency with that code, else the known crypto coin, else empty
         */
        static Optional<Currency> tryParse(@Nullable String code) {
            if (code == null || code.isBlank()) return Optional.empty();
            FiatCurrency fiat = FiatCurrency.fromCode(code);
            if (fiat != FiatCurrency.UNKNOWN) return Optional.of(fiat);
            return CryptoCoin.tryParse(code).map(c -> c);
        }

        /**
         * Returns the currency's code: the ISO 4217 code for a fiat currency, the ticker for a crypto coin.
         *
         * @return the code, e.g. {@code "USD"} or {@code "BTC"}
         */
        String code();

        /**
         * Returns the currency's name.
         *
         * @return the name, e.g. {@code "US Dollar"} or {@code "Bitcoin"}
         */
        String displayName();

        /**
         * Returns the currency's symbol.
         *
         * @return the symbol, e.g. {@code "$"} or {@code "₿"}
         */
        String symbol();

        /**
         * Returns the number of decimal places amounts in this currency are held at: its minor unit.
         *
         * @return the decimal places, e.g. 2 for {@code USD}, 0 for {@code JPY}, 8 for {@code BTC}
         */
        int decimalPlaces();

        /**
         * Returns the kind of currency: {@code "FiatCurrency"} for a fiat currency, {@code "CryptoCoin"} for any
         * crypto coin (known or custom); otherwise the implementing class's simple name.
         *
         * @return the kind of currency
         */
        default String type() {
            return this.getClass().getSimpleName();
        }

        /**
         * Compares currencies by their code alphabetically.
         *
         * @param other the Currency to compare to
         * @return negative if this comes before, zero if equal, positive if after
         * @throws ValueObjectException if {@code other} is {@code null}
         */
        default int compareByCode(Currency other) {
            if (other == null) throw new ValueObjectException("Currency cannot be null");
            return this.code().compareTo(other.code());
        }

        /**
         * Compares currencies by their display name alphabetically.
         *
         * @param other the Currency to compare to
         * @return negative if this comes before, zero if equal, positive if after
         * @throws ValueObjectException if {@code other} is {@code null}
         */
        default int compareByName(Currency other) {
            if (other == null) throw new ValueObjectException("Currency cannot be null");
            return this.displayName().compareToIgnoreCase(other.displayName());
        }

    /**
     * Looks up a currency by its code, as {@link #tryParse(String)}.
     *
     * @param text an ISO 4217 code or a known crypto ticker, any case
     * @return the currency
     * @throws ValueObjectException if it is {@code null} or not a known currency code
     */
    static Currency parse(String text) {
        return tryParse(text).orElseThrow(() -> new ValueObjectException("Not a Currency: '" + text + "'"));
    }

    /**
     * Tells whether text is a known fiat code or crypto ticker.
     *
     * @param text any text
     * @return whether it is a {@link Currency}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
