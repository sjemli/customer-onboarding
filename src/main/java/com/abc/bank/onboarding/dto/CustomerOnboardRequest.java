package com.abc.bank.onboarding.dto;

import com.abc.bank.onboarding.validator.Adult;
import com.abc.bank.onboarding.validator.Bsn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Schema(description = "Customer onboarding request details")
public record CustomerOnboardRequest(

        @Schema(description = "First name", example = "Seif")
        @NotBlank(message = "{firstName.required}")
        @Pattern(regexp = "^[a-zA-Z ]+$", message = "{firstName.alphaSpace}")
        String firstName,

        @Schema(description = "Last name", example = "Jemli")
        @NotBlank(message = "{lastName.required}")
        @Pattern(regexp = "^[a-zA-Z ]+$", message = "{lastName.alphaSpace}")
        String lastName,

        @Schema(description = "Gender", example = "MALE", allowableValues = {"MALE", "FEMALE", "OTHER"})
        @NotNull(message = "{gender.required}")
        Gender gender,

        @Schema(description = "Date of birth", example = "1985-05-15")
        @Adult(message = "{dob.adult}")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dateOfBirth,

        @Schema(description = "Dutch phone number", examples = {"+31612345678", "0612345678"})
        @NotBlank(message = "{phone.required}")
        @Pattern(regexp = "^(\\+31|0)[1-9]\\d{8}$", message = "{phone.invalid}")
        String phoneNumber,

        @Schema(description = "Email", example = "seif.jemli@domain.com")
        @Email(message = "{email.invalid}")
        @NotBlank(message = "{email.required}")
        String email,

        @Schema(description = "Nationality ISO code", example = "NL")
        @NotBlank(message = "{nationality.required}")
        @Pattern(regexp = "^[A-Z]{2}$", message = "{nationality.invalid}")
        String nationality,

        @Schema(description = "Residential address", example = "Gustav Mahlerlaan 10, 1082 PP Amsterdam, Netherlands")
        @NotBlank(message = "{address.required}")
        String residentialAddress,

        @Schema(description = "Dutch BSN", example = "123456782")
        @Bsn(message = "{bsn.invalid}")
        @NotBlank(message = "{bsn.required}")
        String socialSecurityNumber
) {
}