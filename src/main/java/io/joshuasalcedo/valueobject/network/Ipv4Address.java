package io.joshuasalcedo.valueobject.network;

import java.util.Optional;
import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * An IPv4 address in dotted-decimal form, {@code 192.168.1.10}: four numbers of 0 to 255 without leading zeros.
 * Only the literal is read; host names are rejected and no DNS lookup is made.
 *
 * @param value the address, trimmed
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Ipv4Address(String value) implements ValueObject, IpAddress {

    // IPv4 pattern: 4 octets of 0-255
    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$"
    );

    // Common addresses
    /** Localhost: {@code new Ipv4Address("127.0.0.1")}. */
    public static final Ipv4Address LOCALHOST = new Ipv4Address("127.0.0.1");
    /** Any: {@code new Ipv4Address("0.0.0.0")}. */
    public static final Ipv4Address ANY = new Ipv4Address("0.0.0.0");
    /** Broadcast: {@code new Ipv4Address("255.255.255.255")}. */
    public static final Ipv4Address BROADCAST = new Ipv4Address("255.255.255.255");

    /**
     * Creates an address from its dotted-decimal text, trimmed.
     *
     * @throws ValueObjectException if {@code value} is {@code null} or not four numbers of 0 to 255 without leading zeros
     */
    public Ipv4Address {
        if (value == null) {
            throw new ValueObjectException("IPv4 address cannot be null");
        }
        value = value.trim();

        if (!IPV4_PATTERN.matcher(value).matches()) {
            throw new ValueObjectException("Invalid IPv4 address: " + value);
        }
    }

    /**
     * Creates an address from its dotted-decimal text; the same as the constructor.
     *
     * @param address the address string, e.g. {@code "10.0.0.1"}
     * @return the address
     * @throws ValueObjectException if it is not a dotted-decimal IPv4 address
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
     * @return the address
     * @throws ValueObjectException if an octet is outside 0 to 255
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
                String.format(Locale.ROOT, "%s octet must be 0-255, got %d", position, value)
            );
        }
    }

    /**
     * Creates an address from its four bytes, most significant first; each is read as unsigned.
     *
     * @param bytes 4 bytes
     * @return the address
     * @throws ValueObjectException if {@code bytes} is {@code null} or not 4 bytes long
     */
    public static Ipv4Address of(byte[] bytes) {
        if (bytes == null || bytes.length != 4) {
            throw new ValueObjectException("IPv4 address requires exactly 4 bytes");
        }
        return of(bytes[0] & 0xFF, bytes[1] & 0xFF, bytes[2] & 0xFF, bytes[3] & 0xFF);
    }


    /**
     * Parses an address from its dotted-decimal text, trimmed.
     *
     * @param text the text
     * @return the address
     * @throws ValueObjectException if it is {@code null} or not a dotted-decimal IPv4 address
     */
    public static Ipv4Address parse(String text) {
        if (text == null) throw new ValueObjectException("A Ipv4Address is required");
        return of(text);
    }

    /**
     * Checks whether a text, once trimmed, is a dotted-decimal IPv4 address.
     *
     * @param address the address string; may be {@code null}
     * @return whether it is valid
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
     * Returns the four numbers of the address.
     *
     * @return the 4 octets, each 0 to 255
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
     * Returns the address as an unsigned 32-bit number.
     *
     * @return the numeric value, 0 to 4294967295
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
     * Checks whether this is the limited broadcast address, {@code 255.255.255.255}.
     *
     * @return whether it is the broadcast address
     */
    public boolean isBroadcast() {
        return "255.255.255.255".equals(value);
    }

    /**
     * Checks whether this is the unspecified ("any") address, {@code 0.0.0.0}.
     *
     * @return whether it is the any address
     */
    public boolean isAny() {
        return "0.0.0.0".equals(value);
    }

    /**
     * Returns the old classful network class, from the first number: A (0 to 127), B (128 to 191),
     * C (192 to 223), D (224 to 239, multicast) or E (240 to 255).
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

    /**
     * Parses an address as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as an {@link Ipv4Address}, or empty when it is not one; never throws
     */
    public static Optional<Ipv4Address> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

}
