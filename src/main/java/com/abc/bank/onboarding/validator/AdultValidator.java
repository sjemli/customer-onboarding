package com.abc.bank.onboarding.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AdultValidator implements ConstraintValidator<Adult, LocalDate> {

    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext context) {
        return dateOfBirth != null && Period.between(dateOfBirth, LocalDate.now()).getYears() >= 18;
    }
}