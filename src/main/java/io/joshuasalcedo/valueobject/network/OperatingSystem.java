package io.joshuasalcedo.valueobject.network;

import io.joshuasalcedo.valueobject.ValueObject;

import java.util.Arrays;
import java.util.Locale;

public enum OperatingSystem implements ValueObject {
    WINDOWS("win", "Windows"),
    LINUX("lin", "Linux"),
    MACOS("mac", "macOS"),
    UNKNOWN("unknown", "Unknown");

    private final String code;
    private final String displayName;

    OperatingSystem(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String code() {
        return code;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isUnixLike() {
        return this == LINUX || this == MACOS;
    }

    public String pathSeparator() {
        return this == WINDOWS ? "\\" : "/";
    }

    public String lineEnding() {
        return this == WINDOWS ? "\r\n" : "\n";
    }

    public static OperatingSystem fromCode(String code) {
        if (code == null || code.isBlank()) {
            return UNKNOWN;
        }
        return Arrays.stream(values())
                .filter(os -> os.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static OperatingSystem detect() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.contains("win")) return WINDOWS;
        if (osName.contains("mac") || osName.contains("darwin")) return MACOS;
        if (osName.contains("nux") || osName.contains("nix")) return LINUX;
        return UNKNOWN;
    }
}