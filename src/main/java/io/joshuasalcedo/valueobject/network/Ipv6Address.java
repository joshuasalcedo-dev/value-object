package io.joshuasalcedo.valueobject.network;

import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;

/**
 * An IPv6 address. The text must be an IPv6 literal (RFC 4291): eight groups of one to four hex digits, at most
 * one {@code ::} standing for a run of zero groups, and optionally a dotted-quad IPv4 address in place of the last
 * two groups. Host names, zone ids ({@code %eth0}) and brackets are rejected; no DNS lookup is ever made.
 *
 * <p>The address is stored in the RFC 5952 canonical form: lower-case hex, no leading zeros, the longest run of
 * two or more zero groups (the first, on a tie) compressed to {@code ::}, so {@code "0:0:0:0:0:0:0:1"} becomes
 * {@code "::1"} and {@code "2001:DB8:0:0:1:0:0:1"} becomes {@code "2001:db8::1:0:0:1"}. An IPv4-mapped address
 * keeps the IPv4 part in dotted form, {@code "::ffff:192.0.2.1"}. Because the canonical form is unique, two
 * addresses are equal exactly when their bytes are.
 *
 * @param value the address in RFC 5952 canonical form
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Ipv6Address(String value) implements ValueObject, IpAddress {

    // Common addresses
    /** Localhost: {@code new Ipv6Address("::1")}. */
    public static final Ipv6Address LOCALHOST = new Ipv6Address("::1");
    /** Any: {@code new Ipv6Address("::")}. */
    public static final Ipv6Address ANY = new Ipv6Address("::");

    /**
     * Creates an address from its text, trimmed and stored in canonical form as described above.
     *
     * @throws ValueObjectException if {@code value} is {@code null} or not an IPv6 literal
     */
    public Ipv6Address {
        if (value == null) {
            throw new ValueObjectException("IPv6 address cannot be null");
        }
        value = canonical(readLiteral(value.trim()));
    }

    /**
     * Reads an IPv6 literal into its sixteen bytes.
     *
     * @param text the trimmed text
     * @return the 16 bytes
     * @throws ValueObjectException if it is not an IPv6 literal
     */
    private static byte[] readLiteral(String text) {
        if (text.isEmpty() || text.length() > 45) {
            throw new ValueObjectException("Invalid IPv6 address: " + text);
        }
        int[] groups = new int[8];
        int doubleColon = text.indexOf("::");
        if (doubleColon >= 0) {
            if (text.indexOf("::", doubleColon + 1) >= 0) {
                throw new ValueObjectException("Invalid IPv6 address (more than one ::): " + text);
            }
            String head = text.substring(0, doubleColon);
            String tail = text.substring(doubleColon + 2);
            int[] headGroups = readGroups(head, text, false);
            int[] tailGroups = readGroups(tail, text, true);
            if (headGroups.length + tailGroups.length > 7) {
                throw new ValueObjectException("Invalid IPv6 address (too many groups): " + text);
            }
            System.arraycopy(headGroups, 0, groups, 0, headGroups.length);
            System.arraycopy(tailGroups, 0, groups, 8 - tailGroups.length, tailGroups.length);
        } else {
            int[] all = readGroups(text, text, true);
            if (all.length != 8) {
                throw new ValueObjectException("Invalid IPv6 address (needs 8 groups): " + text);
            }
            groups = all;
        }
        byte[] bytes = new byte[16];
        for (int i = 0; i < 8; i++) {
            bytes[2 * i] = (byte) (groups[i] >> 8);
            bytes[2 * i + 1] = (byte) groups[i];
        }
        return bytes;
    }

    /**
     * Reads colon-separated groups; the last may be a dotted-quad IPv4 address, read as two groups.
     *
     * @param part      the groups, or empty
     * @param text      the whole text, for messages
     * @param ipv4Last  whether the last group may be an IPv4 address
     * @return the 16-bit groups
     */
    private static int[] readGroups(String part, String text, boolean ipv4Last) {
        if (part.isEmpty()) {
            return new int[0];
        }
        String[] pieces = part.split(":", -1);
        int[] out = new int[pieces.length + 1];
        int n = 0;
        for (int i = 0; i < pieces.length; i++) {
            String piece = pieces[i];
            if (piece.indexOf('.') >= 0) {
                if (i != pieces.length - 1 || !ipv4Last || !Ipv4Address.isValid(piece)
                        || !piece.equals(piece.trim())) {
                    throw new ValueObjectException("Invalid IPv6 address: " + text);
                }
                byte[] v4 = Ipv4Address.of(piece).toBytes();
                out[n++] = ((v4[0] & 0xFF) << 8) | (v4[1] & 0xFF);
                out[n++] = ((v4[2] & 0xFF) << 8) | (v4[3] & 0xFF);
                continue;
            }
            if (piece.isEmpty() || piece.length() > 4) {
                throw new ValueObjectException("Invalid IPv6 address: " + text);
            }
            int group = 0;
            for (int c = 0; c < piece.length(); c++) {
                char ch = piece.charAt(c);
                int digit = ch < 128 ? Character.digit(ch, 16) : -1;
                if (digit < 0) {
                    throw new ValueObjectException("Invalid IPv6 address: " + text);
                }
                group = (group << 4) | digit;
            }
            out[n++] = group;
        }
        return Arrays.copyOf(out, n);
    }

    /**
     * Writes sixteen bytes in RFC 5952 canonical form.
     *
     * @param bytes the 16 bytes
     * @return the canonical text
     */
    private static String canonical(byte[] bytes) {
        int[] groups = new int[8];
        for (int i = 0; i < 8; i++) {
            groups[i] = ((bytes[2 * i] & 0xFF) << 8) | (bytes[2 * i + 1] & 0xFF);
        }
        boolean mapped = groups[0] == 0 && groups[1] == 0 && groups[2] == 0 && groups[3] == 0
                && groups[4] == 0 && groups[5] == 0xFFFF;
        if (mapped) {
            return "::ffff:" + (bytes[12] & 0xFF) + "." + (bytes[13] & 0xFF) + "."
                    + (bytes[14] & 0xFF) + "." + (bytes[15] & 0xFF);
        }
        int bestStart = -1;
        int bestLength = 1;
        for (int i = 0; i < 8; ) {
            if (groups[i] != 0) {
                i++;
                continue;
            }
            int j = i;
            while (j < 8 && groups[j] == 0) {
                j++;
            }
            if (j - i > bestLength) {
                bestStart = i;
                bestLength = j - i;
            }
            i = j;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i == bestStart) {
                sb.append("::");
                i += bestLength - 1;
                continue;
            }
            if (!sb.isEmpty() && sb.charAt(sb.length() - 1) != ':') {
                sb.append(':');
            }
            sb.append(Integer.toHexString(groups[i]));
        }
        return sb.toString();
    }

    /**
     * Creates an address from its text; the same as the constructor.
     *
     * @param address the address string, e.g. {@code "2001:db8::1"}
     * @return the address
     * @throws ValueObjectException if it is {@code null} or not an IPv6 literal
     */
    public static Ipv6Address of(String address) {
        return new Ipv6Address(address);
    }

    /**
     * Creates an address from its sixteen bytes, most significant first.
     *
     * @param bytes 16 bytes
     * @return the address
     * @throws ValueObjectException if {@code bytes} is {@code null} or not 16 bytes long
     */
    public static Ipv6Address of(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            throw new ValueObjectException("IPv6 address requires exactly 16 bytes");
        }
        return new Ipv6Address(canonical(bytes));
    }


    /**
     * Parses an address from its text, trimmed.
     *
     * @param text the text
     * @return the address
     * @throws ValueObjectException if it is {@code null} or not an IPv6 literal
     */
    public static Ipv6Address parse(String text) {
        if (text == null) throw new ValueObjectException("A Ipv6Address is required");
        return of(text);
    }

    /**
     * Checks whether a text, once trimmed, is an IPv6 literal. No DNS lookup is made.
     *
     * @param address the address string; may be {@code null}
     * @return whether it is valid
     */
    public static boolean isValid(@Nullable String address) {
        return tryParse(address).isPresent();
    }

    @Override
    public int version() {
        return 6;
    }

    @Override
    public byte[] toBytes() {
        return readLiteral(value);
    }

    /**
     * Returns the fully expanded form: eight groups of four lower-case hex digits.
     *
     * @return the expanded form, e.g. {@code "0000:0000:0000:0000:0000:0000:0000:0001"}
     */
    public String expanded() {
        byte[] bytes = toBytes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i += 2) {
            if (i > 0) sb.append(":");
            sb.append(String.format(Locale.ROOT, "%02x%02x", bytes[i] & 0xFF, bytes[i + 1] & 0xFF));
        }
        return sb.toString();
    }

    /**
     * Returns the RFC 5952 canonical form, which is the stored value: the longest run of zero groups is
     * compressed to {@code ::}.
     *
     * @return the compressed form, e.g. {@code "::1"}
     */
    public String compressed() {
        return value;
    }

    @Override
    public boolean isLoopback() {
        return "::1".equals(value);
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
     * Checks whether this is the unspecified ("any") address, {@code ::}.
     *
     * @return whether every byte is 0
     */
    public boolean isAny() {
        return "::".equals(value);
    }

    /**
     * Checks whether this is an IPv4-mapped address ({@code ::ffff:0:0/96}), such as {@code ::ffff:192.0.2.1}.
     *
     * @return whether it is IPv4-mapped
     */
    public boolean isIpv4Mapped() {
        byte[] bytes = toBytes();
        for (int i = 0; i < 10; i++) {
            if (bytes[i] != 0) return false;
        }
        return (bytes[10] & 0xFF) == 0xFF && (bytes[11] & 0xFF) == 0xFF;
    }

    /**
     * Returns the IPv4 address in the last four bytes, when this is an {@link #isIpv4Mapped() IPv4-mapped}
     * address.
     *
     * @return the embedded IPv4 address, or {@code null} when it is not IPv4-mapped
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
            return Arrays.compareUnsigned(this.toBytes(), ipv6.toBytes());
        }
        // IPv6 sorts after IPv4
        return 1;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Parses an address as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as an {@link Ipv6Address}, or empty when it is not one; never throws
     */
    public static Optional<Ipv6Address> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

}
