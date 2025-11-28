package com.epam.microservices.songservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class DurationFormatValidator implements ConstraintValidator<DurationFormat, String> {

    private static final Pattern DURATION_PATTERN = Pattern.compile("^\\d{2}:\\d{2}$");

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
        
        if (!DURATION_PATTERN.matcher(value).matches()) {
            return false;
        }
        
        // Validate that seconds are between 00 and 59
        String[] parts = value.split(":");
        try {
            int seconds = Integer.parseInt(parts[1]);
            if (seconds < 0 || seconds > 59) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        
        return true;
    }
}


