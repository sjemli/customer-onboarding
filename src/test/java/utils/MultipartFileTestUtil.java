package utils;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;

public class MultipartFileTestUtil {

    public static MockMultipartFile createMultipartFile(String resourcePath, String paramName) throws Exception {
        File file = ResourceUtils.getFile("classpath:" + resourcePath);
        try (FileInputStream fis = new FileInputStream(file)) {
            return new MockMultipartFile(paramName, file.getName(), "application/octet-stream", fis);
        }
    }
}
