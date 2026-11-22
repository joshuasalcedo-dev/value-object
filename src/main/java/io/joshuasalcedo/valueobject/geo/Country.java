package io.joshuasalcedo.valueobject.geo;

import io.joshuasalcedo.valueobject.ValueObject;


import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.joshuasalcedo.valueobject.geo.Continent.*;

/**
 * Country value object representing a sovereign state or territory.
 *
 * <p>Each country has an ISO 3166-1 alpha-2 code, a display name, a primary {@link Demonym},
 * and a {@link Continent}. A country may also have additional demonyms beyond the primary one
 * (e.g. Philippines has Filipino, Pinoy/Pinay).</p>
 *
 * @author JoshuaSalcedo
 * @since 2/25/2026
 * @see Demonym
 * @see Continent
 */
public enum Country implements ValueObject {

    AFGHANISTAN("AF", "Afghanistan", Demonym.AFGHAN, ASIA),
    ALBANIA("AL", "Albania", Demonym.ALBANIAN, EUROPE),
    ALGERIA("DZ", "Algeria", Demonym.ALGERIAN, AFRICA),
    AMERICAN_SAMOA("AS", "American Samoa", Demonym.AMERICAN_SAMOAN, OCEANIA),
    ANDORRA("AD", "Andorra", Demonym.ANDORRAN, EUROPE),
    ANGOLA("AO", "Angola", Demonym.ANGOLAN, AFRICA),
    ANGUILLA("AI", "Anguilla", Demonym.ANGUILLIAN, NORTH_AMERICA),
    ANTIGUA_AND_BARBUDA("AG", "Antigua & Barbuda", Demonym.ANTIGUAN, NORTH_AMERICA),
    ARGENTINA("AR", "Argentina", Demonym.ARGENTINE, SOUTH_AMERICA),
    ARMENIA("AM", "Armenia", Demonym.ARMENIAN, ASIA),
    ARUBA("AW", "Aruba", Demonym.ARUBAN, NORTH_AMERICA),
    AUSTRALIA("AU", "Australia", Demonym.AUSTRALIAN, OCEANIA),
    AUSTRIA("AT", "Austria", Demonym.AUSTRIAN, EUROPE),
    AZERBAIJAN("AZ", "Azerbaijan", Demonym.AZERBAIJANI, ASIA),
    BAHAMAS("BS", "Bahamas", Demonym.BAHAMIAN, NORTH_AMERICA),
    BAHRAIN("BH", "Bahrain", Demonym.BAHRAINI, ASIA),
    BANGLADESH("BD", "Bangladesh", Demonym.BANGLADESHI, ASIA),
    BARBADOS("BB", "Barbados", Demonym.BARBADIAN, NORTH_AMERICA),
    BELARUS("BY", "Belarus", Demonym.BELARUSIAN, EUROPE),
    BELGIUM("BE", "Belgium", Demonym.BELGIAN, EUROPE),
    BELIZE("BZ", "Belize", Demonym.BELIZEAN, NORTH_AMERICA),
    BENIN("BJ", "Benin", Demonym.BENINESE, AFRICA),
    BERMUDA("BM", "Bermuda", Demonym.BERMUDIAN, NORTH_AMERICA),
    BHUTAN("BT", "Bhutan", Demonym.BHUTANESE, ASIA),
    BOLIVIA("BO", "Bolivia", Demonym.BOLIVIAN, SOUTH_AMERICA),
    BOSNIA_AND_HERZEGOVINA("BA", "Bosnia & Herzegovina", Demonym.BOSNIAN, EUROPE),
    BOTSWANA("BW", "Botswana", Demonym.BOTSWANAN, AFRICA),
    BRAZIL("BR", "Brazil", Demonym.BRAZILIAN, SOUTH_AMERICA),
    BRUNEI("BN", "Brunei", Demonym.BRUNEIAN, ASIA),
    BULGARIA("BG", "Bulgaria", Demonym.BULGARIAN, EUROPE),
    BURKINA_FASO("BF", "Burkina Faso", Demonym.BURKINABE, AFRICA),
    BURUNDI("BI", "Burundi", Demonym.BURUNDIAN, AFRICA),
    CAMBODIA("KH", "Cambodia", Demonym.CAMBODIAN, ASIA),
    CAMEROON("CM", "Cameroon", Demonym.CAMEROONIAN, AFRICA),
    CANADA("CA", "Canada", Demonym.CANADIAN, NORTH_AMERICA),
    CHILE("CL", "Chile", Demonym.CHILEAN, SOUTH_AMERICA),
    CHINA("CN", "China", Demonym.CHINESE, ASIA),
    COLOMBIA("CO", "Colombia", Demonym.COLOMBIAN, SOUTH_AMERICA),
    COSTA_RICA("CR", "Costa Rica", Demonym.COSTA_RICAN, NORTH_AMERICA),
    CROATIA("HR", "Croatia", Demonym.CROATIAN, EUROPE),
    CUBA("CU", "Cuba", Demonym.CUBAN, NORTH_AMERICA),
    CZECHIA("CZ", "Czechia", Demonym.CZECH, EUROPE),
    DENMARK("DK", "Denmark", Demonym.DANISH, EUROPE),
    ECUADOR("EC", "Ecuador", Demonym.ECUADORIAN, SOUTH_AMERICA),
    EGYPT("EG", "Egypt", Demonym.EGYPTIAN, AFRICA),
    FINLAND("FI", "Finland", Demonym.FINNISH, EUROPE),
    FRANCE("FR", "France", Demonym.FRENCH, EUROPE),
    GERMANY("DE", "Germany", Demonym.GERMAN, EUROPE),
    GREECE("GR", "Greece", Demonym.GREEK, EUROPE),
    INDIA("IN", "India", Demonym.INDIAN, ASIA),
    INDONESIA("ID", "Indonesia", Demonym.INDONESIAN, ASIA),
    IRELAND("IE", "Ireland", Demonym.IRISH, EUROPE),
    ISRAEL("IL", "Israel", Demonym.ISRAELI, ASIA),
    ITALY("IT", "Italy", Demonym.ITALIAN, EUROPE),
    JAMAICA("JM", "Jamaica", Demonym.JAMAICAN, NORTH_AMERICA),
    JAPAN("JP", "Japan", Demonym.JAPANESE, ASIA),
    MALAYSIA("MY", "Malaysia", Demonym.MALAYSIAN, ASIA),
    MEXICO("MX", "Mexico", Demonym.MEXICAN, NORTH_AMERICA),
    NETHERLANDS("NL", "Netherlands", Demonym.DUTCH, EUROPE),
    NEW_ZEALAND("NZ", "New Zealand", Demonym.NEW_ZEALANDER, OCEANIA),
    NIGERIA("NG", "Nigeria", Demonym.NIGERIAN, AFRICA),
    NORWAY("NO", "Norway", Demonym.NORWEGIAN, EUROPE),
    PAKISTAN("PK", "Pakistan", Demonym.PAKISTANI, ASIA),
    PERU("PE", "Peru", Demonym.PERUVIAN, SOUTH_AMERICA),
    PHILIPPINES("PH", "Philippines", Demonym.FILIPINO, ASIA),
    POLAND("PL", "Poland", Demonym.POLISH, EUROPE),
    PORTUGAL("PT", "Portugal", Demonym.PORTUGUESE, EUROPE),
    RUSSIA("RU", "Russia", Demonym.RUSSIAN, EUROPE),
    SAUDI_ARABIA("SA", "Saudi Arabia", Demonym.SAUDI, ASIA),
    SINGAPORE("SG", "Singapore", Demonym.SINGAPOREAN, ASIA),
    SOUTH_AFRICA("ZA", "South Africa", Demonym.SOUTH_AFRICAN, AFRICA),
    SOUTH_KOREA("KR", "South Korea", Demonym.SOUTH_KOREAN, ASIA),
    SPAIN("ES", "Spain", Demonym.SPANISH, EUROPE),
    SWEDEN("SE", "Sweden", Demonym.SWEDISH, EUROPE),
    SWITZERLAND("CH", "Switzerland", Demonym.SWISS, EUROPE),
    TAIWAN("TW", "Taiwan", Demonym.TAIWANESE, ASIA),
    THAILAND("TH", "Thailand", Demonym.THAI, ASIA),
    TURKEY("TR", "Türkiye", Demonym.TURKISH, ASIA),
    UKRAINE("UA", "Ukraine", Demonym.UKRAINIAN, EUROPE),
    UNITED_ARAB_EMIRATES("AE", "United Arab Emirates", Demonym.EMIRATI, ASIA),
    UNITED_KINGDOM("GB", "United Kingdom", Demonym.BRITISH, EUROPE),
    UNITED_STATES("US", "United States", Demonym.AMERICAN, NORTH_AMERICA),
    VIETNAM("VN", "Vietnam", Demonym.VIETNAMESE, ASIA),

    UNKNOWN("XX", "Unknown", Demonym.UNKNOWN, Continent.UNKNOWN);

    /**
     * @return whether this is {@link #UNKNOWN}
     */
    public boolean isNull() {
        return this == UNKNOWN;
    }

    // Note: Full country list truncated for brevity - in production, include all countries

    private final String code;
    private final String displayName;
    private final Demonym demonym;
    private final Continent continent;

    Country(String code, String displayName, Demonym demonym, Continent continent) {
        this.code = code;
        this.displayName = displayName;
        this.demonym = demonym;
        this.continent = continent;
    }

    // ========================= Accessors =========================

    public String code() {
        return code;
    }

    public String displayName() {
        return displayName;
    }

    /**
     * Returns the primary {@link Demonym} for this country.
     *
     * @return the demonym value object
     */
    public Demonym demonym() {
        return demonym;
    }

    public Continent continent() {
        return continent;
    }

    /**
     * Computes the flag emoji from the ISO country code.
     * Regional Indicator Symbols: A=U+1F1E6 through Z=U+1F1FF
     */
    public String flagEmoji() {
        if (code.length() != 2) {
            return "";
        }
        int firstChar = code.charAt(0) - 'A' + 0x1F1E6;
        int secondChar = code.charAt(1) - 'A' + 0x1F1E6;
        return new String(Character.toChars(firstChar)) + new String(Character.toChars(secondChar));
    }

    // ========================= Lookup Methods =========================

    /**
     * Finds a country by matching against code, enum name, display name,
     * or demonym display name. Case-insensitive. Returns null if not found.
     */
    public static @Nullable Country fromAny(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return Arrays.stream(values())
                .filter(country ->
                        country.code.equalsIgnoreCase(trimmed)
                                || country.name().equalsIgnoreCase(trimmed)
                                || country.displayName.equalsIgnoreCase(trimmed)
                                || country.demonym.matches(trimmed)
                )
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds a country by ISO code or enum name. Case-insensitive.
     */
    public static @Nullable Country fromCode(String code) {
        return Arrays.stream(values())
                .filter(country -> country.code.equalsIgnoreCase(code) || country.name().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds a country by display name. Case-insensitive.
     */
    public static Optional<Country> fromName(String name) {
        return Arrays.stream(values())
                .filter(country -> country.displayName.equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Returns all countries on the given continent.
     */
    public static List<Country> byContinent(Continent continent) {
        return Arrays.stream(values())
                .filter(country -> country.continent == continent)
                .toList();
    }

    /**
     * Returns all countries that share the given demonym.
     */
    public static List<Country> byDemonym(Demonym demonym) {
        return Arrays.stream(values())
                .filter(country -> country.demonym == demonym)
                .toList();
    }

    // ========================= Comparison =========================

    /**
     * Compares countries by display name alphabetically.
     *
     * @param other the Country to compare to
     * @return negative if this comes before, zero if equal, positive if after
     */
    public int compareByName(Country other) {
        return this.displayName.compareToIgnoreCase(other.displayName);
    }

    /**
     * Compares countries by ISO code alphabetically.
     *
     * @param other the Country to compare to
     * @return negative if this comes before, zero if equal, positive if after
     */
    public int compareByCode(Country other) {
        return this.code.compareTo(other.code);
    }

    /**
     * Returns the null object representation of Country.
     *
     * @return the UNKNOWN country constant
     */
    public static Country nullValue() {
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return displayName;
    }
}