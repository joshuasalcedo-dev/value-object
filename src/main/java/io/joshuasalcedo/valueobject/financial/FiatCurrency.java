package io.joshuasalcedo.valueobject.financial;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import io.joshuasalcedo.valueobject.core.ValueObject;

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
    /** Placeholder for an unknown or missing currency ({@code XXX}, symbol {@code ?}, no decimal places). */
    UNKNOWN("XXX", 999, "Unknown Currency", "?", 0),

    // ── A ────────────────────────────────────────────────────────────────────
    /** UAE Dirham ({@code AED}). */
    AED("AED", 784, "UAE Dirham", "د.إ", 2),
    /** Afghani ({@code AFN}). */
    AFN("AFN", 971, "Afghani", "؋", 2),
    /** Lek ({@code ALL}). */
    ALL("ALL", 8, "Lek", "L", 2),
    /** Armenian Dram ({@code AMD}). */
    AMD("AMD", 51, "Armenian Dram", "֏", 2),
    /** Netherlands Antillean Guilder ({@code ANG}). */
    ANG("ANG", 532, "Netherlands Antillean Guilder", "ƒ", 2),
    /** Kwanza ({@code AOA}). */
    AOA("AOA", 973, "Kwanza", "Kz", 2),
    /** Argentine Peso ({@code ARS}). */
    ARS("ARS", 32, "Argentine Peso", "$", 2),
    /** Australian Dollar ({@code AUD}). */
    AUD("AUD", 36, "Australian Dollar", "A$", 2),
    /** Aruban Florin ({@code AWG}). */
    AWG("AWG", 533, "Aruban Florin", "ƒ", 2),
    /** Azerbaijanian Manat ({@code AZN}). */
    AZN("AZN", 944, "Azerbaijanian Manat", "₼", 2),

    // ── B ────────────────────────────────────────────────────────────────────
    /** Convertible Mark ({@code BAM}). */
    BAM("BAM", 977, "Convertible Mark", "KM", 2),
    /** Barbados Dollar ({@code BBD}). */
    BBD("BBD", 52, "Barbados Dollar", "Bds$", 2),
    /** Taka ({@code BDT}). */
    BDT("BDT", 50, "Taka", "৳", 2),
    /** Bulgarian Lev ({@code BGN}). */
    BGN("BGN", 975, "Bulgarian Lev", "лв", 2),
    /** Bahraini Dinar ({@code BHD}). */
    BHD("BHD", 48, "Bahraini Dinar", ".د.ب", 3),
    /** Burundi Franc ({@code BIF}). */
    BIF("BIF", 108, "Burundi Franc", "FBu", 0),
    /** Bermudian Dollar ({@code BMD}). */
    BMD("BMD", 60, "Bermudian Dollar", "$", 2),
    /** Brunei Dollar ({@code BND}). */
    BND("BND", 96, "Brunei Dollar", "B$", 2),
    /** Boliviano ({@code BOB}). */
    BOB("BOB", 68, "Boliviano", "Bs.", 2),
    /** Brazilian Real ({@code BRL}). */
    BRL("BRL", 986, "Brazilian Real", "R$", 2),
    /** Bahamian Dollar ({@code BSD}). */
    BSD("BSD", 44, "Bahamian Dollar", "B$", 2),
    /** Ngultrum ({@code BTN}). */
    BTN("BTN", 64, "Ngultrum", "Nu.", 2),
    /** Pula ({@code BWP}). */
    BWP("BWP", 72, "Pula", "P", 2),
    /** Belarussian Ruble ({@code BYN}). */
    BYN("BYN", 933, "Belarussian Ruble", "Br", 2),
    /** Belize Dollar ({@code BZD}). */
    BZD("BZD", 84, "Belize Dollar", "BZ$", 2),

    // ── C ────────────────────────────────────────────────────────────────────
    /** Canadian Dollar ({@code CAD}). */
    CAD("CAD", 124, "Canadian Dollar", "C$", 2),
    /** Congolese Franc ({@code CDF}). */
    CDF("CDF", 976, "Congolese Franc", "FC", 2),
    /** Swiss Franc ({@code CHF}). */
    CHF("CHF", 756, "Swiss Franc", "CHF", 2),
    /** Chilean Peso ({@code CLP}). */
    CLP("CLP", 152, "Chilean Peso", "$", 0),
    /** Chinese Yuan ({@code CNY}). */
    CNY("CNY", 156, "Chinese Yuan", "¥", 2),
    /** Colombian Peso ({@code COP}). */
    COP("COP", 170, "Colombian Peso", "$", 2),
    /** Costa Rican Colon ({@code CRC}). */
    CRC("CRC", 188, "Costa Rican Colon", "₡", 2),
    /** Cuban Peso ({@code CUP}). */
    CUP("CUP", 192, "Cuban Peso", "₱", 2),
    /** Cabo Verde Escudo ({@code CVE}). */
    CVE("CVE", 132, "Cabo Verde Escudo", "$", 2),
    /** Czech Koruna ({@code CZK}). */
    CZK("CZK", 203, "Czech Koruna", "Kč", 2),

    // ── D ────────────────────────────────────────────────────────────────────
    /** Djibouti Franc ({@code DJF}). */
    DJF("DJF", 262, "Djibouti Franc", "Fdj", 0),
    /** Danish Krone ({@code DKK}). */
    DKK("DKK", 208, "Danish Krone", "kr", 2),
    /** Dominican Peso ({@code DOP}). */
    DOP("DOP", 214, "Dominican Peso", "RD$", 2),
    /** Algerian Dinar ({@code DZD}). */
    DZD("DZD", 12, "Algerian Dinar", "د.ج", 2),

    // ── E ────────────────────────────────────────────────────────────────────
    /** Egyptian Pound ({@code EGP}). */
    EGP("EGP", 818, "Egyptian Pound", "E£", 2),
    /** Nakfa ({@code ERN}). */
    ERN("ERN", 232, "Nakfa", "Nfk", 2),
    /** Ethiopian Birr ({@code ETB}). */
    ETB("ETB", 230, "Ethiopian Birr", "Br", 2),
    /** Euro ({@code EUR}). */
    EUR("EUR", 978, "Euro", "€", 2),

    // ── F ────────────────────────────────────────────────────────────────────
    /** Fiji Dollar ({@code FJD}). */
    FJD("FJD", 242, "Fiji Dollar", "FJ$", 2),
    /** Falkland Islands Pound ({@code FKP}). */
    FKP("FKP", 238, "Falkland Islands Pound", "£", 2),

    // ── G ────────────────────────────────────────────────────────────────────
    /** Pound Sterling ({@code GBP}). */
    GBP("GBP", 826, "Pound Sterling", "£", 2),
    /** Lari ({@code GEL}). */
    GEL("GEL", 981, "Lari", "₾", 2),
    /** Ghana Cedi ({@code GHS}). */
    GHS("GHS", 936, "Ghana Cedi", "GH₵", 2),
    /** Gibraltar Pound ({@code GIP}). */
    GIP("GIP", 292, "Gibraltar Pound", "£", 2),
    /** Dalasi ({@code GMD}). */
    GMD("GMD", 270, "Dalasi", "D", 2),
    /** Guinea Franc ({@code GNF}). */
    GNF("GNF", 324, "Guinea Franc", "FG", 0),
    /** Quetzal ({@code GTQ}). */
    GTQ("GTQ", 320, "Quetzal", "Q", 2),
    /** Guyana Dollar ({@code GYD}). */
    GYD("GYD", 328, "Guyana Dollar", "G$", 2),

    // ── H ────────────────────────────────────────────────────────────────────
    /** Hong Kong Dollar ({@code HKD}). */
    HKD("HKD", 344, "Hong Kong Dollar", "HK$", 2),
    /** Lempira ({@code HNL}). */
    HNL("HNL", 340, "Lempira", "L", 2),
    /** Gourde ({@code HTG}). */
    HTG("HTG", 332, "Gourde", "G", 2),
    /** Forint ({@code HUF}). */
    HUF("HUF", 348, "Forint", "Ft", 2),

    // ── I ────────────────────────────────────────────────────────────────────
    /** Rupiah ({@code IDR}). */
    IDR("IDR", 360, "Rupiah", "Rp", 2),
    /** New Israeli Sheqel ({@code ILS}). */
    ILS("ILS", 376, "New Israeli Sheqel", "₪", 2),
    /** Indian Rupee ({@code INR}). */
    INR("INR", 356, "Indian Rupee", "₹", 2),
    /** Iraqi Dinar ({@code IQD}). */
    IQD("IQD", 368, "Iraqi Dinar", "ع.د", 3),
    /** Iranian Rial ({@code IRR}). */
    IRR("IRR", 364, "Iranian Rial", "﷼", 2),
    /** Iceland Krona ({@code ISK}). */
    ISK("ISK", 352, "Iceland Krona", "kr", 0),

    // ── J ────────────────────────────────────────────────────────────────────
    /** Jamaican Dollar ({@code JMD}). */
    JMD("JMD", 388, "Jamaican Dollar", "J$", 2),
    /** Jordanian Dinar ({@code JOD}). */
    JOD("JOD", 400, "Jordanian Dinar", "د.ا", 3),
    /** Japanese Yen ({@code JPY}). */
    JPY("JPY", 392, "Japanese Yen", "¥", 0),

    // ── K ────────────────────────────────────────────────────────────────────
    /** Kenyan Shilling ({@code KES}). */
    KES("KES", 404, "Kenyan Shilling", "KSh", 2),
    /** Som ({@code KGS}). */
    KGS("KGS", 417, "Som", "лв", 2),
    /** Riel ({@code KHR}). */
    KHR("KHR", 116, "Riel", "៛", 2),
    /** Comoro Franc ({@code KMF}). */
    KMF("KMF", 174, "Comoro Franc", "CF", 0),
    /** North Korean Won ({@code KPW}). */
    KPW("KPW", 408, "North Korean Won", "₩", 2),
    /** South Korean Won ({@code KRW}). */
    KRW("KRW", 410, "South Korean Won", "₩", 0),
    /** Kuwaiti Dinar ({@code KWD}). */
    KWD("KWD", 414, "Kuwaiti Dinar", "د.ك", 3),
    /** Cayman Islands Dollar ({@code KYD}). */
    KYD("KYD", 136, "Cayman Islands Dollar", "CI$", 2),
    /** Tenge ({@code KZT}). */
    KZT("KZT", 398, "Tenge", "₸", 2),

    // ── L ────────────────────────────────────────────────────────────────────
    /** Kip ({@code LAK}). */
    LAK("LAK", 418, "Kip", "₭", 2),
    /** Lebanese Pound ({@code LBP}). */
    LBP("LBP", 422, "Lebanese Pound", "L£", 2),
    /** Sri Lanka Rupee ({@code LKR}). */
    LKR("LKR", 144, "Sri Lanka Rupee", "Rs", 2),
    /** Liberian Dollar ({@code LRD}). */
    LRD("LRD", 430, "Liberian Dollar", "L$", 2),
    /** Loti ({@code LSL}). */
    LSL("LSL", 426, "Loti", "M", 2),
    /** Libyan Dinar ({@code LYD}). */
    LYD("LYD", 434, "Libyan Dinar", "ل.د", 3),

    // ── M ────────────────────────────────────────────────────────────────────
    /** Moroccan Dirham ({@code MAD}). */
    MAD("MAD", 504, "Moroccan Dirham", "MAD", 2),
    /** Moldovan Leu ({@code MDL}). */
    MDL("MDL", 498, "Moldovan Leu", "L", 2),
    /** Malagasy Ariary ({@code MGA}). */
    MGA("MGA", 969, "Malagasy Ariary", "Ar", 2),
    /** Denar ({@code MKD}). */
    MKD("MKD", 807, "Denar", "ден", 2),
    /** Kyat ({@code MMK}). */
    MMK("MMK", 104, "Kyat", "K", 2),
    /** Tugrik ({@code MNT}). */
    MNT("MNT", 496, "Tugrik", "₮", 2),
    /** Pataca ({@code MOP}). */
    MOP("MOP", 446, "Pataca", "MOP$", 2),
    /** Ouguiya ({@code MRU}). */
    MRU("MRU", 929, "Ouguiya", "UM", 2),
    /** Mauritius Rupee ({@code MUR}). */
    MUR("MUR", 480, "Mauritius Rupee", "₨", 2),
    /** Rufiyaa ({@code MVR}). */
    MVR("MVR", 462, "Rufiyaa", "Rf", 2),
    /** Kwacha ({@code MWK}). */
    MWK("MWK", 454, "Kwacha", "MK", 2),
    /** Mexican Peso ({@code MXN}). */
    MXN("MXN", 484, "Mexican Peso", "MX$", 2),
    /** Malaysian Ringgit ({@code MYR}). */
    MYR("MYR", 458, "Malaysian Ringgit", "RM", 2),
    /** Mozambique Metical ({@code MZN}). */
    MZN("MZN", 943, "Mozambique Metical", "MT", 2),

    // ── N ────────────────────────────────────────────────────────────────────
    /** Namibia Dollar ({@code NAD}). */
    NAD("NAD", 516, "Namibia Dollar", "N$", 2),
    /** Naira ({@code NGN}). */
    NGN("NGN", 566, "Naira", "₦", 2),
    /** Cordoba Oro ({@code NIO}). */
    NIO("NIO", 558, "Cordoba Oro", "C$", 2),
    /** Norwegian Krone ({@code NOK}). */
    NOK("NOK", 578, "Norwegian Krone", "kr", 2),
    /** Nepalese Rupee ({@code NPR}). */
    NPR("NPR", 524, "Nepalese Rupee", "₨", 2),
    /** New Zealand Dollar ({@code NZD}). */
    NZD("NZD", 554, "New Zealand Dollar", "NZ$", 2),

    // ── O ────────────────────────────────────────────────────────────────────
    /** Rial Omani ({@code OMR}). */
    OMR("OMR", 512, "Rial Omani", "ر.ع.", 3),

    // ── P ────────────────────────────────────────────────────────────────────
    /** Balboa ({@code PAB}). */
    PAB("PAB", 590, "Balboa", "B/.", 2),
    /** Nuevo Sol ({@code PEN}). */
    PEN("PEN", 604, "Nuevo Sol", "S/.", 2),
    /** Kina ({@code PGK}). */
    PGK("PGK", 598, "Kina", "K", 2),
    /** Philippine Peso ({@code PHP}). */
    PHP("PHP", 608, "Philippine Peso", "₱", 2),
    /** Pakistan Rupee ({@code PKR}). */
    PKR("PKR", 586, "Pakistan Rupee", "₨", 2),
    /** Zloty ({@code PLN}). */
    PLN("PLN", 985, "Zloty", "zł", 2),
    /** Guarani ({@code PYG}). */
    PYG("PYG", 600, "Guarani", "₲", 0),

    // ── Q ────────────────────────────────────────────────────────────────────
    /** Qatari Rial ({@code QAR}). */
    QAR("QAR", 634, "Qatari Rial", "ر.ق", 2),

    // ── R ────────────────────────────────────────────────────────────────────
    /** Romanian Leu ({@code RON}). */
    RON("RON", 946, "Romanian Leu", "lei", 2),
    /** Serbian Dinar ({@code RSD}). */
    RSD("RSD", 941, "Serbian Dinar", "дин.", 2),
    /** Russian Ruble ({@code RUB}). */
    RUB("RUB", 643, "Russian Ruble", "₽", 2),
    /** Rwanda Franc ({@code RWF}). */
    RWF("RWF", 646, "Rwanda Franc", "RF", 0),

    // ── S ────────────────────────────────────────────────────────────────────
    /** Saudi Riyal ({@code SAR}). */
    SAR("SAR", 682, "Saudi Riyal", "ر.س", 2),
    /** Solomon Islands Dollar ({@code SBD}). */
    SBD("SBD", 90, "Solomon Islands Dollar", "SI$", 2),
    /** Seychelles Rupee ({@code SCR}). */
    SCR("SCR", 690, "Seychelles Rupee", "₨", 2),
    /** Sudanese Pound ({@code SDG}). */
    SDG("SDG", 938, "Sudanese Pound", "ج.س.", 2),
    /** Swedish Krona ({@code SEK}). */
    SEK("SEK", 752, "Swedish Krona", "kr", 2),
    /** Singapore Dollar ({@code SGD}). */
    SGD("SGD", 702, "Singapore Dollar", "S$", 2),
    /** Saint Helena Pound ({@code SHP}). */
    SHP("SHP", 654, "Saint Helena Pound", "£", 2),
    /** Leone ({@code SLE}). */
    SLE("SLE", 925, "Leone", "Le", 2),
    /** Somali Shilling ({@code SOS}). */
    SOS("SOS", 706, "Somali Shilling", "Sh", 2),
    /** Surinam Dollar ({@code SRD}). */
    SRD("SRD", 968, "Surinam Dollar", "$", 2),
    /** South Sudanese Pound ({@code SSP}). */
    SSP("SSP", 728, "South Sudanese Pound", "£", 2),
    /** Dobra ({@code STN}). */
    STN("STN", 930, "Dobra", "Db", 2),
    /** El Salvador Colon ({@code SVC}). */
    SVC("SVC", 222, "El Salvador Colon", "₡", 2),
    /** Syrian Pound ({@code SYP}). */
    SYP("SYP", 760, "Syrian Pound", "£S", 2),
    /** Lilangeni ({@code SZL}). */
    SZL("SZL", 748, "Lilangeni", "E", 2),

    // ── T ────────────────────────────────────────────────────────────────────
    /** Baht ({@code THB}). */
    THB("THB", 764, "Baht", "฿", 2),
    /** Somoni ({@code TJS}). */
    TJS("TJS", 972, "Somoni", "SM", 2),
    /** Turkmenistan New Manat ({@code TMT}). */
    TMT("TMT", 934, "Turkmenistan New Manat", "T", 2),
    /** Tunisian Dinar ({@code TND}). */
    TND("TND", 788, "Tunisian Dinar", "د.ت", 3),
    /** Pa'anga ({@code TOP}). */
    TOP("TOP", 776, "Pa'anga", "T$", 2),
    /** Turkish Lira ({@code TRY}). */
    TRY("TRY", 949, "Turkish Lira", "₺", 2),
    /** Trinidad and Tobago Dollar ({@code TTD}). */
    TTD("TTD", 780, "Trinidad and Tobago Dollar", "TT$", 2),
    /** New Taiwan Dollar ({@code TWD}). */
    TWD("TWD", 901, "New Taiwan Dollar", "NT$", 2),
    /** Tanzanian Shilling ({@code TZS}). */
    TZS("TZS", 834, "Tanzanian Shilling", "TSh", 2),

    // ── U ────────────────────────────────────────────────────────────────────
    /** Hryvnia ({@code UAH}). */
    UAH("UAH", 980, "Hryvnia", "₴", 2),
    /** Uganda Shilling ({@code UGX}). */
    UGX("UGX", 800, "Uganda Shilling", "USh", 0),
    /** US Dollar ({@code USD}). */
    USD("USD", 840, "US Dollar", "$", 2),
    /** Peso Uruguayo ({@code UYU}). */
    UYU("UYU", 858, "Peso Uruguayo", "$U", 2),
    /** Uzbekistan Sum ({@code UZS}). */
    UZS("UZS", 860, "Uzbekistan Sum", "сўм", 2),

    // ── V ────────────────────────────────────────────────────────────────────
    /** Bolivar Digital ({@code VED}). */
    VED("VED", 926, "Bolivar Digital", "Bs.D", 2),
    /** Bolivar Soberano ({@code VES}). */
    VES("VES", 928, "Bolivar Soberano", "Bs.S", 2),
    /** Dong ({@code VND}). */
    VND("VND", 704, "Dong", "₫", 0),
    /** Vatu ({@code VUV}). */
    VUV("VUV", 548, "Vatu", "VT", 0),

    // ── W ────────────────────────────────────────────────────────────────────
    /** Tala ({@code WST}). */
    WST("WST", 882, "Tala", "WS$", 2),

    // ── X (Regional / Supranational) ────────────────────────────────────────
    /** CFA Franc BEAC ({@code XAF}). */
    XAF("XAF", 950, "CFA Franc BEAC", "FCFA", 0),
    /** East Caribbean Dollar ({@code XCD}). */
    XCD("XCD", 951, "East Caribbean Dollar", "EC$", 2),
    /** CFA Franc BCEAO ({@code XOF}). */
    XOF("XOF", 952, "CFA Franc BCEAO", "CFA", 0),
    /** CFP Franc ({@code XPF}). */
    XPF("XPF", 953, "CFP Franc", "₣", 0),

    // ── Y ────────────────────────────────────────────────────────────────────
    /** Yemeni Rial ({@code YER}). */
    YER("YER", 886, "Yemeni Rial", "﷼", 2),

    // ── Z ────────────────────────────────────────────────────────────────────
    /** South African Rand ({@code ZAR}). */
    ZAR("ZAR", 710, "South African Rand", "R", 2),
    /** Zambian Kwacha ({@code ZMW}). */
    ZMW("ZMW", 967, "Zambian Kwacha", "ZK", 2),
    /** Zimbabwe Dollar ({@code ZWL}). */
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
     * Returns the ISO 4217 numeric code, e.g. 840 for {@code USD} (999 for {@link #UNKNOWN}).
     *
     * @return the numeric code
     */
    public int numericCode() {
        return numericCode;
    }

    // ── Queries ─────────────────────────────────────────────────────────────

    /**
     * Tells whether this is the {@link #UNKNOWN} placeholder.
     *
     * @return whether this is {@link #UNKNOWN}
     */
    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    /**
     * Tells whether this currency has minor units (more than 0 decimal places).
     *
     * @return whether {@link #decimalPlaces()} is above 0
     */
    public boolean hasMinorUnit() {
        return decimalPlaces > 0;
    }

    /**
     * Tells whether this currency has 3 decimal places, like the dinar-type currencies
     * ({@code BHD}, {@code IQD}, {@code JOD}, {@code KWD}, {@code LYD}, {@code OMR}, {@code TND}).
     *
     * @return whether {@link #decimalPlaces()} is 3
     */
    public boolean isThreeDecimal() {
        return decimalPlaces == 3;
    }

    /**
     * Tells whether this currency has no minor units, like {@code JPY} or {@code KRW}. {@link #UNKNOWN} is not
     * counted.
     *
     * @return whether {@link #decimalPlaces()} is 0 and this is not {@link #UNKNOWN}
     */
    public boolean isZeroDecimal() {
        return decimalPlaces == 0 && this != UNKNOWN;
    }

    // ── Lookup Methods ──────────────────────────────────────────────────────

    /**
     * Looks up a currency by its ISO 4217 alpha code, ignoring case and surrounding spaces.
     *
     * @param code the 3-letter code, e.g. {@code "USD"}, {@code "eur"}
     * @return the matching currency, or {@link #UNKNOWN} when the code is {@code null}, blank or not known
     */
    public static FiatCurrency fromCode(String code) {
        if (code == null || code.isBlank()) return UNKNOWN;
        FiatCurrency result = BY_CODE.get(code.strip().toUpperCase(Locale.ROOT));
        return result != null ? result : UNKNOWN;
    }

    /**
     * Looks up the currency a country uses, from the JDK's ISO 4217 data.
     *
     * @param country e.g. {@code Country.PHILIPPINES}
     * @return its currency, e.g. PHP; empty for {@code null}, {@code Country.UNKNOWN}, a territory without one,
     *         or a currency this enum does not list
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

    /**
     * Looks up a currency by its ISO 4217 numeric code.
     *
     * @param numericCode the numeric code, e.g. 840 for USD
     * @return the matching currency, or {@link #UNKNOWN} when the code is not known
     */
    public static FiatCurrency fromNumericCode(int numericCode) {
        FiatCurrency result = BY_NUMERIC.get(numericCode);
        return result != null ? result : UNKNOWN;
    }

    /**
     * Looks up a currency by its ISO 4217 alpha code, ignoring case and surrounding spaces.
     *
     * @param code the 3-letter code
     * @return the currency, or empty if the code is {@code null}, blank, not known, or {@code XXX}
     */
    public static Optional<FiatCurrency> findByCode(@Nullable String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        FiatCurrency result = BY_CODE.get(code.strip().toUpperCase(Locale.ROOT));
        return result != null && result != UNKNOWN ? Optional.of(result) : Optional.empty();
    }

    /**
     * Tells whether a code is a known ISO 4217 alpha code, ignoring case and surrounding spaces.
     * {@code XXX} ({@link #UNKNOWN}) does not count.
     *
     * @param code the code to check
     * @return whether it names a currency in this enum other than {@link #UNKNOWN}
     */
    public static boolean isValidCode(@Nullable String code) {
        if (code == null || code.isBlank()) return false;
        FiatCurrency result = BY_CODE.get(code.strip().toUpperCase(Locale.ROOT));
        return result != null && result != UNKNOWN;
    }

    /**
     * Returns the default currency, {@link #UNKNOWN}.
     *
     * @return {@link #UNKNOWN}
     */
    public static FiatCurrency defaultCurrency() {
        return UNKNOWN;
    }

    /**
     * Formats as the code and the name, e.g. {@code "USD (US Dollar)"}.
     *
     * @return the code and name
     */
    @Override
    public String toString() {
        return "%s (%s)".formatted(code, displayName);
    }

    /**
     * Parses an ISO 4217 alpha code, ignoring case and surrounding spaces.
     *
     * @param text an ISO 4217 code, e.g. {@code "PHP"}
     * @return the currency
     * @throws ValueObjectException if it is {@code null}, not a known code, or {@code XXX}
     */
    public static FiatCurrency parse(String text) {
        if (text == null) throw new ValueObjectException("A FiatCurrency is required");
        return findByCode(text.strip()).filter(c -> c != UNKNOWN)
                .orElseThrow(() -> new ValueObjectException("Not an ISO 4217 currency code: '" + text + "'"));
    }

    /**
     * Parses an ISO 4217 alpha code like {@link #parse(String)}, without throwing.
     *
     * @param text any text
     * @return it as a {@link FiatCurrency}, or empty when it is not one; never throws
     */
    public static Optional<FiatCurrency> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether text is a known ISO 4217 alpha code other than {@code XXX}.
     *
     * @param text any text
     * @return whether it is a {@link FiatCurrency}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
