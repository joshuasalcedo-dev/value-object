package io.joshuasalcedo.valueobject.geo;

import java.util.Locale;
import io.joshuasalcedo.valueobject.core.ValueObjectException;
import io.joshuasalcedo.valueobject.core.ValueObject;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Race and ethnicity value object representing common racial and ethnic classifications
 * used across various systems worldwide.
 *
 * <p>Race is widely recognized as a social construct rather than a biological category.
 * This enum provides standardized categories used in census data, demographics, medical
 * research, and self-identification. It draws from multiple classification systems:</p>
 *
 * <ul>
 *     <li><b>U.S. OMB/Census</b> — White, Black/African American, Asian, Native Hawaiian/Pacific Islander,
 *         American Indian/Alaska Native, Hispanic/Latino, Two or More Races</li>
 *     <li><b>Historical anthropological</b> — Caucasoid, Mongoloid, Negroid, Australoid, Amerindian (Blumenbach/Linnaeus)</li>
 *     <li><b>Brazilian Census (IBGE)</b> — Branco, Pardo, Preto, Amarelo, Indígena</li>
 *     <li><b>South African</b> — Black, White, Coloured, Indian/Asian</li>
 *     <li><b>UK Census</b> — White, Mixed, Asian/Asian British, Black/Black British, Other</li>
 *     <li><b>Common self-identification</b> — Brown, Mestizo, Mulatto, Creole, etc.</li>
 * </ul>
 *
 * <p>Each entry includes a display name, the classification system it belongs to,
 * and alternative names commonly used for that category.</p>
 *
 * @author JoshuaSalcedo
 * @since 2/25/2026
 * @see <a href="https://en.wikipedia.org/wiki/Race_(human_categorization)">Wikipedia: Race</a>
 * @see <a href="https://www.census.gov/topics/population/race/about.html">U.S. Census Bureau: Race</a>
 */
public enum Race implements ValueObject {

    // ========================================================================================
    // U.S. Census / OMB Standard Categories (1997 Revised Standards)
    // ========================================================================================

    /** A person having origins in any of the original peoples of Europe, the Middle East, or North Africa. */
    WHITE("White", Classification.US_CENSUS, "Caucasian", "Anglo"),

    /** A person having origins in any of the Black racial groups of Africa. */
    BLACK("Black", Classification.US_CENSUS, "African American", "Black American", "Afro-American"),

    /** A person having origins in any of the original peoples of the Far East, Southeast Asia, or the Indian subcontinent. */
    ASIAN("Asian", Classification.US_CENSUS, "Asian American"),

    /** A person having origins in any of the original peoples of Hawaii, Guam, Samoa, or other Pacific Islands. */
    NATIVE_HAWAIIAN_PACIFIC_ISLANDER("Native Hawaiian or Other Pacific Islander", Classification.US_CENSUS,
            "Pacific Islander", "NHPI", "Pasifika"),

    /** A person having origins in any of the original peoples of North and South America (including Central America). */
    AMERICAN_INDIAN_ALASKA_NATIVE("American Indian or Alaska Native", Classification.US_CENSUS,
            "Native American", "Indigenous American", "First Nations", "AIAN"),

    /** A person of Cuban, Mexican, Puerto Rican, South or Central American, or other Spanish culture or origin. */
    HISPANIC_LATINO("Hispanic or Latino", Classification.US_CENSUS,
            "Hispanic", "Latino", "Latina", "Latinx", "Latine"),

    /** A person identifying with two or more racial categories. */
    TWO_OR_MORE_RACES("Two or More Races", Classification.US_CENSUS,
            "Multiracial", "Mixed Race", "Biracial", "Mixed"),

    /** Used when race is not specified or does not fit other categories. */
    OTHER("Other", Classification.US_CENSUS, "Some Other Race"),

    // ========================================================================================
    // Middle Eastern / North African (Proposed U.S. Census, commonly used globally)
    // ========================================================================================

    /** A person having origins in the Middle East or North Africa. Used in many international contexts. */
    MIDDLE_EASTERN_NORTH_AFRICAN("Middle Eastern or North African", Classification.INTERNATIONAL,
            "MENA", "Arab", "Middle Eastern"),

    // ========================================================================================
    // Historical / Anthropological Classifications (Blumenbach, 1775)
    // Included for historical reference and data compatibility — these terms are outdated.
    // ========================================================================================

    /** Historical classification by Blumenbach (1775). Considered outdated. */
    CAUCASOID("Caucasoid", Classification.HISTORICAL, "Caucasian"),

    /** Historical classification by Blumenbach (1775). Considered outdated and offensive. */
    MONGOLOID("Mongoloid", Classification.HISTORICAL),

    /** Historical classification by Blumenbach (1775). Considered outdated and offensive. */
    NEGROID("Negroid", Classification.HISTORICAL, "Negro"),

    /** Historical classification. Indigenous peoples of Australia, New Guinea, and parts of South Asia. */
    AUSTRALOID("Australoid", Classification.HISTORICAL),

    /** Historical classification for indigenous peoples of the Americas. */
    AMERINDIAN("Amerindian", Classification.HISTORICAL, "American Indian"),

    /** Historical Blumenbach classification for peoples of Southeast Asia and Pacific Islands. */
    MALAY("Malay", Classification.HISTORICAL, "Malayan"),

    // ========================================================================================
    // Brazilian Census (IBGE) Categories
    // ========================================================================================

    /** White — Brazilian census category. */
    BRANCO("Branco", Classification.BRAZILIAN, "White"),

    /** Mixed/Multiracial — Brazilian census category. Largest self-identified group in Brazil. */
    PARDO("Pardo", Classification.BRAZILIAN, "Brown", "Mixed", "Multiracial"),

    /** Black — Brazilian census category. */
    PRETO("Preto", Classification.BRAZILIAN, "Black"),

    /** Asian — Brazilian census category (primarily Japanese-Brazilian and other East Asians). */
    AMARELO("Amarelo", Classification.BRAZILIAN, "Yellow", "Asian"),

    /** Indigenous — Brazilian census category. */
    INDIGENA("Indígena", Classification.BRAZILIAN, "Indigenous"),

    // ========================================================================================
    // South African Categories (Population Registration Act legacy)
    // ========================================================================================

    /** Mixed ethnic background — South African classification. */
    COLOURED("Coloured", Classification.SOUTH_AFRICAN, "Cape Coloured"),

    /** Indian/Asian — South African classification. */
    INDIAN_SOUTH_AFRICAN("Indian/Asian", Classification.SOUTH_AFRICAN, "Indian"),

    // ========================================================================================
    // Common Self-Identification / Global Terms
    // ========================================================================================

    /** Person of mixed European and Indigenous American ancestry. Common in Latin America. */
    MESTIZO("Mestizo", Classification.SELF_IDENTIFICATION, "Mestiza"),

    /** Person of mixed European and African ancestry. Historical term. */
    MULATTO("Mulatto", Classification.SELF_IDENTIFICATION, "Mulata"),

    /** Mixed-heritage people, especially from Louisiana/Caribbean French colonial contexts. */
    CREOLE("Creole", Classification.SELF_IDENTIFICATION),

    /** Mixed-heritage term used in Canada, particularly for Métis people. */
    METIS("Métis", Classification.SELF_IDENTIFICATION),

    /** Indigenous peoples of Australia. */
    ABORIGINAL_AUSTRALIAN("Aboriginal Australian", Classification.SELF_IDENTIFICATION,
            "Aboriginal", "First Australians", "Indigenous Australian"),

    /** Indigenous peoples of New Zealand. */
    MAORI("Māori", Classification.SELF_IDENTIFICATION),

    /** Indigenous peoples of northern Europe (Norway, Sweden, Finland, Russia). */
    SAMI("Sámi", Classification.SELF_IDENTIFICATION, "Sami", "Lapp"),

    /** Roma people — ethnic group of South Asian origin spread across Europe and the Americas. */
    ROMA("Roma", Classification.SELF_IDENTIFICATION, "Romani", "Romany"),

    /** Self-identification as Brown, used informally in many cultures. */
    BROWN("Brown", Classification.SELF_IDENTIFICATION, "Moreno", "Morena"),

    /** A person who prefers not to disclose their race. */
    PREFER_NOT_TO_SAY("Prefer Not to Say", Classification.SELF_IDENTIFICATION, "Declined", "Not Disclosed"),

    /** Unknown or not collected. */
    UNKNOWN("Unknown", Classification.SELF_IDENTIFICATION, "Not Specified", "N/A");

    /** {@return whether this is {@link #UNKNOWN}} */
    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    // ========================================================================================
    // Fields
    // ========================================================================================

    private final String displayName;
    private final Classification classification;
    private final String[] alternativeNames;

    // ========================================================================================
    // Constructor
    // ========================================================================================

    /**
     * Constructs a Race entry.
     *
     * @param displayName      the primary display name
     * @param classification   the classification system this category belongs to
     * @param alternativeNames optional alternative terms
     */
    Race(String displayName, Classification classification, String... alternativeNames) {
        this.displayName = displayName;
        this.classification = classification;
        this.alternativeNames = alternativeNames;
    }

    // ========================================================================================
    // Accessors
    // ========================================================================================

    /**
     * Returns the primary display name.
     *
     * @return the display name (e.g. "Black", "Pardo", "Mestizo")
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Returns the classification system this race/ethnicity category belongs to.
     *
     * @return the classification system
     */
    public Classification classification() {
        return classification;
    }

    /**
     * Returns alternative names for this race/ethnicity.
     *
     * @return array of alternative names (may be empty, never null)
     */
    public String[] alternativeNames() {
        return alternativeNames.clone();
    }

    /**
     * Returns all known names: display name plus all alternatives.
     *
     * @return unmodifiable list of all names
     */
    public List<String> allNames() {
        var names = new java.util.ArrayList<String>();
        names.add(displayName);
        names.addAll(Arrays.asList(alternativeNames));
        return List.copyOf(names);
    }

    /**
     * Returns true if this category has alternative names.
     *
     * @return true if alternatives exist
     */
    public boolean hasAlternatives() {
        return alternativeNames.length > 0;
    }

    // ========================================================================================
    // Lookup Methods
    // ========================================================================================

    /**
     * Finds a race by matching against display name, alternative names, or enum name.
     * Case-insensitive. Returns null if not found.
     *
     * <p>Examples: "Black", "African American", "Afro-American" all return {@code BLACK}.</p>
     *
     * @param value the value to search for
     * @return the matching Race, or null
     */
    public static @Nullable Race fromAny(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return Arrays.stream(values())
                .filter(r -> matchesInternal(r, trimmed))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds a race by matching against all forms. Case-insensitive.
     *
     * @param value the value to search for
     * @return an Optional containing the matching Race
     */
    public static Optional<Race> findByAny(String value) {
        return Optional.ofNullable(fromAny(value));
    }

    /**
     * Finds a race by its primary display name only. Case-insensitive.
     *
     * @param name the display name to search for
     * @return an Optional containing the matching Race
     */
    public static Optional<Race> fromName(String name) {
        return Arrays.stream(values())
                .filter(r -> r.displayName.equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Returns all race/ethnicity categories belonging to a specific classification system.
     *
     * @param classification the classification system
     * @return list of matching categories
     */
    public static List<Race> byClassification(Classification classification) {
        return Arrays.stream(values())
                .filter(r -> r.classification == classification)
                .toList();
    }

    /**
     * Returns the standard U.S. Census / OMB categories only.
     *
     * @return list of U.S. Census race categories
     */
    public static List<Race> usCensusCategories() {
        return byClassification(Classification.US_CENSUS);
    }

    /**
     * Returns all race/ethnicity categories whose display name starts with the given prefix.
     * Case-insensitive.
     *
     * @param prefix the prefix to search for
     * @return list of matching categories
     */
    public static List<Race> startingWith(String prefix) {
        String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(r -> r.displayName.toLowerCase(Locale.ROOT).startsWith(lowerPrefix))
                .toList();
    }

    // ========================================================================================
    // Helpers
    // ========================================================================================

    /**
     * Returns true if this category belongs to the U.S. Census classification.
     *
     * @return true if U.S. Census category
     */
    public boolean isUsCensus() {
        return classification == Classification.US_CENSUS;
    }

    /**
     * Returns true if this is a historical/outdated classification.
     * These terms are included for data compatibility but should generally be avoided.
     *
     * @return true if historical classification
     */
    public boolean isHistorical() {
        return classification == Classification.HISTORICAL;
    }

    /**
     * Case-insensitive match against the display name, enum name, or any alternative name.
     *
     * @param value the value to check
     * @return true if it matches any form of this race/ethnicity
     */
    public boolean matches(String value) {
        return matchesInternal(this, value);
    }

    /**
     * Compare by display name alphabetically. Case-insensitive.
     *
     * @param other the Race to compare to
     * @return negative if this comes before, zero if equal, positive if after
     */
    public int compareByName(Race other) {
        return this.displayName.compareToIgnoreCase(other.displayName);
    }


    @Override
    public String toString() {
        return displayName;
    }

    // ========================================================================================
    // Internal
    // ========================================================================================

    private static boolean matchesInternal(Race race, String value) {
        if (race.displayName.equalsIgnoreCase(value)) return true;
        if (race.name().equalsIgnoreCase(value)) return true;
        for (String alt : race.alternativeNames) {
            if (alt.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    // ========================================================================================
    // Classification System
    // ========================================================================================

    /**
     * The classification system a racial/ethnic category originates from.
     */
    public enum Classification {

        /** U.S. Office of Management and Budget / Census Bureau standard (1997 revised). */
        US_CENSUS("U.S. Census / OMB"),

        /** Historical anthropological classifications (18th-19th century). Considered outdated. */
        HISTORICAL("Historical / Anthropological"),

        /** Brazilian Institute of Geography and Statistics (IBGE) census categories. */
        BRAZILIAN("Brazilian Census (IBGE)"),

        /** South African population classification categories. */
        SOUTH_AFRICAN("South African"),

        /** Internationally recognized categories not tied to a single country's census. */
        INTERNATIONAL("International"),

        /** Common terms used in self-identification across cultures. */
        SELF_IDENTIFICATION("Self-Identification");

        private final String displayName;

        Classification(String displayName) {
            this.displayName = displayName;
        }

        /** {@return the name of the classification system, e.g. {@code "Self-Identification"}} */
        public String displayName() {
            return displayName;
        }
    }

    /**
     * Parses a race from its display name, enum name or an alternative name, ignoring case and surrounding
     * whitespace, as {@link #fromAny(String)} does.
     *
     * @param text the name, e.g. {@code "Asian"} or {@code "Caucasian"}
     * @return the race
     * @throws ValueObjectException if it is {@code null} or matches nothing other than {@link #UNKNOWN}
     */
    public static Race parse(String text) {
        if (text == null) throw new ValueObjectException("A Race is required");
        Race r = fromAny(text);
        if (r == null || r == UNKNOWN) throw new ValueObjectException("Not a race: '" + text + "'");
        return r;
    }

    /**
     * Parses a race as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as a {@link Race}, or empty when it is not one; never throws
     */
    public static Optional<Race> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text names a race, as {@link #parse(String)} reads it.
     *
     * @param text any text
     * @return whether it is a {@link Race}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
