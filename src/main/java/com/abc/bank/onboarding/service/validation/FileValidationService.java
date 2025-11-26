package com.abc.bank.onboarding.service.validation;

import com.abc.bank.onboarding.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class FileValidationService {

    public static final long MAX_FILE_SIZE_BYTES = 2_097_152;

    public static final Set<String> ALLOWED_TYPES = Set.of(
            MediaType.APPLICATION_PDF_VALUE,
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE
    );


    public void validateFile(MultipartFile file, String fileName) {
        validatePresenceAndSize(file, fileName);
        validateContentType(file, fileName);
    }


    private void validatePresenceAndSize(MultipartFile file, String fileName) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException(fileName + " missing or empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ValidationException(String.format(
                    "%s is oversized (max size %d)", fileName, MAX_FILE_SIZE_BYTES));
        }
    }

    private void validateContentType(MultipartFile file, String fileName) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new ValidationException(String.format(
                    "%s has invalid content type '%s'. Allowed: %s",
                    fileName, contentType, String.join(", ", ALLOWED_TYPES)));
        }
    }
}
