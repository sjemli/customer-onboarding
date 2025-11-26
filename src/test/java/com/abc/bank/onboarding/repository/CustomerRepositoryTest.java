package com.abc.bank.onboarding.repository;

import com.abc.bank.onboarding.dto.Gender;
import com.abc.bank.onboarding.model.Customer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.mock.web.MockMultipartFile;
import utils.MultipartFileTestUtil;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        MockMultipartFile idProof = MultipartFileTestUtil.createMultipartFile("files/passport.png",
                "idProof");
        MockMultipartFile photo = MultipartFileTestUtil.createMultipartFile("files/photo.png",
                "photo");

        Customer customer = new Customer();
        customer.setFirstName("Seif");
        customer.setLastName("Jemli");
        customer.setGender(Gender.MALE);
        customer.setDateOfBirth(LocalDate.of(1985, 5, 15));
        customer.setPhoneNumber("+31612345678");
        customer.setEmail("seif.jemli@domain.com");
        customer.setNationality("NL");
        customer.setResidentialAddress("Amsterdam");
        customer.setSocialSecurityNumber("123456782");
        customer.setIdProof(idProof.getBytes());
        customer.setPhoto(photo.getBytes());
        customer.setAccountNumber("NL12YYYY012345678");

        customerRepository.save(customer);
    }


    @Test
    void should_find_customer_by_account_number() {
        Optional<Customer> found = customerRepository.findByAccountNumber("NL12YYYY012345678");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("seif.jemli@domain.com");
    }

    @Test
    void should_check_if_exists_customer_by_social_security_number_or_email() {
        boolean found =
                customerRepository.existsBySocialSecurityNumberOrEmail("123456782",
                        "a@gmail.com");

        assertThat(found).isTrue();
    }
}