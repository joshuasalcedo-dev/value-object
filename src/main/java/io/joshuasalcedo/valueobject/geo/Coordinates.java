package io.joshuasalcedo.valueobject.geo;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.measurement.Distance;
import io.joshuasalcedo.valueobject.measurement.DistanceUnit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing geographic coordinates (latitude and longitude).
 *
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

    public Coordinates {
        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            throw new ValueObjectException(
                String.format("Latitude must be between %.1f and %.1f: %f",
                    MIN_LATITUDE, MAX_LATITUDE, latitude)
            );
        }
        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            throw new ValueObjectException(
                String.format("Longitude must be between %.1f and %.1f: %f",
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
     * @return new Coordinates
     */
    public static Coordinates of(double latitude, double longitude) {
        return new Coordinates(latitude, longitude);
    }

    /**
     * Creates coordinates from decimal degrees strings.
     *
     * @param latitude the latitude string
     * @param longitude the longitude string
     * @return new Coordinates
     */
    public static Coordinates of(String latitude, String longitude) {
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
     * Parses coordinates from a string like "40.7128, -74.0060".
     *
     * @param coordinates the coordinates string
     * @return new Coordinates
     */
    public static Coordinates parse(String coordinates) {
        Objects.requireNonNull(coordinates, "Coordinates string cannot be null");
        String[] parts = coordinates.split(",");
        if (parts.length != 2) {
            throw new ValueObjectException(
                "Coordinates must be in format 'latitude, longitude': " + coordinates
            );
        }
        return of(parts[0].trim(), parts[1].trim());
    }

    /**
     * Creates coordinates from degrees, minutes, seconds format.
     *
     * @param latDeg latitude degrees
     * @param latMin latitude minutes
     * @param latSec latitude seconds
     * @param latDir latitude direction (N or S)
     * @param lonDeg longitude degrees
     * @param lonMin longitude minutes
     * @param lonSec longitude seconds
     * @param lonDir longitude direction (E or W)
     * @return new Coordinates
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

    public static Coordinates equatorPrimeMeridian() {
        return new Coordinates(0.0, 0.0);
    }

    public static Coordinates northPole() {
        return new Coordinates(90.0, 0.0);
    }

    public static Coordinates southPole() {
        return new Coordinates(-90.0, 0.0);
    }

    // ==================== Distance Calculation ====================

    /**
     * Calculates the distance to another coordinate using the Haversine formula.
     *
     * @param other the other coordinates
     * @return the distance
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
     * Calculates the initial bearing to another coordinate.
     *
     * @param other the destination coordinates
     * @return the bearing in degrees (0-360)
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
     * Calculates a destination coordinate given distance and bearing.
     *
     * @param distance the distance to travel
     * @param bearing the bearing in degrees
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

        return new Coordinates(Math.toDegrees(lat2), Math.toDegrees(lon2));
    }

    // ==================== Query Methods ====================

    /**
     * Checks if this location is in the northern hemisphere.
     *
     * @return true if northern
     */
    public boolean isNorthernHemisphere() {
        return latitude >= 0;
    }

    /**
     * Checks if this location is in the southern hemisphere.
     *
     * @return true if southern
     */
    public boolean isSouthernHemisphere() {
        return latitude < 0;
    }

    /**
     * Checks if this location is in the eastern hemisphere.
     *
     * @return true if eastern
     */
    public boolean isEasternHemisphere() {
        return longitude >= 0;
    }

    /**
     * Checks if this location is in the western hemisphere.
     *
     * @return true if western
     */
    public boolean isWesternHemisphere() {
        return longitude < 0;
    }

    /**
     * Checks if this is within a certain distance of another coordinate.
     *
     * @param other the other coordinate
     * @param maxDistance the maximum distance
     * @return true if within range
     */
    public boolean isWithin(Coordinates other, Distance maxDistance) {
        return distanceTo(other).compareTo(maxDistance) <= 0;
    }

    // ==================== Formatting ====================

    /**
     * Returns the coordinates in decimal degrees format.
     *
     * @return the decimal format string
     */
    public String toDecimalString() {
        return String.format("%.6f, %.6f", latitude, longitude);
    }

    /**
     * Returns the coordinates in DMS (degrees, minutes, seconds) format.
     *
     * @return the DMS format string
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

        return String.format("%d°%d'%.2f\"%c", degrees, minutes, seconds, direction);
    }

    /**
     * Returns a Google Maps URL for this location.
     *
     * @return the Google Maps URL
     */
    public String toGoogleMapsUrl() {
        return String.format("https://www.google.com/maps?q=%.6f,%.6f", latitude, longitude);
    }

    /**
     * Returns an Open Street Map URL for this location.
     *
     * @return the OSM URL
     */
    public String toOpenStreetMapUrl() {
        return String.format("https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f", latitude, longitude);
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
}
