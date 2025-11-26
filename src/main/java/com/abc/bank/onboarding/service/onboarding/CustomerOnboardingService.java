package com.abc.bank.onboarding.service.onboarding;

import com.abc.bank.onboarding.dto.CustomerOnboardRequest;
import com.abc.bank.onboarding.dto.CustomerOnboardResponse;
import com.abc.bank.onboarding.exception.AccountNumberGenerationException;
import com.abc.bank.onboarding.exception.DuplicateCustomerException;
import com.abc.bank.onboarding.exception.UnexpectedOnboardingException;
import com.abc.bank.onboarding.exception.ValidationException;
import com.abc.bank.onboarding.mapper.CustomerMapper;
import com.abc.bank.onboarding.model.Customer;
import com.abc.bank.onboarding.repository.CustomerRepository;
import com.abc.bank.onboarding.service.generator.AccountNumberGenerator;
import com.abc.bank.onboarding.service.notification.NotificationService;
import com.abc.bank.onboarding.service.validation.FileValidationService;
import com.abc.bank.onboarding.service.validation.RequestValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerOnboardingService {

    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final RequestValidationService requestValidationService;
    private final FileValidationService fileValidationService;
    private final CustomerRepository customerRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final NotificationService notificationService;


    @Transactional
    public CustomerOnboardResponse onboard(CustomerOnboardRequest request,
                                           MultipartFile idProof,
                                           MultipartFile photo) {
        try {
            validateRequest(request, idProof, photo);
            checkDuplicate(request.socialSecurityNumber(), request.email());

            Customer customer = CustomerMapper.toCustomer(request, idProof, photo);
            saveCustomerWithRetry(customer);

            notifySuccess(request.email(), customer.getAccountNumber());
            return buildResponse(customer);

        } catch (ValidationException | DuplicateCustomerException ex) {
            handleFailure(request.email(), ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            handleFailure(request.email(), "Unexpected error occurred", ex);
            throw new UnexpectedOnboardingException("Unexpected error occurred", ex);
        }
    }

    private void validateRequest(CustomerOnboardRequest request, MultipartFile idProof, MultipartFile photo) {
        requestValidationService.validate(request);
        fileValidationService.validateFile(idProof, "idProof");
        fileValidationService.validateFile(photo, "photo");
    }


    private void checkDuplicate(String ssn, String email) {
        if (customerRepository.existsBySocialSecurityNumberOrEmail(ssn, email)) {
            throw new DuplicateCustomerException("Customer with same socialSecurityNumber or email already exists");
        }
    }


    private void saveCustomerWithRetry(Customer customer) {
        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                String accountNumber = accountNumberGenerator.generate();
                customer.setAccountNumber(accountNumber);
                customerRepository.save(customer);
                return;
            } catch (DataIntegrityViolationException ex) {
                attempts++;
                log.warn("Account number conflict, retrying... attempt {}", attempts);
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    throw new AccountNumberGenerationException("Failed to generate unique account number after retries");
                }
            }
        }
    }

    private void notifySuccess(String email, String accountNumber) {
        notificationService.notifySuccess(email, accountNumber);
        log.info("Customer onboarded successfully with accountNumber={}", accountNumber);
    }

    private CustomerOnboardResponse buildResponse(Customer customer) {
        return new CustomerOnboardResponse("SUCCESS",
                "Customer onboarded successfully",
                customer.getAccountNumber());
    }

    private void handleFailure(String email, String reason, Exception ex) {
        notificationService.notifyFailure(email, reason);
        log.error("Onboarding failed for email={} reason={}", email, reason, ex);
    }
}