package io.joshuasalcedo.valueobject.network;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

/**
 * Value object representing a network port number.
 * Valid range is 0-65535.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Port(int value) implements ValueObject, Comparable<Port> {

    // Port range constants
    public static final int MIN_PORT = 0;
    public static final int MAX_PORT = 65535;
    public static final int SYSTEM_PORTS_END = 1023;
    public static final int REGISTERED_PORTS_END = 49151;

    // Common well-known ports
    public static final Port HTTP = new Port(80);
    public static final Port HTTPS = new Port(443);
    public static final Port FTP = new Port(21);
    public static final Port SSH = new Port(22);
    public static final Port TELNET = new Port(23);
    public static final Port SMTP = new Port(25);
    public static final Port DNS = new Port(53);
    public static final Port POP3 = new Port(110);
    public static final Port IMAP = new Port(143);
    public static final Port MYSQL = new Port(3306);
    public static final Port POSTGRESQL = new Port(5432);
    public static final Port REDIS = new Port(6379);
    public static final Port MONGODB = new Port(27017);

    public Port {
        if (value < MIN_PORT || value > MAX_PORT) {
            throw new ValueObjectException(
                String.format("Port must be between %d and %d: %d", MIN_PORT, MAX_PORT, value)
            );
        }
    }

    /**
     * Creates a Port from an integer value.
     *
     * @param port the port number
     * @return a new Port
     */
    public static Port of(int port) {
        return new Port(port);
    }

    /**
     * Creates a Port from a string value.
     *
     * @param port the port string
     * @return a new Port
     */
    public static Port of(String port) {
        try {
            return new Port(Integer.parseInt(port.trim()));
        } catch (NumberFormatException e) {
            throw new ValueObjectException("Invalid port number: " + port, e);
        }
    }

    /**
     * Parses a port string, returning null if invalid.
     *
     * @param port the port string
     * @return the Port or null
     */
    public static @Nullable Port parse(String port) {
        try {
            return of(port);
        } catch (ValueObjectException e) {
            return null;
        }
    }

    /**
     * Checks if a port number is valid.
     *
     * @param port the port number
     * @return true if valid
     */
    public static boolean isValid(int port) {
        return port >= MIN_PORT && port <= MAX_PORT;
    }

    /**
     * Checks if this is a system/well-known port (0-1023).
     * These typically require root/admin privileges.
     *
     * @return true if system port
     */
    public boolean isSystemPort() {
        return value <= SYSTEM_PORTS_END;
    }

    /**
     * Checks if this is a registered/user port (1024-49151).
     *
     * @return true if registered port
     */
    public boolean isRegisteredPort() {
        return value > SYSTEM_PORTS_END && value <= REGISTERED_PORTS_END;
    }

    /**
     * Checks if this is a dynamic/private/ephemeral port (49152-65535).
     *
     * @return true if dynamic port
     */
    public boolean isDynamicPort() {
        return value > REGISTERED_PORTS_END;
    }

    /**
     * Checks if this port requires elevated privileges (typically < 1024).
     *
     * @return true if requires privileges
     */
    public boolean requiresPrivileges() {
        return isSystemPort();
    }

    /**
     * Gets the category name for this port range.
     *
     * @return the category name
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
     * Gets the common port name for well-known ports.
     *
     * @return the port name or null
     */
    public String commonServiceName() {
        return switch (value) {
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
        };
    }

    @Override
    public int compareTo(Port other) {
        return Integer.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
