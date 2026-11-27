package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.jspecify.annotations.Nullable;
import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import java.math.BigDecimal;

/**
 * Units of data size, each with its factor to bytes, the base unit: bits, binary (IEC, powers of 1024)
 * and decimal (SI, powers of 1000) units.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public enum DataUnit implements ValueObject, MeasurementUnit<DataUnit> {

    // Base
    /** bytes ({@code B}), the base unit. */
    BYTE("B", "bytes", BigDecimal.ONE),
    /** bits ({@code b}). */
    BIT("b", "bits", new BigDecimal("0.125")),

    // Binary units (IEC - powers of 1024)
    /** kibibytes ({@code KiB}): 1024 bytes. */
    KIBIBYTE("KiB", "kibibytes", new BigDecimal("1024")),
    /** mebibytes ({@code MiB}). */
    MEBIBYTE("MiB", "mebibytes", new BigDecimal("1048576")),       // 1024^2
    /** gibibytes ({@code GiB}). */
    GIBIBYTE("GiB", "gibibytes", new BigDecimal("1073741824")),    // 1024^3
    /** tebibytes ({@code TiB}). */
    TEBIBYTE("TiB", "tebibytes", new BigDecimal("1099511627776")), // 1024^4
    /** pebibytes ({@code PiB}). */
    PEBIBYTE("PiB", "pebibytes", new BigDecimal("1125899906842624")), // 1024^5

    // Decimal units (SI - powers of 1000)
    /** kilobytes ({@code KB}): 1000 bytes. */
    KILOBYTE("KB", "kilobytes", new BigDecimal("1000")),
    /** megabytes ({@code MB}). */
    MEGABYTE("MB", "megabytes", new BigDecimal("1000000")),
    /** gigabytes ({@code GB}). */
    GIGABYTE("GB", "gigabytes", new BigDecimal("1000000000")),
    /** terabytes ({@code TB}). */
    TERABYTE("TB", "terabytes", new BigDecimal("1000000000000")),
    /** petabytes ({@code PB}). */
    PETABYTE("PB", "petabytes", new BigDecimal("1000000000000000"));

    private final String symbol;
    private final String unitName;
    private final BigDecimal toBytesFactor;

    DataUnit(String symbol, String unitName, BigDecimal toBytesFactor) {
        this.symbol = symbol;
        this.unitName = unitName;
        this.toBytesFactor = toBytesFactor;
    }

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public String unitName() {
        return unitName;
    }

    @Override
    public BigDecimal toBaseUnitFactor() {
        return toBytesFactor;
    }

    @Override
    public DataUnit baseUnit() {
        return BYTE;
    }

    /**
     * Checks if this is a binary (IEC) unit (powers of 1024).
     *
     * @return true if binary unit
     */
    public boolean isBinary() {
        return this == KIBIBYTE || this == MEBIBYTE ||
               this == GIBIBYTE || this == TEBIBYTE || this == PEBIBYTE;
    }

    /**
     * Checks if this is a decimal (SI) unit (powers of 1000).
     *
     * @return true if decimal unit
     */
    public boolean isDecimal() {
        return this == KILOBYTE || this == MEGABYTE ||
               this == GIGABYTE || this == TERABYTE || this == PETABYTE;
    }

    /**
     * Gets the corresponding binary unit for a decimal unit: {@code KB} gives {@code KiB}, and so on.
     * Any other unit is returned as is.
     *
     * @return the binary equivalent
     */
    public DataUnit toBinaryEquivalent() {
        return switch (this) {
            case KILOBYTE -> KIBIBYTE;
            case MEGABYTE -> MEBIBYTE;
            case GIGABYTE -> GIBIBYTE;
            case TERABYTE -> TEBIBYTE;
            case PETABYTE -> PEBIBYTE;
            default -> this;
        };
    }

    /**
     * Gets the corresponding decimal unit for a binary unit: {@code KiB} gives {@code KB}, and so on.
     * Any other unit is returned as is.
     *
     * @return the decimal equivalent
     */
    public DataUnit toDecimalEquivalent() {
        return switch (this) {
            case KIBIBYTE -> KILOBYTE;
            case MEBIBYTE -> MEGABYTE;
            case GIBIBYTE -> GIGABYTE;
            case TEBIBYTE -> TERABYTE;
            case PEBIBYTE -> PETABYTE;
            default -> this;
        };
    }

    /**
     * Looks up a unit by its constant name (spaces, dashes and dots allowed, e.g. {@code "mebibyte"}), its symbol
     * or its plural name, ignoring surrounding spaces. Names ignore case, and so do symbols except for their last
     * letter, which must match: by convention a lower-case {@code b} is a bit and an upper-case {@code B} a byte.
     * So {@code "b"} is {@link #BIT} and {@code "B"} {@link #BYTE}; {@code "mB"}, {@code "kB"} and {@code "GIB"}
     * read as megabytes, kilobytes and gibibytes; but {@code "mb"}, {@code "Mb"} and {@code "kb"}, which usually
     * mean megabits and kilobits, are not read as bytes (and are not units here).
     *
     * @param text its symbol or name, e.g. {@code "MB"}
     * @return the unit
     * @throws ValueObjectException if it is {@code null} or not a unit
     */
    public static DataUnit parse(String text) {
        if (text == null) throw new ValueObjectException("A DataUnit is required");
        String t = text.strip();
        String key = t.replace(".", "").replace(' ', '_').replace('-', '_');
        for (DataUnit v : values()) {
            if (v.name().equalsIgnoreCase(key) || v.symbol().equals(t) || v.unitName().equalsIgnoreCase(t)) return v;
        }
        if (!t.isEmpty()) {
            char last = t.charAt(t.length() - 1);
            String head = t.substring(0, t.length() - 1);
            for (DataUnit v : values()) {
                String s = v.symbol();
                if (s.charAt(s.length() - 1) == last && s.substring(0, s.length() - 1).equalsIgnoreCase(head)) return v;
            }
        }
        throw new ValueObjectException("Not a DataUnit: '" + text + "'");
    }

    /**
     * Looks up a unit like {@link #parse(String)}, without throwing.
     *
     * @param text any text
     * @return it as a {@link DataUnit}, or empty when it is not one; never throws
     */
    public static Optional<DataUnit> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether text names a unit, as {@link #parse(String)} reads it.
     *
     * @param text any text
     * @return whether it is a {@link DataUnit}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
