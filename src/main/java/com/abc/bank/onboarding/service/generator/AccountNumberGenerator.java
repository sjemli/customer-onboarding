package com.abc.bank.onboarding.service.generator;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String COUNTRY_CODE = "NL";
    private static final String BANK_CODE = "ABC";
    private static final int ACCOUNT_NUMBER_LENGTH = 10;


    //Dutch IBAN format: NL kk BANK_CODE ACCOUNT_NUMBER
    public String generate() {

        StringBuilder accountNumber = new StringBuilder();
        for (int i = 0; i < ACCOUNT_NUMBER_LENGTH; i++) {
            accountNumber.append(RANDOM.nextInt(10));
        }
        StringBuilder initialDigits = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            initialDigits.append(RANDOM.nextInt(10));
        }

        return COUNTRY_CODE + initialDigits + BANK_CODE + accountNumber;
    }
}
