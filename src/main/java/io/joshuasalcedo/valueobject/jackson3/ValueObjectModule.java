package io.joshuasalcedo.valueobject.jackson3;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.joshuasalcedo.valueobject.ValueObject;
import io.joshuasalcedo.valueobject.exception.ValueObjectException;
import io.joshuasalcedo.valueobject.format.Json;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.deser.Deserializers;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.Serializers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Jackson 3 ({@code tools.jackson}, as Spring Boot 4 uses) support: every {@link ValueObject} is written and read the
 * way {@link Json} writes it. A value object of one part is its text ({@code "jane@example.com"}), {@code Money} is
 * {@code {"amount": 42.50, "currency": "USD"}}, an enum is its name, a record is an object of its parts.
 *
 * <pre>{@code
 * JsonMapper mapper = JsonMapper.builder().addModule(new ValueObjectModule()).build();
 * mapper.writeValueAsString(Email.of("jane@example.com"));     // "jane@example.com"
 * mapper.readValue("\"jane@example.com\"", Email.class);       // Email
 * }</pre>
 *
 * <p>Needs {@code tools.jackson.core:jackson-databind} 3.x, an optional dependency of this library. In Spring Boot 4,
 * declaring this module as a bean registers it. For Jackson 2 use
 * {@link io.joshuasalcedo.valueobject.jackson.ValueObjectModule}. Numbers are read exactly, as {@code BigDecimal}.
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
            public ValueSerializer<?> findSerializer(SerializationConfig config, JavaType type,
                                                     BeanDescription.Supplier beanDesc, JsonFormat.Value formatOverrides) {
                return ValueObject.class.isAssignableFrom(type.getRawClass()) ? WRITER : null;
            }
        });
        context.addDeserializers(new Deserializers.Base() {
            @Override
            public ValueDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config,
                                                             BeanDescription.Supplier beanDesc) {
                return ValueObject.class.isAssignableFrom(type.getRawClass()) ? new Reader(type) : null;
            }

            @Override
            public ValueDeserializer<?> findEnumDeserializer(JavaType type, DeserializationConfig config,
                                                             BeanDescription.Supplier beanDesc) {
                return ValueObject.class.isAssignableFrom(type.getRawClass()) ? new Reader(type) : null;
            }

            @Override
            public boolean hasDeserializerFor(DeserializationConfig config, Class<?> valueType) {
                return ValueObject.class.isAssignableFrom(valueType);
            }
        });
    }

    private static final ValueSerializer<Object> WRITER = new ValueSerializer<>() {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            gen.writePOJO(Json.toPlain(value));
        }
    };

    /** Reads the JSON as plain values (numbers exact), then makes the value object from them. */
    private static final class Reader extends ValueDeserializer<Object> {
        private final JavaType type;

        Reader(JavaType type) {
            this.type = type;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            Object plain = plain(p);
            try {
                return Json.fromPlain(plain, type.getRawClass());
            } catch (ValueObjectException e) {
                return ctxt.handleWeirdStringValue(type.getRawClass(), String.valueOf(plain), e.getMessage());
            }
        }

        private static @Nullable Object plain(JsonParser p) throws JacksonException {
            JsonToken t = p.currentToken();
            if (t == null) t = p.nextToken();
            switch (t) {
                case START_OBJECT -> {
                    Map<String, Object> out = new LinkedHashMap<>();
                    while (p.nextToken() == JsonToken.PROPERTY_NAME) {
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
                    return p.getString();
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
