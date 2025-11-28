package com.epam.microservices.songservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = YearRangeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface YearRange {
    String message() default "Year must be between 1900 and 2099";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}


