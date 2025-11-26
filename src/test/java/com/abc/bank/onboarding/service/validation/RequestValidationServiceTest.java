package com.abc.bank.onboarding.service.validation;

import com.abc.bank.onboarding.dto.CustomerOnboardRequest;
import com.abc.bank.onboarding.exception.ValidationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static com.abc.bank.onboarding.dto.Gender.MALE;
import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestValidationServiceTest {

    private RequestValidationService service;

    static Stream<TestCase> provideRequests() {
        return Stream.of(
                // valid request
                new TestCase(new CustomerOnboardRequest(
                        "Seif", "Jemli", MALE, LocalDate.of(1985, 5, 15),
                        "+31612345678", "seif.jemli@domain.com", "NL",
                        "Gustav Mahlerlaan 10", "123456782"
                ), true, null),

                // empty first name
                new TestCase(new CustomerOnboardRequest(
                        "", "Jemli", MALE, LocalDate.of(1985, 5, 15),
                        "+31612345678", "seif.jemli@domain.com", "NL",
                        "Gustav Mahlerlaan 10", "123456782"
                ), false, "firstName"),

                // wrong email format
                new TestCase(new CustomerOnboardRequest(
                        "Seif", "Jemli", MALE, LocalDate.of(1985, 5, 15),
                        "+31612345678", "invalid-email", "NL",
                        "Gustav Mahlerlaan 10", "123456782"
                ), false, "email"),

                // not adult customer
                new TestCase(new CustomerOnboardRequest(
                        "Seif", "Jemli", MALE, LocalDate.now().minusYears(10),
                        "+31612345678", "seif.jemli@domain.com", "NL",
                        "Gustav Mahlerlaan 10", "123456782"
                ), false, "dateOfBirth")
        );
    }

    @BeforeEach
    void setUp() {
        Validator validator = buildDefaultValidatorFactory().getValidator();
        service = new RequestValidationService(validator);
    }

    @ParameterizedTest(name = "should_{1}_validation_for_request")
    @MethodSource("provideRequests")
    void should_validate_request_based_on_constraints(TestCase testCase) {
        if (testCase.isValid) {
            assertDoesNotThrow(() -> service.validate(testCase.request));
        } else {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> service.validate(testCase.request));
            assertTrue(ex.getMessage().contains(testCase.expectedField),
                    "Expected message to contain field: " + testCase.expectedField);
        }
    }

    private record TestCase(CustomerOnboardRequest request, boolean isValid, String expectedField) {
    }
}
