package io.joshuasalcedo.valueobject.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.Serializers;
import io.joshuasalcedo.valueobject.core.ValueObject;
import io.joshuasalcedo.valueobject.core.ValueObjectException;
import io.joshuasalcedo.valueobject.format.Json;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Jackson 2 ({@code com.fasterxml.jackson}) support: every {@link ValueObject} is written and read the way
 * {@link Json} writes it. A value object of one part is its text ({@code "jane@example.com"}), {@code Money} is
 * {@code {"amount": 42.50, "currency": "USD"}}, an enum is its name, a record is an object of its parts.
 *
 * <pre>{@code
 * ObjectMapper mapper = new ObjectMapper().registerModule(new ValueObjectModule());
 * mapper.writeValueAsString(Email.of("jane@example.com"));     // "jane@example.com"
 * mapper.readValue("\"jane@example.com\"", Email.class);       // Email
 * }</pre>
 *
 * <p>Needs {@code com.fasterxml.jackson.core:jackson-databind} 2.x, an optional dependency of this library. For
 * Jackson 3 ({@code tools.jackson}, Spring Boot 4) use {@link io.joshuasalcedo.valueobject.jackson3.ValueObjectModule}.
 * Numbers are read exactly, as {@code BigDecimal}, so amounts keep their scale.
 *
 * @author JoshuaSalcedo
 */
public final class ValueObjectModule extends SimpleModule {

    /** Makes the module. */
    public ValueObjectModule() {
        super("value-object");
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addSerializers(new Serializers.Base() {
            @Override
            public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
                return ValueObject.class.isAssignableFrom(type.getRawClass()) ? WRITER : null;
            }
        });
        context.addDeserializers(new Deserializers.Base() {
            @Override
            public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) {
                return ValueObject.class.isAssignableFrom(type.getRawClass()) ? new Reader(type) : null;
            }

            @Override
            public JsonDeserializer<?> findEnumDeserializer(Class<?> type, DeserializationConfig config, BeanDescription beanDesc) {
                return ValueObject.class.isAssignableFrom(type) ? new Reader(config.constructType(type)) : null;
            }
        });
    }

    private static final JsonSerializer<Object> WRITER = new JsonSerializer<>() {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeObject(Json.toPlain(value));
        }
    };

    /** Reads the JSON as plain values (numbers exact), then makes the value object from them. */
    private static final class Reader extends JsonDeserializer<Object> {
        private final JavaType type;

        Reader(JavaType type) {
            this.type = type;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Object plain = plain(p);
            try {
                return Json.fromPlain(plain, type.getRawClass());
            } catch (ValueObjectException e) {
                return ctxt.handleWeirdStringValue(type.getRawClass(), String.valueOf(plain), e.getMessage());
            }
        }

        private static @Nullable Object plain(JsonParser p) throws IOException {
            JsonToken t = p.currentToken();
            if (t == null) t = p.nextToken();
            switch (t) {
                case START_OBJECT -> {
                    Map<String, Object> out = new LinkedHashMap<>();
                    while (p.nextToken() == JsonToken.FIELD_NAME) {
                        String name = p.currentName();
                        p.nextToken();
                        out.put(name, plain(p));
                    }
                    return out;
                }
                case START_ARRAY -> {
                    List<Object> out = new ArrayList<>();
                    while (p.nextToken() != JsonToken.END_ARRAY) out.add(plain(p));
                    return out;
                }
                case VALUE_STRING -> {
                    return p.getText();
                }
                case VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT -> {
                    return p.getDecimalValue();
                }
                case VALUE_TRUE -> {
                    return Boolean.TRUE;
                }
                case VALUE_FALSE -> {
                    return Boolean.FALSE;
                }
                default -> {
                    return null;
                }
            }
        }
    }
}
