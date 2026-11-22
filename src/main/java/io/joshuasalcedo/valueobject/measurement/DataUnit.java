package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.ValueObject;

import java.math.BigDecimal;

/**
 * Enumeration of file size units with conversion factors.
 * Base unit is bytes.
 * Includes both binary (IEC) and decimal (SI) units.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public enum DataUnit implements ValueObject, MeasurementUnit<DataUnit> {

    // Base
    BYTE("B", "bytes", BigDecimal.ONE),
    BIT("b", "bits", new BigDecimal("0.125")),

    // Binary units (IEC - powers of 1024)
    KIBIBYTE("KiB", "kibibytes", new BigDecimal("1024")),
    MEBIBYTE("MiB", "mebibytes", new BigDecimal("1048576")),       // 1024^2
    GIBIBYTE("GiB", "gibibytes", new BigDecimal("1073741824")),    // 1024^3
    TEBIBYTE("TiB", "tebibytes", new BigDecimal("1099511627776")), // 1024^4
    PEBIBYTE("PiB", "pebibytes", new BigDecimal("1125899906842624")), // 1024^5

    // Decimal units (SI - powers of 1000)
    KILOBYTE("KB", "kilobytes", new BigDecimal("1000")),
    MEGABYTE("MB", "megabytes", new BigDecimal("1000000")),
    GIGABYTE("GB", "gigabytes", new BigDecimal("1000000000")),
    TERABYTE("TB", "terabytes", new BigDecimal("1000000000000")),
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
     * Gets the corresponding binary unit for a decimal unit.
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
     * Gets the corresponding decimal unit for a binary unit.
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
}
