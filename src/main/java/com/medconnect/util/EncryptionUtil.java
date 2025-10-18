//package com.medconnect.util;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.Cipher;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
//import java.util.Base64;
//
//@Component
//public class EncryptionUtil {
//    private static final String ALGORITHM = "AES";
//
//    @Value("${encryption.secret-key}")
//    private String secretKey;
//
//    public String encrypt(String data) throws Exception {
//        if (data == null || data.isEmpty()) {
//            throw new IllegalArgumentException("Data to encrypt cannot be null or empty");
//        }
//        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
//        Cipher cipher = Cipher.getInstance(ALGORITHM);
//        cipher.init(Cipher.ENCRYPT_MODE, key);
//        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
//        return Base64.getEncoder().encodeToString(encryptedBytes);
//    }
//
//    public String decrypt(String encryptedData) throws Exception {
//        if (encryptedData == null || encryptedData.isEmpty()) {
//            throw new IllegalArgumentException("Encrypted data cannot be null or empty");
//        }
//        try {
//            // Kiểm tra định dạng Base64
//            Base64.getDecoder().decode(encryptedData);
//        } catch (IllegalArgumentException e) {
//            throw new IllegalArgumentException("Invalid Base64 format: " + encryptedData, e);
//        }
//        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
//        Cipher cipher = Cipher.getInstance(ALGORITHM);
//        cipher.init(Cipher.DECRYPT_MODE, key);
//        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
//        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
//        return new String(decryptedBytes, StandardCharsets.UTF_8);
//    }
//
//    // Getter để sử dụng trong test
//    public String getSecretKey() {
//        return secretKey;
//    }
//
//    // Setter để sử dụng trong test
//    public void setSecretKey(String secretKey) {
//        this.secretKey = secretKey;
//    }
//}