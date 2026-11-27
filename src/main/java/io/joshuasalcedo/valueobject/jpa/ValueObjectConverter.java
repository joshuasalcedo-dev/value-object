package io.joshuasalcedo.valueobject.jpa;

import io.joshuasalcedo.valueobject.core.ValueObject;
import io.joshuasalcedo.valueobject.core.ValueObjectException;
import io.joshuasalcedo.valueobject.format.Csv;
import jakarta.persistence.AttributeConverter;
import org.jspecify.annotations.Nullable;

/**
 * Stores a value object in one text column, as the text its {@code parse} reads back: {@code jane@example.com},
 * {@code 42.50 USD}, {@code 2.5 kg}. The converters in {@link Converters} extend it; extend it for your own:
 *
 * <pre>{@code
 * @Converter(autoApply = true)
 * public class SlugConverter extends ValueObjectConverter<Slug> {
 *     public SlugConverter() { super(Slug.class); }
 * }
 * }</pre>
 *
 * <p>Needs {@code jakarta.persistence:jakarta.persistence-api} 3.x, an optional dependency of this library.
 *
 * @param <T> the value object
 * @author JoshuaSalcedo
 */
public abstract class ValueObjectConverter<T extends ValueObject> implements AttributeConverter<T, String> {

    private final Class<T> type;

    /**
     * Makes a converter for a value object.
     *
     * @param type the value object's class
     */
    protected ValueObjectConverter(Class<T> type) {
        this.type = type;
    }

    /**
     * Writes a value object as its text.
     *
     * @param value the value, or null
     * @return its text, or null for null
     */
    @Override
    public @Nullable String convertToDatabaseColumn(@Nullable T value) {
        return value == null ? null : text(value);
    }

    /**
     * Reads a value object back from its text.
     *
     * @param column the column's text, or null
     * @return the value, or null for null or blank text
     * @throws ValueObjectException if the text is not one (the column holds something else)
     */
    @Override
    public @Nullable T convertToEntityAttribute(@Nullable String column) {
        if (column == null || column.isBlank()) return null;
        return type.cast(read(column));
    }

    /**
     * The text a value is stored as; by default as {@link Csv#cell(Object)} writes it, which its {@code parse} reads.
     *
     * @param value the value
     * @return its text
     */
    protected String text(T value) {
        return Csv.cell(value);
    }

    /**
     * Reads a value from its stored text; by default through the generated {@code ValueObject.Type} parser.
     *
     * @param column the text
     * @return the value
     */
    protected Object read(String column) {
        return ValueObject.Type.of(type)
                .orElseThrow(() -> new ValueObjectException("Not a listed value object: " + type.getSimpleName()))
                .parse(column);
    }

    /**
     * The value object's class.
     *
     * @return the class this converter stores
     */
    public Class<T> type() {
        return type;
    }
}
