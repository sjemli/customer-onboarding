package com.abc.bank.onboarding.controller;

import com.abc.bank.onboarding.dto.CustomerOnboardRequest;
import com.abc.bank.onboarding.dto.CustomerOnboardResponse;
import com.abc.bank.onboarding.service.onboarding.CustomerOnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerOnboardingController {

    private final CustomerOnboardingService customerOnboardingService;


    @PostMapping(value = "/onboard", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            method = "POST",
            summary = "Onboard a new customer",
            description = "Submits customer details and files for onboarding"
    )
    @ApiResponse(responseCode = "201", description = "Onboarding successful")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "409", description = "Customer already exists")
    @ApiResponse(responseCode = "500", description = "Internal Server Problem")
    @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            encoding = {@Encoding(name = "CustomerOnboardRequest", contentType = "application/json"),
                    @Encoding(name = "idProof", contentType = "application/octet-stream"),
                    @Encoding(name = "photo", contentType = "application/octet-stream")}
    ))
    public ResponseEntity<CustomerOnboardResponse> onboard(
            @RequestPart("CustomerOnboardRequest")
            @Schema(implementation = CustomerOnboardRequest.class)
            CustomerOnboardRequest request,

            @RequestPart("idProof")
            @Parameter(description = "ID file (max size 2MB) - PDF or Image")
            @Schema(type = "string", format = "binary")
            MultipartFile idProof,

            @RequestPart("photo")
            @Parameter(description = "Photo (max size 2MB) - PDF or Image")
            @Schema(type = "string", format = "binary")
            MultipartFile photo) {

        CustomerOnboardResponse response = customerOnboardingService.onboard(request, idProof, photo);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}