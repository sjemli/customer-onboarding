package com.abc.bank.onboarding.mapper;

import com.abc.bank.onboarding.dto.CustomerOnboardRequest;
import com.abc.bank.onboarding.model.Customer;
import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@UtilityClass
public class CustomerMapper {

    public static Customer toCustomer(CustomerOnboardRequest request,
                                      MultipartFile idProof,
                                      MultipartFile photo) throws IOException {
        Customer customer = new Customer();
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setGender(request.gender());
        customer.setDateOfBirth(request.dateOfBirth());
        customer.setPhoneNumber(request.phoneNumber());
        customer.setEmail(request.email());
        customer.setNationality(request.nationality());
        customer.setResidentialAddress(request.residentialAddress());
        customer.setSocialSecurityNumber(request.socialSecurityNumber());
        customer.setIdProof(idProof.getBytes());
        customer.setPhoto(photo.getBytes());
        return customer;
    }
}
