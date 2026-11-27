package io.joshuasalcedo.valueobject.validation;

import io.joshuasalcedo.valueobject.core.ValueObject;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated text must be an amount with its currency, e.g. {@code "149.99 USD"}. Null passes (combine with {@code @NotNull}).
 * The same as {@code @ValidValue(ValueObject.Type.MONEY)}.
 *
 * @author JoshuaSalcedo
 */
@Documented
@ValidValue(ValueObject.Type.MONEY)
@Constraint(validatedBy = {})
@ReportAsSingleViolation
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT,
        ElementType.TYPE_USE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMoney {

    /**
     * The message when it is not.
     *
     * @return the message
     */
    String message() default "must be an amount with its currency";

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
}
