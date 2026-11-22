package io.joshuasalcedo.valueobject;

import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A yes/no question about a value, asked of anything: the input is parsed first (a {@code String},
 * the value object itself, or whatever its factories take), then tested.
 *
 * <pre>{@code
 * ValueObject.Validate.MONEY.is.USD.test("149.99 USD");     // true
 * ValueObject.Validate.GENDER.is.MALE.test(Gender.FEMALE);  // false
 * ValueObject.Validate.EMAIL.valid.test("nope");            // false
 * }</pre>
 *
 * <p>Input that does not parse is simply {@code false}, never an exception.
 *
 * @param <T> the value object it tests
 * @author JoshuaSalcedo
 */
public final class Rule<T extends ValueObject> implements Predicate<@Nullable Object> {

    private final String description;
    private final Function<@Nullable Object, Optional<T>> parser;
    private final Predicate<T> test;

    Rule(String description, Function<@Nullable Object, Optional<T>> parser, Predicate<T> test) {
        this.description = description;
        this.parser = parser;
        this.test = test;
    }

    /**
     * @param value anything
     * @return whether it parses as {@code T} and passes
     */
    @Override
    public boolean test(@Nullable Object value) {
        return parser.apply(value).filter(test).isPresent();
    }

    /**
     * @param value anything
     * @return the parsed value when it passes, else empty
     */
    public Optional<T> match(@Nullable Object value) {
        return parser.apply(value).filter(test);
    }

    /**
     * @param value anything
     * @return the parsed value
     * @throws io.joshuasalcedo.valueobject.exception.ValueObjectException if it does not parse or does not pass
     */
    public T require(@Nullable Object value) {
        return match(value).orElseThrow(() ->
                new io.joshuasalcedo.valueobject.exception.ValueObjectException(
                        "'" + value + "' is not " + description));
    }

    /** @return what it checks, e.g. {@code "MONEY is USD"} */
    @Override
    public String toString() {
        return description;
    }
}
