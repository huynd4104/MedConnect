package com.medconnect.service;

import com.medconnect.dto.RegisterDTO;
import com.medconnect.entity.Doctor;
import com.medconnect.entity.Patient;
import com.medconnect.entity.Token;
import com.medconnect.entity.User;
import com.medconnect.repository.DoctorRepository;
import com.medconnect.repository.PatientRepository;
import com.medconnect.repository.TokenRepository;
import com.medconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void register(RegisterDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("MSG01: Email already registered.");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match.");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user = userRepository.saveAndFlush(user);

        if (dto.getRole() == User.Role.Doctor) {
            Doctor doctor = new Doctor();
            doctor.setUser(user);
            doctor.setExperienceYears(0);
            doctor.setLicenseNumber("Pending");
            doctorRepository.save(doctor);
        } else if (dto.getRole() == User.Role.Patient) {
            Patient patient = new Patient();
            patient.setUser(user);
            patient.setFullName("");
            patientRepository.save(patient);
        }

        // Tạo token sau khi tất cả save thành công
        String tokenStr = UUID.randomUUID().toString();
        Token token = new Token();
        token.setUser(user);
        token.setToken(tokenStr);
        token.setTokenType(Token.TokenType.Verification);
        token.setExpiryDateTime(LocalDateTime.now().plusHours(24));
        tokenRepository.save(token);

        String verificationLink = "http://localhost:8080/verify?token=" + tokenStr;
        try {
            emailService.sendVerificationEmail(dto.getEmail(), verificationLink);
        } catch (Exception e) {
            System.err.println("Failed to send verification email to " + dto.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void verifyToken(String tokenStr) {
        Token token = tokenRepository.findValidByTokenAndType(tokenStr, Token.TokenType.Verification)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token."));
        User user = token.getUser();
        user.setVerified(true);
        token.setUsed(true);
        userRepository.save(user);
        tokenRepository.save(token);
    }

    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Nhập password: ");
        String password = scanner.nextLine();

        String encodedPassword = encoder.encode(password);
        System.out.println("Hash Password: " + encodedPassword);
    }
}