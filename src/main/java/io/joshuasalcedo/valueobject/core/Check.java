package io.joshuasalcedo.valueobject.core;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The checks for one value object, reached as {@code ValueObject.Validate.EMAIL},
 * {@code ValueObject.Validate.MONEY} and so on. Types with enum values add an {@code is} with one
 * {@link Rule} per value, e.g. {@code ValueObject.Validate.GENDER.is.MALE}.
 *
 * @param <T> the value object
 * @author JoshuaSalcedo
 */
public class Check<T extends ValueObject> {

    private final ValueObject.Type valueObject;
    private final Function<@Nullable Object, Optional<T>> parser;

    /** Passes whatever parses as this value object. */
    public final Rule<T> valid;

    /**
     * @param valueObject the value object it checks
     * @param parse       its strict parse, e.g. {@code ValueObject.Parse::Email}
     */
    Check(ValueObject.Type valueObject, Function<@Nullable Object, T> parse) {
        this.valueObject = valueObject;
        this.parser = value -> {
            try {
                return Optional.of(parse.apply(value));
            } catch (RuntimeException e) {
                return Optional.empty();
            }
        };
        this.valid = new Rule<>("a valid " + valueObject, parser, v -> true);
    }

    /** {@return the value object this checks} */
    public ValueObject.Type valueObject() {
        return valueObject;
    }

    /**
     * {@return it parsed as this value object, or empty}
     *
     * @param value anything
     */
    public Optional<T> parse(@Nullable Object value) {
        return parser.apply(value);
    }

    /**
     * {@return whether it parses as this value object}
     *
     * @param value anything
     */
    public boolean isValid(@Nullable Object value) {
        return valid.test(value);
    }

    /**
     * {@return a rule: parse, then test}
     *
     * @param name what the rule checks, for messages
     * @param test the test on the parsed value
     */
    public Rule<T> rule(String name, Predicate<T> test) {
        return new Rule<>(valueObject + " " + name, parser, test);
    }

    /** A rule that the parsed value, or one of its parts, is exactly {@code expected}. */
    Rule<T> isRule(String name, Function<T, @Nullable Object> part, Object expected) {
        return rule("is " + name, v -> Objects.equals(part.apply(v), expected));
    }

    @Override
    public String toString() {
        return "Check[" + valueObject + "]";
    }
}
