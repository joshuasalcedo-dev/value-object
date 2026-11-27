package io.joshuasalcedo.valueobject.core;

/**
 * A value object found in a text, with how it was found: the shape that matched it and where.
 *
 * <pre>{@code
 * for (Found f : ValueObject.scan("Mail jane@example.com, call +63 917 555 0142")) {
 *     f.type();    // EMAIL
 *     f.value();   // Email[jane@example.com]
 *     f.text();    // "jane@example.com", as printed
 *     f.start();   // 5
 *     f.shape();   // the EMAIL regex from codegen/detection.txt
 * }
 * }</pre>
 *
 * @param type  what it is
 * @param value it, parsed
 * @param text  the text it was read from, as printed
 * @param start where that text starts in the whole text (0-based)
 * @param end   where it ends (exclusive)
 * @param shape the regex that recognised it ({@code codegen/detection.txt})
 * @author JoshuaSalcedo
 */
public record Found(ValueObject.Type type, ValueObject value, String text, int start, int end, String shape) {

    /**
     * The line it is on.
     *
     * @param whole the text it was found in
     * @return its line in that text, from 0
     */
    public int line(String whole) {
        int line = 0;
        for (int i = 0; i < Math.min(start, whole.length()); i++) if (whole.charAt(i) == '\n') line++;
        return line;
    }
}
