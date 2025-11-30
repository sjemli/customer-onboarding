package com.abc.bank.onboarding.advice;

import com.abc.bank.onboarding.exception.DuplicateCustomerException;
import com.abc.bank.onboarding.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String TIMESTAMP = "timestamp";


    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ProblemDetail> handleMissingPart(MissingServletRequestPartException ex, WebRequest request) {
        ProblemDetail pd = getProblemDetail(HttpStatus.BAD_REQUEST,
                "Missing required part: " + ex.getRequestPartName(),
                "Missing Request Part",
                request);
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidation(ValidationException ex, WebRequest request) {
        ProblemDetail pd = getProblemDetail(HttpStatus.BAD_REQUEST, ex.getMessage(), "Invalid Data", request);
        return new ResponseEntity<>(pd, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateCustomerException.class)
    public ResponseEntity<ProblemDetail> handleDuplicate(DuplicateCustomerException ex, WebRequest request) {
        ProblemDetail pd = getProblemDetail(HttpStatus.CONFLICT, ex.getMessage(), "Customer conflict", request);
        return new ResponseEntity<>(pd, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Malformed Request");

        Throwable cause = ex.getCause();

        String message = "Invalid request body. Please check JSON syntax and field values.";
        if (cause instanceof tools.jackson.databind.exc.InvalidFormatException invalidFormat) {
            String fieldName = invalidFormat.getPath().isEmpty() ? "unknown" :
                    invalidFormat.getPath().getFirst().getPropertyName();
            Class<?> targetType = invalidFormat.getTargetType();

            if (targetType.isEnum()) {
                Object[] enumValues = targetType.getEnumConstants();
                message = String.format("Invalid value for field '%s'. Allowed values: %s (non case sensitive)",
                        fieldName, Arrays.toString(enumValues));
            } else {
                message = String.format("Invalid value for field '%s'. Expected type: %s",
                        fieldName, targetType.getSimpleName());
            }
        }
        pd.setDetail(message);
        return ResponseEntity.badRequest().body(pd);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneralException(Exception ex, WebRequest request) {
        ProblemDetail pd = getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.",
                "Internal Server Error", request);
        return ResponseEntity.internalServerError().body(pd);
    }

    private static ProblemDetail getProblemDetail(HttpStatus httpStatus,
                                                  String detail,
                                                  String title,
                                                  WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(httpStatus, detail);
        pd.setTitle(title);
        pd.setInstance(URI.create(request.getDescription(false)));
        pd.setProperty(TIMESTAMP, Instant.now());
        return pd;
    }
}