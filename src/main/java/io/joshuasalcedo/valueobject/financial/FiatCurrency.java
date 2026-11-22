package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.ValueObject;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Fiat currency enumeration based on ISO 4217.
 *
 * <p>Contains all active ISO 4217 currency codes with their numeric code,
 * display name, symbol, and decimal places (minor unit).</p>
 *
 * @see <a href="https://www.iso.org/iso-4217-currency-codes.html">ISO 4217</a>
 * @see <a href="https://www.iban.com/currency-codes">IBAN Currency Codes</a>
 * @author JoshuaSalcedo
 */
public enum FiatCurrency implements ValueObject, Currency {

    // ── Unknown / Default ───────────────────────────────────────────────────
    UNKNOWN("XXX", 999, "Unknown Currency", "?", 0),

    // ── A ────────────────────────────────────────────────────────────────────
    AED("AED", 784, "UAE Dirham", "د.إ", 2),
    AFN("AFN", 971, "Afghani", "؋", 2),
    ALL("ALL", 8, "Lek", "L", 2),
    AMD("AMD", 51, "Armenian Dram", "֏", 2),
    ANG("ANG", 532, "Netherlands Antillean Guilder", "ƒ", 2),
    AOA("AOA", 973, "Kwanza", "Kz", 2),
    ARS("ARS", 32, "Argentine Peso", "$", 2),
    AUD("AUD", 36, "Australian Dollar", "A$", 2),
    AWG("AWG", 533, "Aruban Florin", "ƒ", 2),
    AZN("AZN", 944, "Azerbaijanian Manat", "₼", 2),

    // ── B ────────────────────────────────────────────────────────────────────
    BAM("BAM", 977, "Convertible Mark", "KM", 2),
    BBD("BBD", 52, "Barbados Dollar", "Bds$", 2),
    BDT("BDT", 50, "Taka", "৳", 2),
    BGN("BGN", 975, "Bulgarian Lev", "лв", 2),
    BHD("BHD", 48, "Bahraini Dinar", ".د.ب", 3),
    BIF("BIF", 108, "Burundi Franc", "FBu", 0),
    BMD("BMD", 60, "Bermudian Dollar", "$", 2),
    BND("BND", 96, "Brunei Dollar", "B$", 2),
    BOB("BOB", 68, "Boliviano", "Bs.", 2),
    BRL("BRL", 986, "Brazilian Real", "R$", 2),
    BSD("BSD", 44, "Bahamian Dollar", "B$", 2),
    BTN("BTN", 64, "Ngultrum", "Nu.", 2),
    BWP("BWP", 72, "Pula", "P", 2),
    BYN("BYN", 933, "Belarussian Ruble", "Br", 2),
    BZD("BZD", 84, "Belize Dollar", "BZ$", 2),

    // ── C ────────────────────────────────────────────────────────────────────
    CAD("CAD", 124, "Canadian Dollar", "C$", 2),
    CDF("CDF", 976, "Congolese Franc", "FC", 2),
    CHF("CHF", 756, "Swiss Franc", "CHF", 2),
    CLP("CLP", 152, "Chilean Peso", "$", 0),
    CNY("CNY", 156, "Chinese Yuan", "¥", 2),
    COP("COP", 170, "Colombian Peso", "$", 2),
    CRC("CRC", 188, "Costa Rican Colon", "₡", 2),
    CUP("CUP", 192, "Cuban Peso", "₱", 2),
    CVE("CVE", 132, "Cabo Verde Escudo", "$", 2),
    CZK("CZK", 203, "Czech Koruna", "Kč", 2),

    // ── D ────────────────────────────────────────────────────────────────────
    DJF("DJF", 262, "Djibouti Franc", "Fdj", 0),
    DKK("DKK", 208, "Danish Krone", "kr", 2),
    DOP("DOP", 214, "Dominican Peso", "RD$", 2),
    DZD("DZD", 12, "Algerian Dinar", "د.ج", 2),

    // ── E ────────────────────────────────────────────────────────────────────
    EGP("EGP", 818, "Egyptian Pound", "E£", 2),
    ERN("ERN", 232, "Nakfa", "Nfk", 2),
    ETB("ETB", 230, "Ethiopian Birr", "Br", 2),
    EUR("EUR", 978, "Euro", "€", 2),

    // ── F ────────────────────────────────────────────────────────────────────
    FJD("FJD", 242, "Fiji Dollar", "FJ$", 2),
    FKP("FKP", 238, "Falkland Islands Pound", "£", 2),

    // ── G ────────────────────────────────────────────────────────────────────
    GBP("GBP", 826, "Pound Sterling", "£", 2),
    GEL("GEL", 981, "Lari", "₾", 2),
    GHS("GHS", 936, "Ghana Cedi", "GH₵", 2),
    GIP("GIP", 292, "Gibraltar Pound", "£", 2),
    GMD("GMD", 270, "Dalasi", "D", 2),
    GNF("GNF", 324, "Guinea Franc", "FG", 0),
    GTQ("GTQ", 320, "Quetzal", "Q", 2),
    GYD("GYD", 328, "Guyana Dollar", "G$", 2),

    // ── H ────────────────────────────────────────────────────────────────────
    HKD("HKD", 344, "Hong Kong Dollar", "HK$", 2),
    HNL("HNL", 340, "Lempira", "L", 2),
    HTG("HTG", 332, "Gourde", "G", 2),
    HUF("HUF", 348, "Forint", "Ft", 2),

    // ── I ────────────────────────────────────────────────────────────────────
    IDR("IDR", 360, "Rupiah", "Rp", 2),
    ILS("ILS", 376, "New Israeli Sheqel", "₪", 2),
    INR("INR", 356, "Indian Rupee", "₹", 2),
    IQD("IQD", 368, "Iraqi Dinar", "ع.د", 3),
    IRR("IRR", 364, "Iranian Rial", "﷼", 2),
    ISK("ISK", 352, "Iceland Krona", "kr", 0),

    // ── J ────────────────────────────────────────────────────────────────────
    JMD("JMD", 388, "Jamaican Dollar", "J$", 2),
    JOD("JOD", 400, "Jordanian Dinar", "د.ا", 3),
    JPY("JPY", 392, "Japanese Yen", "¥", 0),

    // ── K ────────────────────────────────────────────────────────────────────
    KES("KES", 404, "Kenyan Shilling", "KSh", 2),
    KGS("KGS", 417, "Som", "лв", 2),
    KHR("KHR", 116, "Riel", "៛", 2),
    KMF("KMF", 174, "Comoro Franc", "CF", 0),
    KPW("KPW", 408, "North Korean Won", "₩", 2),
    KRW("KRW", 410, "South Korean Won", "₩", 0),
    KWD("KWD", 414, "Kuwaiti Dinar", "د.ك", 3),
    KYD("KYD", 136, "Cayman Islands Dollar", "CI$", 2),
    KZT("KZT", 398, "Tenge", "₸", 2),

    // ── L ────────────────────────────────────────────────────────────────────
    LAK("LAK", 418, "Kip", "₭", 2),
    LBP("LBP", 422, "Lebanese Pound", "L£", 2),
    LKR("LKR", 144, "Sri Lanka Rupee", "Rs", 2),
    LRD("LRD", 430, "Liberian Dollar", "L$", 2),
    LSL("LSL", 426, "Loti", "M", 2),
    LYD("LYD", 434, "Libyan Dinar", "ل.د", 3),

    // ── M ────────────────────────────────────────────────────────────────────
    MAD("MAD", 504, "Moroccan Dirham", "MAD", 2),
    MDL("MDL", 498, "Moldovan Leu", "L", 2),
    MGA("MGA", 969, "Malagasy Ariary", "Ar", 2),
    MKD("MKD", 807, "Denar", "ден", 2),
    MMK("MMK", 104, "Kyat", "K", 2),
    MNT("MNT", 496, "Tugrik", "₮", 2),
    MOP("MOP", 446, "Pataca", "MOP$", 2),
    MRU("MRU", 929, "Ouguiya", "UM", 2),
    MUR("MUR", 480, "Mauritius Rupee", "₨", 2),
    MVR("MVR", 462, "Rufiyaa", "Rf", 2),
    MWK("MWK", 454, "Kwacha", "MK", 2),
    MXN("MXN", 484, "Mexican Peso", "MX$", 2),
    MYR("MYR", 458, "Malaysian Ringgit", "RM", 2),
    MZN("MZN", 943, "Mozambique Metical", "MT", 2),

    // ── N ────────────────────────────────────────────────────────────────────
    NAD("NAD", 516, "Namibia Dollar", "N$", 2),
    NGN("NGN", 566, "Naira", "₦", 2),
    NIO("NIO", 558, "Cordoba Oro", "C$", 2),
    NOK("NOK", 578, "Norwegian Krone", "kr", 2),
    NPR("NPR", 524, "Nepalese Rupee", "₨", 2),
    NZD("NZD", 554, "New Zealand Dollar", "NZ$", 2),

    // ── O ────────────────────────────────────────────────────────────────────
    OMR("OMR", 512, "Rial Omani", "ر.ع.", 3),

    // ── P ────────────────────────────────────────────────────────────────────
    PAB("PAB", 590, "Balboa", "B/.", 2),
    PEN("PEN", 604, "Nuevo Sol", "S/.", 2),
    PGK("PGK", 598, "Kina", "K", 2),
    PHP("PHP", 608, "Philippine Peso", "₱", 2),
    PKR("PKR", 586, "Pakistan Rupee", "₨", 2),
    PLN("PLN", 985, "Zloty", "zł", 2),
    PYG("PYG", 600, "Guarani", "₲", 0),

    // ── Q ────────────────────────────────────────────────────────────────────
    QAR("QAR", 634, "Qatari Rial", "ر.ق", 2),

    // ── R ────────────────────────────────────────────────────────────────────
    RON("RON", 946, "Romanian Leu", "lei", 2),
    RSD("RSD", 941, "Serbian Dinar", "дин.", 2),
    RUB("RUB", 643, "Russian Ruble", "₽", 2),
    RWF("RWF", 646, "Rwanda Franc", "RF", 0),

    // ── S ────────────────────────────────────────────────────────────────────
    SAR("SAR", 682, "Saudi Riyal", "ر.س", 2),
    SBD("SBD", 90, "Solomon Islands Dollar", "SI$", 2),
    SCR("SCR", 690, "Seychelles Rupee", "₨", 2),
    SDG("SDG", 938, "Sudanese Pound", "ج.س.", 2),
    SEK("SEK", 752, "Swedish Krona", "kr", 2),
    SGD("SGD", 702, "Singapore Dollar", "S$", 2),
    SHP("SHP", 654, "Saint Helena Pound", "£", 2),
    SLE("SLE", 925, "Leone", "Le", 2),
    SOS("SOS", 706, "Somali Shilling", "Sh", 2),
    SRD("SRD", 968, "Surinam Dollar", "$", 2),
    SSP("SSP", 728, "South Sudanese Pound", "£", 2),
    STN("STN", 930, "Dobra", "Db", 2),
    SVC("SVC", 222, "El Salvador Colon", "₡", 2),
    SYP("SYP", 760, "Syrian Pound", "£S", 2),
    SZL("SZL", 748, "Lilangeni", "E", 2),

    // ── T ────────────────────────────────────────────────────────────────────
    THB("THB", 764, "Baht", "฿", 2),
    TJS("TJS", 972, "Somoni", "SM", 2),
    TMT("TMT", 934, "Turkmenistan New Manat", "T", 2),
    TND("TND", 788, "Tunisian Dinar", "د.ت", 3),
    TOP("TOP", 776, "Pa'anga", "T$", 2),
    TRY("TRY", 949, "Turkish Lira", "₺", 2),
    TTD("TTD", 780, "Trinidad and Tobago Dollar", "TT$", 2),
    TWD("TWD", 901, "New Taiwan Dollar", "NT$", 2),
    TZS("TZS", 834, "Tanzanian Shilling", "TSh", 2),

    // ── U ────────────────────────────────────────────────────────────────────
    UAH("UAH", 980, "Hryvnia", "₴", 2),
    UGX("UGX", 800, "Uganda Shilling", "USh", 0),
    USD("USD", 840, "US Dollar", "$", 2),
    UYU("UYU", 858, "Peso Uruguayo", "$U", 2),
    UZS("UZS", 860, "Uzbekistan Sum", "сўм", 2),

    // ── V ────────────────────────────────────────────────────────────────────
    VED("VED", 926, "Bolivar Digital", "Bs.D", 2),
    VES("VES", 928, "Bolivar Soberano", "Bs.S", 2),
    VND("VND", 704, "Dong", "₫", 0),
    VUV("VUV", 548, "Vatu", "VT", 0),

    // ── W ────────────────────────────────────────────────────────────────────
    WST("WST", 882, "Tala", "WS$", 2),

    // ── X (Regional / Supranational) ────────────────────────────────────────
    XAF("XAF", 950, "CFA Franc BEAC", "FCFA", 0),
    XCD("XCD", 951, "East Caribbean Dollar", "EC$", 2),
    XOF("XOF", 952, "CFA Franc BCEAO", "CFA", 0),
    XPF("XPF", 953, "CFP Franc", "₣", 0),

    // ── Y ────────────────────────────────────────────────────────────────────
    YER("YER", 886, "Yemeni Rial", "﷼", 2),

    // ── Z ────────────────────────────────────────────────────────────────────
    ZAR("ZAR", 710, "South African Rand", "R", 2),
    ZMW("ZMW", 967, "Zambian Kwacha", "ZK", 2),
    ZWL("ZWL", 932, "Zimbabwe Dollar", "Z$", 2);

    // ── Fields ──────────────────────────────────────────────────────────────

    private final String code;
    private final int numericCode;
    private final String displayName;
    private final String symbol;
    private final int decimalPlaces;

    // ── Lookup caches ───────────────────────────────────────────────────────

    private static final Map<String, FiatCurrency> BY_CODE =
        Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(FiatCurrency::code, Function.identity()));

    private static final Map<Integer, FiatCurrency> BY_NUMERIC =
        Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                FiatCurrency::numericCode, Function.identity(),
                (a, b) -> a // first wins on duplicate numeric codes
            ));

    // ── Constructor ─────────────────────────────────────────────────────────

    FiatCurrency(String code, int numericCode, String displayName, String symbol, int decimalPlaces) {
        this.code = code;
        this.numericCode = numericCode;
        this.displayName = displayName;
        this.symbol = symbol;
        this.decimalPlaces = decimalPlaces;
    }

    // ── Currency interface ───────────────────────────────────────────────────

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

    /**
     * Returns the ISO 4217 numeric code.
     */
    public int numericCode() {
        return numericCode;
    }

    // ── Queries ─────────────────────────────────────────────────────────────

    /**
     * Whether this is the unknown/placeholder currency.
     */
    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    /**
     * Whether this currency has minor units (decimal places > 0).
     */
    public boolean hasMinorUnit() {
        return decimalPlaces > 0;
    }

    /**
     * Whether this is a 3-decimal dinar-type currency (BHD, IQD, JOD, KWD, LYD, OMR, TND).
     */
    public boolean isThreeDecimal() {
        return decimalPlaces == 3;
    }

    /**
     * Whether this is a zero-decimal currency (JPY, KRW, etc.).
     */
    public boolean isZeroDecimal() {
        return decimalPlaces == 0 && this != UNKNOWN;
    }

    // ── Lookup Methods ──────────────────────────────────────────────────────

    /**
     * Looks up a currency by its ISO 4217 alpha code (case-insensitive).
     *
     * @param code the 3-letter code, e.g. "USD", "eur"
     * @return the matching FiatCurrency
     */
    public static FiatCurrency fromCode(String code) {
        if (code == null || code.isBlank()) return UNKNOWN;
        FiatCurrency result = BY_CODE.get(code.toUpperCase().trim());
        return result != null ? result : UNKNOWN;
    }

    /**
     * Looks up a currency by its ISO 4217 numeric code.
     *
     * @param numericCode the numeric code, e.g. 840 for USD
     * @return the matching FiatCurrency
     */
    /**
     * The currency a country uses, from the JDK's ISO 4217 data.
     *
     * @param country e.g. {@code Country.PHILIPPINES}
     * @return its currency, e.g. PHP; empty for {@code Country.UNKNOWN} or a territory without one
     */
    public static Optional<FiatCurrency> forCountry(io.joshuasalcedo.valueobject.geo.Country country) {
        if (country == null || country == io.joshuasalcedo.valueobject.geo.Country.UNKNOWN) return Optional.empty();
        try {
            java.util.Currency c = java.util.Currency.getInstance(Locale.of("", country.code()));
            return c == null ? Optional.empty() : findByCode(c.getCurrencyCode());
        } catch (IllegalArgumentException noCurrency) {
            return Optional.empty();
        }
    }

    public static FiatCurrency fromNumericCode(int numericCode) {
        FiatCurrency result = BY_NUMERIC.get(numericCode);
        return result != null ? result : UNKNOWN;
    }

    /**
     * Looks up a currency by code, returning Optional.
     *
     * @param code the 3-letter code
     * @return Optional containing the currency, or empty if not found
     */
    public static Optional<FiatCurrency> findByCode(@Nullable String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        FiatCurrency result = BY_CODE.get(code.toUpperCase().trim());
        return result != null && result != UNKNOWN ? Optional.of(result) : Optional.empty();
    }

    /**
     * Checks if a given code is a valid ISO 4217 currency code.
     *
     * @param code the code to check
     * @return true if valid
     */
    public static boolean isValidCode(@Nullable String code) {
        if (code == null || code.isBlank()) return false;
        FiatCurrency result = BY_CODE.get(code.toUpperCase().trim());
        return result != null && result != UNKNOWN;
    }

    /**
     * Returns the default currency (UNKNOWN).
     */
    public static FiatCurrency defaultCurrency() {
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return "%s (%s)".formatted(code, displayName);
    }
}