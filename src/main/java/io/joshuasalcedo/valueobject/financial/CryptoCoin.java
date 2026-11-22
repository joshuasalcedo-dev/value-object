package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A crypto currency. Open, since new coins and tokens appear all the time: the well-known ones are the
 * enum {@link Known}, and any other is a {@link Custom}.
 *
 * <pre>{@code
 * CryptoCoin btc  = CryptoCoin.Known.BTC;
 * CryptoCoin eth  = CryptoCoin.of("eth");                          // Known.ETH
 * CryptoCoin pepe = CryptoCoin.custom("PEPE", "Pepe", "PEPE", 18); // not in Known
 * }</pre>
 *
 * @author JoshuaSalcedo
 */
public interface CryptoCoin extends ValueObject, Currency {

    /**
     * Looks up a {@link Known} coin by its ticker, ignoring case and surrounding spaces.
     *
     * @param code a known coin's ticker, any case, e.g. {@code "btc"}
     * @return that coin
     * @throws ValueObjectException if it is {@code null} or not a {@link Known} coin
     */
    static CryptoCoin of(String code) {
        return tryParse(code).orElseThrow(() -> new ValueObjectException("Unknown crypto coin: " + code));
    }

    /**
     * Looks up a {@link Known} coin by its ticker, ignoring case and surrounding spaces, without throwing.
     * {@link Custom} coins are never found.
     *
     * @param code a ticker, any case, e.g. {@code "btc"}
     * @return the {@link Known} coin with that ticker, or empty
     */
    static Optional<CryptoCoin> tryParse(@Nullable String code) {
        return Known.fromCode(code).map(Function.<CryptoCoin>identity());
    }

    /**
     * Creates a coin that is not in {@link Known}. The code is trimmed and upper-cased; the name and symbol
     * are trimmed.
     *
     * @param code          its ticker, 2-10 letters or digits, e.g. {@code "PEPE"}
     * @param displayName   e.g. {@code "Pepe"}
     * @param symbol        e.g. {@code "PEPE"}
     * @param decimalPlaces its smallest unit, 0-36 places
     * @return a coin that is not in {@link Known}
     * @throws ValueObjectException if any part is {@code null} or invalid, or the code is a {@link Known} one
     */
    static CryptoCoin custom(String code, String displayName, String symbol, int decimalPlaces) {
        return new Custom(code, displayName, symbol, decimalPlaces);
    }

    /**
     * Returns {@code "CryptoCoin"}, for a {@link Known} coin and a {@link Custom} one alike.
     *
     * @return {@code "CryptoCoin"}
     */
    @Override
    default String type() {
        return "CryptoCoin";
    }

    /** The well-known crypto currencies. */
    enum Known implements CryptoCoin {
        /** Bitcoin ({@code BTC}). */
        BTC("BTC", "Bitcoin", "₿", 8),
        /** Ethereum ({@code ETH}). */
        ETH("ETH", "Ethereum", "Ξ", 18),
        /** Tether ({@code USDT}). */
        USDT("USDT", "Tether", "₮", 6),
        /** Bnb ({@code BNB}). */
        BNB("BNB", "BNB", "BNB", 8),
        /** Solana ({@code SOL}). */
        SOL("SOL", "Solana", "SOL", 9),
        /** Xrp ({@code XRP}). */
        XRP("XRP", "XRP", "XRP", 6),
        /** Cardano ({@code ADA}). */
        ADA("ADA", "Cardano", "₳", 6),
        /** Dogecoin ({@code DOGE}). */
        DOGE("DOGE", "Dogecoin", "Ð", 8),
        /** Polygon ({@code MATIC}). */
        MATIC("MATIC", "Polygon", "MATIC", 18),
        /** Polkadot ({@code DOT}). */
        DOT("DOT", "Polkadot", "DOT", 10),
        /** Avalanche ({@code AVAX}). */
        AVAX("AVAX", "Avalanche", "AVAX", 9),
        /** Chainlink ({@code LINK}). */
        LINK("LINK", "Chainlink", "LINK", 18),
        /** Uniswap ({@code UNI}). */
        UNI("UNI", "Uniswap", "UNI", 18),
        /** Litecoin ({@code LTC}). */
        LTC("LTC", "Litecoin", "Ł", 8),
        /** Cosmos ({@code ATOM}). */
        ATOM("ATOM", "Cosmos", "ATOM", 6),
        /** USD Coin ({@code USDC}). */
        USDC("USDC", "USD Coin", "USDC", 6),
        /** TRON ({@code TRX}). */
        TRX("TRX", "TRON", "TRX", 6),
        /** Toncoin ({@code TON}). */
        TON("TON", "Toncoin", "TON", 9),
        /** Stellar ({@code XLM}). */
        XLM("XLM", "Stellar", "XLM", 7),
        /** Bitcoin Cash ({@code BCH}). */
        BCH("BCH", "Bitcoin Cash", "BCH", 8),
        /** Shiba Inu ({@code SHIB}). */
        SHIB("SHIB", "Shiba Inu", "SHIB", 18),
        /** Dai ({@code DAI}). */
        DAI("DAI", "Dai", "DAI", 18);

        private static final Map<String, Known> BY_CODE = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(Known::code, Function.identity()));

        private final String code;
        private final String displayName;
        private final String symbol;
        private final int decimalPlaces;

        Known(String code, String displayName, String symbol, int decimalPlaces) {
            this.code = code;
            this.displayName = displayName;
            this.symbol = symbol;
            this.decimalPlaces = decimalPlaces;
        }

        /**
         * Looks up a known coin by its ticker, ignoring case and surrounding spaces.
         *
         * @param code a ticker, any case
         * @return the known coin with that ticker, or empty
         */
        public static Optional<Known> fromCode(@Nullable String code) {
            if (code == null) return Optional.empty();
            return Optional.ofNullable(BY_CODE.get(code.trim().toUpperCase(Locale.ROOT)));
        }

        @Override
        public String code() {
            return code;
        }

        @Override
        public String displayName() {
            return displayName;
        }

        @Override
        public String symbol() {
            return symbol;
        }

        @Override
        public int decimalPlaces() {
            return decimalPlaces;
        }
    }

    /**
     * A coin or token that is not in {@link Known}.
     *
     * @param code          its ticker, upper-cased, 2-10 letters or digits
     * @param displayName   its name
     * @param symbol        its symbol
     * @param decimalPlaces its smallest unit, 0-36 places
     */
    record Custom(String code, String displayName, String symbol, int decimalPlaces) implements CryptoCoin {

        private static final Pattern CODE = Pattern.compile("^[A-Z0-9]{2,10}$");

        /**
         * Checks and normalises the coin: the code is trimmed and upper-cased, the name and symbol trimmed.
         *
         * @param code          its ticker, 2-10 letters or digits
         * @param displayName   its name, not blank
         * @param symbol        its symbol, not blank
         * @param decimalPlaces its smallest unit, 0-36 places
         * @throws ValueObjectException if any part is {@code null} or invalid, or the code is a {@link Known} one
         */
        public Custom {
            if (code == null || displayName == null || symbol == null)
                throw new ValueObjectException("Crypto coin code, name and symbol are required");
            code = code.trim().toUpperCase(Locale.ROOT);
            displayName = displayName.trim();
            symbol = symbol.trim();
            if (!CODE.matcher(code).matches())
                throw new ValueObjectException("Crypto coin code must be 2-10 letters or digits: " + code);
            if (Known.fromCode(code).isPresent())
                throw new ValueObjectException("Crypto coin " + code + " is a known coin; use CryptoCoin.Known." + code);
            if (displayName.isEmpty() || symbol.isEmpty())
                throw new ValueObjectException("Crypto coin name and symbol must not be blank");
            if (decimalPlaces < 0 || decimalPlaces > 36)
                throw new ValueObjectException("Crypto coin decimal places must be 0-36: " + decimalPlaces);
        }

        /**
         * Returns {@code "CryptoCoin"}.
         *
         * @return {@code "CryptoCoin"}
         */
        @Override
        public String type() {
            return "CryptoCoin";
        }

        /**
         * Returns the ticker, e.g. {@code "PEPE"}.
         *
         * @return the code
         */
        @Override
        public String toString() {
            return code;
        }
    }

    /**
     * Looks up a {@link Known} coin by its ticker, the same as {@link #of(String)}.
     *
     * @param text a known coin's ticker, any case
     * @return that coin
     * @throws ValueObjectException if it is {@code null} or not a {@link Known} coin
     */
    static CryptoCoin parse(String text) {
        return tryParse(text).orElseThrow(() -> new ValueObjectException("Not a CryptoCoin: '" + text + "'"));
    }

    /**
     * Tells whether text is the ticker of a {@link Known} coin.
     *
     * @param text any text
     * @return whether it is a {@link CryptoCoin}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
