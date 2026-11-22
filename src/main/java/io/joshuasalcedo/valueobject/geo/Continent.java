package io.joshuasalcedo.valueobject.geo;

import io.joshuasalcedo.valueobject.ValueObject;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Continent value object representing one of the seven continents.
 *
 * <p>Each continent has an ISO-like two-letter code, a display name,
 * and approximate geographic bounds (latitude/longitude).</p>
 *
 * @author JoshuaSalcedo
 * @since 2/25/2026
 * @see Country
 */
public enum Continent implements ValueObject {

    AFRICA("AF", "Africa", -34.83, 37.35, -17.62, 51.42),
    ANTARCTICA("AN", "Antarctica", -90.0, -60.0, -180.0, 180.0),
    ASIA("AS", "Asia", -11.0, 77.72, 26.04, 180.0),
    EUROPE("EU", "Europe", 34.80, 71.19, -25.27, 69.03),
    NORTH_AMERICA("NA", "North America", 7.20, 83.11, -168.13, -52.23),
    OCEANIA("OC", "Oceania", -47.29, -0.53, 110.95, 180.0),
    SOUTH_AMERICA("SA", "South America", -55.98, 12.46, -81.33, -34.79),

    UNKNOWN("XX", "Unknown", 0.0, 0.0, 0.0, 0.0);

    /**
     * @return whether this is {@link #UNKNOWN}
     */
    public boolean isNull() {
        return this == UNKNOWN;
    }

    private final String code;
    private final String displayName;
    private final double southLatitude;
    private final double northLatitude;
    private final double westLongitude;
    private final double eastLongitude;

    Continent(String code, String displayName,
              double southLatitude, double northLatitude,
              double westLongitude, double eastLongitude) {
        this.code = code;
        this.displayName = displayName;
        this.southLatitude = southLatitude;
        this.northLatitude = northLatitude;
        this.westLongitude = westLongitude;
        this.eastLongitude = eastLongitude;
    }

    // ========================= Accessors =========================

    /**
     * Returns the two-letter continent code.
     * <pre>
     *   AFRICA → "AF"    ASIA → "AS"       EUROPE → "EU"
     *   NORTH_AMERICA → "NA"   SOUTH_AMERICA → "SA"
     *   OCEANIA → "OC"   ANTARCTICA → "AN"
     * </pre>
     *
     * @return the continent code
     */
    public String code() {
        return code;
    }

    /**
     * Returns the human-readable continent name.
     *
     * @return the display name (e.g. "North America")
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Returns the approximate southernmost latitude of this continent.
     *
     * @return south latitude in degrees
     */
    public double southLatitude() {
        return southLatitude;
    }

    /**
     * Returns the approximate northernmost latitude of this continent.
     *
     * @return north latitude in degrees
     */
    public double northLatitude() {
        return northLatitude;
    }

    /**
     * Returns the approximate westernmost longitude of this continent.
     *
     * @return west longitude in degrees
     */
    public double westLongitude() {
        return westLongitude;
    }

    /**
     * Returns the approximate easternmost longitude of this continent.
     *
     * @return east longitude in degrees
     */
    public double eastLongitude() {
        return eastLongitude;
    }

    // ========================= Lookup Methods =========================

    /**
     * Finds a continent by code, display name, or enum name. Case-insensitive.
     * Returns null if not found.
     *
     * @param value the value to search for
     * @return the matching Continent, or null
     */
    public static @Nullable Continent fromAny(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return Arrays.stream(values())
                .filter(c ->
                        c.code.equalsIgnoreCase(trimmed)
                                || c.name().equalsIgnoreCase(trimmed)
                                || c.displayName.equalsIgnoreCase(trimmed)
                )
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds a continent by its two-letter code. Case-insensitive.
     *
     * @param code the continent code (e.g. "AS", "EU")
     * @return an Optional containing the matching Continent
     */
    public static Optional<Continent> fromCode(String code) {
        return Arrays.stream(values())
                .filter(c -> c.code.equalsIgnoreCase(code))
                .findFirst();
    }

    /**
     * Finds a continent by its display name. Case-insensitive.
     *
     * @param name the display name (e.g. "North America")
     * @return an Optional containing the matching Continent
     */
    public static Optional<Continent> fromName(String name) {
        return Arrays.stream(values())
                .filter(c -> c.displayName.equalsIgnoreCase(name))
                .findFirst();
    }

    // ========================= Country Helpers =========================

    /**
     * Returns all countries on this continent.
     *
     * @return unmodifiable list of countries
     */
    public List<Country> countries() {
        return Arrays.stream(Country.values())
                .filter(country -> country.continent() == this)
                .toList();
    }

    /**
     * Returns the number of countries on this continent.
     *
     * @return country count
     */
    public int countryCount() {
        return countries().size();
    }

    /**
     * Returns true if the given country belongs to this continent.
     *
     * @param country the country to check
     * @return true if the country is on this continent
     */
    public boolean contains(Country country) {
        return country.continent() == this;
    }

    // ========================= Geographic Helpers =========================

    /**
     * Returns true if the given latitude/longitude falls within this continent's
     * approximate bounding box.
     *
     * <p>Note: This is a rough approximation using a rectangular bounding box.
     * It does not account for the actual coastline shapes.</p>
     *
     * @param latitude  the latitude in degrees
     * @param longitude the longitude in degrees
     * @return true if the coordinate is within the bounding box
     */
    public boolean containsApproximate(double latitude, double longitude) {
        return latitude >= southLatitude && latitude <= northLatitude
                && longitude >= westLongitude && longitude <= eastLongitude;
    }

    /**
     * Returns the approximate center latitude of this continent.
     *
     * @return center latitude in degrees
     */
    public double centerLatitude() {
        return (southLatitude + northLatitude) / 2.0;
    }

    /**
     * Returns the approximate center longitude of this continent.
     *
     * @return center longitude in degrees
     */
    public double centerLongitude() {
        return (westLongitude + eastLongitude) / 2.0;
    }

    // ========================= Comparison =========================

    /**
     * Compares continents alphabetically by display name. Case-insensitive.
     *
     * @param other the Continent to compare to
     * @return negative if this comes before, zero if equal, positive if after
     */
    public int compareByName(Continent other) {
        return this.displayName.compareToIgnoreCase(other.displayName);
    }

    /**
     * Returns the null object representation of Continent.
     *
     * @return the UNKNOWN continent constant
     */
    public static Continent nullValue() {
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return displayName;
    }
}