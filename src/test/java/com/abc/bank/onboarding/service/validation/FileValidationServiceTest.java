package com.abc.bank.onboarding.service.validation;

import com.abc.bank.onboarding.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class FileValidationServiceTest {

    private FileValidationService fileValidationService;

    private static MultipartFile getMultipartFile(long t) {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(t);
        return file;
    }

    @BeforeEach
    void setUp() {
        fileValidationService = new FileValidationService();
    }

    @ParameterizedTest
    @CsvSource({"application/pdf,idProof", "image/jpeg,photo", "image/png,photo"})
    void should_pass_validation_when_file_is_valid(String contentType, String fileName) {
        MultipartFile file = getMultipartFile(1_000_000L);
        when(file.getContentType()).thenReturn(contentType);
        assertDoesNotThrow(() -> fileValidationService.validateFile(file, fileName));
    }

    @Test
    void should_throw_exception_when_file_is_null() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> fileValidationService.validateFile(null, "idProof"));
        assertTrue(ex.getMessage().contains("idProof missing or empty"));
    }

    @Test
    void should_throw_exception_when_file_is_empty() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> fileValidationService.validateFile(file, "idProof"));
        assertTrue(ex.getMessage().contains("idProof missing or empty"));
    }

    @Test
    void should_throw_exception_when_file_is_oversized() {
        MultipartFile file = getMultipartFile(FileValidationService.MAX_FILE_SIZE_BYTES + 1);
        when(file.getContentType()).thenReturn(MediaType.APPLICATION_PDF_VALUE);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> fileValidationService.validateFile(file, "idProof"));
        assertTrue(ex.getMessage().contains("oversized"));
    }

    @Test
    void should_throw_exception_when_content_type_is_invalid() {
        MultipartFile file = getMultipartFile(1_000_000L);
        when(file.getContentType()).thenReturn("text/plain");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> fileValidationService.validateFile(file, "idProof"));
        assertTrue(ex.getMessage().contains("invalid content type"));
    }

    @Test
    void should_throw_exception_when_content_type_is_null() {
        MultipartFile file = getMultipartFile(1_000_000L);
        when(file.getContentType()).thenReturn(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> fileValidationService.validateFile(file, "idProof"));
        assertTrue(ex.getMessage().contains("invalid content type"));
    }
}