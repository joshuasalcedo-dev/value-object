package io.joshuasalcedo.valueobject.network;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import java.util.Optional;
import io.joshuasalcedo.valueobject.ValueObject;

import org.jspecify.annotations.Nullable;

/**
 * An IP address: either an {@link Ipv4Address} or an {@link Ipv6Address}. IPv4 addresses sort before IPv6 ones.
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
     * Reads an IP address, trying IPv4 first and then IPv6.
     *
     * @param address the address string, e.g. {@code "10.0.0.1"} or {@code "2001:db8::1"}
     * @return the address, an {@link Ipv4Address} or an {@link Ipv6Address}
     * @throws ValueObjectException if it is {@code null}, blank, or neither kind of address
     */
    static IpAddress of(String address) {
        if (address == null || address.isBlank()) {
            throw new ValueObjectException("IP address cannot be null or blank");
        }

        // Try IPv4 first
        Optional<Ipv4Address> ipv4 = Ipv4Address.tryParse(address);
        if (ipv4.isPresent()) {
            return ipv4.get();
        }

        // Try IPv6
        Optional<Ipv6Address> ipv6 = Ipv6Address.tryParse(address);
        if (ipv6.isPresent()) {
            return ipv6.get();
        }

        throw new ValueObjectException("Invalid IP address: " + address);
    }


    /**
     * Parses an IP address as {@link #of(String)} does.
     *
     * @param text an IPv4 or IPv6 address
     * @return the address
     * @throws ValueObjectException if it is {@code null}, blank, or neither kind of address
     */
    static IpAddress parse(String text) {
        if (text == null) throw new ValueObjectException("An IP address is required");
        return of(text);
    }

    /**
     * Checks whether a text is an IPv4 or an IPv6 address.
     *
     * @param address the address string; may be {@code null}
     * @return whether it is valid
     */
    static boolean isValid(@Nullable String address) {
        return Ipv4Address.isValid(address) || Ipv6Address.isValid(address);
    }

    /**
     * Parses an IP address as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as an {@link IpAddress}, or empty when it is not one; never throws
     */
    public static Optional<IpAddress> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

}
