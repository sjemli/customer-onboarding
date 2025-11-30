package com.abc.bank.onboarding.controller;

import com.abc.bank.onboarding.dto.CustomerOnboardRequest;
import com.abc.bank.onboarding.dto.Gender;
import com.abc.bank.onboarding.exception.ValidationException;
import com.abc.bank.onboarding.model.Customer;
import com.abc.bank.onboarding.repository.CustomerRepository;
import com.abc.bank.onboarding.service.generator.AccountNumberGenerator;
import com.abc.bank.onboarding.service.notification.NotificationService;
import com.abc.bank.onboarding.service.onboarding.CustomerOnboardingService;
import com.abc.bank.onboarding.service.validation.FileValidationService;
import com.abc.bank.onboarding.service.validation.RequestValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static com.abc.bank.onboarding.mapper.CustomerMapper.toCustomer;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase
class CustomerOnboardingControllerIT {

    public static final String ACCOUNT_NUMBER = "AC123";
    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CustomerOnboardingService customerOnboardingService;
    @MockitoSpyBean
    private CustomerRepository spyCustomerRepository;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private RequestValidationService requestValidationService;
    @MockitoBean
    private FileValidationService fileValidationService;
    @MockitoBean
    private AccountNumberGenerator accountNumberGenerator;

    private CustomerOnboardRequest validRequest;
    private MockMultipartFile validIdProof;
    private MockMultipartFile validPhoto;

    @BeforeEach
    void setup() {
        spyCustomerRepository.deleteAll();
        Mockito.reset(spyCustomerRepository);
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

        validIdProof = new MockMultipartFile(
                "idProof", "id.pdf", "application/pdf", "dummy-id".getBytes()
        );
        validPhoto = new MockMultipartFile(
                "photo", "photo.jpg", "image/jpeg", "dummy-photo".getBytes()
        );
    }

    private MockMultipartFile requestJson(CustomerOnboardRequest req) throws Exception {
        byte[] json = objectMapper.writeValueAsBytes(req);
        return new MockMultipartFile("CustomerOnboardRequest", "request.json",
                "application/json", json);
    }


    @Test
    void should_onboard_customer_successfully_returns_201_using_h2() throws Exception {
        when(accountNumberGenerator.generate()).thenReturn(ACCOUNT_NUMBER);
        mockMvc.perform(
                        multipart("/api/customers/onboard")
                                .file(validIdProof)
                                .file(validPhoto)
                                .file(requestJson(validRequest))
                                .with(csrf())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.accountNumber").value(ACCOUNT_NUMBER));

        var customers = spyCustomerRepository.findAll();
        assertEquals(1, customers.size());

        Customer persistedCustomer = customers.getFirst();
        assertNotNull(persistedCustomer.getId());
        assertEquals(validRequest.firstName(), persistedCustomer.getFirstName());
        assertEquals(validRequest.lastName(), persistedCustomer.getLastName());
        assertEquals(validRequest.email(), persistedCustomer.getEmail());
        assertEquals(validRequest.socialSecurityNumber(), persistedCustomer.getSocialSecurityNumber());

        assertEquals(ACCOUNT_NUMBER, persistedCustomer.getAccountNumber());
        assertArrayEquals(validIdProof.getBytes(), persistedCustomer.getIdProof());
        assertArrayEquals(validPhoto.getBytes(), persistedCustomer.getPhoto());

        verify(notificationService).notifySuccess(validRequest.email(), ACCOUNT_NUMBER);
    }

    @Test
    void should_return_409_when_duplicate_customer_exists_in_h2() throws Exception {
        Customer existing = toCustomer(validRequest, validIdProof, validPhoto);
        existing.setAccountNumber("1234456");
        spyCustomerRepository.save(existing);

        mockMvc.perform(
                        multipart("/api/customers/onboard")
                                .file(validIdProof)
                                .file(validPhoto)
                                .file(requestJson(validRequest))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Customer conflict"))
                .andExpect(jsonPath("$.detail", containsString("already exists")));

        verify(notificationService).notifyFailure(eq(validRequest.email()), contains("already exists"));
    }

    @Test
    void should_return_400_when_request_validation_fails() throws Exception {
        doThrow(new ValidationException("Invalid data")).when(requestValidationService).validate(any());

        mockMvc.perform(
                        multipart("/api/customers/onboard")
                                .file(validIdProof)
                                .file(validPhoto)
                                .file(requestJson(validRequest))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Data"))
                .andExpect(jsonPath("$.detail").value("Invalid data"));

        verify(notificationService).notifyFailure(eq(validRequest.email()), contains("Invalid data"));
    }

    @Test
    void should_return_400_when_missing_part_returns_problem_detail() throws Exception {
        mockMvc.perform(
                        multipart("/api/customers/onboard")
                                .file(validIdProof)
                                .file(requestJson(validRequest))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Missing Request Part"))
                .andExpect(jsonPath("$.detail", containsString("photo")));
    }

    @Test
    void should_return_400_when_malformed_json_returns_problem_detail() throws Exception {
        MockMultipartFile badJson = new MockMultipartFile(
                "CustomerOnboardRequest", "request.json", "application/json",
                "{invalid-json".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/customers/onboard")
                                .file(validIdProof)
                                .file(validPhoto)
                                .file(badJson)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed Request"))
                .andExpect(jsonPath("$.detail")
                        .value("Invalid request body. Please check JSON syntax and field values."));
    }


    @Test
    void should_return_500_on_unexpected_error_with_transaction_rollback() throws Exception {
        when(accountNumberGenerator.generate()).thenReturn(ACCOUNT_NUMBER);

        doThrow(new RuntimeException("DB down")).when(spyCustomerRepository).save(any());

        mockMvc.perform(
                        multipart("/api/customers/onboard")
                                .file(validIdProof)
                                .file(validPhoto)
                                .file(requestJson(validRequest))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."));

        verify(notificationService).notifyFailure(validRequest.email(), "Unexpected error occurred");
    }

    @Test
    void should_return_500_when_max_retry_attempts_exceeded() throws Exception {
        when(accountNumberGenerator.generate()).thenReturn(ACCOUNT_NUMBER);

        doThrow(new DataIntegrityViolationException("duplicate account number"))
                .doThrow(new DataIntegrityViolationException("duplicate account number"))
                .doThrow(new DataIntegrityViolationException("duplicate account number"))
                .when(spyCustomerRepository).save(any());

        mockMvc.perform(
                        multipart("/api/customers/onboard")
                                .file(validIdProof)
                                .file(validPhoto)
                                .file(requestJson(validRequest))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."));

        verify(notificationService).notifyFailure(validRequest.email(), "Unexpected error occurred");
        verify(spyCustomerRepository, times(3)).save(any());
    }



    @Test
    void should_return_400_when_invalid_gender_value() throws Exception {
        String invalidGenderJson = """
        {
          "firstName": "Seif",
          "lastName": "Jemli",
          "gender": "INVALID",
          "dateOfBirth": "1985-05-15",
          "phoneNumber": "+31612345678",
          "email": "seif.jemli@domain.com",
          "country": "NL",
          "address": "Gustav Mahlerlaan 10, 1082 PP Amsterdam, Netherlands",
          "nationalId": "123456782"
        }
        """;

        MockMultipartFile badJson = getBadJsonFile(invalidGenderJson);

        mockMvc.perform(
                        multipart("/api/customers/onboard")
                                .file(validIdProof)
                                .file(validPhoto)
                                .file(badJson)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(csrf())
                                .with(user("tester").roles("USER"))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed Request"))
                .andExpect(jsonPath("$.detail").value("Invalid value for field 'gender'. Allowed values: [MALE, FEMALE, OTHER] (non case sensitive)"));
    }

    @Test
    void should_return_400_when_invalid_date_format() throws Exception {
        MockMultipartFile badJson = getBadJsonFile("""
        {
          "firstName": "Seif",
          "lastName": "Jemli",
          "gender": "MALE",
          "dateOfBirth": "15-05-55",
          "phoneNumber": "+31612345678",
          "email": "seif.jemli@domain.com",
          "country": "NL",
          "address": "Gustav Mahlerlaan 10, 1082 PP Amsterdam, Netherlands",
          "nationalId": "123456782"
        }
        """);

        mockMvc.perform(
                        multipart("/api/customers/onboard")
                                .file(validIdProof)
                                .file(validPhoto)
                                .file(badJson)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(csrf())
                                .with(user("tester").roles("USER"))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed Request"))
                .andExpect(jsonPath("$.detail")
                        .value("Invalid value for field 'dateOfBirth'. Expected type: LocalDate"));
    }

    @Test
    void should_return_400_when_malformed_json() throws Exception {
        MockMultipartFile badJson = getBadJsonFile("{invalid-json");

        mockMvc.perform(
                        multipart("/api/customers/onboard")
                                .file(validIdProof)
                                .file(validPhoto)
                                .file(badJson)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(csrf())
                                .with(user("tester").roles("USER"))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed Request"))
                .andExpect(jsonPath("$.detail").value("Invalid request body. Please check JSON syntax and field values."));
    }

    private static MockMultipartFile getBadJsonFile(String badJson) {

        return new MockMultipartFile(
                "CustomerOnboardRequest", "request.json", "application/json",
                badJson.getBytes()
        );
    }
}