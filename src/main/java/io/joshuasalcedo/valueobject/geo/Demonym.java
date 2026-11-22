package io.joshuasalcedo.valueobject.geo;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.ValueObject;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Demonym value object representing the name for inhabitants of a place.
 *
 * <p>A demonym (from Greek δῆμος "people" + ὄνυμα "name") identifies a group of people
 * in relation to a particular place — a country, region, city, or territory. Demonyms exist
 * independently of countries; a single country may have multiple demonyms, and demonyms can
 * also exist for regions, cities, and ethnic groups.</p>
 *
 * <p>Each demonym has:</p>
 * <ul>
 *     <li><b>displayName</b> — the primary/default English form (e.g. "Filipino")</li>
 *     <li><b>masculine</b> — the male-specific form, if distinct (e.g. "Pinoy"), or null</li>
 *     <li><b>feminine</b> — the female-specific form, if distinct (e.g. "Pinay"), or null</li>
 *     <li><b>alternativeNames</b> — any other accepted forms (e.g. "Philippine")</li>
 * </ul>
 *
 * <p>When {@code masculine} or {@code feminine} is null, the {@code displayName} is used
 * as the gender-neutral default via {@link #masculineOrDefault()} and {@link #feminineOrDefault()}.</p>
 *
 * @author JoshuaSalcedo
 * @since 2/25/2026
 * @see <a href="https://en.wikipedia.org/wiki/Demonym">Wikipedia: Demonym</a>
 * @see <a href="https://en.wikipedia.org/wiki/List_of_adjectival_and_demonymic_forms_for_countries_and_nations">
 *     Wikipedia: Demonymic forms for countries and nations</a>
 */
public enum Demonym implements ValueObject {

    // ========================================================================================
    // A
    // ========================================================================================
    /** Afghan. */
    AFGHAN("Afghan", null, null),
    /** Albanian. */
    ALBANIAN("Albanian", null, null),
    /** Algerian. */
    ALGERIAN("Algerian", null, null),
    /** American. */
    AMERICAN("American", null, null),
    /** American Samoan. */
    AMERICAN_SAMOAN("American Samoan", null, null),
    /** Andorran. */
    ANDORRAN("Andorran", null, null),
    /** Angolan. */
    ANGOLAN("Angolan", null, null),
    /** Anguillian. */
    ANGUILLIAN("Anguillian", null, null),
    /** Antiguan. */
    ANTIGUAN("Antiguan", null, null, "Barbudan"),
    /** Argentine. */
    ARGENTINE("Argentine", "Argentino", "Argentina", "Argentinian", "Argentinean"),
    /** Armenian. */
    ARMENIAN("Armenian", null, null),
    /** Aruban. */
    ARUBAN("Aruban", null, null),
    /** Australian. */
    AUSTRALIAN("Australian", null, null, "Aussie"),
    /** Austrian. */
    AUSTRIAN("Austrian", null, null),
    /** Azerbaijani. */
    AZERBAIJANI("Azerbaijani", null, null, "Azeri"),

    // ========================================================================================
    // B
    // ========================================================================================
    /** Bahamian. */
    BAHAMIAN("Bahamian", null, null),
    /** Bahraini. */
    BAHRAINI("Bahraini", null, null),
    /** Bangladeshi. */
    BANGLADESHI("Bangladeshi", null, null, "Bengali"),
    /** Barbadian. */
    BARBADIAN("Barbadian", null, null, "Bajan"),
    /** Basotho. */
    BASOTHO("Basotho", "Mosotho", "Mosotho"),
    /** Belarusian. */
    BELARUSIAN("Belarusian", null, null),
    /** Belgian. */
    BELGIAN("Belgian", null, null),
    /** Belizean. */
    BELIZEAN("Belizean", null, null),
    /** Beninese. */
    BENINESE("Beninese", null, null, "Beninois"),
    /** Bermudian. */
    BERMUDIAN("Bermudian", null, null),
    /** Bhutanese. */
    BHUTANESE("Bhutanese", null, null),
    /** Bolivian. */
    BOLIVIAN("Bolivian", null, null),
    /** Bosnian. */
    BOSNIAN("Bosnian", null, null, "Herzegovinian"),
    /** Botswanan. */
    BOTSWANAN("Botswanan", "Motswana", "Motswana", "Batswana"),
    /** Brazilian. */
    BRAZILIAN("Brazilian", "Brasileiro", "Brasileira"),
    /** British. */
    BRITISH("British", "Briton", "Briton", "Brit"),
    /** Bruneian. */
    BRUNEIAN("Bruneian", null, null),
    /** Bulgarian. */
    BULGARIAN("Bulgarian", null, null),
    /** Burkinabé. */
    BURKINABE("Burkinabé", null, null),
    /** Burmese. */
    BURMESE("Burmese", null, null, "Myanmarese"),
    /** Burundian. */
    BURUNDIAN("Burundian", "Umurundi", "Umurundi", "Abarundi"),

    // ========================================================================================
    // C
    // ========================================================================================
    /** Cabo Verdean. */
    CABO_VERDEAN("Cabo Verdean", null, null, "Cape Verdean"),
    /** Cambodian. */
    CAMBODIAN("Cambodian", null, null, "Khmer"),
    /** Cameroonian. */
    CAMEROONIAN("Cameroonian", null, null),
    /** Canadian. */
    CANADIAN("Canadian", null, null, "Canuck"),
    /** Central African. */
    CENTRAL_AFRICAN("Central African", null, null),
    /** Chadian. */
    CHADIAN("Chadian", null, null),
    /** Chilean. */
    CHILEAN("Chilean", "Chileno", "Chilena"),
    /** Chinese. */
    CHINESE("Chinese", null, null),
    /** Colombian. */
    COLOMBIAN("Colombian", "Colombiano", "Colombiana"),
    /** Comorian. */
    COMORIAN("Comorian", null, null, "Comoran"),
    /** Congolese. */
    CONGOLESE("Congolese", null, null),
    /** Costa Rican. */
    COSTA_RICAN("Costa Rican", "Tico", "Tica"),
    /** Croatian. */
    CROATIAN("Croatian", null, null, "Croat"),
    /** Cubano ({@code Cuban}). */
    CUBAN("Cuban", "Cubano", "Cubana"),
    /** Cypriot. */
    CYPRIOT("Cypriot", null, null),
    /** Czech. */
    CZECH("Czech", null, null),

    // ========================================================================================
    // D
    // ========================================================================================
    /** Danish. */
    DANISH("Danish", "Dane", "Dane"),
    /** Djiboutian. */
    DJIBOUTIAN("Djiboutian", null, null),
    /** Dominican. */
    DOMINICAN("Dominican", "Dominicano", "Dominicana"),
    /** Dutchman ({@code Dutch}). */
    DUTCH("Dutch", "Dutchman", "Dutchwoman", "Netherlandic", "Hollander"),

    // ========================================================================================
    // E
    // ========================================================================================
    /** Ecuadorian. */
    ECUADORIAN("Ecuadorian", null, null, "Ecuadorean"),
    /** Egyptian. */
    EGYPTIAN("Egyptian", null, null),
    /** Emirati. */
    EMIRATI("Emirati", null, null, "Emirian"),
    /** Equatorial Guinean. */
    EQUATORIAL_GUINEAN("Equatorial Guinean", null, null, "Equatoguinean"),
    /** Eritrean. */
    ERITREAN("Eritrean", null, null),
    /** Estonian. */
    ESTONIAN("Estonian", null, null),
    /** Ethiopian. */
    ETHIOPIAN("Ethiopian", null, null),

    // ========================================================================================
    // F
    // ========================================================================================
    /** Fijian. */
    FIJIAN("Fijian", null, null, "Kaiviti"),
    /** Filipino. */
    FILIPINO("Filipino", "Pinoy", "Pinay", "Philippine", "Filipina"),
    /** Finnish. */
    FINNISH("Finnish", "Finn", "Finn"),
    /** Frenchman ({@code French}). */
    FRENCH("French", "Frenchman", "Frenchwoman"),

    // ========================================================================================
    // G
    // ========================================================================================
    /** Gabonese. */
    GABONESE("Gabonese", null, null),
    /** Gambian. */
    GAMBIAN("Gambian", null, null),
    /** Georgian. */
    GEORGIAN("Georgian", null, null),
    /** German. */
    GERMAN("German", null, null),
    /** Ghanaian. */
    GHANAIAN("Ghanaian", null, null),
    /** Hellenic ({@code Greek}). */
    GREEK("Greek", null, null, "Hellenic"),
    /** Grenadian. */
    GRENADIAN("Grenadian", null, null),
    /** Guatemalan. */
    GUATEMALAN("Guatemalan", "Guatemalteco", "Guatemalteca", "Chapín", "Chapina"),
    /** Guinean. */
    GUINEAN("Guinean", null, null),
    /** Bissau-Guinean. */
    BISSAU_GUINEAN("Bissau-Guinean", null, null),
    /** Guyanese. */
    GUYANESE("Guyanese", null, null),

    // ========================================================================================
    // H
    // ========================================================================================
    /** Haitian. */
    HAITIAN("Haitian", null, null),
    /** Honduran. */
    HONDURAN("Honduran", "Hondureño", "Hondureña", "Catracho", "Catracha"),
    /** Hungarian. */
    HUNGARIAN("Hungarian", null, null, "Magyar"),

    // ========================================================================================
    // I
    // ========================================================================================
    /** I-Kiribati. */
    I_KIRIBATI("I-Kiribati", null, null),
    /** Icelandic. */
    ICELANDIC("Icelandic", "Icelander", "Icelander"),
    /** Indian. */
    INDIAN("Indian", null, null),
    /** Indonesian. */
    INDONESIAN("Indonesian", null, null),
    /** Iranian. */
    IRANIAN("Iranian", null, null, "Persian"),
    /** Iraqi. */
    IRAQI("Iraqi", null, null),
    /** Irishman ({@code Irish}). */
    IRISH("Irish", "Irishman", "Irishwoman"),
    /** Israeli. */
    ISRAELI("Israeli", null, null),
    /** Italian. */
    ITALIAN("Italian", "Italiano", "Italiana"),
    /** Ivorian. */
    IVORIAN("Ivorian", null, null),

    // ========================================================================================
    // J
    // ========================================================================================
    /** Jamaican. */
    JAMAICAN("Jamaican", null, null),
    /** Japanese. */
    JAPANESE("Japanese", null, null),
    /** Jordanian. */
    JORDANIAN("Jordanian", null, null),

    // ========================================================================================
    // K
    // ========================================================================================
    /** Kazakhstani. */
    KAZAKHSTANI("Kazakhstani", null, null, "Kazakh"),
    /** Kenyan. */
    KENYAN("Kenyan", null, null),
    /** Kittitian. */
    KITTITIAN("Kittitian", null, null, "Nevisian"),
    /** Kuwaiti. */
    KUWAITI("Kuwaiti", null, null),
    /** Kyrgyzstani. */
    KYRGYZSTANI("Kyrgyzstani", null, null, "Kyrgyz", "Kirghiz"),

    // ========================================================================================
    // L
    // ========================================================================================
    /** Lao. */
    LAO("Lao", null, null, "Laotian"),
    /** Latvian. */
    LATVIAN("Latvian", null, null, "Lettish"),
    /** Lebanese. */
    LEBANESE("Lebanese", null, null),
    /** Liberian. */
    LIBERIAN("Liberian", null, null),
    /** Libyan. */
    LIBYAN("Libyan", null, null),
    /** Liechtensteiner. */
    LIECHTENSTEINER("Liechtensteiner", null, null),
    /** Lithuanian. */
    LITHUANIAN("Lithuanian", null, null),
    /** Luxembourgish. */
    LUXEMBOURGISH("Luxembourgish", null, null, "Luxembourger"),

    // ========================================================================================
    // M
    // ========================================================================================
    /** Macedonian. */
    MACEDONIAN("Macedonian", null, null),
    /** Malagasy. */
    MALAGASY("Malagasy", null, null),
    /** Malawian. */
    MALAWIAN("Malawian", null, null),
    /** Malaysian. */
    MALAYSIAN("Malaysian", null, null),
    /** Maldivian. */
    MALDIVIAN("Maldivian", null, null),
    /** Malinese ({@code Malian}). */
    MALIAN("Malian", null, null, "Malinese"),
    /** Maltese. */
    MALTESE("Maltese", null, null),
    /** Marshallese. */
    MARSHALLESE("Marshallese", null, null),
    /** Mauritanian. */
    MAURITANIAN("Mauritanian", null, null),
    /** Mauritian. */
    MAURITIAN("Mauritian", null, null),
    /** Mexican. */
    MEXICAN("Mexican", "Mexicano", "Mexicana"),
    /** Micronesian. */
    MICRONESIAN("Micronesian", null, null),
    /** Moldovan. */
    MOLDOVAN("Moldovan", null, null),
    /** Monégasque. */
    MONEGASQUE("Monégasque", null, null, "Monacan"),
    /** Mongolian. */
    MONGOLIAN("Mongolian", null, null),
    /** Montenegrin. */
    MONTENEGRIN("Montenegrin", null, null),
    /** Moroccan. */
    MOROCCAN("Moroccan", null, null),
    /** Mozambican. */
    MOZAMBICAN("Mozambican", null, null),

    // ========================================================================================
    // N
    // ========================================================================================
    /** Namibian. */
    NAMIBIAN("Namibian", null, null),
    /** Nauruan. */
    NAURUAN("Nauruan", null, null),
    /** Nepalese ({@code Nepali}). */
    NEPALI("Nepali", null, null, "Nepalese"),
    /** New Zealander. */
    NEW_ZEALANDER("New Zealander", null, null, "Kiwi"),
    /** Nicaraguan. */
    NICARAGUAN("Nicaraguan", "Nicaragüense", "Nicaragüense", "Nica"),
    /** Nigerien. */
    NIGERIEN("Nigerien", null, null),
    /** Nigerian. */
    NIGERIAN("Nigerian", null, null),
    /** North Korean. */
    NORTH_KOREAN("North Korean", null, null),
    /** Norwegian. */
    NORWEGIAN("Norwegian", "Norseman", "Norsewoman"),

    // ========================================================================================
    // O
    // ========================================================================================
    /** Omani. */
    OMANI("Omani", null, null),

    // ========================================================================================
    // P
    // ========================================================================================
    /** Pakistani. */
    PAKISTANI("Pakistani", null, null),
    /** Palauan. */
    PALAUAN("Palauan", null, null),
    /** Palestinian. */
    PALESTINIAN("Palestinian", null, null),
    /** Panamanian. */
    PANAMANIAN("Panamanian", "Panameño", "Panameña"),
    /** Papua New Guinean. */
    PAPUA_NEW_GUINEAN("Papua New Guinean", null, null, "Papuan"),
    /** Paraguayan. */
    PARAGUAYAN("Paraguayan", null, null),
    /** Peruvian. */
    PERUVIAN("Peruvian", "Peruano", "Peruana"),
    /** Pole ({@code Polish}). */
    POLISH("Polish", "Pole", "Pole"),
    /** Portuguese. */
    PORTUGUESE("Portuguese", "Português", "Portuguesa"),
    /** Puerto Rican. */
    PUERTO_RICAN("Puerto Rican", "Puertorriqueño", "Puertorriqueña", "Boricua"),

    // ========================================================================================
    // Q
    // ========================================================================================
    /** Qatari. */
    QATARI("Qatari", null, null),

    // ========================================================================================
    // R
    // ========================================================================================
    /** Romanian. */
    ROMANIAN("Romanian", null, null),
    /** Russian. */
    RUSSIAN("Russian", null, null),
    /** Rwandan. */
    RWANDAN("Rwandan", null, null),

    // ========================================================================================
    // S
    // ========================================================================================
    /** Saint Lucian. */
    SAINT_LUCIAN("Saint Lucian", null, null),
    /** Vincentian. */
    VINCENTIAN("Vincentian", null, null, "Saint Vincentian"),
    /** Salvadoran. */
    SALVADORAN("Salvadoran", "Salvadoreño", "Salvadoreña", "Guanaco", "Guanaca"),
    /** Samoan. */
    SAMOAN("Samoan", null, null),
    /** Sammarinese. */
    SAMMARINESE("Sammarinese", null, null),
    /** São Toméan. */
    SAO_TOMEAN("São Toméan", null, null),
    /** Saudi Arabian ({@code Saudi}). */
    SAUDI("Saudi", null, null, "Saudi Arabian"),
    /** Senegalese. */
    SENEGALESE("Senegalese", null, null),
    /** Serbian. */
    SERBIAN("Serbian", null, null, "Serb"),
    /** Seychellois. */
    SEYCHELLOIS("Seychellois", null, null, "Seychelloise"),
    /** Sierra Leonean. */
    SIERRA_LEONEAN("Sierra Leonean", null, null),
    /** Singaporean. */
    SINGAPOREAN("Singaporean", null, null),
    /** Slovak. */
    SLOVAK("Slovak", null, null),
    /** Slovenian. */
    SLOVENIAN("Slovenian", null, null, "Slovene"),
    /** Solomon Islander. */
    SOLOMON_ISLANDER("Solomon Islander", null, null),
    /** Somali. */
    SOMALI("Somali", null, null),
    /** South African. */
    SOUTH_AFRICAN("South African", null, null),
    /** South Korean. */
    SOUTH_KOREAN("South Korean", null, null),
    /** South Sudanese. */
    SOUTH_SUDANESE("South Sudanese", null, null),
    /** Spanish. */
    SPANISH("Spanish", "Spaniard", "Spaniard"),
    /** Sri Lankan. */
    SRI_LANKAN("Sri Lankan", null, null),
    /** Sudanese. */
    SUDANESE("Sudanese", null, null),
    /** Surinamese. */
    SURINAMESE("Surinamese", null, null),
    /** Liswati ({@code Swazi}). */
    SWAZI("Swazi", "Liswati", "Liswati", "Emaswati"),
    /** Swedish. */
    SWEDISH("Swedish", "Swede", "Swede"),
    /** Swiss. */
    SWISS("Swiss", null, null),
    /** Syrian. */
    SYRIAN("Syrian", null, null),

    // ========================================================================================
    // T
    // ========================================================================================
    /** Taiwanese. */
    TAIWANESE("Taiwanese", null, null),
    /** Tajikistani. */
    TAJIKISTANI("Tajikistani", null, null, "Tajik"),
    /** Tanzanian. */
    TANZANIAN("Tanzanian", null, null),
    /** Thai. */
    THAI("Thai", null, null),
    /** Timorese. */
    TIMORESE("Timorese", null, null, "East Timorese"),
    /** Togolese. */
    TOGOLESE("Togolese", null, null),
    /** Tongan. */
    TONGAN("Tongan", null, null),
    /** Trinidadian. */
    TRINIDADIAN("Trinidadian", null, null, "Tobagonian", "Trini"),
    /** Tunisian. */
    TUNISIAN("Tunisian", null, null),
    /** Turkish. */
    TURKISH("Turkish", "Turk", "Turk"),
    /** Turkmen. */
    TURKMEN("Turkmen", null, null, "Turkmenistani"),
    /** Tuvaluan. */
    TUVALUAN("Tuvaluan", null, null),

    // ========================================================================================
    // U
    // ========================================================================================
    /** Ugandan. */
    UGANDAN("Ugandan", null, null),
    /** Ukrainian. */
    UKRAINIAN("Ukrainian", null, null),
    /** Uruguayan. */
    URUGUAYAN("Uruguayan", null, null),
    /** Uzbekistani. */
    UZBEKISTANI("Uzbekistani", null, null, "Uzbek"),

    // ========================================================================================
    // V
    // ========================================================================================
    /** Ni-Vanuatu. */
    NI_VANUATU("Ni-Vanuatu", null, null, "Vanuatuan"),
    /** Vatican. */
    VATICAN("Vatican", null, null),
    /** Venezuelan. */
    VENEZUELAN("Venezuelan", "Venezolano", "Venezolana"),
    /** Vietnamese. */
    VIETNAMESE("Vietnamese", null, null),

    // ========================================================================================
    // W
    // ========================================================================================
    /** Welsh. */
    WELSH("Welsh", "Welshman", "Welshwoman"),

    // ========================================================================================
    // Y
    // ========================================================================================
    /** Yemeni. */
    YEMENI("Yemeni", null, null),

    // ========================================================================================
    // Z
    // ========================================================================================
    /** Zambian. */
    ZAMBIAN("Zambian", null, null),
    /** Zimbabwean. */
    ZIMBABWEAN("Zimbabwean", null, null),

    // ========================================================================================
    // Regional / Continental Demonyms
    // ========================================================================================
    /** African. */
    AFRICAN("African", null, null),
    /** Antarctic. */
    ANTARCTIC("Antarctic", null, null),
    /** Asian. */
    ASIAN("Asian", null, null),
    /** European. */
    EUROPEAN("European", null, null),
    /** North American. */
    NORTH_AMERICAN("North American", null, null),
    /** South American. */
    SOUTH_AMERICAN("South American", null, null),
    /** Oceanian. */
    OCEANIAN("Oceanian", null, null),
    /** Caribbean. */
    CARIBBEAN("Caribbean", null, null),
    /** Central American. */
    CENTRAL_AMERICAN("Central American", null, null),
    /** Middle Eastern. */
    MIDDLE_EASTERN("Middle Eastern", null, null),
    /** Scandinavian. */
    SCANDINAVIAN("Scandinavian", null, null),
    /** Polynesian. */
    POLYNESIAN("Polynesian", null, null),
    /** Melanesian. */
    MELANESIAN("Melanesian", null, null),
    /** Latin American. */
    LATIN_AMERICAN("Latin American", "Latino", "Latina"),
    /** Hispanic. */
    HISPANIC("Hispanic", "Hispano", "Hispana"),
    /** Arabic ({@code Arab}). */
    ARAB("Arab", null, null, "Arabic"),

    // ========================================================================================
    // City / Notable Place Demonyms
    // ========================================================================================
    /** Parisian. */
    PARISIAN("Parisian", "Parisien", "Parisienne"),
    /** Londoner. */
    LONDONER("Londoner", null, null),
    /** New Yorker. */
    NEW_YORKER("New Yorker", null, null),
    /** Berliner. */
    BERLINER("Berliner", null, null),
    /** Romano ({@code Roman}). */
    ROMAN("Roman", "Romano", "Romana"),
    /** Muscovite. */
    MUSCOVITE("Muscovite", null, null),
    /** Tokyoite. */
    TOKYOITE("Tokyoite", null, null),
    /** Manileño. */
    MANILENO("Manileño", "Manileño", "Manileña"),
    /** Sydneysider. */
    SYDNEYSIDER("Sydneysider", null, null),
    /** Habanero. */
    HABANERO("Habanero", "Habanero", "Habanera"),
    /** Limeña ({@code Limeño}). */
    LIMENO("Limeño", "Limeño", "Limeña"),
    /** Porteño. */
    PORTENO("Porteño", "Porteño", "Porteña"),
    /** Paulistano. */
    PAULISTANO("Paulistano", "Paulistano", "Paulistana"),
    /** Carioca. */
    CARIOCA("Carioca", null, null),
    /** Scottish. */
    SCOTSMAN("Scottish", "Scotsman", "Scotswoman", "Scot"),

    // ── added with the full ISO 3166-1 list ──
    /** Ålander. */
    ALANDER("Ålander", null, null, "Ålandic"),
    /** Caymanian. */
    CAYMANIAN("Caymanian", null, null),
    /** Cook Islander. */
    COOK_ISLANDER("Cook Islander", null, null),
    /** Curaçaoan. */
    CURACAOAN("Curaçaoan", null, null),
    /** Falkland Islander. */
    FALKLAND_ISLANDER("Falkland Islander", null, null, "Falklander"),
    /** Faroese. */
    FAROESE("Faroese", null, null),
    /** French Guianese. */
    FRENCH_GUIANESE("French Guianese", null, null),
    /** French Polynesian. */
    FRENCH_POLYNESIAN("French Polynesian", null, null),
    /** Gibraltarian. */
    GIBRALTARIAN("Gibraltarian", null, null),
    /** Greenlandic. */
    GREENLANDIC("Greenlandic", "Greenlander", "Greenlander"),
    /** Guadeloupean. */
    GUADELOUPEAN("Guadeloupean", null, null),
    /** Guamanian. */
    GUAMANIAN("Guamanian", null, null),
    /** Hongkonger. */
    HONG_KONGER("Hongkonger", null, null, "Hong Konger", "Hong Kongese"),
    /** Macanese. */
    MACANESE("Macanese", null, null),
    /** Mahoran. */
    MAHORAN("Mahoran", null, null),
    /** Manxman ({@code Manx}). */
    MANX("Manx", "Manxman", "Manxwoman"),
    /** Martinican. */
    MARTINICAN("Martinican", null, null, "Martiniquais"),
    /** Montserratian. */
    MONTSERRATIAN("Montserratian", null, null),
    /** New Caledonian. */
    NEW_CALEDONIAN("New Caledonian", null, null),
    /** Niuean. */
    NIUEAN("Niuean", null, null),
    /** Réunionese. */
    REUNIONESE("Réunionese", null, null, "Réunionnais"),
    /** Sahrawi. */
    SAHRAWI("Sahrawi", null, null, "Sahraoui"),
    /** Saint Helenian. */
    SAINT_HELENIAN("Saint Helenian", null, null),
    /** Tokelauan. */
    TOKELAUAN("Tokelauan", null, null),
    /** Virgin Islander. */
    VIRGIN_ISLANDER("Virgin Islander", null, null),

    /** Not known, or not one of the others. */
    UNKNOWN("Unknown", null, null);

    /** {@return whether this is {@link #UNKNOWN}} */
    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    // ========================================================================================
    // Fields
    // ========================================================================================

    private final String displayName;
    private final @Nullable String masculine;
    private final @Nullable String feminine;
    private final String[] alternativeNames;

    // ========================================================================================
    // Constructor
    // ========================================================================================

    /**
     * Constructs a Demonym with its primary display name, optional gendered forms,
     * and optional alternative names.
     *
     * @param displayName      the primary/default English demonym (e.g. "Filipino")
     * @param masculine        the male-specific form (e.g. "Pinoy"), or null if same as displayName
     * @param feminine         the female-specific form (e.g. "Pinay"), or null if same as displayName
     * @param alternativeNames optional additional accepted forms (e.g. "Philippine", "Filipina")
     */
    Demonym(String displayName, @Nullable String masculine, @Nullable String feminine, String... alternativeNames) {
        this.displayName = displayName;
        this.masculine = masculine;
        this.feminine = feminine;
        this.alternativeNames = alternativeNames;
    }

    // ========================================================================================
    // Accessors
    // ========================================================================================

    /**
     * Returns the primary demonym display name.
     * This is the gender-neutral / most commonly used English form.
     *
     * @return the primary demonym (e.g. "Filipino")
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Returns the masculine form if explicitly defined, or null.
     *
     * @return the masculine demonym (e.g. "Pinoy"), or null
     */
    public @Nullable String masculine() {
        return masculine;
    }

    /**
     * Returns the feminine form if explicitly defined, or null.
     *
     * @return the feminine demonym (e.g. "Pinay"), or null
     */
    public @Nullable String feminine() {
        return feminine;
    }

    /**
     * Returns the masculine form if defined, otherwise falls back to displayName.
     * <pre>
     *   FILIPINO.masculineOrDefault()  → "Pinoy"
     *   AMERICAN.masculineOrDefault()  → "American"
     *   BRAZILIAN.masculineOrDefault() → "Brasileiro"
     * </pre>
     *
     * @return the masculine form or the default displayName
     */
    public String masculineOrDefault() {
        return masculine != null ? masculine : displayName;
    }

    /**
     * Returns the feminine form if defined, otherwise falls back to displayName.
     * <pre>
     *   FILIPINO.feminineOrDefault()  → "Pinay"
     *   AMERICAN.feminineOrDefault()  → "American"
     *   BRAZILIAN.feminineOrDefault() → "Brasileira"
     * </pre>
     *
     * @return the feminine form or the default displayName
     */
    public String feminineOrDefault() {
        return feminine != null ? feminine : displayName;
    }

    /**
     * Returns true if this demonym has distinct masculine and/or feminine forms.
     *
     * @return true if gendered forms exist
     */
    public boolean hasGenderedForms() {
        return masculine != null || feminine != null;
    }

    /**
     * Returns alternative demonym forms, if any.
     * For example, FILIPINO has alternatives: "Philippine", "Filipina".
     *
     * @return array of alternative names (may be empty, never null)
     */
    public String[] alternativeNames() {
        return alternativeNames.clone();
    }

    /**
     * Returns true if this demonym has alternative forms beyond the primary and gendered forms.
     *
     * @return true if alternative names exist
     */
    public boolean hasAlternatives() {
        return alternativeNames.length > 0;
    }

    /**
     * Returns all known names for this demonym: displayName, masculine, feminine, and alternatives.
     * Null gendered forms are excluded.
     *
     * @return unmodifiable list of all demonym forms
     */
    public List<String> allNames() {
        var names = new java.util.ArrayList<String>();
        names.add(displayName);
        if (masculine != null) names.add(masculine);
        if (feminine != null) names.add(feminine);
        names.addAll(Arrays.asList(alternativeNames));
        return List.copyOf(names);
    }

    // ========================================================================================
    // Lookup Methods
    // ========================================================================================

    /**
     * Finds a demonym by matching against display name, masculine, feminine,
     * alternative names, or enum name. Case-insensitive. Returns null if not found.
     *
     * <p>This searches across ALL forms, so searching for "Pinoy", "Pinay",
     * "Filipino", "Philippine", or "Filipina" all return {@code FILIPINO}.</p>
     *
     * @param value the value to search for
     * @return the matching Demonym, or null
     */
    public static @Nullable Demonym fromAny(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return Arrays.stream(values())
                .filter(d -> matchesInternal(d, trimmed))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds a demonym by matching against all forms. Case-insensitive.
     *
     * @param value the value to search for
     * @return an Optional containing the matching Demonym
     */
    public static Optional<Demonym> findByAny(String value) {
        return Optional.ofNullable(fromAny(value));
    }

    /**
     * Finds a demonym by its primary display name only. Case-insensitive.
     *
     * @param name the display name to search for
     * @return an Optional containing the matching Demonym
     */
    public static Optional<Demonym> fromName(String name) {
        return Arrays.stream(values())
                .filter(d -> d.displayName.equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Returns all demonyms whose display name starts with the given prefix. Case-insensitive.
     *
     * @param prefix the prefix to search for
     * @return list of matching demonyms
     */
    public static List<Demonym> startingWith(String prefix) {
        String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(d -> d.displayName.toLowerCase(Locale.ROOT).startsWith(lowerPrefix))
                .toList();
    }

    /**
     * Returns all demonyms that have distinct masculine/feminine forms.
     *
     * @return list of gendered demonyms
     */
    public static List<Demonym> allGendered() {
        return Arrays.stream(values())
                .filter(Demonym::hasGenderedForms)
                .toList();
    }

    // ========================================================================================
    // Helpers
    // ========================================================================================

    /**
     * Returns the demonym in lowercase.
     *
     * @return lowercase demonym (e.g. "filipino")
     */
    public String lowercased() {
        return displayName.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the demonym in uppercase.
     *
     * @return uppercase demonym (e.g. "FILIPINO")
     */
    public String uppercased() {
        return displayName.toUpperCase(Locale.ROOT);
    }

    /**
     * Returns the plural form of the demonym.
     *
     * <p>Handles English pluralization rules for demonyms:</p>
     * <pre>
     *   -ese endings → unchanged  ("Chinese", "Japanese", "Vietnamese")
     *   -sh  endings → unchanged  ("Finnish", "Turkish", "Welsh")
     *   -ch  endings → unchanged  ("French", "Dutch")
     *   -ss  endings → unchanged  ("Swiss")
     *   all others   → append "s" ("Americans", "Filipinos", "Brazilians")
     * </pre>
     *
     * @return the plural form of the demonym
     */
    public String plural() {
        String lower = displayName.toLowerCase(Locale.ROOT);
        if (lower.endsWith("ese") || lower.endsWith("sh")
                || lower.endsWith("ch") || lower.endsWith("ss")) {
            return displayName;
        }
        return displayName + "s";
    }

    /**
     * Case-insensitive match against the display name, gendered forms,
     * alternative names, and enum name.
     *
     * @param value the value to check
     * @return true if it matches any form of this demonym
     */
    public boolean matches(String value) {
        return matchesInternal(this, value);
    }

    /**
     * Compare demonyms alphabetically by display name. Case-insensitive.
     *
     * @param other the Demonym to compare to
     * @return negative if this comes before, zero if equal, positive if after
     */
    public int compareByName(Demonym other) {
        return this.displayName.compareToIgnoreCase(other.displayName);
    }


    @Override
    public String toString() {
        return displayName;
    }

    // ========================================================================================
    // Internal
    // ========================================================================================

    private static boolean matchesInternal(Demonym demonym, String value) {
        return Stream.concat(
                Stream.of(
                        demonym.displayName,
                        demonym.masculine,
                        demonym.feminine,
                        demonym.name()
                ),
                Arrays.stream(demonym.alternativeNames)
        ).anyMatch(name -> name != null && name.equalsIgnoreCase(value));
    }

    /**
     * Parses a demonym from any of its forms, as {@link #fromAny(String)} matches them, ignoring case and
     * surrounding whitespace.
     *
     * @param text the demonym, e.g. {@code "Filipino"}, {@code "Pinay"}
     * @return the demonym
     * @throws ValueObjectException if it is {@code null} or matches no demonym other than {@link #UNKNOWN}
     */
    public static Demonym parse(String text) {
        if (text == null) throw new ValueObjectException("A Demonym is required");
        Demonym d = fromAny(text);
        if (d == null || d == UNKNOWN) throw new ValueObjectException("Not a demonym: '" + text + "'");
        return d;
    }

    /**
     * Parses a demonym as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as a {@link Demonym}, or empty when it is not one; never throws
     */
    public static Optional<Demonym> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text names a demonym, as {@link #parse(String)} reads it.
     *
     * @param text any text
     * @return whether it is a {@link Demonym}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
