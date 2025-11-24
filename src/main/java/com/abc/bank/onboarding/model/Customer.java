package com.abc.bank.onboarding.model;

import com.abc.bank.onboarding.dto.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;


@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customer_ssn", columnList = "socialSecurityNumber"),
        @Index(name = "idx_customer_account_number", columnList = "accountNumber")
})
@Getter
@Setter
@ToString
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 15)
    private String phoneNumber;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 2)
    private String nationality;

    @Column(nullable = false)
    private String residentialAddress;

    @Column(nullable = false, unique = true, length = 9)
    private String socialSecurityNumber;

    @Column(nullable = false)
    private String idProofPath;

    @Column(nullable = false)
    private String photoPath;

    @Column(nullable = false, length = 18)
    private String accountNumber;
}