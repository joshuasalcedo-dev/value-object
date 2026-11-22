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
     * @param code a known coin's ticker, any case, e.g. {@code "btc"}
     * @return that coin
     * @throws ValueObjectException if it is not a {@link Known} coin
     */
    static CryptoCoin of(String code) {
        return parse(code).orElseThrow(() -> new ValueObjectException("Unknown crypto coin: " + code));
    }

    /**
     * @param code a ticker, any case, e.g. {@code "btc"}
     * @return the {@link Known} coin with that ticker, or empty
     */
    static Optional<CryptoCoin> parse(@Nullable String code) {
        return Known.fromCode(code).map(Function.<CryptoCoin>identity());
    }

    /**
     * @param code          its ticker, 2-10 letters or digits, e.g. {@code "PEPE"}
     * @param displayName   e.g. {@code "Pepe"}
     * @param symbol        e.g. {@code "PEPE"}
     * @param decimalPlaces its smallest unit, 0-36 places
     * @return a coin that is not in {@link Known}
     * @throws ValueObjectException if any part is invalid
     */
    static CryptoCoin custom(String code, String displayName, String symbol, int decimalPlaces) {
        return new Custom(code, displayName, symbol, decimalPlaces);
    }

    /** The well-known crypto currencies. */
    enum Known implements CryptoCoin {
        BTC("BTC", "Bitcoin", "₿", 8),
        ETH("ETH", "Ethereum", "Ξ", 18),
        USDT("USDT", "Tether", "₮", 6),
        BNB("BNB", "BNB", "BNB", 8),
        SOL("SOL", "Solana", "SOL", 9),
        XRP("XRP", "XRP", "XRP", 6),
        ADA("ADA", "Cardano", "₳", 6),
        DOGE("DOGE", "Dogecoin", "Ð", 8),
        MATIC("MATIC", "Polygon", "MATIC", 18),
        DOT("DOT", "Polkadot", "DOT", 10),
        AVAX("AVAX", "Avalanche", "AVAX", 9),
        LINK("LINK", "Chainlink", "LINK", 18),
        UNI("UNI", "Uniswap", "UNI", 18),
        LTC("LTC", "Litecoin", "Ł", 8),
        ATOM("ATOM", "Cosmos", "ATOM", 6),
        USDC("USDC", "USD Coin", "USDC", 6),
        TRX("TRX", "TRON", "TRX", 6),
        TON("TON", "Toncoin", "TON", 9),
        XLM("XLM", "Stellar", "XLM", 7),
        BCH("BCH", "Bitcoin Cash", "BCH", 8),
        SHIB("SHIB", "Shiba Inu", "SHIB", 18),
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
         * @throws ValueObjectException if any part is invalid, or the code is a {@link Known} one
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

        @Override
        public String type() {
            return "CryptoCoin";
        }

        @Override
        public String toString() {
            return code;
        }
    }
}
