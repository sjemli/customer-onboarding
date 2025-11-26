package com.abc.bank.onboarding.exception;

public class UnexpectedOnboardingException extends RuntimeException {
    public UnexpectedOnboardingException(String message, Throwable cause) {
        super(message, cause);
    }
}