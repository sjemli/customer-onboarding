package com.abc.bank.onboarding.service.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class AccountNumberGeneratorTest {

    private final AccountNumberGenerator generator = new AccountNumberGenerator();

    @Test
    void should_generate_valid_iban_format() {
        String iban = generator.generate();

        assertNotNull(iban);
        assertFalse(iban.isBlank());
        assertTrue(iban.startsWith("NL"));

        assertEquals(17, iban.length());

        String digitsAfterNL = iban.substring(2, 4);
        assertTrue(digitsAfterNL.matches("\\d{2}"));

        String bankCode = iban.substring(4, 7);
        assertEquals("ABC", bankCode);

        String accountNumberPart = iban.substring(7);
        assertTrue(accountNumberPart.matches("\\d{10}"));
    }

    @Test
    void should_generate_unique_values_on_multiple_calls() {
        String iban1 = generator.generate();
        String iban2 = generator.generate();
        assertNotEquals(iban1, iban2);
    }
}
