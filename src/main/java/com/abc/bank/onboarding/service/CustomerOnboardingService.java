package com.abc.bank.onboarding.service;

import com.abc.bank.onboarding.dto.CustomerOnboardRequest;
import com.abc.bank.onboarding.dto.CustomerOnboardResponse;
import com.abc.bank.onboarding.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CustomerOnboardingService {

    private final CustomerRepository customerRepository;

    public CustomerOnboardResponse onboardCustomer(CustomerOnboardRequest request,
                                                   MultipartFile idProof,
                                                   MultipartFile photo) {

        return null;

    }
}
