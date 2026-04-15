package com.directharvest.backend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IndianPincodeValidator implements ConstraintValidator<IndianPincode, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        // Indian pincode: 6 digits
        return value.matches("^[0-9]{6}$");
    }
}

