
package com.abc.bank.onboarding.repository;

import com.abc.bank.onboarding.dto.Gender;
import com.abc.bank.onboarding.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
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
        customer.setIdProofPath("/path/id.pdf");
        customer.setPhotoPath("/path/photo.jpg");
        customer.setAccountNumber("NL12YYYY0123456789");

        customerRepository.save(customer);
    }

    @Test
    void should_find_customer_by_social_security_number() {
        Optional<Customer> found = customerRepository.findBySocialSecurityNumber("123456782");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("seif.jemli@domain.com");
    }

    @Test
    void should_find_customer_by_account_number() {
        Optional<Customer> found = customerRepository.findByAccountNumber("NL12YYYY0123456789");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("seif.jemli@domain.com");
    }
}