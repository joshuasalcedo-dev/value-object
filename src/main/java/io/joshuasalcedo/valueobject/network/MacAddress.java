package io.joshuasalcedo.valueobject.network;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a MAC (Media Access Control) address.
 * Stores in canonical format (uppercase, colon-separated).
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record MacAddress(byte[] bytes) implements ValueObject, Comparable<MacAddress> {

    // MAC address is 6 bytes (48 bits)
    private static final int MAC_LENGTH = 6;

    // Various MAC format patterns
    private static final Pattern COLON_FORMAT = Pattern.compile("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$");
    private static final Pattern HYPHEN_FORMAT = Pattern.compile("^([0-9A-Fa-f]{2}-){5}[0-9A-Fa-f]{2}$");
    private static final Pattern DOT_FORMAT = Pattern.compile("^([0-9A-Fa-f]{4}\\.){2}[0-9A-Fa-f]{4}$");
    private static final Pattern PLAIN_FORMAT = Pattern.compile("^[0-9A-Fa-f]{12}$");

    public MacAddress {
        Objects.requireNonNull(bytes, "MAC address bytes cannot be null");
        if (bytes.length != MAC_LENGTH) {
            throw new ValueObjectException(
                "MAC address must be exactly " + MAC_LENGTH + " bytes, got " + bytes.length
            );
        }
        // Defensive copy
        bytes = bytes.clone();
    }

    /**
     * Creates a MacAddress from a byte array.
     *
     * @param bytes the 6 bytes of the MAC address
     * @return a new MacAddress
     */
    public static MacAddress of(byte[] bytes) {
        return new MacAddress(bytes);
    }

    /**
     * Creates a MacAddress from a string in various formats.
     * Supported formats:
     * - Colon-separated: 00:1A:2B:3C:4D:5E
     * - Hyphen-separated: 00-1A-2B-3C-4D-5E
     * - Dot-separated (Cisco): 001A.2B3C.4D5E
     * - Plain: 001A2B3C4D5E
     *
     * @param mac the MAC address string
     * @return a new MacAddress
     */
    public static MacAddress of(String mac) {
        Objects.requireNonNull(mac, "MAC address string cannot be null");
        mac = mac.trim();

        String normalized;
        if (COLON_FORMAT.matcher(mac).matches()) {
            normalized = mac.replace(":", "");
        } else if (HYPHEN_FORMAT.matcher(mac).matches()) {
            normalized = mac.replace("-", "");
        } else if (DOT_FORMAT.matcher(mac).matches()) {
            normalized = mac.replace(".", "");
        } else if (PLAIN_FORMAT.matcher(mac).matches()) {
            normalized = mac;
        } else {
            throw new ValueObjectException("Invalid MAC address format: " + mac);
        }

        byte[] bytes = new byte[MAC_LENGTH];
        for (int i = 0; i < MAC_LENGTH; i++) {
            String byteStr = normalized.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(byteStr, 16);
        }

        return new MacAddress(bytes);
    }

    /**
     * Parses a MAC address string, returning null if invalid.
     *
     * @param mac the MAC address string
     * @return the MacAddress or null
     */
    public static @Nullable MacAddress parse(String mac) {
        try {
            return of(mac);
        } catch (ValueObjectException e) {
            return null;
        }
    }

    /**
     * Checks if a string is a valid MAC address format.
     *
     * @param mac the string to check
     * @return true if valid
     */
    public static boolean isValid(String mac) {
        if (mac == null || mac.isBlank()) {
            return false;
        }
        mac = mac.trim();
        return COLON_FORMAT.matcher(mac).matches() ||
               HYPHEN_FORMAT.matcher(mac).matches() ||
               DOT_FORMAT.matcher(mac).matches() ||
               PLAIN_FORMAT.matcher(mac).matches();
    }

    /**
     * Gets the byte array (defensive copy).
     *
     * @return copy of the bytes
     */
    @Override
    public byte[] bytes() {
        return bytes.clone();
    }

    /**
     * Returns the MAC address in colon-separated format (canonical).
     *
     * @return the colon-separated MAC address
     */
    public String toColonFormat() {
        return String.format("%02X:%02X:%02X:%02X:%02X:%02X",
            bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF,
            bytes[3] & 0xFF, bytes[4] & 0xFF, bytes[5] & 0xFF);
    }

    /**
     * Returns the MAC address in hyphen-separated format (Windows style).
     *
     * @return the hyphen-separated MAC address
     */
    public String toHyphenFormat() {
        return String.format("%02X-%02X-%02X-%02X-%02X-%02X",
            bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF,
            bytes[3] & 0xFF, bytes[4] & 0xFF, bytes[5] & 0xFF);
    }

    /**
     * Returns the MAC address in dot-separated format (Cisco style).
     *
     * @return the dot-separated MAC address
     */
    public String toDotFormat() {
        return String.format("%02X%02X.%02X%02X.%02X%02X",
            bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF,
            bytes[3] & 0xFF, bytes[4] & 0xFF, bytes[5] & 0xFF);
    }

    /**
     * Returns the MAC address as a plain hex string.
     *
     * @return the plain hex string
     */
    public String toPlainFormat() {
        return String.format("%02X%02X%02X%02X%02X%02X",
            bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF,
            bytes[3] & 0xFF, bytes[4] & 0xFF, bytes[5] & 0xFF);
    }

    /**
     * Returns the Organizationally Unique Identifier (first 3 bytes).
     * This identifies the manufacturer.
     *
     * @return the OUI as a hex string
     */
    public String oui() {
        return String.format("%02X:%02X:%02X",
            bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF);
    }

    /**
     * Returns the Network Interface Controller specific part (last 3 bytes).
     *
     * @return the NIC part as a hex string
     */
    public String nic() {
        return String.format("%02X:%02X:%02X",
            bytes[3] & 0xFF, bytes[4] & 0xFF, bytes[5] & 0xFF);
    }

    /**
     * Checks if this is a unicast address.
     *
     * @return true if unicast
     */
    public boolean isUnicast() {
        return (bytes[0] & 0x01) == 0;
    }

    /**
     * Checks if this is a multicast address.
     *
     * @return true if multicast
     */
    public boolean isMulticast() {
        return (bytes[0] & 0x01) == 1;
    }

    /**
     * Checks if this is a locally administered address.
     *
     * @return true if locally administered
     */
    public boolean isLocallyAdministered() {
        return (bytes[0] & 0x02) == 2;
    }

    /**
     * Checks if this is a universally administered address.
     *
     * @return true if universally administered
     */
    public boolean isUniversallyAdministered() {
        return (bytes[0] & 0x02) == 0;
    }

    /**
     * Checks if this is the broadcast address (FF:FF:FF:FF:FF:FF).
     *
     * @return true if broadcast
     */
    public boolean isBroadcast() {
        for (byte b : bytes) {
            if ((b & 0xFF) != 0xFF) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(MacAddress other) {
        for (int i = 0; i < MAC_LENGTH; i++) {
            int cmp = Integer.compare(this.bytes[i] & 0xFF, other.bytes[i] & 0xFF);
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MacAddress that)) return false;
        return Arrays.equals(bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return toColonFormat();
    }
}
