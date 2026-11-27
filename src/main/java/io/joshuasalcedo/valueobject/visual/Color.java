package io.joshuasalcedo.valueobject.visual;

import java.util.Locale;
import java.util.Optional;
import io.joshuasalcedo.valueobject.core.ValueObject;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.joshuasalcedo.valueobject.visual.Color.Name.BLACK;
import static io.joshuasalcedo.valueobject.visual.Color.Name.WHITE;

/**
 * An sRGB color with an alpha channel, each component 0 to 255.
 * Made from RGB or RGBA values, hex strings, CSS {@code rgb()} / {@code rgba()} strings, {@link Name named colors}
 * or HSL, or generated at random.
 *
 * <p>Any component may be {@code null}; the {@code *Value()} accessors then read 0 for red, green and blue and
 * 255 (opaque) for alpha.
 *
 * <pre>{@code
 * Color c = Color.parse("#FF5733");
 * c.toRgbString();          // "rgb(255, 87, 51)"
 * c.withOpacity(0.5);       // rgba(255, 87, 51, 0.50)
 * c.contrastingTextColor(); // white or black, whichever reads better on it
 * }</pre>
 *
 * @param red   the red component, 0 to 255, or {@code null}
 * @param green the green component, 0 to 255, or {@code null}
 * @param blue  the blue component, 0 to 255, or {@code null}
 * @param alpha the alpha component, 0 (transparent) to 255 (opaque), or {@code null}
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
    private static final Pattern RGBA_PATTERN = Pattern.compile("^rgba\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d+(?:\\.\\d+)?|\\.\\d+)(%?)\\s*\\)$");

    /**
     * The relative luminance at which black and white text have the same WCAG contrast ratio:
     * {@code sqrt(1.05 * 0.05) - 0.05}, about 0.179.
     */
    private static final double CONTRAST_MIDPOINT = Math.sqrt(1.05 * 0.05) - 0.05;

    // ==================== Name Enum ====================

    /**
     * CSS named colors as an enum. Use {@code Color.Name.RED.toColor()} or
     * {@code Color.of(Color.Name.RED)}.
     */
    public enum Name {
        /** Black. */
        BLACK(0, 0, 0),
        /** White. */
        WHITE(255, 255, 255),
        /** Red. */
        RED(255, 0, 0),
        /** Green. */
        GREEN(0, 128, 0),
        /** Blue. */
        BLUE(0, 0, 255),
        /** Yellow. */
        YELLOW(255, 255, 0),
        /** Cyan. */
        CYAN(0, 255, 255),
        /** Magenta. */
        MAGENTA(255, 0, 255),
        /** Gray. */
        GRAY(128, 128, 128),
        /** Grey. */
        GREY(128, 128, 128),
        /** Silver. */
        SILVER(192, 192, 192),
        /** Maroon. */
        MAROON(128, 0, 0),
        /** Olive. */
        OLIVE(128, 128, 0),
        /** Lime. */
        LIME(0, 255, 0),
        /** Aqua. */
        AQUA(0, 255, 255),
        /** Teal. */
        TEAL(0, 128, 128),
        /** Navy. */
        NAVY(0, 0, 128),
        /** Fuchsia. */
        FUCHSIA(255, 0, 255),
        /** Purple. */
        PURPLE(128, 0, 128),
        /** Orange. */
        ORANGE(255, 165, 0),
        /** Pink. */
        PINK(255, 192, 203),
        /** Brown. */
        BROWN(165, 42, 42),
        /** Gold. */
        GOLD(255, 215, 0),
        /** Coral. */
        CORAL(255, 127, 80),
        /** Salmon. */
        SALMON(250, 128, 114),
        /** Violet. */
        VIOLET(238, 130, 238),
        /** Indigo. */
        INDIGO(75, 0, 130),
        /** Crimson. */
        CRIMSON(220, 20, 60),
        /** Turquoise. */
        TURQUOISE(64, 224, 208),
        /** Tomato. */
        TOMATO(255, 99, 71),
        /** Khaki. */
        KHAKI(240, 230, 140),
        /** Lavender. */
        LAVENDER(230, 230, 250),
        /** Plum. */
        PLUM(221, 160, 221),
        /** Sienna. */
        SIENNA(160, 82, 45),
        /** Tan. */
        TAN(210, 180, 140),
        /** Wheat. */
        WHEAT(245, 222, 179),
        /** Ivory. */
        IVORY(255, 255, 240),
        /** Beige. */
        BEIGE(245, 245, 220),
        /** Mint. */
        MINT(189, 252, 201),
        /** Peach. */
        PEACH(255, 218, 185),
        /** Charcoal. */
        CHARCOAL(54, 69, 79);

        private final int r, g, b;

        Name(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        /** {@return its red component, 0 to 255} */
        public int r() { return r; }
        /** {@return its green component, 0 to 255} */
        public int g() { return g; }
        /** {@return its blue component, 0 to 255} */
        public int b() { return b; }

        /**
         * Converts this named color to an opaque {@link Color}.
         *
         * @return the color, alpha 255
         */
        public Color toColor() {
            return Color.rgb(r, g, b);
        }

        /**
         * Converts this named color to a {@link Color} with the given alpha.
         *
         * @param alpha the alpha, 0 (transparent) to 255 (opaque)
         * @return the color
         * @throws ValueObjectException if {@code alpha} is outside 0 to 255
         */
        public Color toColor(int alpha) {
            return Color.rgba(r, g, b, alpha);
        }

        /**
         * Converts this named color to a {@link Color} with the given opacity.
         *
         * @param opacity the opacity, 0.0 (transparent) to 1.0 (opaque); scaled to an alpha of 0 to 255
         * @return the color
         * @throws ValueObjectException if {@code opacity} is outside 0.0 to 1.0
         */
        public Color toColor(double opacity) {
            return Color.rgba(r, g, b, opacity);
        }

        /**
         * Looks up a name by its label, ignoring case and surrounding whitespace.
         *
         * @param name the color name, e.g. {@code "coral"}
         * @return the matching name
         * @throws ValueObjectException if it is {@code null} or no named color has that label
         */
        public static Name fromString(String name) {
            if (name == null) {
                throw new ValueObjectException("A color name is required");
            }
            try {
                return Name.valueOf(name.toUpperCase(Locale.ROOT).trim());
            } catch (IllegalArgumentException e) {
                throw new ValueObjectException("Unknown color name: " + name);
            }
        }
    }

    // ==================== Common Color Constants ====================

    /** Transparent: {@code new Color(0, 0, 0, 0)}. */
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    // ==================== Compact Constructor ====================

    /**
     * Creates a color from its components; any of them may be {@code null}.
     *
     * @throws ValueObjectException if a non-null component is outside 0 to 255
     */
    public Color {
        if (red != null) validate(red, "red");
        if (green != null) validate(green, "green");
        if (blue != null) validate(blue, "blue");
        if (alpha != null) validate(alpha, "alpha");
    }

    private static void validate(int value, String component) {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new ValueObjectException(
                String.format(Locale.ROOT, "%s must be between %d and %d: %d",
                    component, MIN_VALUE, MAX_VALUE, value)
            );
        }
    }

    // ==================== Safe Accessors ====================

    /** {@return the red component, or 0 when it is {@code null}} */
    public int redValue() {
        return red != null ? red : 0;
    }

    /** {@return the green component, or 0 when it is {@code null}} */
    public int greenValue() {
        return green != null ? green : 0;
    }

    /** {@return the blue component, or 0 when it is {@code null}} */
    public int blueValue() {
        return blue != null ? blue : 0;
    }

    /** {@return the alpha component, or 255 (opaque) when it is {@code null}} */
    public int alphaValue() {
        return alpha != null ? alpha : 255;
    }

    // ==================== Static Color Factories ====================

    /**
     * Creates an opaque color from a {@link Name} constant.
     *
     * @param name the named color, e.g. {@code Color.Name.RED}
     * @return the color
     */
    public static Color of(Name name) {
        return name.toColor();
    }

    /**
     * Creates an opaque color from a color name, ignoring case.
     *
     * @param name e.g. {@code "red"}, {@code "BLUE"}, {@code "coral"}
     * @return the color
     * @throws ValueObjectException if no named color has that name
     */
    public static Color of(String name) {
        return Name.fromString(name).toColor();
    }

    // ==================== Factory Methods ====================

    /**
     * Creates an opaque color from RGB values.
     *
     * @param red   the red component, 0 to 255
     * @param green the green component, 0 to 255
     * @param blue  the blue component, 0 to 255
     * @return the color, alpha 255
     * @throws ValueObjectException if a component is outside 0 to 255
     */
    public static Color rgb(int red, int green, int blue) {
        return new Color(red, green, blue, 255);
    }

    /**
     * Creates a color from RGBA values, alpha given as 0 to 255. Unlike CSS, where {@code rgba(r, g, b, 1)} is
     * opaque, an {@code int} alpha of 1 here is almost transparent; for a CSS-style opacity pass a
     * {@code double}, {@code rgba(r, g, b, 1.0)}, or parse the CSS text with {@link #parse(String)}.
     *
     * @param red   the red component, 0 to 255
     * @param green the green component, 0 to 255
     * @param blue  the blue component, 0 to 255
     * @param alpha the alpha, 0 (transparent) to 255 (opaque)
     * @return the color
     * @throws ValueObjectException if a component is outside 0 to 255
     */
    public static Color rgba(int red, int green, int blue, int alpha) {
        return new Color(red, green, blue, alpha);
    }

    /**
     * Creates a color from RGBA values, alpha given as an opacity of 0.0 to 1.0 and rounded to 0 to 255.
     *
     * @param red   the red component, 0 to 255
     * @param green the green component, 0 to 255
     * @param blue  the blue component, 0 to 255
     * @param alpha the opacity, 0.0 (transparent) to 1.0 (opaque)
     * @return the color
     * @throws ValueObjectException if a component is outside its range or the opacity is NaN
     */
    public static Color rgba(int red, int green, int blue, double alpha) {
        if (Double.isNaN(alpha) || alpha < 0.0 || alpha > 1.0) {
            throw new ValueObjectException("Opacity must be between 0.0 and 1.0: " + alpha);
        }
        return new Color(red, green, blue, (int) Math.round(alpha * 255));
    }

    /**
     * Creates a color from a hex string of 3 ({@code RGB}), 6 ({@code RRGGBB}) or 8 ({@code RRGGBBAA}) hex
     * digits, with or without a leading {@code #}; surrounding whitespace is ignored.
     *
     * @param hex e.g. {@code "#FF5733"}, {@code "FF5733"}, {@code "#F53"}, {@code "#FF573380"}
     * @return the color; opaque unless 8 digits were given
     * @throws ValueObjectException if it is {@code null} or not one of those forms
     */
    public static Color fromHex(String hex) {
        if (hex == null) {
            throw new ValueObjectException("Hex string cannot be null");
        }
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
     * Creates an opaque color from a color name, ignoring case; the same as {@link #of(String)}.
     *
     * @param name e.g. {@code "red"}, {@code "blue"}
     * @return the color
     * @throws ValueObjectException if no named color has that name
     */
    public static Color fromName(String name) {
        return Name.fromString(name).toColor();
    }

    /**
     * Parses a color from a color name ({@code "coral"}), a hex string ({@code "#F53"}, {@code "FF5733"},
     * {@code "#FF573380"}), {@code "rgb(r, g, b)"} or {@code "rgba(r, g, b, alpha)"}. As in CSS, the alpha is an
     * opacity, either a number from 0 to 1 ({@code 1} and {@code 1.0} are opaque, {@code 0.5} half transparent)
     * or a percentage from {@code 0%} to {@code 100%}. Case and surrounding whitespace are ignored.
     *
     * @param value the text
     * @return the color
     * @throws ValueObjectException if it is {@code null}, none of those forms, or a component is out of range
     */
    public static Color parse(String value) {
        if (value == null) {
            throw new ValueObjectException("Color value cannot be null");
        }
        value = value.trim().toLowerCase(Locale.ROOT);

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
                rgbaMatcher.group(5).isEmpty()
                        ? Double.parseDouble(rgbaMatcher.group(4))
                        : Double.parseDouble(rgbaMatcher.group(4)) / 100.0
            );
        }

        throw new ValueObjectException("Cannot parse color: " + value);
    }

    /**
     * Creates an opaque gray with equal red, green and blue.
     *
     * @param value the gray level, 0 (black) to 255 (white)
     * @return the color
     * @throws ValueObjectException if {@code value} is outside 0 to 255
     */
    public static Color grayscale(int value) {
        return rgb(value, value, value);
    }

    /**
     * Creates an opaque color from HSL values.
     *
     * @param hue        hue in degrees; wrapped into 0 to 360, so negative and larger values work
     * @param saturation saturation, 0.0 to 1.0
     * @param lightness  lightness, 0.0 to 1.0
     * @return the color
     * @throws ValueObjectException if a saturation or lightness out of range gives a component outside 0 to 255
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

    /** {@return a random opaque color, each of red, green and blue uniform over 0 to 255} */
    public static Color random() {
        var rng = ThreadLocalRandom.current();
        return rgb(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
    }

    /**
     * Generates a random color with the given alpha.
     *
     * @param alpha the alpha, 0 (transparent) to 255 (opaque)
     * @return the color
     * @throws ValueObjectException if {@code alpha} is outside 0 to 255
     */
    public static Color random(int alpha) {
        var rng = ThreadLocalRandom.current();
        return rgba(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256), alpha);
    }

    /**
     * Generates a random color with the given opacity.
     *
     * @param opacity the opacity, 0.0 (transparent) to 1.0 (opaque)
     * @return the color
     * @throws ValueObjectException if {@code opacity} is outside 0.0 to 1.0
     */
    public static Color random(double opacity) {
        var rng = ThreadLocalRandom.current();
        return rgba(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256), opacity);
    }

    /** {@return a random opaque pastel color: any hue, saturation 0.5 to 0.8, lightness 0.75 to 0.9} */
    public static Color randomPastel() {
        var rng = ThreadLocalRandom.current();
        return fromHsl(rng.nextDouble(360), 0.5 + rng.nextDouble(0.3), 0.75 + rng.nextDouble(0.15));
    }

    /** {@return a random opaque dark color: any hue, saturation 0.4 to 0.9, lightness 0.1 to 0.35} */
    public static Color randomDark() {
        var rng = ThreadLocalRandom.current();
        return fromHsl(rng.nextDouble(360), 0.4 + rng.nextDouble(0.5), 0.1 + rng.nextDouble(0.25));
    }

    /** {@return a random opaque vibrant color: any hue, saturation 0.8 to 1.0, lightness 0.4 to 0.6} */
    public static Color randomVibrant() {
        var rng = ThreadLocalRandom.current();
        return fromHsl(rng.nextDouble(360), 0.8 + rng.nextDouble(0.2), 0.4 + rng.nextDouble(0.2));
    }

    /** {@return a random opaque gray, level uniform over 0 to 255} */
    public static Color randomGray() {
        int v = ThreadLocalRandom.current().nextInt(256);
        return rgb(v, v, v);
    }

    /** {@return a random opaque warm color (reds, oranges, yellows): hue 0 to 60 or 330 to 360} */
    public static Color randomWarm() {
        var rng = ThreadLocalRandom.current();
        double hue = rng.nextDouble(60); // 0-60 range
        if (rng.nextBoolean()) hue = 360 - rng.nextDouble(30); // or 330-360
        return fromHsl(hue, 0.6 + rng.nextDouble(0.4), 0.4 + rng.nextDouble(0.3));
    }

    /** {@return a random opaque cool color (cyans, blues, purples, magentas): hue 180 to 360} */
    public static Color randomCool() {
        var rng = ThreadLocalRandom.current();
        return fromHsl(180 + rng.nextDouble(180), 0.5 + rng.nextDouble(0.4), 0.3 + rng.nextDouble(0.4));
    }

    // ==================== Conversion Methods ====================

    /** {@return it as {@code #RRGGBB} in upper case, alpha left out} */
    public String toHex() {
        return String.format(Locale.ROOT, "#%02X%02X%02X", redValue(), greenValue(), blueValue());
    }

    /** {@return it as {@code #RRGGBBAA} in upper case} */
    public String toHexWithAlpha() {
        return String.format(Locale.ROOT, "#%02X%02X%02X%02X", redValue(), greenValue(), blueValue(), alphaValue());
    }

    /** {@return it as a CSS {@code rgb(r, g, b)} string, alpha left out} */
    public String toRgbString() {
        return String.format(Locale.ROOT, "rgb(%d, %d, %d)", redValue(), greenValue(), blueValue());
    }

    /** {@return it as a CSS {@code rgba(r, g, b, a)} string, the opacity to two decimals} */
    public String toRgbaString() {
        return String.format(Locale.ROOT, "rgba(%d, %d, %d, %.2f)", redValue(), greenValue(), blueValue(), alphaValue() / 255.0);
    }

    /** {@return it as HSL: hue in degrees (0 to 360), saturation (0.0 to 1.0) and lightness (0.0 to 1.0)} */
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

    /** {@return its opacity, alpha divided by 255: 0.0 (transparent) to 1.0 (opaque)} */
    public double opacity() {
        return alphaValue() / 255.0;
    }

    /** {@return whether its alpha is 255 (or {@code null})} */
    public boolean isOpaque() {
        return alphaValue() == 255;
    }

    /** {@return whether its alpha is 0} */
    public boolean isTransparent() {
        return alphaValue() == 0;
    }

    // ==================== Manipulation Methods ====================

    /**
     * Returns a copy with another alpha.
     *
     * @param newAlpha the alpha, 0 (transparent) to 255 (opaque)
     * @return the copy
     * @throws ValueObjectException if {@code newAlpha} is outside 0 to 255
     */
    public Color withAlpha(int newAlpha) {
        return new Color(redValue(), greenValue(), blueValue(), newAlpha);
    }

    /**
     * Returns a copy with another opacity.
     *
     * @param opacity the opacity, 0.0 (transparent) to 1.0 (opaque); rounded to an alpha of 0 to 255
     * @return the copy
     * @throws ValueObjectException if {@code opacity} is NaN or outside 0.0 to 1.0
     */
    public Color withOpacity(double opacity) {
        return rgba(redValue(), greenValue(), blueValue(), opacity);
    }

    /**
     * Returns a lighter copy: its HSL lightness raised by {@code amount}, capped at 1.0. Alpha is kept.
     *
     * @param amount how much to add to the lightness, e.g. {@code 0.1}
     * @return the lighter color
     */
    public Color lighten(double amount) {
        double[] hsl = toHsl();
        double newL = Math.min(1.0, hsl[2] + amount);
        Color lighter = fromHsl(hsl[0], hsl[1], newL);
        return rgba(lighter.redValue(), lighter.greenValue(), lighter.blueValue(), this.alphaValue());
    }

    /**
     * Returns a darker copy: its HSL lightness lowered by {@code amount}, floored at 0.0. Alpha is kept.
     *
     * @param amount how much to take from the lightness, e.g. {@code 0.1}
     * @return the darker color
     */
    public Color darken(double amount) {
        double[] hsl = toHsl();
        double newL = Math.max(0.0, hsl[2] - amount);
        Color darker = fromHsl(hsl[0], hsl[1], newL);
        return rgba(darker.redValue(), darker.greenValue(), darker.blueValue(), this.alphaValue());
    }

    /** {@return its inverse, each of red, green and blue subtracted from 255; alpha is kept} */
    public Color complementary() {
        return rgba(255 - redValue(), 255 - greenValue(), 255 - blueValue(), alphaValue());
    }

    /**
     * Mixes this color with another, blending red, green, blue and alpha linearly.
     *
     * @param other the other color
     * @param ratio how much of {@code other}: 0.0 gives this color, 1.0 gives {@code other}; clamped to that
     *              range, NaN counting as 0.0
     * @return the blended color
     * @throws ValueObjectException if {@code other} is {@code null}
     */
    public Color mix(Color other, double ratio) {
        if (other == null) {
            throw new ValueObjectException("A color to mix with is required");
        }
        ratio = Double.isNaN(ratio) ? 0.0 : Math.max(0.0, Math.min(1.0, ratio));
        return rgba(
            (int) Math.round(redValue() * (1 - ratio) + other.redValue() * ratio),
            (int) Math.round(greenValue() * (1 - ratio) + other.greenValue() * ratio),
            (int) Math.round(blueValue() * (1 - ratio) + other.blueValue() * ratio),
            (int) Math.round(alphaValue() * (1 - ratio) + other.alphaValue() * ratio)
        );
    }

    /** {@return its WCAG relative luminance, 0.0 (black) to 1.0 (white); alpha is ignored} */
    public double luminance() {
        double r = adjustGamma(redValue() / 255.0);
        double g = adjustGamma(greenValue() / 255.0);
        double b = adjustGamma(blueValue() / 255.0);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private double adjustGamma(double value) {
        return value <= 0.03928 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
    }

    /**
     * Tells whether the color is dark: whether its {@link #luminance() relative luminance} is below about 0.179,
     * the point at which white text and black text have the same WCAG {@link #contrastRatio(Color) contrast
     * ratio} on it. A mid grey such as {@code #808080} (luminance 0.216) is therefore light: black text reads
     * better on it. Alpha is ignored.
     *
     * @return whether white text contrasts more with it than black text
     */
    public boolean isDark() {
        return luminance() < CONTRAST_MIDPOINT;
    }

    /**
     * Tells whether the color is light, the opposite of {@link #isDark()}: its relative luminance is about 0.179
     * or more.
     *
     * @return whether black text contrasts at least as much with it as white text
     */
    public boolean isLight() {
        return !isDark();
    }

    /** {@return white when it {@link #isDark() is dark}, otherwise black: a text color that reads on it} */
    public Color contrastingTextColor() {
        return isDark() ? WHITE.toColor() : BLACK.toColor();
    }

    /**
     * Calculates the WCAG contrast ratio between this color and another; alpha is ignored.
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

    /**
     * Parses a color as {@link #parse(String)} does, without throwing.
     *
     * @param text any text
     * @return it as a {@link Color}, or empty when it is not one; never throws
     */
    public static Optional<Color> tryParse(@Nullable String text) {
        if (text == null) return Optional.empty();
        try {
            return Optional.of(parse(text));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Tells whether a text parses as a color with {@link #parse(String)}.
     *
     * @param text any text
     * @return whether it is a {@link Color}
     */
    public static boolean isValid(@Nullable String text) {
        return tryParse(text).isPresent();
    }

}
