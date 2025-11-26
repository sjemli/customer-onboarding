package com.abc.bank.onboarding.service.onboarding;

import com.abc.bank.onboarding.dto.CustomerOnboardRequest;
import com.abc.bank.onboarding.dto.CustomerOnboardResponse;
import com.abc.bank.onboarding.exception.DuplicateCustomerException;
import com.abc.bank.onboarding.exception.ValidationException;
import com.abc.bank.onboarding.model.Customer;
import com.abc.bank.onboarding.repository.CustomerRepository;
import com.abc.bank.onboarding.service.generator.AccountNumberGenerator;
import com.abc.bank.onboarding.service.notification.NotificationService;
import com.abc.bank.onboarding.service.validation.FileValidationService;
import com.abc.bank.onboarding.service.validation.RequestValidationService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

import static com.abc.bank.onboarding.dto.Gender.MALE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerOnboardingServiceTest {

    @InjectMocks
    private CustomerOnboardingService service;

    @Mock
    private RequestValidationService requestValidationService;
    @Mock
    private FileValidationService fileValidationService;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private AccountNumberGenerator accountNumberGenerator;
    @Mock
    private NotificationService notificationService;

    @Mock
    private MultipartFile idProof;
    @Mock
    private MultipartFile photo;

    private CustomerOnboardRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new CustomerOnboardRequest(
                "Seif",
                "Jemli",
                MALE,
                LocalDate.of(1990, 12, 21),
                "+31612345678",
                "seif.jemli@example.com",
                "FR",
                "Amsterdam, NL",
                "123456782"
        );
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void clear() {
        TransactionSynchronizationManager.clear();
    }

    @SneakyThrows
    @Test
    void should_onboard_customer_successfully_when_all_validations_pass() {
        when(accountNumberGenerator.generate()).thenReturn("ACC123");
        when(customerRepository.existsBySocialSecurityNumberOrEmail(any(), any())).thenReturn(false);

        CustomerOnboardResponse response = service.onboard(request, idProof, photo);

        assertEquals("SUCCESS", response.status());
        assertEquals("ACC123", response.accountNumber());
        verify(notificationService).notifySuccess("seif.jemli@example.com", "ACC123");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void should_fail_onboarding_when_request_validation_fails() {
        doThrow(new ValidationException("Invalid request")).when(requestValidationService).validate(any());

        ValidationException ex = assertThrows(ValidationException.class, () -> service.onboard(request, idProof, photo));
        assertEquals("Invalid request", ex.getMessage());
        verify(notificationService).notifyFailure(eq("seif.jemli@example.com"), contains("Invalid request"));
    }

    @Test
    void should_fail_onboarding_when_duplicate_customer_detected() {
        when(customerRepository.existsBySocialSecurityNumberOrEmail(any(), any())).thenReturn(true);

        DuplicateCustomerException ex = assertThrows(DuplicateCustomerException.class, () -> service.onboard(request, idProof, photo));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(notificationService).notifyFailure(eq("seif.jemli@example.com"), contains("already exists"));
    }


    @SneakyThrows
    @Test
    void should_fail_onboarding_when_unexpected_error_occurs() {
        when(customerRepository.existsBySocialSecurityNumberOrEmail(any(), any())).thenReturn(false);
        when(accountNumberGenerator.generate()).thenReturn("ACC123");
        doThrow(new RuntimeException("DB down")).when(customerRepository).save(any());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.onboard(request, idProof, photo));
        assertTrue(ex.getMessage().contains("Unexpected error occurred"));
        verify(notificationService).notifyFailure(eq("seif.jemli@example.com"), contains("Unexpected error occurred"));
    }

    @SneakyThrows
    @Test
    void should_retry_account_number_generation_when_conflict_occurs() {
        when(customerRepository.existsBySocialSecurityNumberOrEmail(any(), any())).thenReturn(false);
        when(accountNumberGenerator.generate()).thenReturn("ACC123");
        doThrow(new DataIntegrityViolationException("Duplicate"))
                .doThrow(new DataIntegrityViolationException("Duplicate"))
                .doReturn(new Customer())
                .when(customerRepository).save(any());

        CustomerOnboardResponse response = service.onboard(request, idProof, photo);
        assertEquals("ACC123", response.accountNumber());
        verify(customerRepository, times(3)).save(any());
    }


    @SneakyThrows
    @Test
    void should_throw_runtime_exception_when_max_retry_attempts_exceeded() {
        when(customerRepository.existsBySocialSecurityNumberOrEmail(any(), any())).thenReturn(false);
        when(accountNumberGenerator.generate()).thenReturn("ACC123");

        doThrow(new DataIntegrityViolationException("Duplicate"))
                .doThrow(new DataIntegrityViolationException("Duplicate"))
                .doThrow(new DataIntegrityViolationException("Duplicate"))
                .when(customerRepository).save(any());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.onboard(request, idProof, photo));

        assertTrue(ex.getCause().getMessage().contains("Failed to generate unique account number after retries"));
        verify(customerRepository, times(3)).save(any());
        verify(notificationService).notifyFailure(eq("seif.jemli@example.com"), contains("Unexpected error occurred"));
    }

}