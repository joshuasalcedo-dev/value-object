package io.joshuasalcedo.valueobject.network;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.jspecify.annotations.Nullable;
import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import java.util.Arrays;
import java.util.Locale;

/**
 * A desktop operating system family, with a short code and the path separator and line ending it uses.
 *
 * <pre>{@code
 * OperatingSystem os = OperatingSystem.detect();   // from the "os.name" system property
 * os.lineEnding();                                 // "\r\n" on Windows, "\n" elsewhere
 * }</pre>
 */
public enum OperatingSystem implements ValueObject {
    /** Windows ({@code win}). */
    WINDOWS("win", "Windows"),
    /** Linux ({@code lin}). */
    LINUX("lin", "Linux"),
    /** macOS ({@code mac}). */
    MACOS("mac", "macOS"),
    /** Not known, or not one of the others. */
    UNKNOWN("unknown", "Unknown");

    private final String code;
    private final String displayName;

    OperatingSystem(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    /** {@return its short code: {@code "win"}, {@code "lin"}, {@code "mac"} or {@code "unknown"}} */
    public String code() {
        return code;
    }

    /** {@return its display name, e.g. {@code "macOS"}} */
    public String displayName() {
        return displayName;
    }

    /** {@return whether it is Linux or macOS} */
    public boolean isUnixLike() {
        return this == LINUX || this == MACOS;
    }

    /**
     * {@return the separator between the names in a file path: a backslash on Windows, {@code "/"} otherwise
     * (including {@link #UNKNOWN})}
     */
    public String pathSeparator() {
        return this == WINDOWS ? "\\" : "/";
    }

    /** {@return its line ending: {@code "\r\n"} on Windows, {@code "\n"} otherwise} */
    public String lineEnding() {
        return this == WINDOWS ? "\r\n" : "\n";
    }

    /**
     * Finds an operating system by its code, ignoring case and surrounding whitespace.
     *
     * @param code e.g. {@code "win"}
     * @return the operating system, or {@link #UNKNOWN} when none has that code or it is {@code null} or blank
     */
    public static OperatingSystem fromCode(@Nullable String code) {
        if (code == null || code.isBlank()) {
            return UNKNOWN;
        }
        return Arrays.stream(values())
                .filter(os -> os.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElse(UNKNOWN);
    }

    /**
     * Detects the operating system this JVM runs on, from the {@code os.name} system property, as
     * {@link #fromOsName(String)} reads it.
     *
     * @return the operating system, or {@link #UNKNOWN} when the name matches none of those
     */
    public static OperatingSystem detect() {
        return fromOsName(System.getProperty("os.name", ""));
    }

    /**
     * Reads an {@code os.name} value: one containing {@code mac} or {@code darwin} is macOS (checked first, so
     * {@code "Darwin"} is not taken for Windows), one starting with {@code windows} or containing {@code win} as
     * a whole word is Windows, and one containing {@code linux}, {@code nux} or {@code nix} is Linux. Case and
     * surrounding whitespace are ignored.
     *
     * @param osName an {@code os.name} value, e.g. {@code "Windows 11"} or {@code "Mac OS X"}; may be
     *               {@code null}
     * @return the operating system, or {@link #UNKNOWN} when the name matches none of those
     */
    public static OperatingSystem fromOsName(@Nullable String osName) {
        if (osName == null) {
            return UNKNOWN;
        }
        String name = osName.strip().toLowerCase(Locale.ROOT);
        if (name.contains("mac") || name.contains("darwin")) return MACOS;
        if (name.startsWith("windows") || name.matches(".*\\bwin(32|64)?\\b.*")) return WINDOWS;
        if (name.contains("nux") || name.contains("nix")) return LINUX;
        return UNKNOWN;
    }

    /**
     * Parses an operating system from its code or enum name, ignoring case and surrounding whitespace.
     *
     * @param text the code or name, e.g. {@code "mac"} or {@code "MACOS"}
     * @return the operating system
     * @throws ValueObjectException if it is {@code null} or names no operating system other than
     *                              {@link #UNKNOWN}
     */
    public static OperatingSystem parse(String text) {
        if (text == null) throw new ValueObjectException("A OperatingSystem is required");
        String t = text.strip();
        for (OperatingSystem os : values()) {
            if (os != UNKNOWN && (os.name().equalsIgnoreCase(t) || os.fromCode(t) == os)) return os;
        }
        throw new ValueObjectException("Not an operating system: '" + text + "'");
    }

    /**
     * Parses an operating system as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as an {@link OperatingSystem}, or empty when it is not one; never throws
     */
    public static Optional<OperatingSystem> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text names an operating system, as {@link #parse(String)} reads it.
     *
     * @param text any text
     * @return whether it is an {@link OperatingSystem}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
