package io.joshuasalcedo.valueobject.format;

import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.financial.Currency;
import io.joshuasalcedo.valueobject.financial.Money;
import io.joshuasalcedo.valueobject.measurement.UnitOfMeasure;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Turns plain values (what {@link Json#parse(String)} gives, or CSV cells) back into Java values of a given type:
 * the reverse of {@link Json#toPlain(Object)}. Hand-written; shared by {@link Json} and {@link Csv}.
 */
final class Values {

    private Values() {}

    /**
     * @param plain a map, list, string, number, boolean or null
     * @param type  the type wanted
     * @return the value, of that type
     * @throws ValueObjectException if it cannot be one
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static @Nullable Object convert(@Nullable Object plain, Type type) {
        Class<?> raw = raw(type);
        if (raw == Optional.class) {
            Object inner = plain == null ? null : convert(plain, argument(type, 0));
            return Optional.ofNullable(inner);
        }
        if (plain == null) return null;
        if (raw == Object.class) return plain;
        if (raw.isInstance(plain) && !(plain instanceof Map) && !(plain instanceof List)) return plain;
        if (raw == String.class) return text(plain);
        if (raw == BigDecimal.class) return new BigDecimal(text(plain).replace(",", "").strip());
        if (raw == BigInteger.class) return new BigInteger(text(plain).strip());
        if (raw == int.class || raw == Integer.class) return new BigDecimal(text(plain).strip()).intValueExact();
        if (raw == long.class || raw == Long.class) return new BigDecimal(text(plain).strip()).longValueExact();
        if (raw == double.class || raw == Double.class) return Double.parseDouble(text(plain).strip());
        if (raw == float.class || raw == Float.class) return Float.parseFloat(text(plain).strip());
        if (raw == boolean.class || raw == Boolean.class) return Boolean.parseBoolean(text(plain).strip());
        if (raw == LocalDate.class) return LocalDate.parse(text(plain).strip());
        if (raw == LocalTime.class) return LocalTime.parse(text(plain).strip());
        if (raw == LocalDateTime.class) return LocalDateTime.parse(text(plain).strip());
        if (raw == Instant.class) return Instant.parse(text(plain).strip());
        if (raw == OffsetDateTime.class) return OffsetDateTime.parse(text(plain).strip());
        if (raw == ZonedDateTime.class) return ZonedDateTime.parse(text(plain).strip());
        if (raw == Duration.class) return Duration.parse(text(plain).strip());
        if (raw == Period.class) return Period.parse(text(plain).strip());
        if (raw == ZoneId.class) return ZoneId.of(text(plain).strip());
        if (raw == UUID.class) return UUID.fromString(text(plain).strip());
        if (raw == byte[].class) return java.util.Base64.getDecoder().decode(text(plain));
        if (List.class.isAssignableFrom(raw) || Set.class.isAssignableFrom(raw)) {
            List<Object> out = new ArrayList<>();
            for (Object o : list(plain)) out.add(convert(o, argument(type, 0)));
            return Set.class.isAssignableFrom(raw) ? Set.copyOf(out) : List.copyOf(out);
        }
        if (Map.class.isAssignableFrom(raw)) {
            if (!(plain instanceof Map<?, ?> m)) throw new ValueObjectException("Expected an object for " + type.getTypeName());
            Map<Object, Object> out = new LinkedHashMap<>();
            m.forEach((k, v) -> out.put(convert(k, argument(type, 0)), convert(v, argument(type, 1))));
            return out;
        }
        if (raw == Money.class && plain instanceof Map<?, ?> m) {
            Currency currency = Currency.parse(text(m.get("currency")));
            return Money.of(new BigDecimal(text(m.get("amount"))), currency);
        }
        if (Currency.class.isAssignableFrom(raw)) return Currency.parse(text(plain).strip());
        if (raw == UnitOfMeasure.class) return UnitOfMeasure.parse(text(plain).strip());
        if (raw.isEnum()) return enumConstant((Class<? extends Enum>) raw, text(plain).strip());
        if (raw.isRecord()) {
            RecordComponent[] parts = raw.getRecordComponents();
            if (plain instanceof Map<?, ?> m) return record(raw, parts, m);
            // a value object of one part, or one with its own parser, from its text
            if (ValueObject.class.isAssignableFrom(raw)) return byParser(raw, text(plain));
            if (parts.length == 1) return newRecord(raw, parts, new Object[] {convert(plain, parts[0].getGenericType())});
        }
        if (ValueObject.class.isAssignableFrom(raw)) return byParser(raw, text(plain));
        throw new ValueObjectException("Cannot read a " + type.getTypeName() + " from " + plain.getClass().getSimpleName());
    }

    /** A value object through its generated parser ({@code ValueObject.Type.X.parse}), which takes text. */
    private static Object byParser(Class<?> raw, String text) {
        ValueObject.Type t = ValueObject.Type.of(raw)
                .orElseThrow(() -> new ValueObjectException("Not a value object this library lists: " + raw.getSimpleName()));
        Object v = t.parse(text);
        if (!raw.isInstance(v)) throw new ValueObjectException("'" + text + "' is a " + v.getClass().getSimpleName() + ", not a " + raw.getSimpleName());
        return v;
    }

    private static Object record(Class<?> raw, RecordComponent[] parts, Map<?, ?> m) {
        Object[] args = new Object[parts.length];
        // a part typed only as "some value object" (Found.value) is the one its sibling "type" names
        Class<?> named = null;
        if (m.get("type") instanceof String t) {
            try {
                named = ValueObject.Type.valueOf(t).type();
            } catch (IllegalArgumentException notAType) {
                named = null;
            }
        }
        for (int i = 0; i < parts.length; i++) {
            Object value = m.get(parts[i].getName());
            Type want = parts[i].getType() == ValueObject.class && named != null ? named : parts[i].getGenericType();
            args[i] = convert(value, want);
            if (args[i] == null && parts[i].getType().isPrimitive()) {
                throw new ValueObjectException("Missing " + parts[i].getName() + " for a " + raw.getSimpleName());
            }
        }
        return newRecord(raw, parts, args);
    }

    private static Object newRecord(Class<?> raw, RecordComponent[] parts, Object[] args) {
        try {
            Class<?>[] types = new Class<?>[parts.length];
            for (int i = 0; i < parts.length; i++) types[i] = parts[i].getType();
            Constructor<?> c = raw.getDeclaredConstructor(types);
            c.setAccessible(true);
            return c.newInstance(args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException r) throw r;
            throw new ValueObjectException("Cannot make a " + raw.getSimpleName() + ": " + e.getCause(), e);
        } catch (ReflectiveOperationException e) {
            throw new ValueObjectException("Cannot make a " + raw.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object enumConstant(Class<? extends Enum> type, String text) {
        String key = text.replace(' ', '_').replace('-', '_').toUpperCase(Locale.ROOT);
        for (Enum e : type.getEnumConstants()) if (e.name().equals(key)) return e;
        if (ValueObject.class.isAssignableFrom(type)) return byParser(type, text);
        throw new ValueObjectException("Not a " + type.getSimpleName() + ": '" + text + "'");
    }

    private static String text(@Nullable Object plain) {
        if (plain == null) throw new ValueObjectException("A value is required");
        if (plain instanceof BigDecimal d) return d.toPlainString();
        return String.valueOf(plain);
    }

    private static List<?> list(Object plain) {
        if (plain instanceof List<?> l) return l;
        if (plain instanceof String s) {
            List<String> out = new ArrayList<>();
            for (String part : s.split(";")) if (!part.isBlank()) out.add(part.strip());
            return out;
        }
        return List.of(plain);
    }

    private static Class<?> raw(Type type) {
        if (type instanceof Class<?> c) return c;
        if (type instanceof ParameterizedType p) return (Class<?>) p.getRawType();
        return Object.class;
    }

    private static Type argument(Type type, int index) {
        if (type instanceof ParameterizedType p && p.getActualTypeArguments().length > index) {
            Type a = p.getActualTypeArguments()[index];
            if (a instanceof java.lang.reflect.WildcardType w) return w.getUpperBounds()[0];
            return a;
        }
        return Object.class;
    }
}
