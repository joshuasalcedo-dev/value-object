package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing a file/data size.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record FileSize(
        BigDecimal value,
        DataUnit unit
) implements ValueObject, Measurable<FileSize, DataUnit>, Comparable<FileSize> {

    private static final int SCALE = 6;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    public static final FileSize ZERO = new FileSize(BigDecimal.ZERO, DataUnit.BYTE);

    public FileSize {
        Objects.requireNonNull(value, "FileSize value cannot be null");
        Objects.requireNonNull(unit, "FileSize unit cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("FileSize cannot be negative: " + value);
        }
    }

    // ==================== Factory Methods ====================

    public static FileSize of(BigDecimal value, DataUnit unit) {
        return new FileSize(value, unit);
    }

    public static FileSize of(double value, DataUnit unit) {
        return new FileSize(BigDecimal.valueOf(value), unit);
    }

    public static FileSize of(long value, DataUnit unit) {
        return new FileSize(BigDecimal.valueOf(value), unit);
    }

    public static FileSize zero() {
        return ZERO;
    }

    // Convenience methods for common units
    public static FileSize bytes(long value) {
        return of(value, DataUnit.BYTE);
    }

    public static FileSize kilobytes(double value) {
        return of(value, DataUnit.KILOBYTE);
    }

    public static FileSize megabytes(double value) {
        return of(value, DataUnit.MEGABYTE);
    }

    public static FileSize gigabytes(double value) {
        return of(value, DataUnit.GIGABYTE);
    }

    public static FileSize terabytes(double value) {
        return of(value, DataUnit.TERABYTE);
    }

    public static FileSize kibibytes(double value) {
        return of(value, DataUnit.KIBIBYTE);
    }

    public static FileSize mebibytes(double value) {
        return of(value, DataUnit.MEBIBYTE);
    }

    public static FileSize gibibytes(double value) {
        return of(value, DataUnit.GIBIBYTE);
    }

    // ==================== Conversion Methods ====================

    @Override
    public FileSize convertTo(DataUnit targetUnit) {
        if (this.unit == targetUnit) {
            return this;
        }
        BigDecimal converted = unit.convertTo(value, targetUnit);
        return new FileSize(converted.setScale(SCALE, ROUNDING), targetUnit);
    }

    public FileSize toBytes() {
        return convertTo(DataUnit.BYTE);
    }

    public FileSize toKilobytes() {
        return convertTo(DataUnit.KILOBYTE);
    }

    public FileSize toMegabytes() {
        return convertTo(DataUnit.MEGABYTE);
    }

    public FileSize toGigabytes() {
        return convertTo(DataUnit.GIGABYTE);
    }

    public FileSize toKibibytes() {
        return convertTo(DataUnit.KIBIBYTE);
    }

    public FileSize toMebibytes() {
        return convertTo(DataUnit.MEBIBYTE);
    }

    public FileSize toGibibytes() {
        return convertTo(DataUnit.GIBIBYTE);
    }

    // ==================== Arithmetic Operations ====================

    @Override
    public FileSize add(FileSize other) {
        FileSize otherConverted = other.convertTo(this.unit);
        return new FileSize(this.value.add(otherConverted.value), this.unit);
    }

    @Override
    public FileSize subtract(FileSize other) {
        FileSize otherConverted = other.convertTo(this.unit);
        BigDecimal result = this.value.subtract(otherConverted.value);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("Result would be negative");
        }
        return new FileSize(result, this.unit);
    }

    @Override
    public FileSize multiply(BigDecimal multiplier) {
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("Multiplier cannot be negative");
        }
        return new FileSize(value.multiply(multiplier), unit);
    }

    public FileSize multiply(double multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    @Override
    public FileSize divide(BigDecimal divisor) {
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValueObjectException("Divisor must be positive");
        }
        return new FileSize(value.divide(divisor, SCALE, ROUNDING), unit);
    }

    public FileSize divide(double divisor) {
        return divide(BigDecimal.valueOf(divisor));
    }

    // ==================== Human-Readable Formatting ====================

    /**
     * Returns the most appropriate unit for human display.
     * Uses binary units (KiB, MiB, GiB) which are more accurate for file sizes.
     *
     * @return a new FileSize in the best display unit
     */
    public FileSize humanReadable() {
        BigDecimal bytes = toBytes().value;

        if (bytes.compareTo(new BigDecimal("1125899906842624")) >= 0) { // PiB
            return convertTo(DataUnit.PEBIBYTE);
        } else if (bytes.compareTo(new BigDecimal("1099511627776")) >= 0) { // TiB
            return convertTo(DataUnit.TEBIBYTE);
        } else if (bytes.compareTo(new BigDecimal("1073741824")) >= 0) { // GiB
            return convertTo(DataUnit.GIBIBYTE);
        } else if (bytes.compareTo(new BigDecimal("1048576")) >= 0) { // MiB
            return convertTo(DataUnit.MEBIBYTE);
        } else if (bytes.compareTo(new BigDecimal("1024")) >= 0) { // KiB
            return convertTo(DataUnit.KIBIBYTE);
        } else {
            return toBytes();
        }
    }

    // ==================== Comparison ====================

    @Override
    public int compareTo(FileSize other) {
        FileSize thisInBytes = this.toBytes();
        FileSize otherInBytes = other.toBytes();
        return thisInBytes.value.compareTo(otherInBytes.value);
    }

    public boolean isGreaterThan(FileSize other) {
        return compareTo(other) > 0;
    }

    public boolean isLessThan(FileSize other) {
        return compareTo(other) < 0;
    }

    // ==================== Formatting ====================

    /**
     * Formats with default precision.
     *
     * @return the formatted file size
     */
    public String formatted() {
        return formatted(2);
    }

    /**
     * Formats with specified decimal places.
     *
     * @param decimals the number of decimal places
     * @return the formatted file size
     */
    public String formatted(int decimals) {
        return value.setScale(decimals, ROUNDING).toPlainString() + " " + unit.symbol();
    }

    /**
     * Formats in the most human-readable form.
     *
     * @return the human-readable string
     */
    public String toHumanString() {
        return humanReadable().formatted();
    }

    @Override
    public String toString() {
        return formatted();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileSize fileSize)) return false;
        // Compare in base units for equality
        return this.toBytes().value.compareTo(fileSize.toBytes().value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(toBytes().value.stripTrailingZeros());
    }
}
