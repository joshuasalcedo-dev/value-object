package io.joshuasalcedo.valueobject.geo;

import org.jspecify.annotations.Nullable;
import java.util.Optional;
import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.measurement.Distance;
import io.joshuasalcedo.valueobject.measurement.DistanceUnit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

/**
 * A point on the Earth: latitude and longitude in decimal degrees (WGS 84). Two coordinates are equal when both
 * agree to six decimal places (about 0.1 m).
 *
 * <pre>{@code
 * Coordinates manila = Coordinates.parse("14.5995, 120.9842");
 * manila.distanceTo(Coordinates.of(10.3157, 123.8854));   // about 570 km
 * manila.toDmsString();                                   // 14°35'58.20"N, 120°59'3.12"E
 * }</pre>
 *
 * @param latitude  the latitude, -90 (south) to 90 (north)
 * @param longitude the longitude, -180 (west) to 180 (east)
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Coordinates(
        double latitude,
        double longitude
) implements ValueObject {

    // Earth's radius in kilometers (mean radius)
    private static final double EARTH_RADIUS_KM = 6371.0088;

    // Valid ranges
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;

    // Precision for comparisons
    private static final int DECIMAL_PRECISION = 6; // ~0.1 meter precision

    /**
     * Creates coordinates from decimal degrees.
     *
     * @throws ValueObjectException if either is NaN or infinite, the latitude is outside -90 to 90, or the
     *                              longitude is outside -180 to 180
     */
    public Coordinates {
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)) {
            throw new ValueObjectException("Coordinates must be finite numbers: " + latitude + ", " + longitude);
        }
        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            throw new ValueObjectException(
                String.format(Locale.ROOT, "Latitude must be between %.1f and %.1f: %f",
                    MIN_LATITUDE, MAX_LATITUDE, latitude)
            );
        }
        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            throw new ValueObjectException(
                String.format(Locale.ROOT, "Longitude must be between %.1f and %.1f: %f",
                    MIN_LONGITUDE, MAX_LONGITUDE, longitude)
            );
        }
    }

    // ==================== Factory Methods ====================

    /**
     * Creates coordinates from latitude and longitude.
     *
     * @param latitude the latitude (-90 to 90)
     * @param longitude the longitude (-180 to 180)
     * @return the coordinates
     * @throws ValueObjectException if either is out of range
     */
    public static Coordinates of(double latitude, double longitude) {
        return new Coordinates(latitude, longitude);
    }

    /**
     * Creates coordinates from decimal degree strings, trimmed and read with {@link Double#parseDouble(String)}.
     *
     * @param latitude the latitude, e.g. {@code "14.5995"}
     * @param longitude the longitude, e.g. {@code "120.9842"}
     * @return the coordinates
     * @throws ValueObjectException if either is {@code null}, not a number, or out of range
     */
    public static Coordinates of(String latitude, String longitude) {
        if (latitude == null || longitude == null) {
            throw new ValueObjectException("Latitude and longitude are required");
        }
        try {
            return new Coordinates(
                Double.parseDouble(latitude.trim()),
                Double.parseDouble(longitude.trim())
            );
        } catch (NumberFormatException e) {
            throw new ValueObjectException("Invalid coordinate format", e);
        }
    }

    /**
     * Parses coordinates from {@code "latitude, longitude"} in decimal degrees, e.g. {@code "40.7128, -74.0060"}.
     *
     * @param coordinates the text
     * @return the coordinates
     * @throws ValueObjectException if it is {@code null}, not two numbers separated by one comma, or they are
     *                              out of range
     */
    public static Coordinates parse(String coordinates) {
        if (coordinates == null) {
            throw new ValueObjectException("Coordinates string cannot be null");
        }
        String[] parts = coordinates.trim().split(",", -1);
        if (parts.length != 2) {
            throw new ValueObjectException(
                "Coordinates must be in format 'latitude, longitude': " + coordinates
            );
        }
        return of(parts[0].trim(), parts[1].trim());
    }

    /**
     * Creates coordinates from degrees, minutes and seconds. A direction of {@code S} or {@code W} (either case)
     * makes that value negative; any other character leaves it positive.
     *
     * @param latDeg latitude degrees
     * @param latMin latitude minutes
     * @param latSec latitude seconds
     * @param latDir latitude direction (N or S)
     * @param lonDeg longitude degrees
     * @param lonMin longitude minutes
     * @param lonSec longitude seconds
     * @param lonDir longitude direction (E or W)
     * @return the coordinates
     * @throws ValueObjectException if the result is out of range
     */
    public static Coordinates fromDMS(
            int latDeg, int latMin, double latSec, char latDir,
            int lonDeg, int lonMin, double lonSec, char lonDir) {

        double lat = latDeg + latMin / 60.0 + latSec / 3600.0;
        if (latDir == 'S' || latDir == 's') lat = -lat;

        double lon = lonDeg + lonMin / 60.0 + lonSec / 3600.0;
        if (lonDir == 'W' || lonDir == 'w') lon = -lon;

        return new Coordinates(lat, lon);
    }

    // ==================== Notable Locations ====================

    /** {@return the point where the equator meets the prime meridian, 0, 0} */
    public static Coordinates equatorPrimeMeridian() {
        return new Coordinates(0.0, 0.0);
    }

    /** {@return the North Pole, 90, 0} */
    public static Coordinates northPole() {
        return new Coordinates(90.0, 0.0);
    }

    /** {@return the South Pole, -90, 0} */
    public static Coordinates southPole() {
        return new Coordinates(-90.0, 0.0);
    }

    // ==================== Distance Calculation ====================

    /**
     * Calculates the great-circle distance to another point with the haversine formula, on a sphere of the
     * Earth's mean radius (6371.0088 km).
     *
     * @param other the other coordinates
     * @return the distance, in kilometres
     */
    public Distance distanceTo(Coordinates other) {
        double lat1 = Math.toRadians(this.latitude);
        double lat2 = Math.toRadians(other.latitude);
        double deltaLat = Math.toRadians(other.latitude - this.latitude);
        double deltaLon = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distanceKm = EARTH_RADIUS_KM * c;
        return Distance.of(distanceKm, DistanceUnit.KILOMETER);
    }

    /**
     * Calculates the initial bearing (forward azimuth) of the great-circle path to another point.
     *
     * @param other the destination coordinates
     * @return the bearing in degrees clockwise from north, 0 (inclusive) to 360 (exclusive)
     */
    public double bearingTo(Coordinates other) {
        double lat1 = Math.toRadians(this.latitude);
        double lat2 = Math.toRadians(other.latitude);
        double deltaLon = Math.toRadians(other.longitude - this.longitude);

        double y = Math.sin(deltaLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) -
                   Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);

        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }

    /**
     * Calculates the point reached by travelling a distance along a great circle from here, starting at a bearing.
     * The longitude is wrapped into -180 to 180, so a path may cross the 180th meridian.
     *
     * @param distance the distance to travel
     * @param bearing the starting bearing in degrees clockwise from north
     * @return the destination coordinates
     */
    public Coordinates destinationPoint(Distance distance, double bearing) {
        double distanceKm = distance.convertTo(DistanceUnit.KILOMETER).value().doubleValue();
        double angularDistance = distanceKm / EARTH_RADIUS_KM;
        double bearingRad = Math.toRadians(bearing);

        double lat1 = Math.toRadians(this.latitude);
        double lon1 = Math.toRadians(this.longitude);

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(angularDistance) +
                                Math.cos(lat1) * Math.sin(angularDistance) * Math.cos(bearingRad));

        double lon2 = lon1 + Math.atan2(
            Math.sin(bearingRad) * Math.sin(angularDistance) * Math.cos(lat1),
            Math.cos(angularDistance) - Math.sin(lat1) * Math.sin(lat2)
        );

        double latDeg = Math.max(MIN_LATITUDE, Math.min(MAX_LATITUDE, Math.toDegrees(lat2)));
        double lonDeg = Math.toDegrees(lon2);
        lonDeg = ((lonDeg + 540.0) % 360.0 + 360.0) % 360.0 - 180.0;
        return new Coordinates(latDeg, lonDeg);
    }

    // ==================== Query Methods ====================

    /**
     * Checks whether this location is in the northern hemisphere; the equator counts as northern.
     *
     * @return whether the latitude is 0 or more
     */
    public boolean isNorthernHemisphere() {
        return latitude >= 0;
    }

    /**
     * Checks whether this location is in the southern hemisphere.
     *
     * @return whether the latitude is below 0
     */
    public boolean isSouthernHemisphere() {
        return latitude < 0;
    }

    /**
     * Checks whether this location is in the eastern hemisphere; the prime meridian counts as eastern.
     *
     * @return whether the longitude is 0 or more
     */
    public boolean isEasternHemisphere() {
        return longitude >= 0;
    }

    /**
     * Checks whether this location is in the western hemisphere.
     *
     * @return whether the longitude is below 0
     */
    public boolean isWesternHemisphere() {
        return longitude < 0;
    }

    /**
     * Checks whether another point is within a distance of this one, by {@link #distanceTo(Coordinates)}.
     *
     * @param other the other coordinate
     * @param maxDistance the maximum distance, inclusive
     * @return whether it is within range
     */
    public boolean isWithin(Coordinates other, Distance maxDistance) {
        return distanceTo(other).compareTo(maxDistance) <= 0;
    }

    // ==================== Formatting ====================

    /**
     * Returns the coordinates as {@code "latitude, longitude"} in decimal degrees, six decimal places each,
     * always with a {@code .} decimal point whatever the default locale, so {@link #parse(String)} reads it back.
     *
     * @return the decimal format string, e.g. {@code "40.712800, -74.006000"}
     */
    public String toDecimalString() {
        return String.format(Locale.ROOT, "%.6f, %.6f", latitude, longitude);
    }

    /**
     * Returns the coordinates in degrees, minutes and seconds, with seconds to two decimals and a hemisphere
     * letter.
     *
     * @return the DMS format string, e.g. {@code 40°42'46.08"N, 74°0'21.60"W}
     */
    public String toDmsString() {
        return toDms(latitude, latitude >= 0 ? 'N' : 'S') + ", " +
               toDms(longitude, longitude >= 0 ? 'E' : 'W');
    }

    private String toDms(double value, char direction) {
        double absValue = Math.abs(value);
        int degrees = (int) absValue;
        double minutesDecimal = (absValue - degrees) * 60;
        int minutes = (int) minutesDecimal;
        double seconds = (minutesDecimal - minutes) * 60;

        return String.format(Locale.ROOT, "%d°%d'%.2f\"%c", degrees, minutes, seconds, direction);
    }

    /**
     * Returns a Google Maps URL that shows this location.
     *
     * @return the Google Maps URL, {@code https://www.google.com/maps?q=lat,lon}
     */
    public String toGoogleMapsUrl() {
        return String.format(Locale.ROOT, "https://www.google.com/maps?q=%.6f,%.6f", latitude, longitude);
    }

    /**
     * Returns an OpenStreetMap URL with a marker at this location.
     *
     * @return the OSM URL, {@code https://www.openstreetmap.org/?mlat=lat&mlon=lon}
     */
    public String toOpenStreetMapUrl() {
        return String.format(Locale.ROOT, "https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f", latitude, longitude);
    }

    @Override
    public String toString() {
        return toDecimalString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinates that)) return false;
        // Compare with precision
        return round(this.latitude) == round(that.latitude) &&
               round(this.longitude) == round(that.longitude);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
            .setScale(DECIMAL_PRECISION, RoundingMode.HALF_EVEN)
            .doubleValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(round(latitude), round(longitude));
    }

    /**
     * Parses coordinates as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as {@link Coordinates}, or empty when it is not one; never throws
     */
    public static Optional<Coordinates> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text parses as coordinates with {@link #parse(String)}.
     *
     * @param text any text
     * @return whether it is {@link Coordinates}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
