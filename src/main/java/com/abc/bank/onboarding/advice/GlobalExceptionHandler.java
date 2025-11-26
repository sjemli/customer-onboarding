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

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String TIMESTAMP = "timestamp";

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
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                      WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Malformed Request");
        pd.setDetail("The request body could not be read. Please check JSON syntax or content type.");
        pd.setProperty("debugMessage", ex.getMessage());
        if (ex.getCause() != null) {
            pd.setProperty("rootCause", ex.getCause().toString());
        }
        pd.setInstance(URI.create(request.getDescription(false)));
        pd.setProperty(TIMESTAMP, Instant.now());
        pd.setProperty("errorType", "HttpMessageNotReadableException");
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneralException(Exception ex, WebRequest request) {
        ProblemDetail pd = getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.",
                "Internal Server Error", request);
        return ResponseEntity.internalServerError().body(pd);
    }
}