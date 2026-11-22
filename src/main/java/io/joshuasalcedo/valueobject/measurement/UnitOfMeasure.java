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
    /** one ({@code C62}): a plain count with no unit printed. */
    ONE("C62", "unit", "one", Category.COUNT, "1", "unit", "units", "u"),
    /** piece ({@code H87}). */
    PIECE("H87", "pc", "piece", Category.COUNT, "1", "pc", "pcs", "piece", "pieces", "nos", "no", "nr", "pce"),
    /** each ({@code EA}). */
    EACH("EA", "ea", "each", Category.COUNT, "1", "ea", "each"),
    /** pair ({@code PR}). */
    PAIR("PR", "pr", "pair", Category.COUNT, null, "pr", "pair", "pairs"),
    /** sets ({@code SET}). */
    SET("SET", "set", "set", Category.COUNT, null, "set", "sets"),
    /** dozen ({@code DZN}). */
    DOZEN("DZN", "doz", "dozen", Category.COUNT, "12", "doz", "dozen", "dzn"),
    /** gross ({@code GRO}). */
    GROSS("GRO", "gross", "gross", Category.COUNT, "144", "gross", "gro"),
    /** lots ({@code LO}). */
    LOT("LO", "lot", "lot", Category.COUNT, null, "lot", "lots"),

    // ── Packaging (Rec 21, X-prefixed) ──────────────────────────────────────
    /** box ({@code XBX}). */
    BOX("XBX", "box", "box", Category.PACKAGE, null, "box", "boxes", "bx"),
    /** pack ({@code XPK}). */
    PACKAGE("XPK", "pack", "package", Category.PACKAGE, null, "pack", "packs", "pk", "pkg", "package", "packages"),
    /** packet ({@code XPA}). */
    PACKET("XPA", "pkt", "packet", Category.PACKAGE, null, "pkt", "packet", "packets"),
    /** bags ({@code XBG}). */
    BAG("XBG", "bag", "bag", Category.PACKAGE, null, "bag", "bags"),
    /** bottle ({@code XBO}). */
    BOTTLE("XBO", "btl", "bottle", Category.PACKAGE, null, "btl", "bottle", "bottles"),
    /** carton ({@code XCT}). */
    CARTON("XCT", "ctn", "carton", Category.PACKAGE, null, "ctn", "carton", "cartons"),
    /** case ({@code XCS}). */
    CASE("XCS", "case", "case", Category.PACKAGE, null, "case", "cases", "cs"),
    /** cans ({@code XCX}). */
    CAN("XCX", "can", "can", Category.PACKAGE, null, "can", "cans"),
    /** roll ({@code XRO}). */
    ROLL("XRO", "roll", "roll", Category.PACKAGE, null, "roll", "rolls", "rl"),
    /** bundle ({@code XBE}). */
    BUNDLE("XBE", "bdl", "bundle", Category.PACKAGE, null, "bdl", "bundle", "bundles"),
    /** pallet ({@code XPX}). */
    PALLET("XPX", "plt", "pallet", Category.PACKAGE, null, "plt", "pallet", "pallets"),
    /** tube ({@code XTU}). */
    TUBE("XTU", "tube", "tube", Category.PACKAGE, null, "tube", "tubes"),

    // ── Weight (base: kilogram) ─────────────────────────────────────────────
    /** milligram ({@code MGM}). */
    MILLIGRAM("MGM", "mg", "milligram", Category.WEIGHT, "0.000001", "mg", "milligram", "milligrams"),
    /** gram ({@code GRM}). */
    GRAM("GRM", "g", "gram", Category.WEIGHT, "0.001", "g", "gr", "gram", "grams", "gm", "gms"),
    /** kilogram ({@code KGM}). */
    KILOGRAM("KGM", "kg", "kilogram", Category.WEIGHT, "1", "kg", "kgs", "kilo", "kilos", "kilogram", "kilograms"),
    /** tonne ({@code TNE}). */
    TONNE("TNE", "t", "tonne", Category.WEIGHT, "1000", "t", "tonne", "tonnes", "metric ton", "mt"),
    /** ounce ({@code ONZ}). */
    OUNCE("ONZ", "oz", "ounce", Category.WEIGHT, "0.028349523125", "oz", "ounce", "ounces"),
    /** pound ({@code LBR}). */
    POUND("LBR", "lb", "pound", Category.WEIGHT, "0.45359237", "lb", "lbs", "pound", "pounds"),
    /** stone ({@code STI}). */
    STONE("STI", "st", "stone", Category.WEIGHT, "6.35029318", "stone"),

    // ── Length (base: metre) ────────────────────────────────────────────────
    /** millimetre ({@code MMT}). */
    MILLIMETRE("MMT", "mm", "millimetre", Category.LENGTH, "0.001", "mm", "millimeter", "millimetre", "millimeters"),
    /** centimetre ({@code CMT}). */
    CENTIMETRE("CMT", "cm", "centimetre", Category.LENGTH, "0.01", "cm", "centimeter", "centimetre", "centimeters"),
    /** metre ({@code MTR}). */
    METRE("MTR", "m", "metre", Category.LENGTH, "1", "m", "meter", "metre", "meters", "metres", "mtr", "mtrs"),
    /** kilometre ({@code KMT}). */
    KILOMETRE("KMT", "km", "kilometre", Category.LENGTH, "1000", "km", "kilometer", "kilometre", "kilometers"),
    /** inch ({@code INH}). */
    INCH("INH", "in", "inch", Category.LENGTH, "0.0254", "in", "inch", "inches"),
    /** foot ({@code FOT}). */
    FOOT("FOT", "ft", "foot", Category.LENGTH, "0.3048", "ft", "foot", "feet"),
    /** yard ({@code YRD}). */
    YARD("YRD", "yd", "yard", Category.LENGTH, "0.9144", "yd", "yard", "yards"),
    /** mile ({@code SMI}). */
    MILE("SMI", "mi", "mile", Category.LENGTH, "1609.344", "mi", "mile", "miles"),

    // ── Area (base: square metre) ───────────────────────────────────────────
    /** square centimetre ({@code CMK}). */
    SQUARE_CENTIMETRE("CMK", "cm²", "square centimetre", Category.AREA, "0.0001", "cm2", "sq cm", "sqcm"),
    /** square metre ({@code MTK}). */
    SQUARE_METRE("MTK", "m²", "square metre", Category.AREA, "1", "m2", "sqm", "sq m", "sq.m", "square meter", "square metre"),
    /** square foot ({@code FTK}). */
    SQUARE_FOOT("FTK", "ft²", "square foot", Category.AREA, "0.09290304", "ft2", "sqft", "sq ft", "square feet"),
    /** hectare ({@code HAR}). */
    HECTARE("HAR", "ha", "hectare", Category.AREA, "10000", "ha", "hectare", "hectares"),
    /** acre ({@code ACR}). */
    ACRE("ACR", "ac", "acre", Category.AREA, "4046.8564224", "acre", "acres"),

    // ── Volume (base: litre) ────────────────────────────────────────────────
    /** millilitre ({@code MLT}). */
    MILLILITRE("MLT", "mL", "millilitre", Category.VOLUME, "0.001", "ml", "milliliter", "millilitre", "milliliters", "cc"),
    /** centilitre ({@code CLT}). */
    CENTILITRE("CLT", "cL", "centilitre", Category.VOLUME, "0.01", "cl", "centiliter", "centilitre"),
    /** litre ({@code LTR}). */
    LITRE("LTR", "L", "litre", Category.VOLUME, "1", "l", "ltr", "ltrs", "liter", "litre", "liters", "litres", "lt"),
    /** cubic metre ({@code MTQ}). */
    CUBIC_METRE("MTQ", "m³", "cubic metre", Category.VOLUME, "1000", "m3", "cu m", "cbm", "cubic meter", "cubic metre", "cu.m"),
    /** US gallon ({@code GLL}). */
    US_GALLON("GLL", "gal", "US gallon", Category.VOLUME, "3.785411784", "gal", "gallon", "gallons"),
    /** fl oz ({@code OZA}). */
    US_FLUID_OUNCE("OZA", "fl oz", "US fluid ounce", Category.VOLUME, "0.0295735295625", "fl oz", "floz", "fl. oz"),

    // ── Time (base: second; months and years do not convert) ───────────────
    /** second ({@code SEC}). */
    SECOND("SEC", "s", "second", Category.TIME, "1", "s", "sec", "secs", "second", "seconds"),
    /** minute ({@code MIN}). */
    MINUTE("MIN", "min", "minute", Category.TIME, "60", "min", "mins", "minute", "minutes"),
    /** hour ({@code HUR}). */
    HOUR("HUR", "h", "hour", Category.TIME, "3600", "h", "hr", "hrs", "hour", "hours"),
    /** days ({@code DAY}). */
    DAY("DAY", "d", "day", Category.TIME, "86400", "day", "days"),
    /** week ({@code WEE}). */
    WEEK("WEE", "wk", "week", Category.TIME, "604800", "wk", "wks", "week", "weeks"),
    /** month ({@code MON}). */
    MONTH("MON", "mo", "month", Category.TIME, null, "mo", "mos", "month", "months", "mth", "mths"),
    /** year ({@code ANN}). */
    YEAR("ANN", "y", "year", Category.TIME, null, "yr", "yrs", "year", "years"),

    // ── Data (base: byte; decimal multiples, as Rec 20 defines them) ────────
    /** byte ({@code AD}). */
    BYTE("AD", "B", "byte", Category.DATA, "1", "byte", "bytes"),
    /** kilobyte ({@code 2P}). */
    KILOBYTE("2P", "kB", "kilobyte", Category.DATA, "1000", "kb", "kilobyte", "kilobytes"),
    /** megabyte ({@code 4L}). */
    MEGABYTE("4L", "MB", "megabyte", Category.DATA, "1000000", "mb", "megabyte", "megabytes"),
    /** gigabyte ({@code E34}). */
    GIGABYTE("E34", "GB", "gigabyte", Category.DATA, "1000000000", "gb", "gigabyte", "gigabytes"),
    /** terabyte ({@code E35}). */
    TERABYTE("E35", "TB", "terabyte", Category.DATA, "1000000000000", "tb", "terabyte", "terabytes"),

    // ── Energy (base: kilowatt hour) ────────────────────────────────────────
    /** kilowatt hour ({@code KWH}). */
    KILOWATT_HOUR("KWH", "kWh", "kilowatt hour", Category.ENERGY, "1", "kwh", "kw-h", "kilowatt hour", "kilowatt hours");

    /** What a unit measures; units convert only within one. */
    public enum Category {
        /** Counts of things; base unit {@link #PIECE}. */
        COUNT,
        /** Packaging units, which do not convert. */
        PACKAGE,
        /** Weight (mass); base unit {@link #KILOGRAM}. */
        WEIGHT,
        /** Length; base unit {@link #METRE}. */
        LENGTH,
        /** Area; base unit {@link #SQUARE_METRE}. */
        AREA,
        /** Volume; base unit {@link #LITRE}. */
        VOLUME,
        /** Time; base unit {@link #SECOND} (months and years do not convert). */
        TIME,
        /** Data size; base unit {@link #BYTE}. */
        DATA,
        /** Energy; base unit {@link #KILOWATT_HOUR}. */
        ENERGY
    }

    private static final Map<String, UnitOfMeasure> BY_CODE = new HashMap<>();
    private static final Map<String, UnitOfMeasure> BY_ALIAS = new HashMap<>();

    static {
        for (UnitOfMeasure u : values()) {
            BY_CODE.put(u.code, u);
        }
        // Every unit's own symbol reads back as that unit, so a printed quantity parses back: symbols first.
        for (UnitOfMeasure u : values()) {
            UnitOfMeasure clash = BY_ALIAS.put(spelling(u.symbol), u);
            if (clash != null) throw new IllegalStateException("Two units print " + u.symbol + ": " + clash + ", " + u.name());
        }
        for (UnitOfMeasure u : values()) {
            for (String a : u.aliases) BY_ALIAS.putIfAbsent(spelling(a), u);
        }
    }

    /** A spelling as {@link #find(String)} compares it: lower case, {@code ²}/{@code ³} as digits, no trailing dot, single spaces. */
    private static String spelling(String text) {
        return text.strip().toLowerCase(Locale.ROOT).replace("²", "2").replace("³", "3").replaceAll("\\.$", "")
                .replaceAll("\\s+", " ");
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
     * Looks up a unit by its UN/ECE Rec 20 code, ignoring case and surrounding spaces.
     *
     * @param code a UN/ECE Rec 20 code, e.g. {@code "KGM"}
     * @return that unit, or empty when the code is {@code null} or not known
     */
    public static Optional<UnitOfMeasure> fromCode(@Nullable String code) {
        return code == null ? Optional.empty() : Optional.ofNullable(BY_CODE.get(code.strip().toUpperCase(Locale.ROOT)));
    }

    /**
     * Reads a unit from its Rec 20 code or a spelling documents print, as {@link #find(String)}.
     *
     * @param text a Rec 20 code ({@code "KGM"}) or a unit as printed ({@code "kgs"}, {@code "Nos"}, {@code "hrs."})
     * @return the unit
     * @throws ValueObjectException if it is {@code null} or not a unit this knows
     */
    public static UnitOfMeasure parse(String text) {
        return find(text).orElseThrow(() -> new ValueObjectException("Not a unit of measure: '" + text + "'"));
    }

    /**
     * Finds a unit from its Rec 20 code (exact case first) or a spelling documents print. Spellings ignore case,
     * surrounding spaces, a trailing dot, dots inside, and runs of spaces; {@code ²} and {@code ³} read as
     * {@code 2} and {@code 3}; a trailing {@code s} is dropped from spellings longer than three letters; and
     * last, the code is tried in upper case. Every unit's {@link #symbol()} is found as that unit ({@code "st"} is a
     * stone, {@code "B"} a byte, {@code "gross"} a gross), and the symbols win over other spellings; {@code "gr"}
     * is a gram.
     *
     * @param text a Rec 20 code or a unit as printed
     * @return the unit, or empty when it is {@code null}, blank or not known
     */
    public static Optional<UnitOfMeasure> find(@Nullable String text) {
        if (text == null || text.isBlank()) return Optional.empty();
        String t = text.strip();
        UnitOfMeasure byCode = BY_CODE.get(t);
        if (byCode != null) return Optional.of(byCode);
        String a = spelling(t);
        UnitOfMeasure u = BY_ALIAS.get(a);
        if (u == null) u = BY_ALIAS.get(a.replace(".", ""));
        if (u == null && a.length() > 3 && a.endsWith("s")) u = BY_ALIAS.get(a.substring(0, a.length() - 1));
        if (u == null) u = BY_CODE.get(t.toUpperCase(Locale.ROOT));
        return Optional.ofNullable(u);
    }

    /**
     * Returns the UN/ECE Rec 20 code (Rec 21 codes for packaging, {@code X}-prefixed).
     *
     * @return its UN/ECE Rec 20 code, e.g. {@code "KGM"}: the {@code unitCode} of a UBL invoice line
     */
    public String code() {
        return code;
    }

    /**
     * Returns the symbol to print after an amount.
     *
     * @return its printed symbol, e.g. {@code "kg"}
     */
    @Override
    public String symbol() {
        return symbol;
    }

    /**
     * Returns the unit's name, singular.
     *
     * @return its name, e.g. {@code "kilogram"}
     */
    @Override
    public String unitName() {
        return unitName;
    }

    /**
     * Returns what the unit measures.
     *
     * @return what it measures
     */
    public Category category() {
        return category;
    }

    /**
     * Tells whether the unit has a factor to its category's base unit. Pairs, sets, lots, packaging, months
     * and years do not.
     *
     * @return whether amounts in it convert to the other units of its category
     */
    public boolean convertible() {
        return factor != null;
    }

    /**
     * Returns the factor to the category's base unit: how many base units one of this unit is.
     *
     * @return its size in its category's base unit; 0 when it does not convert (months, boxes)
     */
    @Override
    public BigDecimal toBaseUnitFactor() {
        return factor == null ? BigDecimal.ZERO : factor;
    }

    /**
     * Returns the base unit of the unit's category. A packaging unit is its own base unit; a count's is
     * {@link #PIECE}.
     *
     * @return its category's base unit (kilogram, metre, litre, ...)
     */
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
     * Converts an amount from this unit to another of its category, rounding half-even to 12 decimal places
     * and dropping trailing zeros. The same unit returns the amount unchanged, even when it does not convert.
     *
     * @param amount an amount in this unit
     * @param target another unit of the same category
     * @return the amount in {@code target}
     * @throws ValueObjectException if the amount or target is {@code null}, or the units do not convert (other
     *                              category, months, packaging)
     */
    public BigDecimal convert(BigDecimal amount, UnitOfMeasure target) {
        if (amount == null || target == null) throw new ValueObjectException("An amount and a target unit are required");
        if (this == target) return amount;
        if (category != target.category || factor == null || target.factor == null) {
            throw new ValueObjectException("Cannot convert " + unitName + " to " + target.unitName);
        }
        BigDecimal out = amount.multiply(factor).divide(target.factor, 12, RoundingMode.HALF_EVEN).stripTrailingZeros();
        return out.scale() < 0 ? out.setScale(0) : out;
    }

    /**
     * Converts an amount from this unit to another, as {@link #convert(BigDecimal, UnitOfMeasure)}: it checks
     * the units convert first, and rounds half-even to 12 decimal places.
     *
     * @param value      the amount in this unit
     * @param targetUnit another unit of the same category
     * @return the amount in {@code targetUnit}
     * @throws ValueObjectException if the amount or target is {@code null}, or the units do not convert (other
     *                              category, months, packaging)
     */
    @Override
    public BigDecimal convertTo(BigDecimal value, UnitOfMeasure targetUnit) {
        return convert(value, targetUnit);
    }

    /**
     * Returns the {@link WeightUnit} for this unit, to make a {@link Weight}.
     *
     * @return the matching {@link WeightUnit}, for weights; empty for other units
     */
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

    /**
     * Returns the {@link DistanceUnit} for this unit, to make a {@link Distance}.
     *
     * @return the matching {@link DistanceUnit}, for lengths; empty for other units
     */
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

    /**
     * Returns the {@link DataUnit} for this unit, to make a {@link FileSize}.
     *
     * @return the matching {@link DataUnit}, for data sizes; empty for other units
     */
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

    /**
     * Returns the symbol, as {@link #symbol()}.
     *
     * @return its symbol
     */
    @Override
    public String toString() {
        return symbol;
    }

    /**
     * Reads a unit like {@link #parse(String)}, without throwing.
     *
     * @param text any text
     * @return it as a {@link UnitOfMeasure}, or empty when it is not one; never throws
     */
    public static Optional<UnitOfMeasure> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether text is a unit this knows, as {@link #parse(String)} reads it.
     *
     * @param text any text
     * @return whether it is a {@link UnitOfMeasure}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
