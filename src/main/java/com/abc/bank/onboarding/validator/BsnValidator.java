package com.abc.bank.onboarding.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BsnValidator implements ConstraintValidator<Bsn, String> {

    //11-proef validation for burgerservicenummer (BSN)
    private static boolean hasValidElevenProefTest(String bsn) {

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            char c = bsn.charAt(i);
            if (!Character.isDigit(c)) return false;
            int digit = c - '0';
            if (i < 8) {
                sum += (9 - i) * digit;
            } else {
                sum -= digit;
            }
        }
        return sum % 11 == 0;
    }

    @Override
    public boolean isValid(String bsn, ConstraintValidatorContext context) {
        return bsn != null && bsn.length() == 9 && hasValidElevenProefTest(bsn);
    }
}