package io.joshuasalcedo.valueobject.geo;

import io.joshuasalcedo.valueobject.ValueObject;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
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
    AFGHAN("Afghan", null, null),
    ALBANIAN("Albanian", null, null),
    ALGERIAN("Algerian", null, null),
    AMERICAN("American", null, null),
    AMERICAN_SAMOAN("American Samoan", null, null),
    ANDORRAN("Andorran", null, null),
    ANGOLAN("Angolan", null, null),
    ANGUILLIAN("Anguillian", null, null),
    ANTIGUAN("Antiguan", null, null, "Barbudan"),
    ARGENTINE("Argentine", "Argentino", "Argentina", "Argentinian", "Argentinean"),
    ARMENIAN("Armenian", null, null),
    ARUBAN("Aruban", null, null),
    AUSTRALIAN("Australian", null, null, "Aussie"),
    AUSTRIAN("Austrian", null, null),
    AZERBAIJANI("Azerbaijani", null, null, "Azeri"),

    // ========================================================================================
    // B
    // ========================================================================================
    BAHAMIAN("Bahamian", null, null),
    BAHRAINI("Bahraini", null, null),
    BANGLADESHI("Bangladeshi", null, null, "Bengali"),
    BARBADIAN("Barbadian", null, null, "Bajan"),
    BASOTHO("Basotho", "Mosotho", "Mosotho"),
    BELARUSIAN("Belarusian", null, null),
    BELGIAN("Belgian", null, null),
    BELIZEAN("Belizean", null, null),
    BENINESE("Beninese", null, null, "Beninois"),
    BERMUDIAN("Bermudian", null, null),
    BHUTANESE("Bhutanese", null, null),
    BOLIVIAN("Bolivian", null, null),
    BOSNIAN("Bosnian", null, null, "Herzegovinian"),
    BOTSWANAN("Botswanan", "Motswana", "Motswana", "Batswana"),
    BRAZILIAN("Brazilian", "Brasileiro", "Brasileira"),
    BRITISH("British", "Briton", "Briton", "Brit"),
    BRUNEIAN("Bruneian", null, null),
    BULGARIAN("Bulgarian", null, null),
    BURKINABE("Burkinabé", null, null),
    BURMESE("Burmese", null, null, "Myanmarese"),
    BURUNDIAN("Burundian", "Umurundi", "Umurundi", "Abarundi"),

    // ========================================================================================
    // C
    // ========================================================================================
    CABO_VERDEAN("Cabo Verdean", null, null, "Cape Verdean"),
    CAMBODIAN("Cambodian", null, null, "Khmer"),
    CAMEROONIAN("Cameroonian", null, null),
    CANADIAN("Canadian", null, null, "Canuck"),
    CENTRAL_AFRICAN("Central African", null, null),
    CHADIAN("Chadian", null, null),
    CHILEAN("Chilean", "Chileno", "Chilena"),
    CHINESE("Chinese", null, null),
    COLOMBIAN("Colombian", "Colombiano", "Colombiana"),
    COMORIAN("Comorian", null, null, "Comoran"),
    CONGOLESE("Congolese", null, null),
    COSTA_RICAN("Costa Rican", "Tico", "Tica"),
    CROATIAN("Croatian", null, null, "Croat"),
    CUBAN("Cuban", "Cubano", "Cubana"),
    CYPRIOT("Cypriot", null, null),
    CZECH("Czech", null, null),

    // ========================================================================================
    // D
    // ========================================================================================
    DANISH("Danish", "Dane", "Dane"),
    DJIBOUTIAN("Djiboutian", null, null),
    DOMINICAN("Dominican", "Dominicano", "Dominicana"),
    DUTCH("Dutch", "Dutchman", "Dutchwoman", "Netherlandic", "Hollander"),

    // ========================================================================================
    // E
    // ========================================================================================
    ECUADORIAN("Ecuadorian", null, null, "Ecuadorean"),
    EGYPTIAN("Egyptian", null, null),
    EMIRATI("Emirati", null, null, "Emirian"),
    EQUATORIAL_GUINEAN("Equatorial Guinean", null, null, "Equatoguinean"),
    ERITREAN("Eritrean", null, null),
    ESTONIAN("Estonian", null, null),
    ETHIOPIAN("Ethiopian", null, null),

    // ========================================================================================
    // F
    // ========================================================================================
    FIJIAN("Fijian", null, null, "Kaiviti"),
    FILIPINO("Filipino", "Pinoy", "Pinay", "Philippine", "Filipina"),
    FINNISH("Finnish", "Finn", "Finn"),
    FRENCH("French", "Frenchman", "Frenchwoman"),

    // ========================================================================================
    // G
    // ========================================================================================
    GABONESE("Gabonese", null, null),
    GAMBIAN("Gambian", null, null),
    GEORGIAN("Georgian", null, null),
    GERMAN("German", null, null),
    GHANAIAN("Ghanaian", null, null),
    GREEK("Greek", null, null, "Hellenic"),
    GRENADIAN("Grenadian", null, null),
    GUATEMALAN("Guatemalan", "Guatemalteco", "Guatemalteca", "Chapín", "Chapina"),
    GUINEAN("Guinean", null, null),
    BISSAU_GUINEAN("Bissau-Guinean", null, null),
    GUYANESE("Guyanese", null, null),

    // ========================================================================================
    // H
    // ========================================================================================
    HAITIAN("Haitian", null, null),
    HONDURAN("Honduran", "Hondureño", "Hondureña", "Catracho", "Catracha"),
    HUNGARIAN("Hungarian", null, null, "Magyar"),

    // ========================================================================================
    // I
    // ========================================================================================
    I_KIRIBATI("I-Kiribati", null, null),
    ICELANDIC("Icelandic", "Icelander", "Icelander"),
    INDIAN("Indian", null, null),
    INDONESIAN("Indonesian", null, null),
    IRANIAN("Iranian", null, null, "Persian"),
    IRAQI("Iraqi", null, null),
    IRISH("Irish", "Irishman", "Irishwoman"),
    ISRAELI("Israeli", null, null),
    ITALIAN("Italian", "Italiano", "Italiana"),
    IVORIAN("Ivorian", null, null),

    // ========================================================================================
    // J
    // ========================================================================================
    JAMAICAN("Jamaican", null, null),
    JAPANESE("Japanese", null, null),
    JORDANIAN("Jordanian", null, null),

    // ========================================================================================
    // K
    // ========================================================================================
    KAZAKHSTANI("Kazakhstani", null, null, "Kazakh"),
    KENYAN("Kenyan", null, null),
    KITTITIAN("Kittitian", null, null, "Nevisian"),
    KUWAITI("Kuwaiti", null, null),
    KYRGYZSTANI("Kyrgyzstani", null, null, "Kyrgyz", "Kirghiz"),

    // ========================================================================================
    // L
    // ========================================================================================
    LAO("Lao", null, null, "Laotian"),
    LATVIAN("Latvian", null, null, "Lettish"),
    LEBANESE("Lebanese", null, null),
    LIBERIAN("Liberian", null, null),
    LIBYAN("Libyan", null, null),
    LIECHTENSTEINER("Liechtensteiner", null, null),
    LITHUANIAN("Lithuanian", null, null),
    LUXEMBOURGISH("Luxembourgish", null, null, "Luxembourger"),

    // ========================================================================================
    // M
    // ========================================================================================
    MACEDONIAN("Macedonian", null, null),
    MALAGASY("Malagasy", null, null),
    MALAWIAN("Malawian", null, null),
    MALAYSIAN("Malaysian", null, null),
    MALDIVIAN("Maldivian", null, null),
    MALIAN("Malian", null, null, "Malinese"),
    MALTESE("Maltese", null, null),
    MARSHALLESE("Marshallese", null, null),
    MAURITANIAN("Mauritanian", null, null),
    MAURITIAN("Mauritian", null, null),
    MEXICAN("Mexican", "Mexicano", "Mexicana"),
    MICRONESIAN("Micronesian", null, null),
    MOLDOVAN("Moldovan", null, null),
    MONEGASQUE("Monégasque", null, null, "Monacan"),
    MONGOLIAN("Mongolian", null, null),
    MONTENEGRIN("Montenegrin", null, null),
    MOROCCAN("Moroccan", null, null),
    MOZAMBICAN("Mozambican", null, null),

    // ========================================================================================
    // N
    // ========================================================================================
    NAMIBIAN("Namibian", null, null),
    NAURUAN("Nauruan", null, null),
    NEPALI("Nepali", null, null, "Nepalese"),
    NEW_ZEALANDER("New Zealander", null, null, "Kiwi"),
    NICARAGUAN("Nicaraguan", "Nicaragüense", "Nicaragüense", "Nica"),
    NIGERIEN("Nigerien", null, null),
    NIGERIAN("Nigerian", null, null),
    NORTH_KOREAN("North Korean", null, null),
    NORWEGIAN("Norwegian", "Norseman", "Norsewoman"),

    // ========================================================================================
    // O
    // ========================================================================================
    OMANI("Omani", null, null),

    // ========================================================================================
    // P
    // ========================================================================================
    PAKISTANI("Pakistani", null, null),
    PALAUAN("Palauan", null, null),
    PALESTINIAN("Palestinian", null, null),
    PANAMANIAN("Panamanian", "Panameño", "Panameña"),
    PAPUA_NEW_GUINEAN("Papua New Guinean", null, null, "Papuan"),
    PARAGUAYAN("Paraguayan", null, null),
    PERUVIAN("Peruvian", "Peruano", "Peruana"),
    POLISH("Polish", "Pole", "Pole"),
    PORTUGUESE("Portuguese", "Português", "Portuguesa"),
    PUERTO_RICAN("Puerto Rican", "Puertorriqueño", "Puertorriqueña", "Boricua"),

    // ========================================================================================
    // Q
    // ========================================================================================
    QATARI("Qatari", null, null),

    // ========================================================================================
    // R
    // ========================================================================================
    ROMANIAN("Romanian", null, null),
    RUSSIAN("Russian", null, null),
    RWANDAN("Rwandan", null, null),

    // ========================================================================================
    // S
    // ========================================================================================
    SAINT_LUCIAN("Saint Lucian", null, null),
    VINCENTIAN("Vincentian", null, null, "Saint Vincentian"),
    SALVADORAN("Salvadoran", "Salvadoreño", "Salvadoreña", "Guanaco", "Guanaca"),
    SAMOAN("Samoan", null, null),
    SAMMARINESE("Sammarinese", null, null),
    SAO_TOMEAN("São Toméan", null, null),
    SAUDI("Saudi", null, null, "Saudi Arabian"),
    SENEGALESE("Senegalese", null, null),
    SERBIAN("Serbian", null, null, "Serb"),
    SEYCHELLOIS("Seychellois", null, null, "Seychelloise"),
    SIERRA_LEONEAN("Sierra Leonean", null, null),
    SINGAPOREAN("Singaporean", null, null),
    SLOVAK("Slovak", null, null),
    SLOVENIAN("Slovenian", null, null, "Slovene"),
    SOLOMON_ISLANDER("Solomon Islander", null, null),
    SOMALI("Somali", null, null),
    SOUTH_AFRICAN("South African", null, null),
    SOUTH_KOREAN("South Korean", null, null),
    SOUTH_SUDANESE("South Sudanese", null, null),
    SPANISH("Spanish", "Spaniard", "Spaniard"),
    SRI_LANKAN("Sri Lankan", null, null),
    SUDANESE("Sudanese", null, null),
    SURINAMESE("Surinamese", null, null),
    SWAZI("Swazi", "Liswati", "Liswati", "Emaswati"),
    SWEDISH("Swedish", "Swede", "Swede"),
    SWISS("Swiss", null, null),
    SYRIAN("Syrian", null, null),

    // ========================================================================================
    // T
    // ========================================================================================
    TAIWANESE("Taiwanese", null, null),
    TAJIKISTANI("Tajikistani", null, null, "Tajik"),
    TANZANIAN("Tanzanian", null, null),
    THAI("Thai", null, null),
    TIMORESE("Timorese", null, null, "East Timorese"),
    TOGOLESE("Togolese", null, null),
    TONGAN("Tongan", null, null),
    TRINIDADIAN("Trinidadian", null, null, "Tobagonian", "Trini"),
    TUNISIAN("Tunisian", null, null),
    TURKISH("Turkish", "Turk", "Turk"),
    TURKMEN("Turkmen", null, null, "Turkmenistani"),
    TUVALUAN("Tuvaluan", null, null),

    // ========================================================================================
    // U
    // ========================================================================================
    UGANDAN("Ugandan", null, null),
    UKRAINIAN("Ukrainian", null, null),
    URUGUAYAN("Uruguayan", null, null),
    UZBEKISTANI("Uzbekistani", null, null, "Uzbek"),

    // ========================================================================================
    // V
    // ========================================================================================
    NI_VANUATU("Ni-Vanuatu", null, null, "Vanuatuan"),
    VATICAN("Vatican", null, null),
    VENEZUELAN("Venezuelan", "Venezolano", "Venezolana"),
    VIETNAMESE("Vietnamese", null, null),

    // ========================================================================================
    // W
    // ========================================================================================
    WELSH("Welsh", "Welshman", "Welshwoman"),

    // ========================================================================================
    // Y
    // ========================================================================================
    YEMENI("Yemeni", null, null),

    // ========================================================================================
    // Z
    // ========================================================================================
    ZAMBIAN("Zambian", null, null),
    ZIMBABWEAN("Zimbabwean", null, null),

    // ========================================================================================
    // Regional / Continental Demonyms
    // ========================================================================================
    AFRICAN("African", null, null),
    ANTARCTIC("Antarctic", null, null),
    ASIAN("Asian", null, null),
    EUROPEAN("European", null, null),
    NORTH_AMERICAN("North American", null, null),
    SOUTH_AMERICAN("South American", null, null),
    OCEANIAN("Oceanian", null, null),
    CARIBBEAN("Caribbean", null, null),
    CENTRAL_AMERICAN("Central American", null, null),
    MIDDLE_EASTERN("Middle Eastern", null, null),
    SCANDINAVIAN("Scandinavian", null, null),
    POLYNESIAN("Polynesian", null, null),
    MELANESIAN("Melanesian", null, null),
    LATIN_AMERICAN("Latin American", "Latino", "Latina"),
    HISPANIC("Hispanic", "Hispano", "Hispana"),
    ARAB("Arab", null, null, "Arabic"),

    // ========================================================================================
    // City / Notable Place Demonyms
    // ========================================================================================
    PARISIAN("Parisian", "Parisien", "Parisienne"),
    LONDONER("Londoner", null, null),
    NEW_YORKER("New Yorker", null, null),
    BERLINER("Berliner", null, null),
    ROMAN("Roman", "Romano", "Romana"),
    MUSCOVITE("Muscovite", null, null),
    TOKYOITE("Tokyoite", null, null),
    MANILENO("Manileño", "Manileño", "Manileña"),
    SYDNEYSIDER("Sydneysider", null, null),
    HABANERO("Habanero", "Habanero", "Habanera"),
    LIMENO("Limeño", "Limeño", "Limeña"),
    PORTENO("Porteño", "Porteño", "Porteña"),
    PAULISTANO("Paulistano", "Paulistano", "Paulistana"),
    CARIOCA("Carioca", null, null),
    SCOTSMAN("Scottish", "Scotsman", "Scotswoman", "Scot"),

    UNKNOWN("Unknown", null, null);

    /**
     * @return whether this is {@link #UNKNOWN}
     */
    public boolean isNull() {
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
        String lowerPrefix = prefix.toLowerCase();
        return Arrays.stream(values())
                .filter(d -> d.displayName.toLowerCase().startsWith(lowerPrefix))
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
        return displayName.toLowerCase();
    }

    /**
     * Returns the demonym in uppercase.
     *
     * @return uppercase demonym (e.g. "FILIPINO")
     */
    public String uppercased() {
        return displayName.toUpperCase();
    }

    /**
     * Returns the plural form of the demonym.
     *
     * <p>Handles English pluralization rules for demonyms:</p>
     * <pre>
     *   -ese endings → unchanged  ("Chinese", "Japanese", "Vietnamese")
     *   -ish endings → unchanged  ("Finnish", "Turkish", "Swedish")
     *   -ch  endings → unchanged  ("French", "Dutch")
     *   -ss  endings → unchanged  ("Swiss")
     *   all others   → append "s" ("Americans", "Filipinos", "Brazilians")
     * </pre>
     *
     * @return the plural form of the demonym
     */
    public String plural() {
        if (displayName.endsWith("ese") || displayName.endsWith("ish")
                || displayName.endsWith("ch") || displayName.endsWith("ss")) {
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

    /**
     * Returns the null object representation of Demonym.
     *
     * @return the UNKNOWN demonym constant
     */
    public static Demonym nullValue() {
        return UNKNOWN;
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
}