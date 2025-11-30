package com.abc.bank.onboarding.controller;

import com.abc.bank.onboarding.dto.CustomerOnboardRequest;
import com.abc.bank.onboarding.dto.CustomerOnboardResponse;
import com.abc.bank.onboarding.dto.Gender;
import com.abc.bank.onboarding.exception.DuplicateCustomerException;
import com.abc.bank.onboarding.exception.ValidationException;
import com.abc.bank.onboarding.service.onboarding.CustomerOnboardingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerOnboardingController.class)
class CustomerOnboardingControllerTest {

    private static final String ACCOUNT_NUMBER = "ACC123";
    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CustomerOnboardingService customerOnboardingService;
    private CustomerOnboardRequest validRequest;
    private MockMultipartFile idProof;
    private MockMultipartFile photo;

    @BeforeEach
    void setUp() {

        validRequest = new CustomerOnboardRequest(
                "Seif",
                "Jemli",
                Gender.MALE,
                LocalDate.of(1985, 5, 15),
                "+31612345678",
                "seif.jemli@domain.com",
                "NL",
                "Gustav Mahlerlaan 10, 1082 PP Amsterdam, Netherlands",
                "123456782"
        );

        idProof = new MockMultipartFile("idProof", "id.pdf", "application/pdf", "id-content".getBytes());
        photo = new MockMultipartFile("photo", "photo.jpg", "image/jpeg", "photo-content".getBytes());
    }

    private MockMultipartFile requestJson(CustomerOnboardRequest req) throws Exception {
        byte[] json = objectMapper.writeValueAsBytes(req);
        return new MockMultipartFile("CustomerOnboardRequest", "request.json", "application/json", json);
    }

    @Test
    void should_return_201_and_response_when_onboarding_successful() throws Exception {
        CustomerOnboardResponse response =
                new CustomerOnboardResponse("SUCCESS", "Customer onboarded successfully", ACCOUNT_NUMBER);
        when(customerOnboardingService.onboard(any(), any(), any())).thenReturn(response);

        mockMvc.perform(
                        multipart("/api/customers/onboard")
                                .file(idProof)
                                .file(photo)
                                .file(requestJson(validRequest))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(csrf())
                                .with(user("tester").roles("USER"))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.accountNumber").value(ACCOUNT_NUMBER));

        verify(customerOnboardingService, times(1)).onboard(any(), any(), any());
    }

    @Test
    void should_return_400_when_validation_exception_thrown() throws Exception {
        when(customerOnboardingService.onboard(any(), any(), any()))
                .thenThrow(new ValidationException("Invalid data"));

        mockMvc.perform(
                        multipart("/api/customers/onboard")
                                .file(idProof)
                                .file(photo)
                                .file(requestJson(validRequest))
                                .with(csrf())
                                .with(user("tester").roles("USER"))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Data"))
                .andExpect(jsonPath("$.detail").value("Invalid data"));
        verify(customerOnboardingService, times(1)).onboard(any(), any(), any());
    }

    @Test
    void should_return_409_when_duplicate_customer_exception_thrown() throws Exception {
        when(customerOnboardingService.onboard(any(), any(), any()))
                .thenThrow(new DuplicateCustomerException("Customer already exists"));

        mockMvc.perform(
                        multipart("/api/customers/onboard")
                                .file(idProof)
                                .file(photo)
                                .file(requestJson(validRequest))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(csrf())
                                .with(user("tester").roles("USER"))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Customer conflict"))
                .andExpect(jsonPath("$.detail").value("Customer already exists"));
        verify(customerOnboardingService, times(1)).onboard(any(), any(), any());

    }

    @Test
    void should_return_400_when_missing_part() throws Exception {
        mockMvc.perform(
                        multipart("/api/customers/onboard")
                                .file(idProof)
                                .file(requestJson(validRequest))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(csrf())
                                .with(user("tester").roles("USER"))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Missing Request Part"))
                .andExpect(jsonPath("$.detail").value("Missing required part: photo"));
        verifyNoInteractions(customerOnboardingService);

    }

    @Test
    void should_return_500_when_unexpected_error_occurs() throws Exception {
        when(customerOnboardingService.onboard(any(), any(), any()))
                .thenThrow(new RuntimeException("Unexpected"));

        mockMvc.perform(
                        multipart("/api/customers/onboard")
                                .file(idProof)
                                .file(photo)
                                .file(requestJson(validRequest))
                                .with(csrf())
                                .with(user("tester").roles("USER"))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."));
        verify(customerOnboardingService, times(1)).onboard(any(), any(), any());
    }
}