package io.joshuasalcedo.valueobject.network;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing an IPv4 address.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Ipv4Address(String value) implements ValueObject, IpAddress {

    // IPv4 pattern: 4 octets of 0-255
    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$"
    );

    // Common addresses
    public static final Ipv4Address LOCALHOST = new Ipv4Address("127.0.0.1");
    public static final Ipv4Address ANY = new Ipv4Address("0.0.0.0");
    public static final Ipv4Address BROADCAST = new Ipv4Address("255.255.255.255");

    public Ipv4Address {
        Objects.requireNonNull(value, "IPv4 address cannot be null");
        value = value.trim();

        if (!IPV4_PATTERN.matcher(value).matches()) {
            throw new ValueObjectException("Invalid IPv4 address: " + value);
        }
    }

    /**
     * Creates an IPv4 address from a string.
     *
     * @param address the address string
     * @return a new Ipv4Address
     */
    public static Ipv4Address of(String address) {
        return new Ipv4Address(address);
    }

    /**
     * Creates an IPv4 address from 4 octets.
     *
     * @param a first octet
     * @param b second octet
     * @param c third octet
     * @param d fourth octet
     * @return a new Ipv4Address
     */
    public static Ipv4Address of(int a, int b, int c, int d) {
        validateOctet(a, "first");
        validateOctet(b, "second");
        validateOctet(c, "third");
        validateOctet(d, "fourth");
        return new Ipv4Address(a + "." + b + "." + c + "." + d);
    }

    private static void validateOctet(int value, String position) {
        if (value < 0 || value > 255) {
            throw new ValueObjectException(
                String.format("%s octet must be 0-255, got %d", position, value)
            );
        }
    }

    /**
     * Creates an IPv4 address from a byte array.
     *
     * @param bytes 4 bytes
     * @return a new Ipv4Address
     */
    public static Ipv4Address of(byte[] bytes) {
        if (bytes == null || bytes.length != 4) {
            throw new ValueObjectException("IPv4 address requires exactly 4 bytes");
        }
        return of(bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF, bytes[3] & 0xFF);
    }

    /**
     * Parses an IPv4 address string, returning null if invalid.
     *
     * @param address the address string
     * @return the Ipv4Address or null
     */
    public static @Nullable Ipv4Address parse(String address) {
        try {
            return of(address);
        } catch (ValueObjectException e) {
            return null;
        }
    }

    /**
     * Checks if a string is a valid IPv4 address.
     *
     * @param address the address string
     * @return true if valid
     */
    public static boolean isValid(String address) {
        if (address == null || address.isBlank()) {
            return false;
        }
        return IPV4_PATTERN.matcher(address.trim()).matches();
    }

    @Override
    public int version() {
        return 4;
    }

    @Override
    public byte[] toBytes() {
        String[] parts = value.split("\\.");
        return new byte[] {
            (byte) Integer.parseInt(parts[0]),
            (byte) Integer.parseInt(parts[1]),
            (byte) Integer.parseInt(parts[2]),
            (byte) Integer.parseInt(parts[3])
        };
    }

    /**
     * Returns the octets as an int array.
     *
     * @return the 4 octets
     */
    public int[] octets() {
        String[] parts = value.split("\\.");
        return new int[] {
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2]),
            Integer.parseInt(parts[3])
        };
    }

    /**
     * Returns the address as a 32-bit integer.
     *
     * @return the numeric value
     */
    public long toLong() {
        int[] o = octets();
        return ((long) o[0] << 24) | ((long) o[1] << 16) | ((long) o[2] << 8) | o[3];
    }

    @Override
    public boolean isLoopback() {
        // 127.0.0.0/8
        return octets()[0] == 127;
    }

    @Override
    public boolean isPrivate() {
        int[] o = octets();
        // 10.0.0.0/8
        if (o[0] == 10) return true;
        // 172.16.0.0/12
        if (o[0] == 172 && o[1] >= 16 && o[1] <= 31) return true;
        // 192.168.0.0/16
        return o[0] == 192 && o[1] == 168;
    }

    @Override
    public boolean isLinkLocal() {
        // 169.254.0.0/16
        int[] o = octets();
        return o[0] == 169 && o[1] == 254;
    }

    @Override
    public boolean isMulticast() {
        // 224.0.0.0/4
        int first = octets()[0];
        return first >= 224 && first <= 239;
    }

    /**
     * Checks if this is a broadcast address (255.255.255.255).
     *
     * @return true if broadcast
     */
    public boolean isBroadcast() {
        return "255.255.255.255".equals(value);
    }

    /**
     * Checks if this is the any/unspecified address (0.0.0.0).
     *
     * @return true if any address
     */
    public boolean isAny() {
        return "0.0.0.0".equals(value);
    }

    /**
     * Gets the network class (A, B, C, D, or E).
     *
     * @return the class letter
     */
    public char networkClass() {
        int first = octets()[0];
        if (first < 128) return 'A';
        if (first < 192) return 'B';
        if (first < 224) return 'C';
        if (first < 240) return 'D';
        return 'E';
    }

    @Override
    public int compareTo(IpAddress other) {
        if (other instanceof Ipv4Address ipv4) {
            return Long.compare(this.toLong(), ipv4.toLong());
        }
        // IPv4 sorts before IPv6
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ipv4Address that)) return false;
        return Arrays.equals(this.toBytes(), that.toBytes());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(toBytes());
    }

    @Override
    public String toString() {
        return value;
    }
}
