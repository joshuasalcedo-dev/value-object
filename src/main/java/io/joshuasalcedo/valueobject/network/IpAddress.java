package io.joshuasalcedo.valueobject.network;

import io.joshuasalcedo.valueobject.ValueObject;

import org.jspecify.annotations.Nullable;

/**
 * Sealed interface for IP addresses.
 * Permits IPv4Address and Ipv6Address implementations.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public sealed interface IpAddress extends ValueObject, Comparable<IpAddress>
        permits Ipv4Address, Ipv6Address {

    /**
     * Returns the string representation of the IP address.
     *
     * @return the IP address string
     */
    String value();

    /**
     * Returns the version of this IP address (4 or 6).
     *
     * @return the IP version
     */
    int version();

    /**
     * Returns the bytes of this IP address.
     *
     * @return the address bytes
     */
    byte[] toBytes();

    /**
     * Checks if this is a loopback address.
     *
     * @return true if loopback
     */
    boolean isLoopback();

    /**
     * Checks if this is a private/local address.
     *
     * @return true if private
     */
    boolean isPrivate();

    /**
     * Checks if this is a link-local address.
     *
     * @return true if link-local
     */
    boolean isLinkLocal();

    /**
     * Checks if this is a multicast address.
     *
     * @return true if multicast
     */
    boolean isMulticast();

    /**
     * Parses an IP address string (IPv4 or IPv6).
     *
     * @param address the address string
     * @return the IpAddress (IPv4 or IPv6)
     */
    static IpAddress of(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("IP address cannot be null or blank");
        }

        // Try IPv4 first
        if (Ipv4Address.isValid(address)) {
            return Ipv4Address.of(address);
        }

        // Try IPv6
        if (Ipv6Address.isValid(address)) {
            return Ipv6Address.of(address);
        }

        throw new IllegalArgumentException("Invalid IP address: " + address);
    }

    /**
     * Parses an IP address string, returning null if invalid.
     *
     * @param address the address string
     * @return the IpAddress or null
     */
    static @Nullable IpAddress parse(String address) {
        try {
            return of(address);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Checks if a string is a valid IP address (v4 or v6).
     *
     * @param address the address string
     * @return true if valid
     */
    static boolean isValid(String address) {
        return Ipv4Address.isValid(address) || Ipv6Address.isValid(address);
    }
}
