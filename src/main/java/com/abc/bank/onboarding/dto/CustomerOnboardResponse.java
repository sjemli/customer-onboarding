package com.abc.bank.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Customer onboarding response")
public record CustomerOnboardResponse(

        @Schema(description = "Status of the onboarding", example = "SUCCESS")
        String status,

        @Schema(description = "Message about the onboarding result", example = "Customer onboarded successfully")
        String message,

        @Schema(description = "New account number for the customer", example = "NL12YYYY0123456789")
        String accountNumber
) {
}