package com.abc.bank.onboarding.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BsnValidatorTest {

    private final BsnValidator validator = new BsnValidator();
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    @Test
    void should_return_true_for_valid_BSN() {
        assertTrue(validator.isValid("123456782", context));
    }

    @Test
    void should_return_false_for_BSN_with_length_not_equal_to_nine() {
        assertFalse(validator.isValid("12345678", context));
    }

    @Test
    void should_return_false_for_BSN_with_mom_valid_elevenProefTest() {
        assertFalse(validator.isValid("123456789", context));
    }

    @Test
    void should_return_false_for_non_numeric_BSN() {
        assertFalse(validator.isValid("12345678a", context));
    }

    @Test
    void should_return_false_for_null_BSN() {
        assertFalse(validator.isValid(null, context));
    }
}