package com.epam.microservices.songservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DurationFormatValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DurationFormat {
    String message() default "Duration must be in mm:ss format with leading zeros";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}


