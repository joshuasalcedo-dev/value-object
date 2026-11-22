package io.joshuasalcedo.valueobject.visual;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.joshuasalcedo.valueobject.visual.Color.Name.BLACK;
import static io.joshuasalcedo.valueobject.visual.Color.Name.WHITE;

/**
 * Value object representing a color.
 * Supports RGB, RGBA, hex, named colors, and random generation.
 *
 * @author JoshuaSalcedo
 * @since 1/24/2026
 */
public record Color(
        @Nullable Integer red,
        @Nullable Integer green,
        @Nullable Integer blue,
        @Nullable Integer alpha
) implements ValueObject {

    // Validation
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 255;

    // Patterns for parsing
    private static final Pattern HEX_3 = Pattern.compile("^#?([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])$");
    private static final Pattern HEX_6 = Pattern.compile("^#?([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})$");
    private static final Pattern HEX_8 = Pattern.compile("^#?([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})$");
    private static final Pattern RGB_PATTERN = Pattern.compile("^rgb\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*\\)$");
    private static final Pattern RGBA_PATTERN = Pattern.compile("^rgba\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*([0-9.]+)\\s*\\)$");

    // ==================== Name Enum ====================

    /**
     * CSS named colors as an enum. Use {@code Color.Name.RED.toColor()} or
     * {@code Color.of(Color.Name.RED)}.
     */
    public enum Name {
        BLACK(0, 0, 0),
        WHITE(255, 255, 255),
        RED(255, 0, 0),
        GREEN(0, 128, 0),
        BLUE(0, 0, 255),
        YELLOW(255, 255, 0),
        CYAN(0, 255, 255),
        MAGENTA(255, 0, 255),
        GRAY(128, 128, 128),
        GREY(128, 128, 128),
        SILVER(192, 192, 192),
        MAROON(128, 0, 0),
        OLIVE(128, 128, 0),
        LIME(0, 255, 0),
        AQUA(0, 255, 255),
        TEAL(0, 128, 128),
        NAVY(0, 0, 128),
        FUCHSIA(255, 0, 255),
        PURPLE(128, 0, 128),
        ORANGE(255, 165, 0),
        PINK(255, 192, 203),
        BROWN(165, 42, 42),
        GOLD(255, 215, 0),
        CORAL(255, 127, 80),
        SALMON(250, 128, 114),
        VIOLET(238, 130, 238),
        INDIGO(75, 0, 130),
        CRIMSON(220, 20, 60),
        TURQUOISE(64, 224, 208),
        TOMATO(255, 99, 71),
        KHAKI(240, 230, 140),
        LAVENDER(230, 230, 250),
        PLUM(221, 160, 221),
        SIENNA(160, 82, 45),
        TAN(210, 180, 140),
        WHEAT(245, 222, 179),
        IVORY(255, 255, 240),
        BEIGE(245, 245, 220),
        MINT(189, 252, 201),
        PEACH(255, 218, 185),
        CHARCOAL(54, 69, 79);

        private final int r, g, b;

        Name(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public int r() { return r; }
        public int g() { return g; }
        public int b() { return b; }

        /**
         * Converts this named color to a Color instance.
         */
        public Color toColor() {
            return Color.rgb(r, g, b);
        }

        /**
         * Converts this named color to a Color with the given alpha.
         */
        public Color toColor(int alpha) {
            return Color.rgba(r, g, b, alpha);
        }

        /**
         * Converts this named color to a Color with the given opacity.
         */
        public Color toColor(double opacity) {
            return Color.rgba(r, g, b, opacity);
        }

        /**
         * Looks up a Name by its string label (case-insensitive).
         *
         * @param name the color name string
         * @return the matching Name
         * @throws ValueObjectException if not found
         */
        public static Name fromString(String name) {
            try {
                return Name.valueOf(name.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                throw new ValueObjectException("Unknown color name: " + name);
            }
        }
    }

    // ==================== Common Color Constants ====================

    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    // ==================== Compact Constructor ====================

    public Color {
        if (red != null) validate(red, "red");
        if (green != null) validate(green, "green");
        if (blue != null) validate(blue, "blue");
        if (alpha != null) validate(alpha, "alpha");
    }

    private static void validate(int value, String component) {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new ValueObjectException(
                String.format("%s must be between %d and %d: %d",
                    component, MIN_VALUE, MAX_VALUE, value)
            );
        }
    }

    // ==================== Safe Accessors ====================

    /**
     * Returns the red component, defaulting to 0 if null.
     */
    public int redValue() {
        return red != null ? red : 0;
    }

    /**
     * Returns the green component, defaulting to 0 if null.
     */
    public int greenValue() {
        return green != null ? green : 0;
    }

    /**
     * Returns the blue component, defaulting to 0 if null.
     */
    public int blueValue() {
        return blue != null ? blue : 0;
    }

    /**
     * Returns the alpha component, defaulting to 255 (opaque) if null.
     */
    public int alphaValue() {
        return alpha != null ? alpha : 255;
    }

    // ==================== Static Color Factories ====================

    /**
     * Creates a Color from a {@link Name} enum constant.
     *
     * @param name the named color, e.g. {@code Color.Name.RED}
     * @return the Color
     */
    public static Color of(Name name) {
        return name.toColor();
    }

    /**
     * Creates a Color from a name string (case-insensitive).
     *
     * @param name e.g. "red", "BLUE", "coral"
     * @return the Color
     */
    public static Color of(String name) {
        return Name.fromString(name).toColor();
    }

    // ==================== Factory Methods ====================

    /**
     * Creates an opaque color from RGB values.
     */
    public static Color rgb(int red, int green, int blue) {
        return new Color(red, green, blue, 255);
    }

    /**
     * Creates a color from RGBA values.
     */
    public static Color rgba(int red, int green, int blue, int alpha) {
        return new Color(red, green, blue, alpha);
    }

    /**
     * Creates a color from RGBA with alpha as float (0.0-1.0).
     */
    public static Color rgba(int red, int green, int blue, double alpha) {
        return new Color(red, green, blue, (int) Math.round(alpha * 255));
    }

    /**
     * Creates a color from a hex string.
     *
     * @param hex e.g. "#FF5733", "FF5733", "#F53"
     */
    public static Color fromHex(String hex) {
        Objects.requireNonNull(hex, "Hex string cannot be null");
        hex = hex.trim();

        Matcher m3 = HEX_3.matcher(hex);
        if (m3.matches()) {
            int r = Integer.parseInt(m3.group(1) + m3.group(1), 16);
            int g = Integer.parseInt(m3.group(2) + m3.group(2), 16);
            int b = Integer.parseInt(m3.group(3) + m3.group(3), 16);
            return rgb(r, g, b);
        }

        Matcher m6 = HEX_6.matcher(hex);
        if (m6.matches()) {
            int r = Integer.parseInt(m6.group(1), 16);
            int g = Integer.parseInt(m6.group(2), 16);
            int b = Integer.parseInt(m6.group(3), 16);
            return rgb(r, g, b);
        }

        Matcher m8 = HEX_8.matcher(hex);
        if (m8.matches()) {
            int r = Integer.parseInt(m8.group(1), 16);
            int g = Integer.parseInt(m8.group(2), 16);
            int b = Integer.parseInt(m8.group(3), 16);
            int a = Integer.parseInt(m8.group(4), 16);
            return rgba(r, g, b, a);
        }

        throw new ValueObjectException("Invalid hex color: " + hex);
    }

    /**
     * Creates a color from a named color string.
     *
     * @param name e.g. "red", "blue"
     */
    public static Color fromName(String name) {
        return Name.fromString(name).toColor();
    }

    /**
     * Parses a color from various formats (hex, rgb(), rgba(), named).
     */
    public static Color parse(String value) {
        Objects.requireNonNull(value, "Color value cannot be null");
        value = value.trim().toLowerCase();

        try {
            return fromName(value);
        } catch (ValueObjectException ignored) {
            // Not a named color — continue to other formats
        }

        if (value.startsWith("#") || value.matches("^[0-9a-f]+$")) {
            return fromHex(value);
        }

        Matcher rgbMatcher = RGB_PATTERN.matcher(value);
        if (rgbMatcher.matches()) {
            return rgb(
                Integer.parseInt(rgbMatcher.group(1)),
                Integer.parseInt(rgbMatcher.group(2)),
                Integer.parseInt(rgbMatcher.group(3))
            );
        }

        Matcher rgbaMatcher = RGBA_PATTERN.matcher(value);
        if (rgbaMatcher.matches()) {
            return rgba(
                Integer.parseInt(rgbaMatcher.group(1)),
                Integer.parseInt(rgbaMatcher.group(2)),
                Integer.parseInt(rgbaMatcher.group(3)),
                Double.parseDouble(rgbaMatcher.group(4))
            );
        }

        throw new ValueObjectException("Cannot parse color: " + value);
    }

    /**
     * Creates a grayscale color.
     *
     * @param value the gray value (0-255)
     */
    public static Color grayscale(int value) {
        return rgb(value, value, value);
    }

    /**
     * Creates a color from HSL values.
     *
     * @param hue        hue in degrees (0-360)
     * @param saturation saturation (0.0-1.0)
     * @param lightness  lightness (0.0-1.0)
     */
    public static Color fromHsl(double hue, double saturation, double lightness) {
        hue = hue % 360;
        if (hue < 0) hue += 360;

        double c = (1 - Math.abs(2 * lightness - 1)) * saturation;
        double x = c * (1 - Math.abs((hue / 60) % 2 - 1));
        double m = lightness - c / 2;

        double r, g, b;
        if (hue < 60)       { r = c; g = x; b = 0; }
        else if (hue < 120) { r = x; g = c; b = 0; }
        else if (hue < 180) { r = 0; g = c; b = x; }
        else if (hue < 240) { r = 0; g = x; b = c; }
        else if (hue < 300) { r = x; g = 0; b = c; }
        else                { r = c; g = 0; b = x; }

        return rgb(
            (int) Math.round((r + m) * 255),
            (int) Math.round((g + m) * 255),
            (int) Math.round((b + m) * 255)
        );
    }

    // ==================== Random Generators ====================

    /**
     * Generates a completely random opaque color.
     */
    public static Color random() {
        var rng = ThreadLocalRandom.current();
        return rgb(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
    }

    /**
     * Generates a random color with a specified alpha.
     *
     * @param alpha the alpha value (0-255)
     */
    public static Color random(int alpha) {
        var rng = ThreadLocalRandom.current();
        return rgba(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256), alpha);
    }

    /**
     * Generates a random color with a specified opacity.
     *
     * @param opacity the opacity (0.0-1.0)
     */
    public static Color random(double opacity) {
        var rng = ThreadLocalRandom.current();
        return rgba(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256), opacity);
    }

    /**
     * Generates a random pastel color (high lightness, medium saturation).
     */
    public static Color randomPastel() {
        var rng = ThreadLocalRandom.current();
        return fromHsl(rng.nextDouble(360), 0.5 + rng.nextDouble(0.3), 0.75 + rng.nextDouble(0.15));
    }

    /**
     * Generates a random dark color (low lightness).
     */
    public static Color randomDark() {
        var rng = ThreadLocalRandom.current();
        return fromHsl(rng.nextDouble(360), 0.4 + rng.nextDouble(0.5), 0.1 + rng.nextDouble(0.25));
    }

    /**
     * Generates a random vibrant/saturated color.
     */
    public static Color randomVibrant() {
        var rng = ThreadLocalRandom.current();
        return fromHsl(rng.nextDouble(360), 0.8 + rng.nextDouble(0.2), 0.4 + rng.nextDouble(0.2));
    }

    /**
     * Generates a random grayscale color.
     */
    public static Color randomGray() {
        int v = ThreadLocalRandom.current().nextInt(256);
        return rgb(v, v, v);
    }

    /**
     * Generates a random warm color (reds, oranges, yellows).
     */
    public static Color randomWarm() {
        var rng = ThreadLocalRandom.current();
        double hue = rng.nextDouble(60); // 0-60 range
        if (rng.nextBoolean()) hue = 360 - rng.nextDouble(30); // or 330-360
        return fromHsl(hue, 0.6 + rng.nextDouble(0.4), 0.4 + rng.nextDouble(0.3));
    }

    /**
     * Generates a random cool color (blues, greens, purples).
     */
    public static Color randomCool() {
        var rng = ThreadLocalRandom.current();
        return fromHsl(180 + rng.nextDouble(180), 0.5 + rng.nextDouble(0.4), 0.3 + rng.nextDouble(0.4));
    }

    // ==================== Conversion Methods ====================

    public String toHex() {
        return String.format("#%02X%02X%02X", redValue(), greenValue(), blueValue());
    }

    public String toHexWithAlpha() {
        return String.format("#%02X%02X%02X%02X", redValue(), greenValue(), blueValue(), alphaValue());
    }

    public String toRgbString() {
        return String.format("rgb(%d, %d, %d)", redValue(), greenValue(), blueValue());
    }

    public String toRgbaString() {
        return String.format("rgba(%d, %d, %d, %.2f)", redValue(), greenValue(), blueValue(), alphaValue() / 255.0);
    }

    public double[] toHsl() {
        double r = redValue() / 255.0;
        double g = greenValue() / 255.0;
        double b = blueValue() / 255.0;

        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double h, s, l = (max + min) / 2;

        if (max == min) {
            h = s = 0;
        } else {
            double d = max - min;
            s = l > 0.5 ? d / (2 - max - min) : d / (max + min);

            if (max == r) {
                h = (g - b) / d + (g < b ? 6 : 0);
            } else if (max == g) {
                h = (b - r) / d + 2;
            } else {
                h = (r - g) / d + 4;
            }
            h /= 6;
        }

        return new double[]{h * 360, s, l};
    }

    public double opacity() {
        return alphaValue() / 255.0;
    }

    public boolean isOpaque() {
        return alphaValue() == 255;
    }

    public boolean isTransparent() {
        return alphaValue() == 0;
    }

    // ==================== Manipulation Methods ====================

    public Color withAlpha(int newAlpha) {
        return new Color(redValue(), greenValue(), blueValue(), newAlpha);
    }

    public Color withOpacity(double opacity) {
        return withAlpha((int) Math.round(opacity * 255));
    }

    public Color lighten(double amount) {
        double[] hsl = toHsl();
        double newL = Math.min(1.0, hsl[2] + amount);
        Color lighter = fromHsl(hsl[0], hsl[1], newL);
        return rgba(lighter.redValue(), lighter.greenValue(), lighter.blueValue(), this.alphaValue());
    }

    public Color darken(double amount) {
        double[] hsl = toHsl();
        double newL = Math.max(0.0, hsl[2] - amount);
        Color darker = fromHsl(hsl[0], hsl[1], newL);
        return rgba(darker.redValue(), darker.greenValue(), darker.blueValue(), this.alphaValue());
    }

    public Color complementary() {
        return rgb(255 - redValue(), 255 - greenValue(), 255 - blueValue());
    }

    /**
     * Mixes this color with another at the given ratio.
     *
     * @param other the other color
     * @param ratio 0.0 = this color, 1.0 = other color
     * @return the blended Color
     */
    public Color mix(Color other, double ratio) {
        ratio = Math.max(0.0, Math.min(1.0, ratio));
        return rgb(
            (int) Math.round(redValue() * (1 - ratio) + other.redValue() * ratio),
            (int) Math.round(greenValue() * (1 - ratio) + other.greenValue() * ratio),
            (int) Math.round(blueValue() * (1 - ratio) + other.blueValue() * ratio)
        );
    }

    public double luminance() {
        double r = adjustGamma(redValue() / 255.0);
        double g = adjustGamma(greenValue() / 255.0);
        double b = adjustGamma(blueValue() / 255.0);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private double adjustGamma(double value) {
        return value <= 0.03928 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
    }

    public boolean isDark() {
        return luminance() < 0.5;
    }

    public boolean isLight() {
        return luminance() >= 0.5;
    }

    public Color contrastingTextColor() {
        return isDark() ? WHITE.toColor() : BLACK.toColor();
    }

    /**
     * Calculates contrast ratio against another color (WCAG).
     *
     * @param other the other color
     * @return contrast ratio (1.0 to 21.0)
     */
    public double contrastRatio(Color other) {
        double l1 = Math.max(luminance(), other.luminance());
        double l2 = Math.min(luminance(), other.luminance());
        return (l1 + 0.05) / (l2 + 0.05);
    }

    @Override
    public String toString() {
        return isOpaque() ? toHex() : toRgbaString();
    }
}