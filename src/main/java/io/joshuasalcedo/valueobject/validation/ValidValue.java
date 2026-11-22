package io.joshuasalcedo.valueobject.validation;

import io.joshuasalcedo.valueobject.ValueObject;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated text must be a valid value object of the given type. Null passes (combine with {@code @NotNull});
 * a blank string fails.
 *
 * <pre>{@code
 * record SignUp(@ValidValue(ValueObject.Type.EMAIL) String email,
 *               @ValidValue(ValueObject.Type.PHONE_NUMBER) String phone) {}
 * }</pre>
 *
 * <p>For the common ones there are shortcuts: {@link ValidEmail}, {@link ValidPhoneNumber}, {@link ValidIban},
 * {@link ValidTaxId}, {@link ValidMoney}. Needs {@code jakarta.validation:jakarta.validation-api} 3.x, an optional
 * dependency of this library, and a provider such as Hibernate Validator.
 *
 * @author JoshuaSalcedo
 */
@Documented
@Constraint(validatedBy = ValueObjectValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT,
        ElementType.TYPE_USE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ValidValue.List.class)
public @interface ValidValue {

    /**
     * The value object the text must be.
     *
     * @return its type
     */
    ValueObject.Type value();

    /**
     * The message when it is not.
     *
     * @return the message; {@code {value}} is the type
     */
    String message() default "must be a valid {value}";

    /**
     * The validation groups it belongs to.
     *
     * @return the groups
     */
    Class<?>[] groups() default {};

    /**
     * The payload, for clients of the constraint.
     *
     * @return the payload
     */
    Class<? extends Payload>[] payload() default {};

    /** Several {@link ValidValue} on one element. */
    @Documented
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT,
            ElementType.TYPE_USE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        /**
         * The constraints.
         *
         * @return them
         */
        ValidValue[] value();
    }
}
