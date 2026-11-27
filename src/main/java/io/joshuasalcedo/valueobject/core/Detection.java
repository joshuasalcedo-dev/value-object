package io.joshuasalcedo.valueobject.core;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The detection behind {@link ValueObject#identify}, {@link ValueObject#detect} and
 * {@link ValueObject#find}. Hand-written; {@code ValueObject} itself is generated.
 *
 * <p>Each detectable value object has a shape (a regex, in {@code codegen/detection.txt}). A whole
 * string is tried against the shapes in {@link ValueObject.Type} order, most specific first, and the first
 * that matches and parses wins. Scanning a text does the same over every match in it; a span already
 * claimed by a more specific value object (the host of an email, the IP in a link) is not counted again.
 */
final class Detection {

    /** Not part of a value on either side: letters, digits and the joiners values are built from. */
    private static final String BEFORE = "(?<![\\p{L}\\p{N}_@#/:.+\\-])";
    private static final String AFTER = "(?![\\p{L}\\p{N}_@#/:\\-]|\\.[\\p{L}\\p{N}])";

    private static final Map<ValueObject.Type, Pattern> WHOLE = new EnumMap<>(ValueObject.Type.class);
    private static final Map<ValueObject.Type, Pattern> SCAN = new EnumMap<>(ValueObject.Type.class);

    static {
        for (ValueObject.Type vo : ValueObject.Type.values()) {
            String shape = vo.shape();
            if (shape == null) continue;
            WHOLE.put(vo, Pattern.compile("^(?:" + shape + ")$", Pattern.CASE_INSENSITIVE));
            if (vo.scannable()) {
                SCAN.put(vo, Pattern.compile(BEFORE + "(?:" + shape + ")" + AFTER, Pattern.CASE_INSENSITIVE));
            }
        }
    }

    private Detection() {}

    static Optional<ValueObject.Type> identify(@Nullable String text) {
        return parse(text).map(Parsed::valueObject);
    }

    static Optional<ValueObject> parseAny(@Nullable String text) {
        return parse(text).map(Parsed::value);
    }

    static List<ValueObject.Type> candidates(@Nullable String text) {
        if (text == null || text.isBlank()) return List.of();
        String trimmed = text.trim();
        List<ValueObject.Type> out = new ArrayList<>();
        WHOLE.forEach((vo, shape) -> {
            if (shape.matcher(trimmed).matches() && vo.tryParse(trimmed).isPresent()) out.add(vo);
        });
        return Collections.unmodifiableList(out);
    }

    static Map<ValueObject.Type, Integer> detect(@Nullable String text) {
        Map<ValueObject.Type, Integer> counts = new EnumMap<>(ValueObject.Type.class);
        find(text).forEach((vo, values) -> counts.put(vo, values.size()));
        return counts;
    }

    static Map<ValueObject.Type, List<ValueObject>> find(@Nullable String text) {
        Map<ValueObject.Type, List<ValueObject>> found = new EnumMap<>(ValueObject.Type.class);
        for (Found f : scan(text)) found.computeIfAbsent(f.type(), k -> new ArrayList<>()).add(f.value());
        return found;
    }

    /** Every value object in the text, in the order they appear, each with the shape that matched it. */
    static List<Found> scan(@Nullable String text) {
        List<Found> found = new ArrayList<>();
        if (text == null || text.isBlank()) return found;
        BitSet claimed = new BitSet(text.length());
        SCAN.forEach((vo, shape) -> {
            Matcher m = shape.matcher(text);
            while (m.find()) {
                if (claimed.get(m.start(), m.end()).cardinality() > 0) continue;
                Optional<ValueObject> value = vo.tryParse(m.group());
                if (value.isEmpty()) continue;
                claimed.set(m.start(), m.end());
                found.add(new Found(vo, value.get(), m.group(), m.start(), m.end(), vo.shape() == null ? "" : vo.shape()));
            }
        });
        found.sort(java.util.Comparator.comparingInt(Found::start));
        return Collections.unmodifiableList(found);
    }

    private record Parsed(ValueObject.Type valueObject, ValueObject value) {}

    private static Optional<Parsed> parse(@Nullable String text) {
        if (text == null || text.isBlank()) return Optional.empty();
        String trimmed = text.trim();
        for (Map.Entry<ValueObject.Type, Pattern> e : WHOLE.entrySet()) {
            if (!e.getValue().matcher(trimmed).matches()) continue;
            Optional<ValueObject> value = e.getKey().tryParse(trimmed);
            if (value.isPresent()) return Optional.of(new Parsed(e.getKey(), value.get()));
        }
        return Optional.empty();
    }
}
