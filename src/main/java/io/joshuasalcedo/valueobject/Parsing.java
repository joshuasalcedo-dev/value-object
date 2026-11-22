package io.joshuasalcedo.valueobject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.function.Function;

/**
 * The shared steps behind every generated {@code ValueObject.Parse} method. Hand-written.
 */
final class Parsing {

    private Parsing() {}

    /**
     * @param type     the value object being made
     * @param javaType its class
     * @param value    the input
     * @param attempt  the generated branches: the made value, or null when no factory takes this input
     * @return the value object
     * @throws ValueObjectException if the input is null, of a type no factory takes, or rejected
     */
    static <T> T parse(ValueObject.Type type, Class<T> javaType, @Nullable Object value,
                       Function<Object, @Nullable T> attempt) {
        if (value == null) throw new ValueObjectException(type + " cannot be made from null");
        if (javaType.isInstance(value)) return javaType.cast(value);
        T made;
        try {
            made = attempt.apply(value);
        } catch (ValueObjectException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ValueObjectException("Not a valid " + type + ": '" + value + "' (" + e.getMessage() + ")", e);
        }
        if (made == null) {
            throw new ValueObjectException("Not a valid " + type + ": '" + value + "' ("
                    + value.getClass().getSimpleName() + ")");
        }
        return made;
    }

    /**
     * Tries each factory in turn: the first non-null result wins. If none gives a value and one threw, that
     * (last) exception is thrown, so the caller reports why.
     */
    @SafeVarargs
    static <I, R> @Nullable R firstOf(I input, Function<I, @Nullable R>... attempts) {
        RuntimeException last = null;
        for (Function<I, @Nullable R> attempt : attempts) {
            try {
                R result = attempt.apply(input);
                if (result != null) return result;
            } catch (RuntimeException e) {
                last = e;
            }
        }
        if (last != null) throw last;
        return null;
    }

    /** @return {@code value}, or null when it is the type's UNKNOWN placeholder (so it counts as a failure) */
    static <T> @Nullable T notUnknown(@Nullable T value, T unknown) {
        return value == unknown ? null : value;
    }

    /** For enums without a String factory: the constant with that name, any case, spaces or dashes for underscores. */
    static <E extends Enum<E>> @Nullable E constant(Class<E> type, String name) {
        String key = name.trim().replace(' ', '_').replace('-', '_').toUpperCase(Locale.ROOT);
        for (E e : type.getEnumConstants()) {
            if (e.name().equals(key)) return e;
        }
        return null;
    }
}
