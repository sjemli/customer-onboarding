package com.abc.bank.onboarding.mapper;


import com.abc.bank.onboarding.dto.CustomerOnboardRequest;
import com.abc.bank.onboarding.model.Customer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import utils.MultipartFileTestUtil;

import java.time.LocalDate;

import static com.abc.bank.onboarding.dto.Gender.MALE;
import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTest {

    @SneakyThrows
    @Test
    void shouldMapDtoToCustomerCorrectly() {
        CustomerOnboardRequest dto = new CustomerOnboardRequest(
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
        MockMultipartFile idProof = MultipartFileTestUtil.createMultipartFile("files/passport.png",
                "idProof");
        MockMultipartFile photo = MultipartFileTestUtil.createMultipartFile("files/photo.png",
                "photo");

        Customer customer = CustomerMapper.toCustomer(dto, idProof, photo);

        assertThat(customer.getFirstName()).isEqualTo(dto.firstName());
        assertThat(customer.getLastName()).isEqualTo(dto.lastName());
        assertThat(customer.getGender()).isEqualTo(dto.gender());
        assertThat(customer.getDateOfBirth()).isEqualTo(dto.dateOfBirth());
        assertThat(customer.getPhoneNumber()).isEqualTo(dto.phoneNumber());
        assertThat(customer.getEmail()).isEqualTo(dto.email());
        assertThat(customer.getNationality()).isEqualTo(dto.nationality());
        assertThat(customer.getResidentialAddress()).isEqualTo(dto.residentialAddress());
        assertThat(customer.getSocialSecurityNumber()).isEqualTo(dto.socialSecurityNumber());
        assertThat(customer.getIdProof()).isEqualTo(idProof.getBytes());
        assertThat(customer.getPhoto()).isEqualTo(photo.getBytes());
    }
}
