package com.abc.bank.onboarding.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AdultValidatorTest {

    private final AdultValidator validator = new AdultValidator();
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    @Test
    void should_return_true_for_age_equal_to_18() {
        LocalDate dateOfBirth = LocalDate.now().minusYears(18);
        assertTrue(validator.isValid(dateOfBirth, context));
    }

    @Test
    void should_return_false_for_age_less_than_18() {
        LocalDate dateOfBirth = LocalDate.now().minusYears(17);
        assertFalse(validator.isValid(dateOfBirth, context));
    }

    @Test
    void should_return_true_for_age_greater_than_18() {
        LocalDate dateOfBirth = LocalDate.now().minusYears(25);
        assertTrue(validator.isValid(dateOfBirth, context));
    }

    @Test
    void should_return_false_for_future_date_of_birth() {
        LocalDate dateOfBirth = LocalDate.now().plusDays(1);
        assertFalse(validator.isValid(dateOfBirth, context));
    }

    @Test
    void should_return_false_for_null_date_of_birth() {
        assertFalse(validator.isValid(null, context));
    }
}