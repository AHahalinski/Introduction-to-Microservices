package com.epam.microservices.songservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class YearRangeValidator implements ConstraintValidator<YearRange, String> {

    private static final Pattern YEAR_PATTERN = Pattern.compile("^\\d{4}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null values should be validated by @NotNull, not by this validator
        if (value == null) {
            return true;
        }
        
        // Empty string is invalid
        if (value.isEmpty()) {
            return false;
        }
        
        if (!YEAR_PATTERN.matcher(value).matches()) {
            return false;
        }

        try {
            int year = Integer.parseInt(value);
            return year >= 1900 && year <= 2099;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}


