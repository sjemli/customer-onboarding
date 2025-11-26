package com.abc.bank.onboarding.repository;

import com.abc.bank.onboarding.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByAccountNumber(String accountNumber);

    boolean existsBySocialSecurityNumberOrEmail(String socialSecurityNumber, String email);
}
