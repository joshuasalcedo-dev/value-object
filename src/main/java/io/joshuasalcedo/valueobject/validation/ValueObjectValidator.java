package io.joshuasalcedo.valueobject.validation;

import io.joshuasalcedo.valueobject.core.ValueObject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jspecify.annotations.Nullable;

/**
 * Checks {@link ValidValue}: the text must parse as the value object the annotation names. Null passes; a blank string
 * fails.
 *
 * @author JoshuaSalcedo
 */
public class ValueObjectValidator implements ConstraintValidator<ValidValue, CharSequence> {

    private ValueObject.Type type = ValueObject.Type.EMAIL;

    /** Makes the validator; the provider calls {@link #initialize(ValidValue)} before using it. */
    public ValueObjectValidator() {
    }

    /**
     * Takes the type to check for from the annotation.
     *
     * @param annotation the annotation on the element
     */
    @Override
    public void initialize(ValidValue annotation) {
        this.type = annotation.value();
    }

    /**
     * Checks one value.
     *
     * @param value   the text, or null
     * @param context the validation context
     * @return true for null, or text that parses as the type
     */
    @Override
    public boolean isValid(@Nullable CharSequence value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return type.tryParse(value.toString()).isPresent();
    }
}
