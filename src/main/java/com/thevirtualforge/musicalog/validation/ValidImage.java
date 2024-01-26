package com.thevirtualforge.musicalog.validation;

import com.thevirtualforge.musicalog.validation.constraint.ValidImageValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ValidImageValidator.class)
public @interface ValidImage {
    int width();
    int height();
    String[] contentTypes() default {"image/png", "image/jpeg"};
    String message() default "must be an image, size {width}x{height}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}