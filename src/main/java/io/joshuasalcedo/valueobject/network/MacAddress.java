package io.joshuasalcedo.valueobject.network;

import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A 48-bit MAC (Media Access Control) address, held as its six bytes. It reads the colon, hyphen, Cisco dot and
 * plain hex forms, and prints as upper-case colon-separated hex ({@code 00:1A:2B:3C:4D:5E}).
 *
 * @param bytes the six bytes, most significant first; copied in and out
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

    /**
     * Creates a MAC address from a copy of its bytes.
     *
     * @throws ValueObjectException if {@code bytes} is {@code null} or there are not exactly six bytes
     */
    public MacAddress {
        if (bytes == null) {
            throw new ValueObjectException("MAC address bytes cannot be null");
        }
        if (bytes.length != MAC_LENGTH) {
            throw new ValueObjectException(
                "MAC address must be exactly " + MAC_LENGTH + " bytes, got " + bytes.length
            );
        }
        // Defensive copy
        bytes = bytes.clone();
    }

    /**
     * Creates a MAC address from a copy of its bytes; the same as the constructor.
     *
     * @param bytes the 6 bytes of the MAC address
     * @return the MAC address
     * @throws ValueObjectException if {@code bytes} is {@code null} or there are not exactly six bytes
     */
    public static MacAddress of(byte[] bytes) {
        return new MacAddress(bytes);
    }

    /**
     * Creates a MAC address from text in one of these forms, in either case, surrounding whitespace ignored:
     * <ul>
     *     <li>colon-separated: {@code 00:1A:2B:3C:4D:5E}</li>
     *     <li>hyphen-separated: {@code 00-1A-2B-3C-4D-5E}</li>
     *     <li>dot-separated (Cisco): {@code 001A.2B3C.4D5E}</li>
     *     <li>plain: {@code 001A2B3C4D5E}</li>
     * </ul>
     *
     * @param mac the MAC address string
     * @return the MAC address
     * @throws ValueObjectException if it is {@code null} or in none of those forms
     */
    public static MacAddress of(String mac) {
        if (mac == null) {
            throw new ValueObjectException("MAC address string cannot be null");
        }
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
     * Parses a MAC address in any of the forms {@link #of(String)} reads.
     *
     * @param text the text
     * @return the MAC address
     * @throws ValueObjectException if it is {@code null} or not a MAC address
     */
    public static MacAddress parse(String text) {
        if (text == null) throw new ValueObjectException("A MacAddress is required");
        return of(text);
    }

    /**
     * Checks whether a text, once trimmed, is a MAC address in any of the forms {@link #of(String)} reads.
     *
     * @param mac the string to check; may be {@code null}
     * @return whether it is valid
     */
    public static boolean isValid(@Nullable String mac) {
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
     * Returns a copy of the six bytes.
     *
     * @return copy of the bytes
     */
    @Override
    public byte[] bytes() {
        return bytes.clone();
    }

    /**
     * Returns the MAC address in canonical colon-separated form.
     *
     * @return the colon-separated MAC address, e.g. {@code 00:1A:2B:3C:4D:5E}
     */
    public String toColonFormat() {
        return String.format(Locale.ROOT, "%02X:%02X:%02X:%02X:%02X:%02X",
            bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF,
            bytes[3] & 0xFF, bytes[4] & 0xFF, bytes[5] & 0xFF);
    }

    /**
     * Returns the MAC address in hyphen-separated form (Windows style).
     *
     * @return the hyphen-separated MAC address, e.g. {@code 00-1A-2B-3C-4D-5E}
     */
    public String toHyphenFormat() {
        return String.format(Locale.ROOT, "%02X-%02X-%02X-%02X-%02X-%02X",
            bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF,
            bytes[3] & 0xFF, bytes[4] & 0xFF, bytes[5] & 0xFF);
    }

    /**
     * Returns the MAC address in dot-separated form (Cisco style).
     *
     * @return the dot-separated MAC address, e.g. {@code 001A.2B3C.4D5E}
     */
    public String toDotFormat() {
        return String.format(Locale.ROOT, "%02X%02X.%02X%02X.%02X%02X",
            bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF,
            bytes[3] & 0xFF, bytes[4] & 0xFF, bytes[5] & 0xFF);
    }

    /**
     * Returns the MAC address as 12 upper-case hex digits with no separators.
     *
     * @return the plain hex string, e.g. {@code 001A2B3C4D5E}
     */
    public String toPlainFormat() {
        return String.format(Locale.ROOT, "%02X%02X%02X%02X%02X%02X",
            bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF,
            bytes[3] & 0xFF, bytes[4] & 0xFF, bytes[5] & 0xFF);
    }

    /**
     * Returns the Organizationally Unique Identifier: the first three bytes, which identify the manufacturer.
     *
     * @return the OUI as colon-separated hex, e.g. {@code 00:1A:2B}
     */
    public String oui() {
        return String.format(Locale.ROOT, "%02X:%02X:%02X",
            bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF);
    }

    /**
     * Returns the part the manufacturer assigns to the network interface: the last three bytes.
     *
     * @return the NIC part as colon-separated hex, e.g. {@code 3C:4D:5E}
     */
    public String nic() {
        return String.format(Locale.ROOT, "%02X:%02X:%02X",
            bytes[3] & 0xFF, bytes[4] & 0xFF, bytes[5] & 0xFF);
    }

    /**
     * Checks whether this is a unicast address: the lowest bit of the first byte is 0.
     *
     * @return whether it is unicast
     */
    public boolean isUnicast() {
        return (bytes[0] & 0x01) == 0;
    }

    /**
     * Checks whether this is a multicast address: the lowest bit of the first byte is 1. The broadcast address
     * is multicast too.
     *
     * @return whether it is multicast
     */
    public boolean isMulticast() {
        return (bytes[0] & 0x01) == 1;
    }

    /**
     * Checks whether this is a locally administered address: the second-lowest bit of the first byte is 1.
     *
     * @return whether it is locally administered
     */
    public boolean isLocallyAdministered() {
        return (bytes[0] & 0x02) == 2;
    }

    /**
     * Checks whether this is a universally administered (manufacturer-assigned) address: the second-lowest bit
     * of the first byte is 0.
     *
     * @return whether it is universally administered
     */
    public boolean isUniversallyAdministered() {
        return (bytes[0] & 0x02) == 0;
    }

    /**
     * Checks whether this is the broadcast address, {@code FF:FF:FF:FF:FF:FF}.
     *
     * @return whether it is the broadcast address
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

    /**
     * Parses a MAC address as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as a {@link MacAddress}, or empty when it is not one; never throws
     */
    public static Optional<MacAddress> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

}
