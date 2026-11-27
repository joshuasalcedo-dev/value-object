package io.joshuasalcedo.valueobject.measurement;

import org.jspecify.annotations.Nullable;
import io.joshuasalcedo.valueobject.core.ValueObject;

import io.joshuasalcedo.valueobject.core.ValueObjectException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A file or data size: a non-negative amount of a {@link DataUnit}. The value is kept as given. Conversions are
 * exact when the result has a finite decimal expansion, else rounded half-even to 34 significant digits
 * ({@link java.math.MathContext#DECIMAL128}). Two sizes are equal, and compare, by their exact amount in bytes,
 * whatever their units and however many trailing zeros they carry.
 *
 * <pre>{@code
 * FileSize s = FileSize.bytes(1_572_864);
 * s.toHumanString();         // "1.50 MiB"
 * s.toMegabytes();           // 1.572864 MB
 * }</pre>
 *
 * @param value the amount of the unit, zero or more
 * @param unit  the unit
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record FileSize(
        BigDecimal value,
        DataUnit unit
) implements ValueObject, Measurable<FileSize, DataUnit>, Comparable<FileSize> {

    private static final RoundingMode ROUNDING = RoundingMode.HALF_EVEN;

    /** Zero: {@code new FileSize(BigDecimal.ZERO, DataUnit.BYTE)}. */
    public static final FileSize ZERO = new FileSize(BigDecimal.ZERO, DataUnit.BYTE);

    /**
     * Creates a file size. The value is kept as given, without rounding.
     *
     * @param value the amount of the unit, zero or more
     * @param unit  the unit
     * @throws ValueObjectException if either is {@code null}, or the value is negative
     */
    public FileSize {
        if (value == null) throw new ValueObjectException("FileSize value cannot be null");
        if (unit == null) throw new ValueObjectException("FileSize unit cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("FileSize cannot be negative: " + value);
        }
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a file size.
     *
     * @param value the amount of the unit, zero or more
     * @param unit  the unit
     * @return the file size
     * @throws ValueObjectException if either is {@code null}, or the value is negative
     */
    public static FileSize of(BigDecimal value, DataUnit unit) {
        return new FileSize(value, unit);
    }

    /**
     * Creates a file size from a {@code double}, converted through its decimal string form.
     *
     * @param value the amount of the unit, zero or more
     * @param unit  the unit
     * @return the file size
     * @throws ValueObjectException if the unit is {@code null}, or the value is negative, NaN or infinite
     */
    public static FileSize of(double value, DataUnit unit) {
        return new FileSize(decimal(value), unit);
    }

    /**
     * Creates a file size from a whole amount of the unit.
     *
     * @param value the amount of the unit, zero or more
     * @param unit  the unit
     * @return the file size
     * @throws ValueObjectException if the unit is {@code null}, or the value is negative
     */
    public static FileSize of(long value, DataUnit unit) {
        return new FileSize(BigDecimal.valueOf(value), unit);
    }

    /**
     * Returns a zero file size, in bytes.
     *
     * @return {@link #ZERO}
     */
    public static FileSize zero() {
        return ZERO;
    }

    // Convenience methods for common units
    /**
     * Creates a file size in bytes.
     *
     * @param value the number of bytes, zero or more
     * @return the file size
     * @throws ValueObjectException if the value is negative
     */
    public static FileSize bytes(long value) {
        return of(value, DataUnit.BYTE);
    }

    /**
     * Creates a file size in kilobytes (1000 bytes).
     *
     * @param value the number of kilobytes (1000 bytes), zero or more
     * @return the file size
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static FileSize kilobytes(double value) {
        return of(value, DataUnit.KILOBYTE);
    }

    /**
     * Creates a file size in megabytes (1000^2 bytes).
     *
     * @param value the number of megabytes (1000^2 bytes), zero or more
     * @return the file size
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static FileSize megabytes(double value) {
        return of(value, DataUnit.MEGABYTE);
    }

    /**
     * Creates a file size in gigabytes (1000^3 bytes).
     *
     * @param value the number of gigabytes (1000^3 bytes), zero or more
     * @return the file size
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static FileSize gigabytes(double value) {
        return of(value, DataUnit.GIGABYTE);
    }

    /**
     * Creates a file size in terabytes (1000^4 bytes).
     *
     * @param value the number of terabytes (1000^4 bytes), zero or more
     * @return the file size
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static FileSize terabytes(double value) {
        return of(value, DataUnit.TERABYTE);
    }

    /**
     * Creates a file size in kibibytes (1024 bytes).
     *
     * @param value the number of kibibytes (1024 bytes), zero or more
     * @return the file size
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static FileSize kibibytes(double value) {
        return of(value, DataUnit.KIBIBYTE);
    }

    /**
     * Creates a file size in mebibytes (1024^2 bytes).
     *
     * @param value the number of mebibytes (1024^2 bytes), zero or more
     * @return the file size
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static FileSize mebibytes(double value) {
        return of(value, DataUnit.MEBIBYTE);
    }

    /**
     * Creates a file size in gibibytes (1024^3 bytes).
     *
     * @param value the number of gibibytes (1024^3 bytes), zero or more
     * @return the file size
     * @throws ValueObjectException if the value is negative, NaN or infinite
     */
    public static FileSize gibibytes(double value) {
        return of(value, DataUnit.GIBIBYTE);
    }

    // ==================== Conversion Methods ====================

    /**
     * Converts to another unit, exactly when the result has a finite decimal expansion, else rounded half-even to 34
     * significant digits ({@link java.math.MathContext#DECIMAL128}), with trailing zeros dropped. The same unit returns this file size
     * unchanged.
     *
     * @param targetUnit the unit to convert to
     * @return the file size in {@code targetUnit}
     * @throws ValueObjectException if the unit is {@code null}
     */
    @Override
    public FileSize convertTo(DataUnit targetUnit) {
        if (targetUnit == null) throw new ValueObjectException("Target unit cannot be null");
        if (this.unit == targetUnit) {
            return this;
        }
        return new FileSize(unit.convertTo(value, targetUnit), targetUnit);
    }

    /**
     * Converts to bytes, exactly where it can be (else to 34 significant digits); one already in bytes is returned as is.
     *
     * @return the file size in bytes
     */
    public FileSize toBytes() {
        return convertTo(DataUnit.BYTE);
    }

    /**
     * Converts to kilobytes (1000 bytes), exactly where it can be (else to 34 significant digits); one already in kilobytes is returned as is.
     *
     * @return the file size in kilobytes
     */
    public FileSize toKilobytes() {
        return convertTo(DataUnit.KILOBYTE);
    }

    /**
     * Converts to megabytes (1000^2 bytes), exactly where it can be (else to 34 significant digits); one already in megabytes is returned as is.
     *
     * @return the file size in megabytes
     */
    public FileSize toMegabytes() {
        return convertTo(DataUnit.MEGABYTE);
    }

    /**
     * Converts to gigabytes (1000^3 bytes), exactly where it can be (else to 34 significant digits); one already in gigabytes is returned as is.
     *
     * @return the file size in gigabytes
     */
    public FileSize toGigabytes() {
        return convertTo(DataUnit.GIGABYTE);
    }

    /**
     * Converts to kibibytes (1024 bytes), exactly where it can be (else to 34 significant digits); one already in kibibytes is returned as is.
     *
     * @return the file size in kibibytes
     */
    public FileSize toKibibytes() {
        return convertTo(DataUnit.KIBIBYTE);
    }

    /**
     * Converts to mebibytes (1024^2 bytes), exactly where it can be (else to 34 significant digits); one already in mebibytes is returned as is.
     *
     * @return the file size in mebibytes
     */
    public FileSize toMebibytes() {
        return convertTo(DataUnit.MEBIBYTE);
    }

    /**
     * Converts to gibibytes (1024^3 bytes), exactly where it can be (else to 34 significant digits); one already in gibibytes is returned as is.
     *
     * @return the file size in gibibytes
     */
    public FileSize toGibibytes() {
        return convertTo(DataUnit.GIBIBYTE);
    }

    // ==================== Arithmetic Operations ====================

    /**
     * Adds another file size: the exact sum in bytes, converted to this one's unit (exact where it can be, else to 34
     * significant digits).
     *
     * @param other the file size to add
     * @return the sum, in this file size's unit
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    @Override
    public FileSize add(FileSize other) {
        if (other == null) throw new ValueObjectException("Cannot add null");
        return new FileSize(fromBase(inBase().add(other.inBase()), unit), this.unit);
    }

    /**
     * Subtracts another file size: the exact difference in bytes, converted to this one's unit (exact where it can be,
     * else to 34 significant digits).
     *
     * @param other the file size to subtract
     * @return the difference, in this file size's unit
     * @throws ValueObjectException if {@code other} is {@code null}, or the result would be negative
     */
    @Override
    public FileSize subtract(FileSize other) {
        if (other == null) throw new ValueObjectException("Cannot subtract null");
        BigDecimal result = fromBase(inBase().subtract(other.inBase()), unit);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("Result would be negative");
        }
        return new FileSize(result, this.unit);
    }

    /**
     * Multiplies by a factor, without rounding.
     *
     * @param multiplier the factor, zero or more
     * @return the product, in this file size's unit
     * @throws ValueObjectException if the factor is {@code null} or negative
     */
    @Override
    public FileSize multiply(BigDecimal multiplier) {
        if (multiplier == null) throw new ValueObjectException("Multiplier cannot be null");
        if (multiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValueObjectException("Multiplier cannot be negative");
        }
        return new FileSize(value.multiply(multiplier), unit);
    }

    /**
     * Multiplies by a factor, without rounding.
     *
     * @param multiplier the factor, zero or more
     * @return the product, in this file size's unit
     * @throws ValueObjectException if the factor is negative, NaN or infinite
     */
    public FileSize multiply(double multiplier) {
        return multiply(decimal(multiplier));
    }

    /**
     * Divides by a divisor, exactly when the result has a finite decimal expansion, else rounded half-even to 34
     * significant digits ({@link java.math.MathContext#DECIMAL128}).
     *
     * @param divisor the divisor, above zero
     * @return the quotient, in this file size's unit
     * @throws ValueObjectException if the divisor is {@code null}, zero or negative
     */
    @Override
    public FileSize divide(BigDecimal divisor) {
        if (divisor == null || divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValueObjectException("Divisor must be positive");
        }
        return new FileSize(exactOrDecimal128(value, divisor), unit);
    }

    /**
     * Divides by a divisor, as {@link #divide(BigDecimal)}.
     *
     * @param divisor the divisor, above zero
     * @return the quotient, in this file size's unit
     * @throws ValueObjectException if the divisor is zero, negative, NaN or infinite
     */
    public FileSize divide(double divisor) {
        return divide(decimal(divisor));
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

    /**
     * Compares by the exact amount in bytes, whatever the units.
     *
     * @param other the file size to compare to
     * @return negative, zero or positive as this is less than, equal to or
     *         more than {@code other}
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    @Override
    public int compareTo(FileSize other) {
        if (other == null) throw new ValueObjectException("Cannot compare with null");
        return inBase().compareTo(other.inBase());
    }

    /**
     * Tells whether this file size is more than another, whatever their units.
     *
     * @param other the file size to compare to
     * @return whether this is greater than {@code other}
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    public boolean isGreaterThan(FileSize other) {
        return compareTo(other) > 0;
    }

    /**
     * Tells whether this file size is less than another, whatever their units.
     *
     * @param other the file size to compare to
     * @return whether this is less than {@code other}
     * @throws ValueObjectException if {@code other} is {@code null}
     */
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

    /**
     * Formats with 2 decimal places, as {@link #formatted()}.
     *
     * @return the value and unit symbol
     */
    @Override
    public String toString() {
        return formatted();
    }

    /**
     * Tells whether another object is a file size of the same amount, compared exactly in bytes, so 1 of a
     * unit equals its equivalent in another unit, and trailing zeros do not matter.
     *
     * @param o the object to compare to
     * @return whether it is an equal file size
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileSize fileSize)) return false;
        return inBase().compareTo(fileSize.inBase()) == 0;
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}: of the amount in bytes.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return inBase().stripTrailingZeros().hashCode();
    }

    private BigDecimal inBase() {
        return value.multiply(unit.toBaseUnitFactor());
    }

    private static BigDecimal fromBase(BigDecimal inBase, DataUnit unit) {
        return exactOrDecimal128(inBase, unit.toBaseUnitFactor());
    }

    private static BigDecimal exactOrDecimal128(BigDecimal dividend, BigDecimal divisor) {
        BigDecimal q;
        try {
            q = dividend.divide(divisor);
        } catch (ArithmeticException nonTerminating) {
            q = dividend.divide(divisor, java.math.MathContext.DECIMAL128);
        }
        q = q.stripTrailingZeros();
        return q.scale() < 0 ? q.setScale(0) : q;
    }

    private static BigDecimal decimal(double d) {
        if (Double.isNaN(d) || Double.isInfinite(d)) throw new ValueObjectException("Not a finite number: " + d);
        return BigDecimal.valueOf(d);
    }

    /**
     * Reads a file size printed as an amount and a unit, e.g. {@code "1.5 GB"}: the amount may have thousands commas, the
     * space before the unit is optional, and the unit is anything {@link DataUnit#parse} reads.
     *
     * @param text the text
     * @return the file size
     * @throws ValueObjectException if it is not an amount followed by a known unit
     */
    public static FileSize parse(String text) {
        if (text == null) throw new ValueObjectException("FileSize text is required");
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("^\\s*([-+]?(?:\\d{1,3}(?:,\\d{3})+|\\d+)(?:\\.\\d+)?)\\s*(\\S.*?)\\s*$").matcher(text);
        if (!m.matches()) throw new ValueObjectException("Not a file size: " + text);
        return of(new BigDecimal(m.group(1).replace(",", "")), DataUnit.parse(m.group(2)));
    }

    /**
     * Reads a file size printed as an amount and a unit, if it is one.
     *
     * @param text the text, may be null
     * @return the file size, or empty
     */
    public static java.util.Optional<FileSize> tryParse(@Nullable String text) {
        if (text == null) return java.util.Optional.empty();
        try {
            return java.util.Optional.of(parse(text));
        } catch (ValueObjectException e) {
            return java.util.Optional.empty();
        }
    }

    /**
     * Tells whether text is a file size printed as an amount and a unit.
     *
     * @param text the text, may be null
     * @return whether {@link #parse} reads it
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }
}
