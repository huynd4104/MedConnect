package com.medconnect.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileUploadUtil {
    private static final String UPLOAD_DIR = "uploads/"; // Thay bằng path từ config

    public static String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty.");
        }
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        String filePath = UPLOAD_DIR + fileName;
        file.transferTo(new File(filePath));
        return filePath;
    }
}