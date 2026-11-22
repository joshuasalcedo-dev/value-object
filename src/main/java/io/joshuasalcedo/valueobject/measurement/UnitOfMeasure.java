package io.joshuasalcedo.valueobject.measurement;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * A unit of measure (UOM), by its <b>UN/ECE Recommendation 20</b> code: the code list e-invoicing uses (UBL, Peppol,
 * EN 16931 {@code unitCode}). Packaging units are UN/ECE Recommendation 21 codes with Rec 20's {@code X} prefix
 * ({@code XBX} box, {@code XPK} package).
 *
 * <p>Each unit knows the spellings documents print ("pcs", "Nos", "ea", "hrs", "kgs"), its {@link Category}, and, where
 * units of a category convert, its factor to the category's base unit (kilogram, metre, square metre, litre, second,
 * byte, piece). Months, years and packaging units do not convert: a box holds whatever it holds.
 *
 * <pre>{@code
 * UnitOfMeasure.parse("pcs");     // PIECE  (H87)
 * UnitOfMeasure.parse("KGM");     // KILOGRAM
 * UnitOfMeasure.KILOGRAM.convert(new BigDecimal("2.5"), UnitOfMeasure.GRAM);   // 2500
 * UnitOfMeasure.DOZEN.convert(BigDecimal.ONE, UnitOfMeasure.PIECE);            // 12
 * }</pre>
 *
 * @see <a href="https://unece.org/trade/uncefact/cl-recommendations">UN/CEFACT code list recommendations 20 and 21</a>
 * @author JoshuaSalcedo
 */
public enum UnitOfMeasure implements MeasurementUnit<UnitOfMeasure> {

    // ── Count ───────────────────────────────────────────────────────────────
    ONE("C62", "unit", "one", Category.COUNT, "1", "unit", "units", "u"),
    PIECE("H87", "pc", "piece", Category.COUNT, "1", "pc", "pcs", "piece", "pieces", "nos", "no", "nr", "pce"),
    EACH("EA", "ea", "each", Category.COUNT, "1", "ea", "each"),
    PAIR("PR", "pr", "pair", Category.COUNT, null, "pr", "pair", "pairs"),
    SET("SET", "set", "set", Category.COUNT, null, "set", "sets"),
    DOZEN("DZN", "doz", "dozen", Category.COUNT, "12", "doz", "dozen", "dzn"),
    GROSS("GRO", "gr", "gross", Category.COUNT, "144", "gross", "gro"),
    LOT("LO", "lot", "lot", Category.COUNT, null, "lot", "lots"),

    // ── Packaging (Rec 21, X-prefixed) ──────────────────────────────────────
    BOX("XBX", "box", "box", Category.PACKAGE, null, "box", "boxes", "bx"),
    PACKAGE("XPK", "pack", "package", Category.PACKAGE, null, "pack", "packs", "pk", "pkg", "package", "packages"),
    PACKET("XPA", "pkt", "packet", Category.PACKAGE, null, "pkt", "packet", "packets"),
    BAG("XBG", "bag", "bag", Category.PACKAGE, null, "bag", "bags"),
    BOTTLE("XBO", "btl", "bottle", Category.PACKAGE, null, "btl", "bottle", "bottles"),
    CARTON("XCT", "ctn", "carton", Category.PACKAGE, null, "ctn", "carton", "cartons"),
    CASE("XCS", "case", "case", Category.PACKAGE, null, "case", "cases", "cs"),
    CAN("XCX", "can", "can", Category.PACKAGE, null, "can", "cans"),
    ROLL("XRO", "roll", "roll", Category.PACKAGE, null, "roll", "rolls", "rl"),
    BUNDLE("XBE", "bdl", "bundle", Category.PACKAGE, null, "bdl", "bundle", "bundles"),
    PALLET("XPX", "plt", "pallet", Category.PACKAGE, null, "plt", "pallet", "pallets"),
    TUBE("XTU", "tube", "tube", Category.PACKAGE, null, "tube", "tubes"),

    // ── Weight (base: kilogram) ─────────────────────────────────────────────
    MILLIGRAM("MGM", "mg", "milligram", Category.WEIGHT, "0.000001", "mg", "milligram", "milligrams"),
    GRAM("GRM", "g", "gram", Category.WEIGHT, "0.001", "g", "gr", "gram", "grams", "gm", "gms"),
    KILOGRAM("KGM", "kg", "kilogram", Category.WEIGHT, "1", "kg", "kgs", "kilo", "kilos", "kilogram", "kilograms"),
    TONNE("TNE", "t", "tonne", Category.WEIGHT, "1000", "t", "tonne", "tonnes", "metric ton", "mt"),
    OUNCE("ONZ", "oz", "ounce", Category.WEIGHT, "0.028349523125", "oz", "ounce", "ounces"),
    POUND("LBR", "lb", "pound", Category.WEIGHT, "0.45359237", "lb", "lbs", "pound", "pounds"),
    STONE("STI", "st", "stone", Category.WEIGHT, "6.35029318", "stone"),

    // ── Length (base: metre) ────────────────────────────────────────────────
    MILLIMETRE("MMT", "mm", "millimetre", Category.LENGTH, "0.001", "mm", "millimeter", "millimetre", "millimeters"),
    CENTIMETRE("CMT", "cm", "centimetre", Category.LENGTH, "0.01", "cm", "centimeter", "centimetre", "centimeters"),
    METRE("MTR", "m", "metre", Category.LENGTH, "1", "m", "meter", "metre", "meters", "metres", "mtr", "mtrs"),
    KILOMETRE("KMT", "km", "kilometre", Category.LENGTH, "1000", "km", "kilometer", "kilometre", "kilometers"),
    INCH("INH", "in", "inch", Category.LENGTH, "0.0254", "in", "inch", "inches"),
    FOOT("FOT", "ft", "foot", Category.LENGTH, "0.3048", "ft", "foot", "feet"),
    YARD("YRD", "yd", "yard", Category.LENGTH, "0.9144", "yd", "yard", "yards"),
    MILE("SMI", "mi", "mile", Category.LENGTH, "1609.344", "mi", "mile", "miles"),

    // ── Area (base: square metre) ───────────────────────────────────────────
    SQUARE_CENTIMETRE("CMK", "cm²", "square centimetre", Category.AREA, "0.0001", "cm2", "sq cm", "sqcm"),
    SQUARE_METRE("MTK", "m²", "square metre", Category.AREA, "1", "m2", "sqm", "sq m", "sq.m", "square meter", "square metre"),
    SQUARE_FOOT("FTK", "ft²", "square foot", Category.AREA, "0.09290304", "ft2", "sqft", "sq ft", "square feet"),
    HECTARE("HAR", "ha", "hectare", Category.AREA, "10000", "ha", "hectare", "hectares"),
    ACRE("ACR", "ac", "acre", Category.AREA, "4046.8564224", "acre", "acres"),

    // ── Volume (base: litre) ────────────────────────────────────────────────
    MILLILITRE("MLT", "mL", "millilitre", Category.VOLUME, "0.001", "ml", "milliliter", "millilitre", "milliliters", "cc"),
    CENTILITRE("CLT", "cL", "centilitre", Category.VOLUME, "0.01", "cl", "centiliter", "centilitre"),
    LITRE("LTR", "L", "litre", Category.VOLUME, "1", "l", "ltr", "ltrs", "liter", "litre", "liters", "litres", "lt"),
    CUBIC_METRE("MTQ", "m³", "cubic metre", Category.VOLUME, "1000", "m3", "cu m", "cbm", "cubic meter", "cubic metre", "cu.m"),
    US_GALLON("GLL", "gal", "US gallon", Category.VOLUME, "3.785411784", "gal", "gallon", "gallons"),
    US_FLUID_OUNCE("OZA", "fl oz", "US fluid ounce", Category.VOLUME, "0.0295735295625", "fl oz", "floz", "fl. oz"),

    // ── Time (base: second; months and years do not convert) ───────────────
    SECOND("SEC", "s", "second", Category.TIME, "1", "s", "sec", "secs", "second", "seconds"),
    MINUTE("MIN", "min", "minute", Category.TIME, "60", "min", "mins", "minute", "minutes"),
    HOUR("HUR", "h", "hour", Category.TIME, "3600", "h", "hr", "hrs", "hour", "hours"),
    DAY("DAY", "d", "day", Category.TIME, "86400", "day", "days"),
    WEEK("WEE", "wk", "week", Category.TIME, "604800", "wk", "wks", "week", "weeks"),
    MONTH("MON", "mo", "month", Category.TIME, null, "mo", "mos", "month", "months", "mth", "mths"),
    YEAR("ANN", "y", "year", Category.TIME, null, "yr", "yrs", "year", "years"),

    // ── Data (base: byte; decimal multiples, as Rec 20 defines them) ────────
    BYTE("AD", "B", "byte", Category.DATA, "1", "byte", "bytes"),
    KILOBYTE("2P", "kB", "kilobyte", Category.DATA, "1000", "kb", "kilobyte", "kilobytes"),
    MEGABYTE("4L", "MB", "megabyte", Category.DATA, "1000000", "mb", "megabyte", "megabytes"),
    GIGABYTE("E34", "GB", "gigabyte", Category.DATA, "1000000000", "gb", "gigabyte", "gigabytes"),
    TERABYTE("E35", "TB", "terabyte", Category.DATA, "1000000000000", "tb", "terabyte", "terabytes"),

    // ── Energy (base: kilowatt hour) ────────────────────────────────────────
    KILOWATT_HOUR("KWH", "kWh", "kilowatt hour", Category.ENERGY, "1", "kwh", "kw-h", "kilowatt hour", "kilowatt hours");

    /** What a unit measures; units convert only within one. */
    public enum Category { COUNT, PACKAGE, WEIGHT, LENGTH, AREA, VOLUME, TIME, DATA, ENERGY }

    private static final Map<String, UnitOfMeasure> BY_CODE = new HashMap<>();
    private static final Map<String, UnitOfMeasure> BY_ALIAS = new HashMap<>();

    static {
        for (UnitOfMeasure u : values()) {
            BY_CODE.put(u.code, u);
            for (String a : u.aliases) BY_ALIAS.putIfAbsent(a, u);
        }
    }

    private final String code;
    private final String symbol;
    private final String unitName;
    private final Category category;
    private final @Nullable BigDecimal factor;
    private final List<String> aliases;

    UnitOfMeasure(String code, String symbol, String unitName, Category category, @Nullable String factor, String... aliases) {
        this.code = code;
        this.symbol = symbol;
        this.unitName = unitName;
        this.category = category;
        this.factor = factor == null ? null : new BigDecimal(factor);
        this.aliases = List.of(aliases);
    }

    /**
     * @param code a UN/ECE Rec 20 code, e.g. {@code "KGM"}
     * @return that unit, or empty
     */
    public static Optional<UnitOfMeasure> fromCode(@Nullable String code) {
        return code == null ? Optional.empty() : Optional.ofNullable(BY_CODE.get(code.strip().toUpperCase(Locale.ROOT)));
    }

    /**
     * @param text a Rec 20 code ({@code "KGM"}) or a unit as printed ({@code "kgs"}, {@code "Nos"}, {@code "hrs."})
     * @return the unit
     * @throws ValueObjectException if it is not a unit this knows
     */
    public static UnitOfMeasure parse(String text) {
        return find(text).orElseThrow(() -> new ValueObjectException("Not a unit of measure: '" + text + "'"));
    }

    /**
     * @param text a Rec 20 code or a unit as printed
     * @return the unit, or empty
     */
    public static Optional<UnitOfMeasure> find(@Nullable String text) {
        if (text == null || text.isBlank()) return Optional.empty();
        String t = text.strip();
        UnitOfMeasure byCode = BY_CODE.get(t);
        if (byCode != null) return Optional.of(byCode);
        String a = t.toLowerCase(Locale.ROOT).replace("²", "2").replace("³", "3").replaceAll("\\.$", "").replaceAll("\\s+", " ");
        UnitOfMeasure u = BY_ALIAS.get(a);
        if (u == null) u = BY_ALIAS.get(a.replace(".", ""));
        if (u == null && a.length() > 3 && a.endsWith("s")) u = BY_ALIAS.get(a.substring(0, a.length() - 1));
        if (u == null) u = BY_CODE.get(t.toUpperCase(Locale.ROOT));
        return Optional.ofNullable(u);
    }

    /** @return its UN/ECE Rec 20 code, e.g. {@code "KGM"}: the {@code unitCode} of a UBL invoice line */
    public String code() {
        return code;
    }

    /** @return its printed symbol, e.g. {@code "kg"} */
    @Override
    public String symbol() {
        return symbol;
    }

    /** @return its name, e.g. {@code "kilogram"} */
    @Override
    public String unitName() {
        return unitName;
    }

    /** @return what it measures */
    public Category category() {
        return category;
    }

    /** @return whether amounts in it convert to the other units of its category */
    public boolean convertible() {
        return factor != null;
    }

    /** @return its size in its category's base unit; 0 when it does not convert (months, boxes) */
    @Override
    public BigDecimal toBaseUnitFactor() {
        return factor == null ? BigDecimal.ZERO : factor;
    }

    /** @return its category's base unit (kilogram, metre, litre, ...) */
    @Override
    public UnitOfMeasure baseUnit() {
        return switch (category) {
            case COUNT -> PIECE;
            case PACKAGE -> this;
            case WEIGHT -> KILOGRAM;
            case LENGTH -> METRE;
            case AREA -> SQUARE_METRE;
            case VOLUME -> LITRE;
            case TIME -> SECOND;
            case DATA -> BYTE;
            case ENERGY -> KILOWATT_HOUR;
        };
    }

    /**
     * @param amount an amount in this unit
     * @param target another unit of the same category
     * @return the amount in {@code target}
     * @throws ValueObjectException if the units do not convert (other category, months, packaging)
     */
    public BigDecimal convert(BigDecimal amount, UnitOfMeasure target) {
        if (this == target) return amount;
        if (category != target.category || factor == null || target.factor == null) {
            throw new ValueObjectException("Cannot convert " + unitName + " to " + target.unitName);
        }
        BigDecimal out = amount.multiply(factor).divide(target.factor, 12, RoundingMode.HALF_EVEN).stripTrailingZeros();
        return out.scale() < 0 ? out.setScale(0) : out;
    }

    /** {@inheritDoc} Checks the units convert first. */
    @Override
    public BigDecimal convertTo(BigDecimal value, UnitOfMeasure targetUnit) {
        return convert(value, targetUnit);
    }

    /** @return the matching {@link WeightUnit}, for weights */
    public Optional<WeightUnit> weightUnit() {
        return Optional.ofNullable(switch (this) {
            case MILLIGRAM -> WeightUnit.MILLIGRAM;
            case GRAM -> WeightUnit.GRAM;
            case KILOGRAM -> WeightUnit.KILOGRAM;
            case TONNE -> WeightUnit.METRIC_TON;
            case OUNCE -> WeightUnit.OUNCE;
            case POUND -> WeightUnit.POUND;
            case STONE -> WeightUnit.STONE;
            default -> null;
        });
    }

    /** @return the matching {@link DistanceUnit}, for lengths */
    public Optional<DistanceUnit> distanceUnit() {
        return Optional.ofNullable(switch (this) {
            case MILLIMETRE -> DistanceUnit.MILLIMETER;
            case CENTIMETRE -> DistanceUnit.CENTIMETER;
            case METRE -> DistanceUnit.METER;
            case KILOMETRE -> DistanceUnit.KILOMETER;
            case INCH -> DistanceUnit.INCH;
            case FOOT -> DistanceUnit.FOOT;
            case YARD -> DistanceUnit.YARD;
            case MILE -> DistanceUnit.MILE;
            default -> null;
        });
    }

    /** @return the matching {@link DataUnit}, for data sizes */
    public Optional<DataUnit> dataUnit() {
        return Optional.ofNullable(switch (this) {
            case BYTE -> DataUnit.BYTE;
            case KILOBYTE -> DataUnit.KILOBYTE;
            case MEGABYTE -> DataUnit.MEGABYTE;
            case GIGABYTE -> DataUnit.GIGABYTE;
            case TERABYTE -> DataUnit.TERABYTE;
            default -> null;
        });
    }

    /** @return its symbol */
    @Override
    public String toString() {
        return symbol;
    }
}
