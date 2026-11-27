package io.joshuasalcedo.valueobject.visual;

import io.joshuasalcedo.valueobject.core.ValueObjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class VisualTest {

    // ---------------- Color

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "#FF5733                | 255 | 87  | 51  | 255",
            "ff5733                 | 255 | 87  | 51  | 255",
            "'  #f53  '             | 255 | 85  | 51  | 255",
            "#FF573380              | 255 | 87  | 51  | 128",
            "coral                  | 255 | 127 | 80  | 255",
            "CORAL                  | 255 | 127 | 80  | 255",
            "rgb(1, 2, 3)           | 1   | 2   | 3   | 255",
            "RGB( 1 ,2,3 )          | 1   | 2   | 3   | 255",
            "rgba(255, 0, 0, 1)     | 255 | 0   | 0   | 255",
            "rgba(255, 0, 0, 1.0)   | 255 | 0   | 0   | 255",
            "rgba(255, 0, 0, 0.5)   | 255 | 0   | 0   | 128",
            "rgba(255, 0, 0, .25)   | 255 | 0   | 0   | 64",
            "rgba(255, 0, 0, 0)     | 255 | 0   | 0   | 0",
            "rgba(255, 0, 0, 50%)   | 255 | 0   | 0   | 128",
            "rgba(255, 0, 0, 100%)  | 255 | 0   | 0   | 255",
            "rgba(255, 0, 0, 0%)    | 255 | 0   | 0   | 0",
    })
    void parsesEveryForm(String text, int r, int g, int b, int a) {
        Color c = Color.parse(text);
        assertEquals(r, c.redValue());
        assertEquals(g, c.greenValue());
        assertEquals(b, c.blueValue());
        assertEquals(a, c.alphaValue());
        assertEquals(c, Color.tryParse(text).orElseThrow());
        assertTrue(Color.isValid(text));
    }

    @Test
    void cssAlphaOfOneIsOpaque() {
        assertTrue(Color.parse("rgba(10, 20, 30, 1)").isOpaque(), "CSS alpha 1 means opaque, not 1/255");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "notacolor", "#12", "#GGGGGG", "rgb(256, 0, 0)", "rgba(1, 2, 3, 1.5)",
            "rgba(1, 2, 3, 101%)", "rgba(1, 2, 3, -0.5)", "rgba(1, 2, 3, 1.2.3)", "rgba(1,2,3)", "rgb(1,2,3,4)"})
    void rejectsBadText(String text) {
        assertThrows(ValueObjectException.class, () -> Color.parse(text));
        assertTrue(Color.tryParse(text).isEmpty());
        assertFalse(Color.isValid(text));
    }

    @Test
    void nullsAreValueObjectExceptions() {
        assertThrows(ValueObjectException.class, () -> Color.fromHex(null));
        assertThrows(ValueObjectException.class, () -> Color.parse(null));
        assertThrows(ValueObjectException.class, () -> Color.fromName(null));
        assertThrows(ValueObjectException.class, () -> Color.of((String) null));
        assertThrows(ValueObjectException.class, () -> Color.Name.fromString(null));
        assertThrows(ValueObjectException.class, () -> Color.rgb(0, 0, 0).mix(null, 0.5));
    }

    @Test
    void factoriesCheckRanges() {
        assertThrows(ValueObjectException.class, () -> Color.rgb(256, 0, 0));
        assertThrows(ValueObjectException.class, () -> Color.rgba(0, 0, 0, -1));
        assertThrows(ValueObjectException.class, () -> Color.rgba(0, 0, 0, 1.01));
        assertThrows(ValueObjectException.class, () -> Color.rgba(0, 0, 0, Double.NaN));
        assertThrows(ValueObjectException.class, () -> Color.rgb(0, 0, 0).withOpacity(2.0));
        assertEquals(1, Color.rgba(0, 0, 0, 1).alphaValue(), "an int alpha is 0-255");
        assertEquals(255, Color.rgba(0, 0, 0, 1.0).alphaValue(), "a double alpha is an opacity");
    }

    @Test
    void mixAndComplementaryKeepAlpha() {
        Color halfRed = Color.rgba(255, 0, 0, 128);
        assertEquals(Color.rgba(0, 255, 255, 128), halfRed.complementary());
        Color mixed = halfRed.mix(Color.rgba(0, 0, 255, 128), 0.5);
        assertEquals(128, mixed.alphaValue());
        assertEquals(128, mixed.redValue());
        assertEquals(128, mixed.blueValue());
        assertEquals(64, Color.rgba(0, 0, 0, 0).mix(Color.rgba(0, 0, 0, 255), 0.25).alphaValue());
        assertEquals(Color.rgb(255, 0, 0), Color.rgb(255, 0, 0).mix(Color.rgb(0, 0, 255), -1));
        assertEquals(Color.rgb(0, 0, 255), Color.rgb(255, 0, 0).mix(Color.rgb(0, 0, 255), 2));
    }

    @Test
    void isDarkUsesTheWcagContrastMidpoint() {
        assertFalse(Color.parse("#808080").isDark(), "mid grey: black text contrasts better");
        assertTrue(Color.parse("#808080").isLight());
        assertEquals(Color.Name.BLACK.toColor(), Color.parse("#808080").contrastingTextColor());
        assertTrue(Color.parse("#000000").isDark());
        assertTrue(Color.parse("#595959").isDark());
        assertFalse(Color.parse("#FFFFFF").isDark());
        assertEquals(Color.Name.WHITE.toColor(), Color.parse("navy").contrastingTextColor());
        // at the midpoint the two contrasts are equal: check the choice is always the better one
        for (int v = 0; v <= 255; v++) {
            Color grey = Color.grayscale(v);
            double withWhite = grey.contrastRatio(Color.Name.WHITE.toColor());
            double withBlack = grey.contrastRatio(Color.Name.BLACK.toColor());
            if (grey.isDark()) assertTrue(withWhite > withBlack, "grey " + v);
            else assertTrue(withBlack >= withWhite, "grey " + v);
        }
    }

    @Test
    void conversions() {
        Color c = Color.parse("#FF5733");
        assertEquals("#FF5733", c.toHex());
        assertEquals("#FF5733FF", c.toHexWithAlpha());
        assertEquals("rgb(255, 87, 51)", c.toRgbString());
        assertEquals("rgba(255, 87, 51, 1.00)", c.toRgbaString());
        assertEquals("#FF5733", c.toString());
        assertEquals("rgba(255, 87, 51, 0.50)", c.withOpacity(0.5).toString());
        double[] hsl = c.toHsl();
        assertEquals(Color.fromHsl(hsl[0], hsl[1], hsl[2]), c);
        assertEquals(21.0, Color.parse("black").contrastRatio(Color.parse("white")), 0.01);
        assertTrue(Color.TRANSPARENT.isTransparent());
        assertEquals(0.5, Color.rgba(0, 0, 0, 0.5).opacity(), 0.01);
        assertEquals(Color.rgb(0, 0, 0).toHexWithAlpha(), new Color(null, null, null, null).toHexWithAlpha());
        assertEquals(Color.rgb(255, 0, 0).withAlpha(10).alphaValue(), 10);
        assertTrue(Color.parse("#336699").lighten(0.1).luminance() > Color.parse("#336699").luminance());
        assertTrue(Color.parse("#336699").darken(0.1).luminance() < Color.parse("#336699").luminance());
        assertEquals(Color.rgb(255, 0, 0), Color.fromHsl(-360, 1, 0.5));
    }

    @Test
    void formattingIgnoresTheDefaultLocale() {
        Locale before = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            assertEquals("rgba(255, 0, 0, 0.50)", Color.rgba(255, 0, 0, 128).toRgbaString());
            assertEquals(Color.rgba(255, 0, 0, 128), Color.parse(Color.rgba(255, 0, 0, 128).toRgbaString()));
        } finally {
            Locale.setDefault(before);
        }
    }

    @Test
    void randomColorsStayInRange() {
        for (int i = 0; i < 50; i++) {
            assertTrue(Color.random().isOpaque());
            assertEquals(7, Color.random(7).alphaValue());
            assertTrue(Color.randomDark().luminance() < 0.5);
            assertNotNull(Color.randomPastel());
            assertNotNull(Color.randomVibrant());
            assertNotNull(Color.randomWarm());
            assertNotNull(Color.randomCool());
            Color g = Color.randomGray();
            assertEquals(g.redValue(), g.blueValue());
        }
    }

    // ---------------- Avatar

    @Test
    void avatarValidatesInTheConstructor() {
        assertThrows(ValueObjectException.class, () -> new Avatar("not a url"));
        assertThrows(ValueObjectException.class, () -> new Avatar(null));
        assertThrows(ValueObjectException.class, () -> new Avatar("relative/path.png"));
        assertThrows(ValueObjectException.class, () -> new Avatar("unknownscheme://x/y.png"));
        assertThrows(ValueObjectException.class, () -> Avatar.create((java.net.URL) null));
        Avatar a = new Avatar("  https://example.com/me.png ");
        assertEquals("https://example.com/me.png", a.value());
        assertEquals("example.com", a.toURL().getHost());
        assertEquals("'avatar' : 'https://example.com/me.png'", a.toString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "nope", "://x", "https://exa mple.com/a.png"})
    void avatarRejectsBadText(String text) {
        assertThrows(ValueObjectException.class, () -> Avatar.parse(text));
        assertThrows(ValueObjectException.class, () -> Avatar.of(text));
        assertTrue(Avatar.tryParse(text).isEmpty());
        assertFalse(Avatar.isValid(text));
    }

    @Test
    void avatarFactories() throws Exception {
        assertEquals(Avatar.of("https://example.com/a.png"), Avatar.create(java.net.URI.create("https://example.com/a.png").toURL()));
        assertTrue(Avatar.createDefault().value().startsWith("https://"));
        assertTrue(Avatar.isValid("file:///tmp/me.png"));
        assertTrue(Avatar.of("https://a.example/1.png").compareTo(Avatar.of("https://b.example/1.png")) < 0);
    }
}
