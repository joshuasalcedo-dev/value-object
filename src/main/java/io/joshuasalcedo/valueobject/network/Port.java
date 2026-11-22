package io.joshuasalcedo.valueobject.network;

import java.util.Locale;
import java.util.Optional;
import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

/**
 * A TCP or UDP port number, 0 to 65535, with its IANA range: system (0 to 1023), registered (1024 to 49151) or
 * dynamic (49152 to 65535).
 *
 * @param value the port number, 0 to 65535
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Port(int value) implements ValueObject, Comparable<Port> {

    // Port range constants
    /** Min port: {@code 0}. */
    public static final int MIN_PORT = 0;
    /** Max port: {@code 65535}. */
    public static final int MAX_PORT = 65535;
    /** System ports end: {@code 1023}. */
    public static final int SYSTEM_PORTS_END = 1023;
    /** Registered ports end: {@code 49151}. */
    public static final int REGISTERED_PORTS_END = 49151;

    // Common well-known ports
    /** Http: {@code new Port(80)}. */
    public static final Port HTTP = new Port(80);
    /** Https: {@code new Port(443)}. */
    public static final Port HTTPS = new Port(443);
    /** Ftp: {@code new Port(21)}. */
    public static final Port FTP = new Port(21);
    /** Ssh: {@code new Port(22)}. */
    public static final Port SSH = new Port(22);
    /** Telnet: {@code new Port(23)}. */
    public static final Port TELNET = new Port(23);
    /** Smtp: {@code new Port(25)}. */
    public static final Port SMTP = new Port(25);
    /** Dns: {@code new Port(53)}. */
    public static final Port DNS = new Port(53);
    /** Pop3: {@code new Port(110)}. */
    public static final Port POP3 = new Port(110);
    /** Imap: {@code new Port(143)}. */
    public static final Port IMAP = new Port(143);
    /** Mysql: {@code new Port(3306)}. */
    public static final Port MYSQL = new Port(3306);
    /** Postgresql: {@code new Port(5432)}. */
    public static final Port POSTGRESQL = new Port(5432);
    /** Redis: {@code new Port(6379)}. */
    public static final Port REDIS = new Port(6379);
    /** Mongodb: {@code new Port(27017)}. */
    public static final Port MONGODB = new Port(27017);

    /**
     * Creates a port.
     *
     * @throws ValueObjectException if {@code value} is outside 0 to 65535
     */
    public Port {
        if (value < MIN_PORT || value > MAX_PORT) {
            throw new ValueObjectException(
                String.format(Locale.ROOT, "Port must be between %d and %d: %d", MIN_PORT, MAX_PORT, value)
            );
        }
    }

    /**
     * Creates a port from a number; the same as the constructor.
     *
     * @param port the port number
     * @return the port
     * @throws ValueObjectException if it is outside 0 to 65535
     */
    public static Port of(int port) {
        return new Port(port);
    }

    /**
     * Creates a port from its decimal text, trimmed.
     *
     * @param port the port string, e.g. {@code "8080"}
     * @return the port
     * @throws ValueObjectException if it is {@code null}, not a whole number of ASCII digits, or outside 0 to
     *                              65535
     */
    public static Port of(String port) {
        if (port == null) {
            throw new ValueObjectException("A Port is required");
        }
        String digits = port.trim();
        if (digits.isEmpty() || digits.length() > 5 || !digits.chars().allMatch(c -> c >= '0' && c <= '9')) {
            throw new ValueObjectException("Invalid port number: " + port);
        }
        try {
            return new Port(Integer.parseInt(digits));
        } catch (NumberFormatException e) {
            throw new ValueObjectException("Invalid port number: " + port, e);
        }
    }


    /**
     * Parses a port from its decimal text, trimmed.
     *
     * @param text the text
     * @return the port
     * @throws ValueObjectException if it is {@code null}, not a whole number, or outside 0 to 65535
     */
    public static Port parse(String text) {
        if (text == null) throw new ValueObjectException("A Port is required");
        return of(text);
    }

    /**
     * Checks whether a number is a port number, 0 to 65535.
     *
     * @param port the port number
     * @return whether it is valid
     */
    public static boolean isValid(int port) {
        return port >= MIN_PORT && port <= MAX_PORT;
    }

    /**
     * Checks whether this is a system (well-known) port, 0 to 1023. Listening on one usually needs root or
     * administrator rights.
     *
     * @return whether it is a system port
     */
    public boolean isSystemPort() {
        return value <= SYSTEM_PORTS_END;
    }

    /**
     * Checks whether this is a registered (user) port, 1024 to 49151.
     *
     * @return whether it is a registered port
     */
    public boolean isRegisteredPort() {
        return value > SYSTEM_PORTS_END && value <= REGISTERED_PORTS_END;
    }

    /**
     * Checks whether this is a dynamic (private, ephemeral) port, 49152 to 65535.
     *
     * @return whether it is a dynamic port
     */
    public boolean isDynamicPort() {
        return value > REGISTERED_PORTS_END;
    }

    /**
     * Checks whether listening on this port usually needs elevated privileges: whether it is a
     * {@link #isSystemPort() system port}.
     *
     * @return whether it requires privileges
     */
    public boolean requiresPrivileges() {
        return isSystemPort();
    }

    /**
     * Returns the name of this port's range.
     *
     * @return {@code "System/Well-Known"}, {@code "Registered/User"} or {@code "Dynamic/Private"}
     */
    public String category() {
        if (isSystemPort()) {
            return "System/Well-Known";
        } else if (isRegisteredPort()) {
            return "Registered/User";
        } else {
            return "Dynamic/Private";
        }
    }

    /**
     * Returns the service usually found on this port, for a few common ports (20, 21, 22, 23, 25, 53, 80, 110,
     * 143, 443, 3306, 5432, 6379, 8080, 8443 and 27017).
     *
     * @return the service name, e.g. {@code "PostgreSQL"}, or empty for any other port
     */
    public Optional<String> commonServiceName() {
        return Optional.ofNullable(switch (value) {
            case 20 -> "FTP Data";
            case 21 -> "FTP Control";
            case 22 -> "SSH";
            case 23 -> "Telnet";
            case 25 -> "SMTP";
            case 53 -> "DNS";
            case 80 -> "HTTP";
            case 110 -> "POP3";
            case 143 -> "IMAP";
            case 443 -> "HTTPS";
            case 3306 -> "MySQL";
            case 5432 -> "PostgreSQL";
            case 6379 -> "Redis";
            case 27017 -> "MongoDB";
            case 8080 -> "HTTP Alternate";
            case 8443 -> "HTTPS Alternate";
            default -> null;
        });
    }

    @Override
    public int compareTo(Port other) {
        return Integer.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Parses a port as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as a {@link Port}, or empty when it is not one; never throws
     */
    public static Optional<Port> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text parses as a port with {@link #parse(String)}.
     *
     * @param text any text
     * @return whether it is a {@link Port}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
