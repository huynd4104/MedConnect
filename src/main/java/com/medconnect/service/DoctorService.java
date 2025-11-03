package com.medconnect.service;

import com.medconnect.dto.DoctorProfileDTO;
import com.medconnect.entity.Doctor;
import com.medconnect.entity.DoctorDocument;
import com.medconnect.entity.Specialization;
import com.medconnect.entity.User;
import com.medconnect.repository.DoctorDocumentRepository;
import com.medconnect.repository.DoctorRepository;
import com.medconnect.repository.SpecializationRepository;
import com.medconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final SpecializationRepository specializationRepository;
    private final DoctorDocumentRepository doctorDocumentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public void completeProfile(Integer userId, DoctorProfileDTO dto) throws IOException {
        // 1. Lấy đối tượng User từ userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // 2. Tìm Doctor theo User. Nếu không có, tạo mới một đối tượng Doctor.
        Doctor doctor = doctorRepository.findByUser(user)
                .orElse(new Doctor());

        // 3. Liên kết Doctor với User (quan trọng cho việc tạo mới)
        doctor.setUser(user);

        // 4. Lấy thông tin Chuyên khoa
        Specialization spec = specializationRepository.findById(dto.getSpecializationId())
                .orElseThrow(() -> new RuntimeException("Specialization not found with ID: " + dto.getSpecializationId()));

        // 5. Cập nhật thông tin từ DTO vào đối tượng Doctor
        doctor.setSpecialization(spec);
        doctor.setExperienceYears(dto.getExperienceYears());
        doctor.setLicenseNumber(dto.getLicenseNumber());
        doctor.setFullName(dto.getFullName());
        doctor.setPhoneNumber(dto.getPhoneNumber());
        doctor.setClinicAddress(dto.getClinicAddress());

        // Upload ảnh đại diện (nếu có)
        if (dto.getPhoto() != null && !dto.getPhoto().isEmpty()) {
            String photoPath = uploadFile(dto.getPhoto());
            doctor.setPhotoPath(photoPath);
        }

        // 6. Đặt trạng thái và lưu lại
        doctor.setStatus(Doctor.Status.Pending);
        doctorRepository.save(doctor);

        // 7. Xử lý upload giấy tờ chứng nhận
        if (dto.getCredentials() != null) {
            for (MultipartFile file : dto.getCredentials()) {
                if (file != null && !file.isEmpty()) {
                    String filePath = uploadFile(file);
                    DoctorDocument doc = new DoctorDocument();
                    doc.setDoctor(doctor);
                    doc.setFilePath(filePath);
                    doc.setDocumentType(file.getContentType());
                    doctorDocumentRepository.save(doc);
                }
            }
        }
    }

    private String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        // 1. Tạo tên file duy nhất để tránh trùng lặp
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            extension = originalFilename.substring(i);
        }
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        // 2. Định nghĩa đường dẫn tuyệt đối tới thư mục uploads
        Path uploadPath = Paths.get("src/main/resources/uploads");

        // 3. Tạo thư mục nếu nó chưa tồn tại
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 4. Lưu file vào thư mục đích
        try (var inputStream = file.getInputStream()) {
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 5. Trả về đường dẫn web có thể truy cập được
        return "/uploads/" + uniqueFileName;
    }

    public List<Doctor> getPendingDoctors() {
        return doctorRepository.findByStatus(Doctor.Status.Pending);
    }

    @CacheEvict(value = "doctors", allEntries = true)
    public void approveDoctor(Integer doctorId, boolean approve, String reason) {
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow();

        if (approve) {
            doctor.setStatus(Doctor.Status.Approved);

            // GỬI THÔNG BÁO PHÊ DUYỆT
            notificationService.sendPushNotification(
                    doctor.getUser(),
                    "Application Approved",
                    "Hồ sơ bác sĩ của bạn đã được phê duyệt. Thông tin của bạn sẽ được cập nhật với bệnh nhân."
            );

        } else {
            doctor.setStatus(Doctor.Status.Rejected);
            doctor.setRejectionReason(reason);

            // GỬI EMAIL TỪ CHỐI (Giữ nguyên logic của bạn)
            try {
                String toEmail = doctor.getUser().getEmail();
                String doctorName = doctor.getFullName();

                String rejectionReason = (reason != null && !reason.isBlank())
                        ? reason
                        : "Không có lý do cụ thể được cung cấp.";

                emailService.sendDoctorRejectionEmail(toEmail, doctorName, rejectionReason);

            } catch (Exception e) {
                System.err.println("LỖI: Không thể gửi email từ chối cho bác sĩ " + doctorId + ": " + e.getMessage());
                e.printStackTrace();
            }

            // GỬI THÔNG BÁO TỪ CHỐI
            notificationService.sendPushNotification(
                    doctor.getUser(),
                    "Application rejection", // Tiêu đề đúng
                    "Hồ sơ của bạn đã bị từ chối, lý do chi tiết vui lòng kiểm tra trong mail." // Nội dung đúng
            );
        }

        // Lưu thay đổi vào CSDL (chỉ 1 lần ở cuối)
        doctorRepository.save(doctor);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public void deleteDocument(Integer documentId) throws IOException {
        // 1. Tìm thông tin tài liệu trong database
        DoctorDocument doc = doctorDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu với ID: " + documentId));

        // 2. Lấy đường dẫn web của file
        String webPath = doc.getFilePath();

        if (webPath != null && !webPath.isEmpty()) {
            try {
                // 3. Xây dựng đường dẫn vật lý đến file trong thư mục resources
                Path projectRoot = Paths.get("").toAbsolutePath();
                Path filePath = projectRoot.resolve("src/main/resources/static" + webPath);

                // 4. Xóa file vật lý
                Files.deleteIfExists(filePath);

            } catch (IOException e) {
                System.err.println("Lỗi khi xóa file: " + e.getMessage());
                throw e;
            }
        }

        // 5. Xóa bản ghi tài liệu khỏi database
        doctorDocumentRepository.deleteById(documentId);
    }
}