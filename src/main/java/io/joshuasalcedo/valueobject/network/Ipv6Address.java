package io.joshuasalcedo.valueobject.network;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Value object representing an IPv6 address.
 * Stores in canonical (fully expanded) form internally but supports compressed display.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Ipv6Address(String value) implements ValueObject, IpAddress {

    // Common addresses
    public static final Ipv6Address LOCALHOST = new Ipv6Address("::1");
    public static final Ipv6Address ANY = new Ipv6Address("::");

    public Ipv6Address {
        Objects.requireNonNull(value, "IPv6 address cannot be null");
        value = normalize(value.trim());
    }

    /**
     * Normalizes and validates an IPv6 address string.
     */
    private static String normalize(String address) {
        try {
            InetAddress inet = InetAddress.getByName(address);
            if (!(inet instanceof Inet6Address)) {
                throw new ValueObjectException("Not an IPv6 address: " + address);
            }
            // Return the canonical form
            return inet.getHostAddress();
        } catch (UnknownHostException e) {
            throw new ValueObjectException("Invalid IPv6 address: " + address, e);
        }
    }

    /**
     * Creates an IPv6 address from a string.
     *
     * @param address the address string
     * @return a new Ipv6Address
     */
    public static Ipv6Address of(String address) {
        return new Ipv6Address(address);
    }

    /**
     * Creates an IPv6 address from a byte array.
     *
     * @param bytes 16 bytes
     * @return a new Ipv6Address
     */
    public static Ipv6Address of(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            throw new ValueObjectException("IPv6 address requires exactly 16 bytes");
        }
        try {
            InetAddress inet = Inet6Address.getByAddress(bytes);
            return new Ipv6Address(inet.getHostAddress());
        } catch (UnknownHostException e) {
            throw new ValueObjectException("Invalid IPv6 bytes", e);
        }
    }

    /**
     * Parses an IPv6 address string, returning null if invalid.
     *
     * @param address the address string
     * @return the Ipv6Address or null
     */
    public static @Nullable Ipv6Address parse(String address) {
        try {
            return of(address);
        } catch (ValueObjectException e) {
            return null;
        }
    }

    /**
     * Checks if a string is a valid IPv6 address.
     *
     * @param address the address string
     * @return true if valid
     */
    public static boolean isValid(String address) {
        if (address == null || address.isBlank()) {
            return false;
        }
        try {
            InetAddress inet = InetAddress.getByName(address.trim());
            return inet instanceof Inet6Address;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    @Override
    public int version() {
        return 6;
    }

    @Override
    public byte[] toBytes() {
        try {
            return InetAddress.getByName(value).getAddress();
        } catch (UnknownHostException e) {
            throw new ValueObjectException("Failed to get bytes", e);
        }
    }

    /**
     * Returns the fully expanded form of the address.
     *
     * @return the expanded form
     */
    public String expanded() {
        byte[] bytes = toBytes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i += 2) {
            if (i > 0) sb.append(":");
            sb.append(String.format("%02x%02x", bytes[i] & 0xFF, bytes[i + 1] & 0xFF));
        }
        return sb.toString();
    }

    /**
     * Returns the compressed form of the address.
     *
     * @return the compressed form
     */
    public String compressed() {
        return value;
    }

    @Override
    public boolean isLoopback() {
        return "0:0:0:0:0:0:0:1".equals(expanded()) || "::1".equals(value);
    }

    @Override
    public boolean isPrivate() {
        // Unique Local Addresses: fc00::/7
        byte[] bytes = toBytes();
        return (bytes[0] & 0xFE) == 0xFC;
    }

    @Override
    public boolean isLinkLocal() {
        // fe80::/10
        byte[] bytes = toBytes();
        return (bytes[0] & 0xFF) == 0xFE && (bytes[1] & 0xC0) == 0x80;
    }

    @Override
    public boolean isMulticast() {
        // ff00::/8
        byte[] bytes = toBytes();
        return (bytes[0] & 0xFF) == 0xFF;
    }

    /**
     * Checks if this is the any/unspecified address (::).
     *
     * @return true if any address
     */
    public boolean isAny() {
        byte[] bytes = toBytes();
        for (byte b : bytes) {
            if (b != 0) return false;
        }
        return true;
    }

    /**
     * Checks if this is an IPv4-mapped address (::ffff:0:0/96).
     *
     * @return true if IPv4-mapped
     */
    public boolean isIpv4Mapped() {
        byte[] bytes = toBytes();
        for (int i = 0; i < 10; i++) {
            if (bytes[i] != 0) return false;
        }
        return (bytes[10] & 0xFF) == 0xFF && (bytes[11] & 0xFF) == 0xFF;
    }

    /**
     * Extracts the embedded IPv4 address if this is an IPv4-mapped address.
     *
     * @return the embedded IPv4Address or null
     */
    public @Nullable Ipv4Address toIpv4() {
        if (!isIpv4Mapped()) {
            return null;
        }
        byte[] bytes = toBytes();
        return Ipv4Address.of(new byte[] {
            bytes[12], bytes[13], bytes[14], bytes[15]
        });
    }

    @Override
    public int compareTo(IpAddress other) {
        if (other instanceof Ipv6Address ipv6) {
            byte[] thisBytes = this.toBytes();
            byte[] otherBytes = ipv6.toBytes();
            for (int i = 0; i < 16; i++) {
                int cmp = Integer.compare(thisBytes[i] & 0xFF, otherBytes[i] & 0xFF);
                if (cmp != 0) return cmp;
            }
            return 0;
        }
        // IPv6 sorts after IPv4
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ipv6Address that)) return false;
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
