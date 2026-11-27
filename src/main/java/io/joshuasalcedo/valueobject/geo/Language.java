package io.joshuasalcedo.valueobject.geo;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.jspecify.annotations.Nullable;
import io.joshuasalcedo.valueobject.core.ValueObject;


import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * A human language, by its ISO 639-1 two-letter code, with its English and native names.
 *
 * <pre>{@code
 * Language.parse("tl");                       // TAGALOG
 * Language.fromLocale(Locale.JAPAN);          // Optional[Japanese]
 * Language.JAPANESE.nativeName();             // the name in Japanese
 * }</pre>
 */
public enum Language implements ValueObject {

    /** Abkhazian ({@code ab}). */
    ABKHAZIAN("ab", "Abkhazian", "Abkhazian"),
    /** Afar ({@code aa}). */
    AFAR("aa", "Afar", "Afar"),
    /** Afrikaans ({@code af}). */
    AFRIKAANS("af", "Afrikaans", "Afrikaans"),
    /** Akan ({@code ak}). */
    AKAN("ak", "Akan", "Akan"),
    /** Albanian ({@code sq}). */
    ALBANIAN("sq", "Albanian", "shqip"),
    /** Amharic ({@code am}). */
    AMHARIC("am", "Amharic", "አማርኛ"),
    /** Arabic ({@code ar}). */
    ARABIC("ar", "Arabic", "العربية"),
    /** Aragonese ({@code an}). */
    ARAGONESE("an", "Aragonese", "Aragonese"),
    /** Armenian ({@code hy}). */
    ARMENIAN("hy", "Armenian", "հայերեն"),
    /** Assamese ({@code as}). */
    ASSAMESE("as", "Assamese", "অসমীয়া"),
    /** Avaric ({@code av}). */
    AVARIC("av", "Avaric", "Avaric"),
    /** Avestan ({@code ae}). */
    AVESTAN("ae", "Avestan", "Avestan"),
    /** Aymara ({@code ay}). */
    AYMARA("ay", "Aymara", "Aymara"),
    /** Azerbaijani ({@code az}). */
    AZERBAIJANI("az", "Azerbaijani", "azərbaycan"),
    /** Bambara ({@code bm}). */
    BAMBARA("bm", "Bambara", "bamanakan"),
    /** Bangla ({@code bn}). */
    BANGLA("bn", "Bangla", "বাংলা"),
    /** Bashkir ({@code ba}). */
    BASHKIR("ba", "Bashkir", "башҡорт"),
    /** Basque ({@code eu}). */
    BASQUE("eu", "Basque", "euskara"),
    /** Belarusian ({@code be}). */
    BELARUSIAN("be", "Belarusian", "беларуская"),
    /** Bangla ({@code bn}). */
    BENGALI("bn", "Bangla", "বাংলা"),
    /** Bislama ({@code bi}). */
    BISLAMA("bi", "Bislama", "Bislama"),
    /** Bosnian ({@code bs}). */
    BOSNIAN("bs", "Bosnian", "bosanski"),
    /** Breton ({@code br}). */
    BRETON("br", "Breton", "brezhoneg"),
    /** Bulgarian ({@code bg}). */
    BULGARIAN("bg", "Bulgarian", "български"),
    /** Burmese ({@code my}). */
    BURMESE("my", "Burmese", "မြန်မာ"),
    /** Catalan ({@code ca}). */
    CATALAN("ca", "Catalan", "català"),
    /** Chamorro ({@code ch}). */
    CHAMORRO("ch", "Chamorro", "Chamorro"),
    /** Chechen ({@code ce}). */
    CHECHEN("ce", "Chechen", "нохчийн"),
    /** Chinese ({@code zh}). */
    CHINESE("zh", "Chinese", "中文"),
    /** Church Slavic ({@code cu}). */
    CHURCH_SLAVIC("cu", "Church Slavic", "Church Slavic"),
    /** Chuvash ({@code cv}). */
    CHUVASH("cv", "Chuvash", "чӑваш чӗлхи"),
    /** Cornish ({@code kw}). */
    CORNISH("kw", "Cornish", "kernewek"),
    /** Corsican ({@code co}). */
    CORSICAN("co", "Corsican", "Corsican"),
    /** Cree ({@code cr}). */
    CREE("cr", "Cree", "Cree"),
    /** Croatian ({@code hr}). */
    CROATIAN("hr", "Croatian", "hrvatski"),
    /** Czech ({@code cs}). */
    CZECH("cs", "Czech", "čeština"),
    /** Danish ({@code da}). */
    DANISH("da", "Danish", "dansk"),
    /** Divehi ({@code dv}). */
    DIVEHI("dv", "Divehi", "Divehi"),
    /** Dutch ({@code nl}). */
    DUTCH("nl", "Dutch", "Nederlands"),
    /** Dzongkha ({@code dz}). */
    DZONGKHA("dz", "Dzongkha", "རྫོང་ཁ"),
    /** English ({@code en}). */
    ENGLISH("en", "English", "English"),
    /** Esperanto ({@code eo}). */
    ESPERANTO("eo", "Esperanto", "Esperanto"),
    /** Estonian ({@code et}). */
    ESTONIAN("et", "Estonian", "eesti"),
    /** eʋegbe ({@code ee}). */
    EWE("ee", "Ewe", "eʋegbe"),
    /** Faroese ({@code fo}). */
    FAROESE("fo", "Faroese", "føroyskt"),
    /** Fijian ({@code fj}). */
    FIJIAN("fj", "Fijian", "Fijian"),
    /** Finnish ({@code fi}). */
    FINNISH("fi", "Finnish", "suomi"),
    /** French ({@code fr}). */
    FRENCH("fr", "French", "français"),
    /** Fula ({@code ff}). */
    FULA("ff", "Fula", "Pulaar"),
    /** Galician ({@code gl}). */
    GALICIAN("gl", "Galician", "galego"),
    /** Ganda ({@code lg}). */
    GANDA("lg", "Ganda", "Luganda"),
    /** Georgian ({@code ka}). */
    GEORGIAN("ka", "Georgian", "ქართული"),
    /** German ({@code de}). */
    GERMAN("de", "German", "Deutsch"),
    /** Greek ({@code el}). */
    GREEK("el", "Greek", "Ελληνικά"),
    /** Guarani ({@code gn}). */
    GUARANI("gn", "Guarani", "Guarani"),
    /** Gujarati ({@code gu}). */
    GUJARATI("gu", "Gujarati", "ગુજરાતી"),
    /** Haitian Creole ({@code ht}). */
    HAITIAN_CREOLE("ht", "Haitian Creole", "Kreyòl Ayisyen"),
    /** Hausa ({@code ha}). */
    HAUSA("ha", "Hausa", "Hausa"),
    /** Hebrew ({@code he}). */
    HEBREW("he", "Hebrew", "עברית"),
    /** Herero ({@code hz}). */
    HERERO("hz", "Herero", "Herero"),
    /** Hindi ({@code hi}). */
    HINDI("hi", "Hindi", "हिन्दी"),
    /** Hiri Motu ({@code ho}). */
    HIRI_MOTU("ho", "Hiri Motu", "Hiri Motu"),
    /** Hungarian ({@code hu}). */
    HUNGARIAN("hu", "Hungarian", "magyar"),
    /** Icelandic ({@code is}). */
    ICELANDIC("is", "Icelandic", "íslenska"),
    /** Ido ({@code io}). */
    IDO("io", "Ido", "Ido"),
    /** Igbo ({@code ig}). */
    IGBO("ig", "Igbo", "Igbo"),
    /** Indonesian ({@code id}). */
    INDONESIAN("id", "Indonesian", "Indonesia"),
    /** Interlingua ({@code ia}). */
    INTERLINGUA("ia", "Interlingua", "interlingua"),
    /** Interlingue ({@code ie}). */
    INTERLINGUE("ie", "Interlingue", "Interlingue"),
    /** Inuktitut ({@code iu}). */
    INUKTITUT("iu", "Inuktitut", "Inuktitut"),
    /** Inupiaq ({@code ik}). */
    INUPIAQ("ik", "Inupiaq", "Inupiaq"),
    /** Irish ({@code ga}). */
    IRISH("ga", "Irish", "Gaeilge"),
    /** Italian ({@code it}). */
    ITALIAN("it", "Italian", "italiano"),
    /** Japanese ({@code ja}). */
    JAPANESE("ja", "Japanese", "日本語"),
    /** Javanese ({@code jv}). */
    JAVANESE("jv", "Javanese", "Jawa"),
    /** Kalaallisut ({@code kl}). */
    KALAALLISUT("kl", "Kalaallisut", "kalaallisut"),
    /** Kannada ({@code kn}). */
    KANNADA("kn", "Kannada", "ಕನ್ನಡ"),
    /** Kanuri ({@code kr}). */
    KANURI("kr", "Kanuri", "Kanuri"),
    /** Kashmiri ({@code ks}). */
    KASHMIRI("ks", "Kashmiri", "کٲشُر"),
    /** Kazakh ({@code kk}). */
    KAZAKH("kk", "Kazakh", "қазақ тілі"),
    /** Khmer ({@code km}). */
    KHMER("km", "Khmer", "ខ្មែរ"),
    /** Kikuyu ({@code ki}). */
    KIKUYU("ki", "Kikuyu", "Gikuyu"),
    /** Kinyarwanda ({@code rw}). */
    KINYARWANDA("rw", "Kinyarwanda", "Ikinyarwanda"),
    /** Komi ({@code kv}). */
    KOMI("kv", "Komi", "Komi"),
    /** Kongo ({@code kg}). */
    KONGO("kg", "Kongo", "Kongo"),
    /** Korean ({@code ko}). */
    KOREAN("ko", "Korean", "한국어"),
    /** Kuanyama ({@code kj}). */
    KUANYAMA("kj", "Kuanyama", "Kuanyama"),
    /** Kurmanji ({@code ku}). */
    KURMANJI("ku", "Kurmanji", "kurmancî"),
    /** Kyrgyz ({@code ky}). */
    KYRGYZ("ky", "Kyrgyz", "кыргызча"),
    /** Lao ({@code lo}). */
    LAO("lo", "Lao", "ລາວ"),
    /** Latin ({@code la}). */
    LATIN("la", "Latin", "Latin"),
    /** Latvian ({@code lv}). */
    LATVIAN("lv", "Latvian", "latviešu"),
    /** Limburgish ({@code li}). */
    LIMBURGISH("li", "Limburgish", "Limburgish"),
    /** Lingala ({@code ln}). */
    LINGALA("ln", "Lingala", "lingála"),
    /** Lithuanian ({@code lt}). */
    LITHUANIAN("lt", "Lithuanian", "lietuvių"),
    /** Luba-Katanga ({@code lu}). */
    LUBA_KATANGA("lu", "Luba-Katanga", "Tshiluba"),
    /** Luxembourgish ({@code lb}). */
    LUXEMBOURGISH("lb", "Luxembourgish", "Lëtzebuergesch"),
    /** Macedonian ({@code mk}). */
    MACEDONIAN("mk", "Macedonian", "македонски"),
    /** Malagasy ({@code mg}). */
    MALAGASY("mg", "Malagasy", "Malagasy"),
    /** Malay ({@code ms}). */
    MALAY("ms", "Malay", "Melayu"),
    /** Malayalam ({@code ml}). */
    MALAYALAM("ml", "Malayalam", "മലയാളം"),
    /** Maltese ({@code mt}). */
    MALTESE("mt", "Maltese", "Malti"),
    /** Manx ({@code gv}). */
    MANX("gv", "Manx", "Gaelg"),
    /** Māori ({@code mi}). */
    MAORI("mi", "Māori", "Māori"),
    /** Marathi ({@code mr}). */
    MARATHI("mr", "Marathi", "मराठी"),
    /** Marshallese ({@code mh}). */
    MARSHALLESE("mh", "Marshallese", "Marshallese"),
    /** Mongolian ({@code mn}). */
    MONGOLIAN("mn", "Mongolian", "монгол"),
    /** Nauru ({@code na}). */
    NAURU("na", "Nauru", "Nauru"),
    /** Navajo ({@code nv}). */
    NAVAJO("nv", "Navajo", "Navajo"),
    /** Ndonga ({@code ng}). */
    NDONGA("ng", "Ndonga", "Ndonga"),
    /** Nepali ({@code ne}). */
    NEPALI("ne", "Nepali", "नेपाली"),
    /** Northern Sami ({@code se}). */
    NORTHERN_SAMI("se", "Northern Sami", "davvisámegiella"),
    /** North Ndebele ({@code nd}). */
    NORTH_NDEBELE("nd", "North Ndebele", "isiNdebele"),
    /** Norwegian ({@code no}). */
    NORWEGIAN("no", "Norwegian", "norsk"),
    /** Norwegian Bokmål ({@code nb}). */
    NORWEGIAN_BOKMAL("nb", "Norwegian Bokmål", "norsk bokmål"),
    /** Norwegian Nynorsk ({@code nn}). */
    NORWEGIAN_NYNORSK("nn", "Norwegian Nynorsk", "norsk nynorsk"),
    /** Nyanja ({@code ny}). */
    NYANJA("ny", "Nyanja", "Nyanja"),
    /** Occitan ({@code oc}). */
    OCCITAN("oc", "Occitan", "occitan"),
    /** Odia ({@code or}). */
    ODIA("or", "Odia", "ଓଡ଼ିଆ"),
    /** Ojibwa ({@code oj}). */
    OJIBWA("oj", "Ojibwa", "Ojibwa"),
    /** Oromo ({@code om}). */
    OROMO("om", "Oromo", "Oromoo"),
    /** Ossetic ({@code os}). */
    OSSETIC("os", "Ossetic", "ирон"),
    /** Pali ({@code pi}). */
    PALI("pi", "Pali", "Pali"),
    /** Pashto ({@code ps}). */
    PASHTO("ps", "Pashto", "پښتو"),
    /** Persian ({@code fa}). */
    PERSIAN("fa", "Persian", "فارسی"),
    /** Polish ({@code pl}). */
    POLISH("pl", "Polish", "polski"),
    /** Portuguese ({@code pt}). */
    PORTUGUESE("pt", "Portuguese", "português"),
    /** Punjabi ({@code pa}). */
    PUNJABI("pa", "Punjabi", "ਪੰਜਾਬੀ"),
    /** Quechua ({@code qu}). */
    QUECHUA("qu", "Quechua", "Runasimi"),
    /** Romanian ({@code ro}). */
    ROMANIAN("ro", "Romanian", "română"),
    /** Romansh ({@code rm}). */
    ROMANSH("rm", "Romansh", "rumantsch"),
    /** Rundi ({@code rn}). */
    RUNDI("rn", "Rundi", "Ikirundi"),
    /** Russian ({@code ru}). */
    RUSSIAN("ru", "Russian", "русский"),
    /** Samoan ({@code sm}). */
    SAMOAN("sm", "Samoan", "Samoan"),
    /** Sango ({@code sg}). */
    SANGO("sg", "Sango", "Sängö"),
    /** Sanskrit ({@code sa}). */
    SANSKRIT("sa", "Sanskrit", "संस्कृत भाषा"),
    /** Sardinian ({@code sc}). */
    SARDINIAN("sc", "Sardinian", "sardu"),
    /** Scottish Gaelic ({@code gd}). */
    SCOTTISH_GAELIC("gd", "Scottish Gaelic", "Gàidhlig"),
    /** Serbian ({@code sr}). */
    SERBIAN("sr", "Serbian", "српски"),
    /** Shona ({@code sn}). */
    SHONA("sn", "Shona", "chiShona"),
    /** Sichuan Yi ({@code ii}). */
    SICHUAN_YI("ii", "Sichuan Yi", "ꆈꌠꉙ"),
    /** Sindhi ({@code sd}). */
    SINDHI("sd", "Sindhi", "سنڌي"),
    /** Sinhala ({@code si}). */
    SINHALA("si", "Sinhala", "සිංහල"),
    /** Slovak ({@code sk}). */
    SLOVAK("sk", "Slovak", "slovenčina"),
    /** Slovenian ({@code sl}). */
    SLOVENIAN("sl", "Slovenian", "slovenščina"),
    /** Somali ({@code so}). */
    SOMALI("so", "Somali", "Soomaali"),
    /** Southern Sotho ({@code st}). */
    SOUTHERN_SOTHO("st", "Southern Sotho", "Sesotho"),
    /** South Ndebele ({@code nr}). */
    SOUTH_NDEBELE("nr", "South Ndebele", "South Ndebele"),
    /** Spanish ({@code es}). */
    SPANISH("es", "Spanish", "español"),
    /** Sundanese ({@code su}). */
    SUNDANESE("su", "Sundanese", "Basa Sunda"),
    /** Swahili ({@code sw}). */
    SWAHILI("sw", "Swahili", "Kiswahili"),
    /** Swati ({@code ss}). */
    SWATI("ss", "Swati", "Swati"),
    /** Swedish ({@code sv}). */
    SWEDISH("sv", "Swedish", "svenska"),
    /** Tagalog ({@code tl}). */
    TAGALOG("tl", "Tagalog", "Tagalog"),
    /** Tahitian ({@code ty}). */
    TAHITIAN("ty", "Tahitian", "Tahitian"),
    /** Tajik ({@code tg}). */
    TAJIK("tg", "Tajik", "тоҷикӣ"),
    /** Tamil ({@code ta}). */
    TAMIL("ta", "Tamil", "தமிழ்"),
    /** Tatar ({@code tt}). */
    TATAR("tt", "Tatar", "татар"),
    /** Telugu ({@code te}). */
    TELUGU("te", "Telugu", "తెలుగు"),
    /** Thai ({@code th}). */
    THAI("th", "Thai", "ไทย"),
    /** Tibetan ({@code bo}). */
    TIBETAN("bo", "Tibetan", "བོད་སྐད་"),
    /** Tigrinya ({@code ti}). */
    TIGRINYA("ti", "Tigrinya", "ትግርኛ"),
    /** Tongan ({@code to}). */
    TONGAN("to", "Tongan", "lea fakatonga"),
    /** Tsonga ({@code ts}). */
    TSONGA("ts", "Tsonga", "Tsonga"),
    /** Tswana ({@code tn}). */
    TSWANA("tn", "Tswana", "Setswana"),
    /** Turkish ({@code tr}). */
    TURKISH("tr", "Turkish", "Türkçe"),
    /** Turkmen ({@code tk}). */
    TURKMEN("tk", "Turkmen", "türkmen dili"),
    /** Twi ({@code tw}). */
    TWI("tw", "Twi", "Twi"),
    /** Ukrainian ({@code uk}). */
    UKRAINIAN("uk", "Ukrainian", "українська"),
    /** Urdu ({@code ur}). */
    URDU("ur", "Urdu", "اردو"),
    /** Uyghur ({@code ug}). */
    UYGHUR("ug", "Uyghur", "ئۇيغۇرچە"),
    /** Uzbek ({@code uz}). */
    UZBEK("uz", "Uzbek", "o‘zbek"),
    /** Venda ({@code ve}). */
    VENDA("ve", "Venda", "Venda"),
    /** Vietnamese ({@code vi}). */
    VIETNAMESE("vi", "Vietnamese", "Tiếng Việt"),
    /** Volapük ({@code vo}). */
    VOLAPUK("vo", "Volapük", "Volapük"),
    /** Walloon ({@code wa}). */
    WALLOON("wa", "Walloon", "Walloon"),
    /** Welsh ({@code cy}). */
    WELSH("cy", "Welsh", "Cymraeg"),
    /** Western Frisian ({@code fy}). */
    WESTERN_FRISIAN("fy", "Western Frisian", "Frysk"),
    /** Wolof ({@code wo}). */
    WOLOF("wo", "Wolof", "Wolof"),
    /** Xhosa ({@code xh}). */
    XHOSA("xh", "Xhosa", "IsiXhosa"),
    /** Yiddish ({@code yi}). */
    YIDDISH("yi", "Yiddish", "ייִדיש"),
    /** Yoruba ({@code yo}). */
    YORUBA("yo", "Yoruba", "Èdè Yorùbá"),
    /** Zhuang ({@code za}). */
    ZHUANG("za", "Zhuang", "Vahcuengh"),
    /** Zulu ({@code zu}). */
    ZULU("zu", "Zulu", "isiZulu"),

    /** Not known, or not one of the others. */
    UNKNOWN("xx", "Unknown", "Unknown");

    /** {@return whether this is {@link #UNKNOWN}} */
    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    private final String code;
    private final String displayName;
    private final String nativeName;

    Language(String code, String displayName, String nativeName) {
        this.code = code;
        this.displayName = displayName;
        this.nativeName = nativeName;
    }


    /** {@return its ISO 639-1 code, lower case, e.g. {@code "en"}; {@code "xx"} for {@link #UNKNOWN}} */
    public String code() {
        return code;
    }

    /** {@return its English name, e.g. {@code "German"}} */
    public String displayName() {
        return displayName;
    }

    /** {@return its name in the language itself, e.g. {@code "Deutsch"}} */
    public String nativeName() {
        return nativeName;
    }

    /**
     * Finds a language by its ISO 639-1 code, ignoring case.
     *
     * @param code e.g. {@code "en"}
     * @return the language, or empty when none has that code
     */
    public static Optional<Language> fromCode(String code) {
        return Arrays.stream(values())
                .filter(language -> language.code.equalsIgnoreCase(code))
                .findFirst();
    }

    /**
     * Finds a language by its English name, ignoring case.
     *
     * @param name e.g. {@code "german"}
     * @return the language, or empty when none has that name
     */
    public static Optional<Language> fromName(String name) {
        return Arrays.stream(values())
                .filter(language -> language.displayName.equalsIgnoreCase(name))
                .findFirst();
    }



    /**
     * Finds the language of a {@link Locale}, by its {@link Locale#getLanguage() language code}.
     *
     * @param locale the locale
     * @return the language, or empty when its code is not a known ISO 639-1 code
     */
    public static Optional<Language> fromLocale(Locale locale) {
        return fromCode(locale.getLanguage());
    }


    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Compares languages by display name alphabetically, ignoring case.
     *
     * @param other the Language to compare to
     * @return negative if this comes before, zero if equal, positive if after
     */
    public int compareByName(Language other) {
        return this.displayName.compareToIgnoreCase(other.displayName);
    }

    /**
     * Compares languages by ISO code alphabetically.
     *
     * @param other the Language to compare to
     * @return negative if this comes before, zero if equal, positive if after
     */
    public int compareByCode(Language other) {
        return this.code.compareTo(other.code);
    }

    /**
     * Parses a language from its ISO 639-1 code or its English name, ignoring case and surrounding whitespace.
     *
     * @param text the ISO 639-1 code or English name, e.g. {@code "en"} or {@code "English"}
     * @return the language
     * @throws ValueObjectException if it is {@code null} or names no language other than {@link #UNKNOWN}
     */
    public static Language parse(String text) {
        if (text == null) throw new ValueObjectException("A Language is required");
        String t = text.strip();
        return fromCode(t).or(() -> fromName(t)).filter(l -> l != UNKNOWN)
                .orElseThrow(() -> new ValueObjectException("Not a language: '" + text + "'"));
    }

    /**
     * Parses a language as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as a {@link Language}, or empty when it is not one; never throws
     */
    public static Optional<Language> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text names a language, as {@link #parse(String)} reads it.
     *
     * @param text any text
     * @return whether it is a {@link Language}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
